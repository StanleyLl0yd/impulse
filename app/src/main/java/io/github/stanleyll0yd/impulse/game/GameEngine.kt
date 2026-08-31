package io.github.stanleyll0yd.impulse.game

import java.util.Random
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val FIXED_STEP_SECONDS = 1.0 / 60.0
private const val MAX_FRAME_SECONDS = 0.1

data class Vec2(val x: Double, val y: Double)

data class ParticleSnapshot(
    val position: Vec2,
    val radius: Double,
    val triggered: Boolean,
    val chainDepth: Int,
)

data class WaveSnapshot(
    val origin: Vec2,
    val radius: Double,
    val maximumRadius: Double,
    val chainDepth: Int,
)

data class GameSnapshot(
    val particles: List<ParticleSnapshot>,
    val waves: List<WaveSnapshot>,
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
) {
    private data class Particle(
        var position: Vec2,
        var velocity: Vec2,
        val radius: Double,
        var triggered: Boolean = false,
        var chainDepth: Int = 0,
    )

    private data class Wave(
        val origin: Vec2,
        var radius: Double,
        val maximumRadius: Double,
        val growthRate: Double,
        val chainDepth: Int,
    )

    private val particles = mutableListOf<Particle>()
    private val waves = mutableListOf<Wave>()
    private var accumulator = 0.0
    private var impulseUsed = false
    private var finished = false
    private var success = false
    private var maximumChainDepth = 0

    init {
        require(particleCount > 0)
        require(requiredCount in 1..particleCount)
        resetParticles(particleCount)
    }

    fun tap(position: Vec2): Boolean {
        if (impulseUsed || finished) return false
        val clamped = Vec2(position.x.coerceIn(0.0, 1.0), position.y.coerceIn(0.0, 1.0))
        waves += Wave(
            origin = clamped,
            radius = 0.0,
            maximumRadius = PLAYER_WAVE_RADIUS,
            growthRate = PLAYER_WAVE_GROWTH,
            chainDepth = 0,
        )
        impulseUsed = true
        return true
    }

    fun advance(deltaSeconds: Double) {
        if (finished) return
        accumulator += deltaSeconds.coerceIn(0.0, MAX_FRAME_SECONDS)
        while (accumulator >= FIXED_STEP_SECONDS) {
            tick(FIXED_STEP_SECONDS)
            accumulator -= FIXED_STEP_SECONDS
        }
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        particles = particles.map {
            ParticleSnapshot(it.position, it.radius, it.triggered, it.chainDepth)
        },
        waves = waves.map {
            WaveSnapshot(it.origin, it.radius, it.maximumRadius, it.chainDepth)
        },
        triggeredCount = particles.count { it.triggered },
        requiredCount = requiredCount,
        impulseUsed = impulseUsed,
        finished = finished,
        success = success,
        maximumChainDepth = maximumChainDepth,
    )

    private fun tick(step: Double) {
        moveParticles(step)
        expandWaves(step)
        triggerCollisions()
        waves.removeAll { it.radius >= it.maximumRadius }

        if (impulseUsed && waves.isEmpty()) {
            finished = true
            success = particles.count { it.triggered } >= requiredCount
        }
    }

    private fun moveParticles(step: Double) {
        particles.filterNot { it.triggered }.forEach { particle ->
            var nextX = particle.position.x + particle.velocity.x * step
            var nextY = particle.position.y + particle.velocity.y * step
            var velocityX = particle.velocity.x
            var velocityY = particle.velocity.y

            if (nextX - particle.radius < 0.0 || nextX + particle.radius > 1.0) {
                velocityX = -velocityX
                nextX = nextX.coerceIn(particle.radius, 1.0 - particle.radius)
            }
            if (nextY - particle.radius < 0.0 || nextY + particle.radius > 1.0) {
                velocityY = -velocityY
                nextY = nextY.coerceIn(particle.radius, 1.0 - particle.radius)
            }

            particle.position = Vec2(nextX, nextY)
            particle.velocity = Vec2(velocityX, velocityY)
        }
    }

    private fun expandWaves(step: Double) {
        waves.forEach { wave ->
            wave.radius = (wave.radius + wave.growthRate * step).coerceAtMost(wave.maximumRadius)
        }
    }

    private fun triggerCollisions() {
        val newlyTriggered = mutableListOf<Particle>()
        particles.filterNot { it.triggered }.forEach { particle ->
            val source = waves.firstOrNull { wave ->
                hypot(
                    particle.position.x - wave.origin.x,
                    particle.position.y - wave.origin.y,
                ) <= wave.radius + particle.radius
            }
            if (source != null) {
                particle.triggered = true
                particle.chainDepth = source.chainDepth + 1
                maximumChainDepth = maxOf(maximumChainDepth, particle.chainDepth)
                newlyTriggered += particle
            }
        }

        newlyTriggered.forEach { particle ->
            waves += Wave(
                origin = particle.position,
                radius = 0.0,
                maximumRadius = REACTION_WAVE_RADIUS,
                growthRate = REACTION_WAVE_GROWTH,
                chainDepth = particle.chainDepth,
            )
        }
    }

    private fun resetParticles(count: Int) {
        val random = Random(seed)
        repeat(count) {
            val radius = PARTICLE_RADIUS_MIN +
                random.nextDouble() * (PARTICLE_RADIUS_MAX - PARTICLE_RADIUS_MIN)
            val position = generatePosition(random, radius)
            val speed = PARTICLE_SPEED_MIN +
                random.nextDouble() * (PARTICLE_SPEED_MAX - PARTICLE_SPEED_MIN)
            val angle = random.nextDouble() * Math.PI * 2.0
            particles += Particle(
                position = position,
                velocity = Vec2(cos(angle) * speed, sin(angle) * speed),
                radius = radius,
            )
        }
    }

    private fun generatePosition(random: Random, radius: Double): Vec2 {
        repeat(MAX_SPAWN_ATTEMPTS) {
            val candidate = Vec2(
                radius + random.nextDouble() * (1.0 - radius * 2.0),
                radius + random.nextDouble() * (1.0 - radius * 2.0),
            )
            val overlaps = particles.any { other ->
                hypot(
                    candidate.x - other.position.x,
                    candidate.y - other.position.y,
                ) < radius + other.radius + SPAWN_GAP
            }
            if (!overlaps) return candidate
        }
        return Vec2(0.5, 0.5)
    }

    companion object {
        const val DEFAULT_SEED = 0x1A2B3C4DL
        const val DEFAULT_PARTICLE_COUNT = 20
        const val DEFAULT_REQUIRED_COUNT = 12

        private const val PARTICLE_RADIUS_MIN = 0.011
        private const val PARTICLE_RADIUS_MAX = 0.017
        private const val PARTICLE_SPEED_MIN = 0.055
        private const val PARTICLE_SPEED_MAX = 0.105
        private const val PLAYER_WAVE_RADIUS = 0.18
        private const val PLAYER_WAVE_GROWTH = 0.42
        private const val REACTION_WAVE_RADIUS = 0.135
        private const val REACTION_WAVE_GROWTH = 0.34
        private const val SPAWN_GAP = 0.006
        private const val MAX_SPAWN_ATTEMPTS = 100
    }
}
