from pathlib import Path
import math
import re
import shutil
import struct
import wave

root = Path('.')
old_id = 'io.github.stanleyll0yd.impulse'
new_id = 'com.sl.impulse'

moves = {
    'app/src/main/java/io/github/stanleyll0yd/impulse/MainActivity.kt': 'app/src/main/java/com/sl/impulse/MainActivity.kt',
    'app/src/main/java/io/github/stanleyll0yd/impulse/feedback/GameSoundController.kt': 'app/src/main/java/com/sl/impulse/feedback/GameSoundController.kt',
    'app/src/main/java/io/github/stanleyll0yd/impulse/game/GameEngine.kt': 'app/src/main/java/com/sl/impulse/game/GameEngine.kt',
    'app/src/main/java/io/github/stanleyll0yd/impulse/ui/GameViewport.kt': 'app/src/main/java/com/sl/impulse/ui/GameViewport.kt',
    'app/src/main/java/io/github/stanleyll0yd/impulse/ui/ImpulseApp.kt': 'app/src/main/java/com/sl/impulse/ui/ImpulseApp.kt',
    'app/src/main/java/io/github/stanleyll0yd/impulse/ui/SplashScreen.kt': 'app/src/main/java/com/sl/impulse/ui/SplashScreen.kt',
    'app/src/androidTest/java/io/github/stanleyll0yd/impulse/ImpulseLaunchTest.kt': 'app/src/androidTest/java/com/sl/impulse/ImpulseLaunchTest.kt',
    'app/src/test/java/io/github/stanleyll0yd/impulse/game/GameEngineTest.kt': 'app/src/test/java/com/sl/impulse/game/GameEngineTest.kt',
    'app/src/test/java/io/github/stanleyll0yd/impulse/ui/GameViewportTest.kt': 'app/src/test/java/com/sl/impulse/ui/GameViewportTest.kt',
}

for source_name, target_name in moves.items():
    source = root / source_name
    target = root / target_name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(source), str(target))

for path in root.rglob('*'):
    if not path.is_file() or '.git' in path.parts or '.github/workflows' in path.as_posix():
        continue
    try:
        text = path.read_text()
    except (UnicodeDecodeError, OSError):
        continue
    if old_id in text:
        path.write_text(text.replace(old_id, new_id))

splash_source = root / 'docs/candidate-10a.bin'
splash_target = root / 'app/src/main/res/drawable-nodpi/impulse_splash.webp'
shutil.copyfile(splash_source, splash_target)

build = root / 'app/build.gradle.kts'
text = build.read_text()
text = text.replace('versionCode = 3', 'versionCode = 4')
text = text.replace('versionName = "0.3.0"', 'versionName = "0.3.1"')
build.write_text(text)

impulse_app = root / 'app/src/main/java/com/sl/impulse/ui/ImpulseApp.kt'
text = impulse_app.read_text()
text = text.replace(
    'import androidx.compose.ui.platform.LocalView',
    'import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalView',
)
text = text.replace(
    '        val soundController = remember { GameSoundController() }',
    '        val context = LocalContext.current\n        val soundController = remember(context.applicationContext) {\n            GameSoundController(context.applicationContext)\n        }',
)
impulse_app.write_text(text)

for filename in ('README.md', 'README_RU.md'):
    path = root / filename
    text = path.read_text().replace('0.3.0', '0.3.1').replace('versionCode 3', 'versionCode 4')
    if 'Application ID / namespace' not in text:
        text = re.sub(
            r'(\| Android \|[^\n]+\n)',
            r'\1| Application ID / namespace | `com.sl.impulse` |\n',
            text,
            count=1,
        )
    path.write_text(text)

project = root / 'PROJECT.md'
text = project.read_text()
marker = '- `minSdk 26`, `targetSdk 37`, `compileSdk 37`.\n'
identity = '- Permanent Android Application ID / Bundle ID and namespace: `com.sl.impulse`.\n'
if identity not in text:
    text = text.replace(marker, marker + identity)
project.write_text(text)

release_doc = root / 'docs/RELEASE.md'
text = release_doc.read_text()
identity_text = 'The permanent Android Application ID / Bundle ID and namespace are `com.sl.impulse`. Release validation rejects any other package identity.\n\n'
if identity_text not in text:
    text = text.replace('## Publishing a GitHub release\n\n', '## Publishing a GitHub release\n\n' + identity_text)
