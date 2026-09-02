package com.sl.impulse.game

import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val FIXED_STEP_SECONDS = 1.0 / 60.0
private const val MAX_FRAME_SECONDS = 0.1
private const val EPSILON = 1e-9

data class Vec2(val x: Double, val y: Double)

data class GameField(
    val width: Double = DEFAULT_WIDTH,
    val height: Double = DEFAULT_HEIGHT,
) {
    init {
        require(width > 0.0)
        require(height > 0.0)
    }

    companion object {
        const val DEFAULT_WIDTH = 1.0
        const val DEFAULT_HEIGHT = 16.0 / 9.0
        val DEFAULT = GameField()
    }
}

data class ParticleSnapshot(
    val previousPosition: Vec2,
    val position: Vec2,
    val radius: Double,
    val type: ParticleType,
    val triggered: Boolean,
    val chainDepth: Int,
    val triggeredAgeSeconds: Double,
    val reactionPending: Boolean,
)

data class WaveSnapshot(
    val origin: Vec2,
    val previousRadius: Double,
    val radius: Double,
    val maximumRadius: Double,
    val chainDepth: Int,
    val sourceType: ParticleType?,
)

data class GameSnapshot(
    val field: GameField,
    val particles: List<ParticleSnapshot>,
    val waves: List<WaveSnapshot>,
    val interpolationAlpha: Double,
    val triggeredCount: Int,
    val requiredCount: Int,
    val impulseUsed: Boolean,
    val finished: Boolean,
    val success: Boolean,
    val maximumChainDepth: Int,
)

