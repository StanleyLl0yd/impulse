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
    val triggered: Boolean,
    val chainDepth: Int,
    val triggeredAgeSeconds: Double,
)

data class WaveSnapshot(
    val origin: Vec2,
    val previousRadius: Double,
    val radius: Double,
    val maximumRadius: Double,
    val chainDepth: Int,
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
    val field: GameField = GameField.DEFAULT,
) {
    private data class Particle(
        var previousPosition: Vec2,
        var position: Vec2,
        var velocity: Vec2,
        val radius: Double,
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
    )

    private val particles = mutableListOf<Particle>()
    private val waves = mutableListOf<Wave>()
    private val newlyTriggered = mutableListOf<Particle>()
    private var cachedParticles = emptyList<ParticleSnapshot>()
    private var cachedWaves = emptyList<WaveSnapshot>()
    private var cachedTriggeredCount = 0
    private var accumulator = 0.0
    private var impulseUsed = false
    private var finished = false
    private var success = false
    private var maximumChainDepth = 0

    init {
        require(particleCount > 0)
        require(requiredCount in 1..particleCount)
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
        triggeredCount = cachedTriggeredCount,
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
            cachedTriggeredCount = particles.count { it.triggered }
            success = cachedTriggeredCount >= requiredCount
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
            if (particle.triggered) continue

            var nextX = particle.position.x + particle.velocity.x * step
            var nextY = particle.position.y + particle.velocity.y * step
            var velocityX = particle.velocity.x
            var velocityY = particle.velocity.y

            if (nextX - particle.radius < 0.0 || nextX + particle.radius > field.width) {
                velocityX = -velocityX
                nextX = nextX.coerceIn(particle.radius, field.width - particle.radius)
            }
            if (nextY - particle.radius < 0.0 || nextY + particle.radius > field.height) {
                velocityY = -velocityY
                nextY = nextY.coerceIn(particle.radius, field.height - particle.radius)
            }

            particle.position = Vec2(nextX, nextY)
            particle.velocity = Vec2(velocityX, velocityY)
        }
    }

    private fun expandWaves(step: Double) {
        for (wave in waves) {
            wave.previousRadius = wave.radius
            wave.radius = (wave.radius + wave.growthRate * step).coerceAtMost(wave.maximumRadius)
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

        for (particle in newlyTriggered) {
            waves += Wave(
                origin = particle.position,
                previousRadius = 0.0,
                radius = 0.0,
                maximumRadius = REACTION_WAVE_RADIUS,
                growthRate = REACTION_WAVE_GROWTH,
                chainDepth = particle.chainDepth,
            )
        }
    }

    private fun findCollisionSource(particle: Particle): Wave? {
        var bestSource: Wave? = null
        var bestImpactTime = Double.POSITIVE_INFINITY

        for (wave in waves) {
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
                triggered = particle.triggered,
                chainDepth = particle.chainDepth,
                triggeredAgeSeconds = particle.triggeredAgeSeconds,
            )
        }
        cachedWaves = waves.map { wave ->
            WaveSnapshot(
                origin = wave.origin,
                previousRadius = wave.previousRadius,
                radius = wave.radius,
                maximumRadius = wave.maximumRadius,
                chainDepth = wave.chainDepth,
            )
        }
        cachedTriggeredCount = particles.count { it.triggered }
    }

    private fun resetParticles(count: Int) {
        particles.clear()
        val random = Random(seed)
        repeat(count) {
            val radius = PARTICLE_RADIUS_MIN +
                random.nextDouble() * (PARTICLE_RADIUS_MAX - PARTICLE_RADIUS_MIN)
            val position = generatePosition(random, radius)
            val speed = PARTICLE_SPEED_MIN +
                random.nextDouble() * (PARTICLE_SPEED_MAX - PARTICLE_SPEED_MIN)
            val angle = random.nextDouble() * Math.PI * 2.0
            particles += Particle(
                previousPosition = position,
                position = position,
                velocity = Vec2(cos(angle) * speed, sin(angle) * speed),
                radius = radius,
            )
        }
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
        private const val SPAWN_GAP = 0.006
        private const val SPAWN_GRID_STEP = PARTICLE_RADIUS_MAX * 2.0 + SPAWN_GAP
        private const val MAX_SPAWN_ATTEMPTS = 100
    }
}