release_doc.write_text(text)

changelog = root / 'CHANGELOG.md'
text = changelog.read_text()
section = '''## 0.3.1 - 2026-08-31

### Added

- Soft melodic game audio for the player impulse, chain growth, success, and failure.

### Changed

- Android Application ID / Bundle ID, namespace, source packages, and test packages are permanently `com.sl.impulse`.
- Launch splash now uses the exact supplied 941×1672 IMPULSE artwork while retaining the three-second fade from pure black.
- CI, release tagging, and signed-release validation now enforce the canonical application identity.

### Removed

- Android `ToneGenerator` system beeps from gameplay feedback.

'''
if '## 0.3.1 - 2026-08-31' not in text:
    text = text.replace('## Unreleased\n\n', '## Unreleased\n\n' + section)
changelog.write_text(text)

raw = root / 'app/src/main/res/raw'
raw.mkdir(parents=True, exist_ok=True)
sample_rate = 44100

def envelope(t, duration, attack=0.018, release_power=2.2):
    attack_part = min(1.0, t / attack) if attack > 0 else 1.0
    release_part = max(0.0, 1.0 - t / duration) ** release_power
    return attack_part * release_part

def chirp(t, duration, start, end):
    slope = (end - start) / duration
    phase = 2.0 * math.pi * (start * t + 0.5 * slope * t * t)
    return math.sin(phase)

