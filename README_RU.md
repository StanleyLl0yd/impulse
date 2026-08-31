# IMPULSE

Минималистичная Android-игра о цепных реакциях. Одно касание. Один импульс. Максимальная цепь.

[English README](README.md)

## Статус

Ранний игровой прототип (`0.1.0`). Проект намеренно остаётся небольшим и работает без обязательного подключения к интернету.

## Игровой цикл

1. Наблюдать за движением частиц.
2. Один раз коснуться экрана и создать расширяющийся импульс.
3. Запустить цепную реакцию.
4. Достичь цели или мгновенно начать новую попытку.

## Техническая база

- Kotlin
- Jetpack Compose
- Собственная детерминированная 2D-симуляция
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`
- Gradle 9.5.0, AGP 9.3.2, Kotlin 2.4.10
- Без аккаунта, backend, аналитики, рекламы и разрешения на доступ в интернет

## Сборка

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## Релизы

Официальный релиз запускается тегом `vMAJOR.MINOR.PATCH`. Для CI используется предоставленный владельцем ключ подписи, переданный только через GitHub Environment secrets. Workflow проверяет подпись APK/AAB и SHA-256 сертификата, создаёт checksums и artifact attestations, после чего публикует GitHub Release.

Подробности: [docs/RELEASE.md](docs/RELEASE.md).

## Безопасность

См. [SECURITY.md](SECURITY.md). В проект заложены CodeQL, Semgrep, Gitleaks, Qodana, Dependabot, закрепление GitHub Actions по commit SHA и минимальные workflow permissions.

## Лицензия

PolyForm Noncommercial License 1.0.0. См. [LICENSE](LICENSE).
