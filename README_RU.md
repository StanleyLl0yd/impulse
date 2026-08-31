<div align="center">

# ◉ IMPULSE

### ОДНО КАСАНИЕ · ОДИН ИМПУЛЬС · МАКСИМАЛЬНАЯ ЦЕПЬ

`○ · ○ · ◎ ))) ◉ ✦ ✦ ✦`

[![Android CI](https://img.shields.io/github/actions/workflow/status/StanleyLl0yd/impulse/ci.yml?branch=main&label=CI&labelColor=050814&color=00E5FF)](https://github.com/StanleyLl0yd/impulse/actions/workflows/ci.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/impulse?label=release&labelColor=050814&color=9E4DFF)](https://github.com/StanleyLl0yd/impulse/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/impulse/total?label=downloads&labelColor=050814&color=00E5FF)](https://github.com/StanleyLl0yd/impulse/releases)
[![Android 8+](https://img.shields.io/badge/Android-8.0%2B-00E5FF?labelColor=050814&logo=android&logoColor=8FF8FF)](https://github.com/StanleyLl0yd/impulse)
[![Offline](https://img.shields.io/badge/network-offline-9E4DFF?labelColor=050814)](https://github.com/StanleyLl0yd/impulse)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial-9E4DFF?labelColor=050814)](LICENSE)

[![English](https://img.shields.io/badge/lang-EN-00E5FF?labelColor=050814)](README.md)
[![Русский](https://img.shields.io/badge/lang-RU-9E4DFF?labelColor=050814)](README_RU.md)

Минималистичная Android-игра о цепных реакциях, управляемая одним касанием.

</div>

Частицы движутся по почти чёрному полю. У игрока есть одно касание. Оно создаёт расширяющийся импульс; каждая задетая частица может породить новую волну, а новая волна — продолжить реакцию. Вся попытка решается одним выбранным моментом.

Текущая версия исходного кода: **0.1.0** (`versionCode 1`) · Min SDK: **26 (Android 8.0)** · Target SDK: **37**

## ⚡ Идея

1. Наблюдать за движением частиц.
2. Один раз коснуться экрана и разместить единственный импульс игрока.
3. Дождаться, пока волна расширится и активирует соседние частицы.
4. Активированные частицы создают собственные волны.
5. Достичь цели до затухания реакции — или мгновенно начать новую попытку.

Сейчас прототип запускается с **20 движущимися частицами**, а для победы требуется активировать **12**.

> Одно касание. Один импульс. Максимальная цепь.

## ✨ Что уже работает

- Управление одним касанием и один импульс на попытку
- Движущиеся частицы с отражением от границ поля
- Расширяющиеся волны игрока и цепной реакции
- Учёт глубины цепи и количества активированных частиц
- Мгновенный повтор с новым детерминированным seed
- Независимая от частоты кадров fixed-step симуляция
- Почти чёрное поле, электрически-голубые обычные частицы и фиолетово-пурпурные цепные реакции
- Русские и английские ресурсы интерфейса
- Портретная Android-ориентация
- Без аккаунта, backend, аналитики, рекламы и разрешения на доступ в интернет

Текущая сборка специально сосредоточена на одном вопросе: **достаточно ли сама цепная реакция приятна и увлекательна, чтобы строить вокруг неё полноценную игру?**

## 🎮 Ощущение игры

IMPULSE строится на ясности и нарастании реакции, а не на визуальном шуме:

- **ожидание** — холодные голубые частицы спокойно движутся по полю;
- **касание** — из выбранной точки расходится один голубой импульс;
- **цепь** — активированные частицы становятся фиолетово-пурпурными и создают новые волны;
- **результат** — попытка быстро завершается, поэтому до следующего запуска цепи всегда один шаг.

Симуляция детерминирована и поддерживает seed, поэтому одно и то же состояние уровня можно воспроизводить на разных устройствах и в тестах, при этом рендеринг может работать с частотой обновления экрана.

## 📦 Установка

Публичной бинарной версии пока нет. До выхода первого релиза проект можно собрать из исходного кода.

Официальные подписанные APK и AAB будут публиковаться только в [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases).

Требуется Android 8.0 или новее.

## 🛠️ Сборка из исходного кода

Требования:

- JDK 17
- Android SDK 37
- Gradle 9.5.0 через Gradle Wrapper из репозитория

```bash
git clone https://github.com/StanleyLl0yd/impulse.git
cd impulse
./gradlew assembleDebug
```

Основная локальная проверка:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## 🧱 Технологии

| Категория | Технология |
| --- | --- |
| Язык | Kotlin 2.4.10 |
| UI | Jetpack Compose + Material 3 |
| Рендеринг | Compose Canvas |
| Симуляция | Собственный детерминированный fixed-step 2D-движок |
| Сборка | Gradle 9.5.0, AGP 9.3.2, Kotlin DSL |
| Android | minSdk 26, targetSdk 37, compileSdk 37 |

Игровое состояние и симуляция отделены от рендеринга, чтобы визуальную часть можно было развивать независимо, не превращая renderer в игровой движок.

## ✅ Проверки качества и безопасности

Pull request и push в `main` автоматически проходят:

- unit tests;
- Android Lint;
- сборку debug и release APK/AAB;
- компиляцию Android instrumentation tests;
- runtime instrumentation tests на API 37;
- еженедельный runtime-прогон на минимальном API 26;
- CodeQL для Java/Kotlin;
- Semgrep с security- и secret-rules;
- Gitleaks по полной истории Git;
- Qodana по расписанию и вручную.

Сторонние GitHub Actions закреплены по неизменяемым commit SHA, workflow используют минимально необходимые permissions, а защищённый `main` требует успешные `Verify`, `Analyze Java and Kotlin`, `Semgrep` и `Gitleaks` перед squash merge.

Об уязвимостях следует сообщать по правилам из [SECURITY.md](SECURITY.md).

## 🔐 Целостность релизов

Официальные релизы запускаются тегами `vMAJOR.MINOR.PATCH`, которые должны совпадать с `versionName`.

Android Release workflow:

1. проверяет тег и версию исходного кода;
2. запускает тесты и Android Lint;
3. восстанавливает предоставленный владельцем ключ подписи только внутри защищённого environment `release`;
4. собирает подписанные APK и AAB;
5. проверяет APK Signature Schemes v2/v3, количество подписантов и ожидаемый SHA-256 сертификата для APK и AAB;
6. создаёт SHA-256 checksums;
7. создаёт GitHub artifact attestations для APK и AAB;
8. публикует GitHub Release только после успешной проверки.

Keystore и пароли подписи никогда не хранятся в репозитории. Подробности: [docs/RELEASE.md](docs/RELEASE.md).

## 🔒 Приватность

- **Offline по умолчанию** — приложение не запрашивает Android-разрешение `INTERNET`
- **Без аккаунта, аналитики, tracking и рекламы**
- Без backend и обязательной облачной инфраструктуры
- В текущем объёме нет опасных Android runtime permissions

Это намеренная граница проекта, которая остаётся базовой, пока у будущей функции не появится конкретная причина её изменить.

## 🌍 Языки

- English — язык по умолчанию
- Русский

Интерфейс следует языку устройства через Android resources.

## 🗺 План развития

- **Прототип:** доказать жизнеспособность цепной реакции и базового game feel
- **Далее:** визуальная, звуковая и тактильная полировка, более сильный instant-retry feedback
- **Затем:** система уровней, прогрессия, scoring и сохранение состояния
- **Контент:** около 60 подготовленных/генерируемых уровней для первой полноценной версии
- **Позже:** endless mode и daily challenge только после того, как базовая игра будет доказана

Направление проекта подробно описано в [PROJECT.md](PROJECT.md).

## 📊 История изменений

- [CHANGELOG.md](CHANGELOG.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/impulse/releases)

## 🤝 Участие в разработке

Bug reports и небольшие целевые pull request приветствуются.

Изменения должны оставаться компактными, сохранять детерминированность геймплея и offline-first границу, не добавлять лишние зависимости и по возможности сопровождаться тестами для изменяемой игровой логики и исправлений. Правила проекта находятся в [AGENTS.md](AGENTS.md).

## 📄 Лицензия

IMPULSE распространяется по **PolyForm Noncommercial License 1.0.0**.

Некоммерческое использование, копирование, изменение и распространение разрешены в рамках условий лицензии. Для коммерческого использования требуется отдельное соглашение. Полный текст: [LICENSE](LICENSE).

Copyright © 2026 Stanley Lloyd.

## 👨‍💻 Автор

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

<div align="center">

`○ ждать · ◎ коснуться · ◉ расширить · ✦ запустить цепь`

**ОДНО КАСАНИЕ · ОДИН ИМПУЛЬС · МАКСИМАЛЬНАЯ ЦЕПЬ**

</div>