def write_wav(path, duration, sample_fn):
    count = int(sample_rate * duration)
    samples = []
    peak = 0.0
    for index in range(count):
        t = index / sample_rate
        value = sample_fn(t, duration)
        peak = max(peak, abs(value))
        samples.append(value)
    scale = 0.88 / peak if peak > 0.88 else 1.0
    with wave.open(str(path), 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(sample_rate)
        for value in samples:
            pcm = int(max(-1.0, min(1.0, value * scale)) * 32767)
            out.writeframesraw(struct.pack('<h', pcm))

def impulse(t, duration):
    env = envelope(t, duration, 0.028, 2.0)
    bloom = chirp(t, duration, 155.0, 410.0)
    harmonic = chirp(t, duration, 310.0, 820.0)
    shimmer = math.sin(2.0 * math.pi * 690.0 * t + 0.35 * math.sin(2.0 * math.pi * 3.0 * t))
    echo_t = t - 0.17
    echo = 0.0
    if echo_t >= 0.0:
        echo = 0.18 * envelope(echo_t, duration - 0.17, 0.015, 2.8) * chirp(echo_t, duration - 0.17, 230.0, 480.0)
    return env * (0.72 * bloom + 0.20 * harmonic + 0.08 * shimmer) + echo

def chain(t, duration):
    env = envelope(t, duration, 0.012, 2.7)
    fundamental = math.sin(2.0 * math.pi * 330.0 * t)
    fifth = math.sin(2.0 * math.pi * 495.0 * t)
    octave = math.sin(2.0 * math.pi * 660.0 * t)
    return env * (0.68 * fundamental + 0.22 * fifth + 0.10 * octave)

def success(t, duration):
    notes = [(0.00, 293.66, 0.46), (0.09, 369.99, 0.38), (0.18, 440.00, 0.34), (0.28, 587.33, 0.24)]
    value = 0.0
    for start, frequency, gain in notes:
        local = t - start
        if local >= 0.0:
            remaining = duration - start
            value += gain * envelope(local, remaining, 0.02, 2.0) * math.sin(2.0 * math.pi * frequency * local)
    return value

def failure(t, duration):
    env = envelope(t, duration, 0.025, 2.4)
    main = chirp(t, duration, 246.94, 174.61)
    low = chirp(t, duration, 123.47, 87.31)
    return env * (0.72 * main + 0.28 * low)

write_wav(raw / 'sound_impulse.wav', 0.52, impulse)
write_wav(raw / 'sound_chain.wav', 0.19, chain)
write_wav(raw / 'sound_success.wav', 0.76, success)
write_wav(raw / 'sound_failure.wav', 0.48, failure)

(root / 'app/src/main/java/com/sl/impulse/feedback/GameSoundController.kt').write_text('''package com.sl.impulse.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.sl.impulse.R
import java.util.Collections
import kotlin.math.min

class GameSoundController(context: Context) {
    private data class Playback(
        val sampleId: Int,
        val volume: Float,
        val rate: Float,
    )

    private val handler = Handler(Looper.getMainLooper())
    private val loaded = Collections.synchronizedSet(mutableSetOf<Int>())
    private var released = false
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
        .also { pool ->
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) loaded += sampleId
            }
        }

    private val impulse = soundPool.load(context, R.raw.sound_impulse, 1)
    private val chain = soundPool.load(context, R.raw.sound_chain, 1)
    private val success = soundPool.load(context, R.raw.sound_success, 1)
    private val failure = soundPool.load(context, R.raw.sound_failure, 1)

    fun playImpulse() {
        play(Playback(impulse, 0.52f, 1.0f))
    }

    fun playChain(chainDepth: Int, triggeredDelta: Int) {
        val depth = min(chainDepth, 8)
        val rate = (0.92f + depth * 0.045f).coerceAtMost(1.28f)
        val volume = (0.24f + min(triggeredDelta, 5) * 0.03f).coerceAtMost(0.38f)
        play(Playback(chain, volume, rate))
    }

    fun playResult(successful: Boolean) {
        if (successful) {
            play(Playback(success, 0.48f, 1.0f))
        } else {
            play(Playback(failure, 0.34f, 1.0f))
        }
    }

    fun release() {
        released = true
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        loaded.clear()
    }

    private fun play(playback: Playback, attempt: Int = 0) {
        if (released) return
        if (loaded.contains(playback.sampleId)) {
            soundPool.play(
                playback.sampleId,
                playback.volume,
                playback.volume,
                1,
                0,
                playback.rate,
            )
            return
        }
        if (attempt < 6) {
            handler.postDelayed({ play(playback, attempt + 1) }, 25L)
        }
    }
}
''')

(root / '.github/release-notes/v0.3.1.md').write_text('''## IMPULSE 0.3.1

This polish release fixes the launch artwork, establishes the permanent Android application identity, and replaces harsh system tones with a softer melodic impulse sound language.

### Highlights

- Application ID / Bundle ID and Kotlin namespace are now permanently `com.sl.impulse`, with source and test package paths migrated accordingly.
- The launch splash now uses the exact supplied 941×1672 IMPULSE artwork and still fades from pure black to full visibility over three seconds.
- Android `ToneGenerator` beeps were replaced by local melodic game audio: a blooming player impulse, pitch-reactive chain pulses, a resolving success phrase, and a subdued failure tone.
- CI, release tagging, and signed-release validation now guard the canonical application identity.

Because the Android application identity changed, 0.3.1 installs as a separate application from builds through 0.3.0. `com.sl.impulse` is the permanent identity for future releases.

The game remains offline-first with no account, analytics, ads, backend, or Android `INTERNET` permission.

Release artifacts are signed with the existing project release certificate and published with SHA-256 checksums and GitHub artifact attestations.
''')

for name in (
    'docs/.placeholder',
    'docs/blob-map.txt',
    'docs/blob-candidate-10a.bin',
    'docs/blob-candidate-b7f.bin',
    'docs/blob-candidate-c661.bin',
    'docs/blob-candidate-10a.txt',
    'docs/blob-candidate-b7f.txt',
    'docs/blob-candidate-c661.txt',
    'docs/blob-candidate-map-complete.txt',
    'docs/candidate-10a.bin',
    'docs/candidate-b7f.bin',
    'docs/candidate-c661.bin',
):
    path = root / name
    if path.exists():
        path.unlink()

assert 'namespace = "com.sl.impulse"' in build.read_text()
assert 'applicationId = "com.sl.impulse"' in build.read_text()
assert 'versionCode = 4' in build.read_text()
assert 'versionName = "0.3.1"' in build.read_text()
assert splash_target.stat().st_size > 100000
for name in ('sound_impulse.wav', 'sound_chain.wav', 'sound_success.wav', 'sound_failure.wav'):
    assert (raw / name).stat().st_size > 1000