class GameEngine(
    private val seed: Long = DEFAULT_SEED,
    particleCount: Int = DEFAULT_PARTICLE_COUNT,
    val requiredCount: Int = DEFAULT_REQUIRED_COUNT,
    private val particleMix: ParticleMix = ParticleMix(),
    val field: GameField = GameField.DEFAULT,
) {
    private data class Particle(
        var previousPosition: Vec2,
        var position: Vec2,
        var velocity: Vec2,
        val radius: Double,
        val type: ParticleType,
        var triggered: Boolean = false,
        var chainDepth: Int = 0,
        var triggeredAgeSeconds: Double = 0.0,
    )

    private data class Wave(
        val origin: Vec2,
        var previousRadius: Double,
        var radius: Double,
        val maximumRadius: Double,
        val growthRate: Double,
        val chainDepth: Int,
        val sourceType: ParticleType?,
        var delayRemainingSeconds: Double = 0.0,
    )

    private val particles = mutableListOf<Particle>()
    private val waves = mutableListOf<Wave>()
    private val newlyTriggered = mutableListOf<Particle>()
    private var cachedParticles = emptyList<ParticleSnapshot>()
    private var cachedWaves = emptyList<WaveSnapshot>()
    private var triggeredCount = 0
    private var accumulator = 0.0
    private var impulseUsed = false
    private var finished = false
    private var success = false
    private var maximumChainDepth = 0

    init {
        require(particleCount > 0)
        require(requiredCount in 1..particleCount)
        require(particleMix.specialCount <= particleCount)
        require(field.width > PARTICLE_RADIUS_MAX * 2.0)
        require(field.height > PARTICLE_RADIUS_MAX * 2.0)
        resetParticles(particleCount)
        refreshCachedState()
    }

    fun tap(position: Vec2): Boolean {
        if (impulseUsed || finished) return false
        val clamped = Vec2(
            position.x.coerceIn(0.0, field.width),
            position.y.coerceIn(0.0, field.height),
        )
        waves += Wave(
            origin = clamped,
            previousRadius = 0.0,
            radius = 0.0,
            maximumRadius = PLAYER_WAVE_RADIUS,
            growthRate = PLAYER_WAVE_GROWTH,
            chainDepth = 0,
            sourceType = null,
        )
        impulseUsed = true
        refreshCachedState()
        return true
    }

    fun advance(deltaSeconds: Double) {
        if (finished) return

        accumulator += deltaSeconds.coerceIn(0.0, MAX_FRAME_SECONDS)
        var changed = false
        while (accumulator >= FIXED_STEP_SECONDS && !finished) {
            tick(FIXED_STEP_SECONDS)
            accumulator -= FIXED_STEP_SECONDS
            changed = true
        }

        if (finished) accumulator = 0.0
        if (changed) refreshCachedState()
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        field = field,
        particles = cachedParticles,
        waves = cachedWaves,
        interpolationAlpha = if (finished) {
            1.0
        } else {
            (accumulator / FIXED_STEP_SECONDS).coerceIn(0.0, 1.0)
        },
        triggeredCount = triggeredCount,
        requiredCount = requiredCount,
        impulseUsed = impulseUsed,
        finished = finished,
        success = success,
        maximumChainDepth = maximumChainDepth,
    )

    private fun tick(step: Double) {
        ageTriggeredParticles(step)
        moveParticles(step)
        expandWaves(step)
        triggerCollisions()
        removeExpiredWaves()

        if (impulseUsed && waves.isEmpty()) {
            finished = true
            success = triggeredCount >= requiredCount
        }
    }

    private fun ageTriggeredParticles(step: Double) {
        for (particle in particles) {
            if (particle.triggered) particle.triggeredAgeSeconds += step
        }
    }

    private fun moveParticles(step: Double) {
        for (particle in particles) {
            particle.previousPosition = particle.position
            if (particle.triggered || particle.type == ParticleType.ANCHOR) continue

            var nextX = particle.position.x + particle.velocity.x * step
            var nextY = particle.position.y + particle.velocity.y * step
            var velocityX = particle.velocity.x
            var velocityY = particle.velocity.y
            var bounced = false

            if (nextX - particle.radius < 0.0 || nextX + particle.radius > field.width) {
                velocityX = -velocityX
                nextX = nextX.coerceIn(particle.radius, field.width - particle.radius)
                bounced = true
            }
            if (nextY - particle.radius < 0.0 || nextY + particle.radius > field.height) {
                velocityY = -velocityY
                nextY = nextY.coerceIn(particle.radius, field.height - particle.radius)
                bounced = true
            }

            particle.position = Vec2(nextX, nextY)
            if (bounced) particle.velocity = Vec2(velocityX, velocityY)
        }
    }

    private fun expandWaves(step: Double) {
        for (wave in waves) {
            wave.previousRadius = wave.radius
            val activeStep = if (wave.delayRemainingSeconds > 0.0) {
                val remaining = wave.delayRemainingSeconds
                wave.delayRemainingSeconds = (remaining - step).coerceAtLeast(0.0)
                (step - remaining).coerceAtLeast(0.0)
            } else {
                step
            }
            if (activeStep > 0.0) {
                wave.radius = (wave.radius + wave.growthRate * activeStep).coerceAtMost(wave.maximumRadius)
            }
        }
    }

    private fun triggerCollisions() {
        newlyTriggered.clear()

        for (particle in particles) {
            if (particle.triggered) continue
            val source = findCollisionSource(particle) ?: continue

            particle.triggered = true
            particle.chainDepth = source.chainDepth + 1
            particle.triggeredAgeSeconds = 0.0
            maximumChainDepth = maxOf(maximumChainDepth, particle.chainDepth)
            newlyTriggered += particle
        }

        triggeredCount += newlyTriggered.size
        for (particle in newlyTriggered) {
            waves += createReactionWave(particle)
        }
    }

    private fun createReactionWave(particle: Particle): Wave {
        val maximumRadius = when (particle.type) {
            ParticleType.BOOSTER -> BOOSTER_WAVE_RADIUS
            else -> REACTION_WAVE_RADIUS
        }
        val growthRate = when (particle.type) {
            ParticleType.BOOSTER -> BOOSTER_WAVE_GROWTH
            else -> REACTION_WAVE_GROWTH
        }
        val delay = if (particle.type == ParticleType.FUSE) FUSE_DELAY_SECONDS else 0.0

        return Wave(
            origin = particle.position,
            previousRadius = 0.0,
            radius = 0.0,
            maximumRadius = maximumRadius,
            growthRate = growthRate,
            chainDepth = particle.chainDepth,
            sourceType = particle.type,
            delayRemainingSeconds = delay,
        )
    }

    private fun findCollisionSource(particle: Particle): Wave? {
        var bestSource: Wave? = null
        var bestImpactTime = Double.POSITIVE_INFINITY

        for (wave in waves) {
            if (wave.delayRemainingSeconds > EPSILON) continue
            val distance = hypot(
                particle.position.x - wave.origin.x,
                particle.position.y - wave.origin.y,
            )
            val contactRadius = (distance - particle.radius).coerceAtLeast(0.0)
            if (contactRadius > wave.radius + EPSILON) continue

            val growth = wave.radius - wave.previousRadius
            val impactTime = when {
                contactRadius <= wave.previousRadius + EPSILON -> 0.0
                growth <= EPSILON -> 1.0
                else -> ((contactRadius - wave.previousRadius) / growth).coerceIn(0.0, 1.0)
            }
            val currentBest = bestSource
            val isEarlier = impactTime < bestImpactTime - EPSILON
            val isDeeperTie = abs(impactTime - bestImpactTime) <= EPSILON &&
                (currentBest == null || wave.chainDepth > currentBest.chainDepth)

            if (isEarlier || isDeeperTie) {
                bestSource = wave
                bestImpactTime = impactTime
            }
        }

        return bestSource
    }

    private fun removeExpiredWaves() {
        for (index in waves.lastIndex downTo 0) {
            if (waves[index].radius >= waves[index].maximumRadius) {
                waves.removeAt(index)
            }
        }
    }

    private fun refreshCachedState() {
        cachedParticles = particles.map { particle ->
            ParticleSnapshot(
                previousPosition = particle.previousPosition,
                position = particle.position,
                radius = particle.radius,
                type = particle.type,
                triggered = particle.triggered,
                chainDepth = particle.chainDepth,
                triggeredAgeSeconds = particle.triggeredAgeSeconds,
                reactionPending = particle.type == ParticleType.FUSE &&
                    particle.triggered &&
                    particle.triggeredAgeSeconds < FUSE_DELAY_SECONDS,
            )
        }
        cachedWaves = waves.map { wave ->
            WaveSnapshot(
                origin = wave.origin,
                previousRadius = wave.previousRadius,
                radius = wave.radius,
                maximumRadius = wave.maximumRadius,
                chainDepth = wave.chainDepth,
                sourceType = wave.sourceType,
            )
        }
    }

    private fun resetParticles(count: Int) {
        particles.clear()
        val types = createParticleTypes(count)
        val random = Random(seed)
        repeat(count) { index ->
            val radius = PARTICLE_RADIUS_MIN +
                random.nextDouble() * (PARTICLE_RADIUS_MAX - PARTICLE_RADIUS_MIN)
            val position = generatePosition(random, radius)
            val speed = PARTICLE_SPEED_MIN +
                random.nextDouble() * (PARTICLE_SPEED_MAX - PARTICLE_SPEED_MIN)
            val angle = random.nextDouble() * Math.PI * 2.0
            val type = types[index]
            val velocity = if (type == ParticleType.ANCHOR) {
                Vec2(0.0, 0.0)
            } else {
                Vec2(cos(angle) * speed, sin(angle) * speed)
            }
            particles += Particle(
                previousPosition = position,
                position = position,
                velocity = velocity,
                radius = radius,
                type = type,
            )
        }
    }

    private fun createParticleTypes(count: Int): List<ParticleType> {
        if (particleMix.specialCount == 0) return List(count) { ParticleType.STANDARD }

        val shuffledIndices = MutableList(count) { it }
        val random = Random(seed xor SPECIAL_TYPE_SEED_MASK)
        for (index in shuffledIndices.lastIndex downTo 1) {
            val swapIndex = random.nextInt(index + 1)
            val value = shuffledIndices[index]
            shuffledIndices[index] = shuffledIndices[swapIndex]
            shuffledIndices[swapIndex] = value
        }

        val types = MutableList(count) { ParticleType.STANDARD }
        var cursor = 0
        repeat(particleMix.boosterCount) { types[shuffledIndices[cursor++]] = ParticleType.BOOSTER }
        repeat(particleMix.fuseCount) { types[shuffledIndices[cursor++]] = ParticleType.FUSE }
        repeat(particleMix.anchorCount) { types[shuffledIndices[cursor++]] = ParticleType.ANCHOR }
        return types
    }

    private fun generatePosition(random: Random, radius: Double): Vec2 {
        repeat(MAX_SPAWN_ATTEMPTS) {
            val candidate = Vec2(
                radius + random.nextDouble() * (field.width - radius * 2.0),
                radius + random.nextDouble() * (field.height - radius * 2.0),
            )
            if (canPlace(candidate, radius)) return candidate
        }

        var y = radius
        while (y <= field.height - radius + EPSILON) {
            var x = radius
            while (x <= field.width - radius + EPSILON) {
                val candidate = Vec2(x, y)
                if (canPlace(candidate, radius)) return candidate
                x += SPAWN_GRID_STEP
            }
            y += SPAWN_GRID_STEP
        }

        throw IllegalArgumentException("Unable to place particles in the configured game field")
    }

    private fun canPlace(candidate: Vec2, radius: Double): Boolean {
        for (other in particles) {
            if (
                hypot(
                    candidate.x - other.position.x,
                    candidate.y - other.position.y,
                ) < radius + other.radius + SPAWN_GAP
            ) {
                return false
            }
        }
        return true
    }

    companion object {
        const val DEFAULT_SEED = 0x1A2B3C4DL
        const val DEFAULT_PARTICLE_COUNT = 20
        const val DEFAULT_REQUIRED_COUNT = 12

        private const val PARTICLE_RADIUS_MIN = 0.011
        private const val PARTICLE_RADIUS_MAX = 0.017
        private const val PARTICLE_SPEED_MIN = 0.055
        private const val PARTICLE_SPEED_MAX = 0.105
        private const val PLAYER_WAVE_RADIUS = 0.24
        private const val PLAYER_WAVE_GROWTH = 0.56
        private const val REACTION_WAVE_RADIUS = 0.18
        private const val REACTION_WAVE_GROWTH = 0.45
        private const val BOOSTER_WAVE_RADIUS = 0.28
        private const val BOOSTER_WAVE_GROWTH = 0.54
        private const val FUSE_DELAY_SECONDS = 0.55
        private const val SPECIAL_TYPE_SEED_MASK = 0x5EED5EEDL
        private const val SPAWN_GAP = 0.006
        private const val SPAWN_GRID_STEP = PARTICLE_RADIUS_MAX * 2.0 + SPAWN_GAP
        private const val MAX_SPAWN_ATTEMPTS = 100
    }
}
