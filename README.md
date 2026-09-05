# MyLibrary

**MyLibrary** — офлайн-приложение для личной библиотеки и чтения книг на Android и iOS. Книги, обложки, настройки и прогресс чтения остаются на устройстве: сервер и аккаунт не нужны.

## Возможности

- Импорт EPUB, FB2 и PDF.
- Библиотека с обложками, фильтрами «Все», «Избранное» и «Прочитано».
- Защита от повторного добавления одной книги по SHA-256.
- EPUB/FB2 reader: горизонтальный и вертикальный режимы, настройки текста, поиск, переход к результату и сохранение позиции чтения.
- PDF reader: pager, pinch-to-zoom, ограниченное перемещение увеличенной страницы, индикатор страницы, поиск с частичной выдачей и подсветкой выбранного результата.
- Уменьшенные копии обложек для списка; при удалении книги удаляются запись БД, исходный файл и обложка.

## Архитектура

Проект построен как Kotlin Multiplatform-приложение с Compose Multiplatform UI. `commonMain` содержит общую бизнес-логику и UI, а `androidMain` и `iosMain` — только платформенные реализации.

```text
androidApp/                 Android entry point
iosApp/                     Xcode host application
shared/                     composition root: App, Navigation, Koin

core/                       общие модели, Result, UI, coroutine utilities
data/
  database/                 Room KMP, DAO, datasource и реализация БД
  storage/                  файлы книг, обложки и platform-specific thumbnail
feature/
  library/                  импорт, парсинг метаданных и экран библиотеки
  reader/                   EPUB/FB2 reader, PDF reader, поиск и настройки
```

Зависимости направлены от верхнего уровня к нижнему:

```text
shared → feature:* → core
                 ↘ data:database, data:storage
```

`shared` — единственное место, где собираются Koin-модули и навигация. Фичи не зависят друг от друга. `data:database` скрывает Room-детали за datasource-интерфейсами, а `data:storage` отвечает за файловое хранилище.

Для EPUB/FB2 используется `LazyBookPaginator`: он строит страницы с учётом размера контейнера и настроек текста, отдаёт первую нужную страницу с высоким приоритетом, а остальные подсчитывает в фоне. Пагинатор отменяется при пересоздании reader или уничтожении его ViewModel.

## Технологии

- Kotlin Multiplatform, Kotlin 2.3 и Compose Multiplatform
- Material 3, Navigation Compose, Lifecycle ViewModel
- Kotlin Coroutines и Flow
- Koin
- Room KMP + SQLite Bundled
- DataStore Preferences
- Coil 3 и FileKit
- epub4kmp для EPUB, KSoup для FB2/XML, PDFium для отображения PDF
- PDFBox Android для PDF-метаданных на Android

## Запуск Android

Требуются Android Studio, JDK 21, Android SDK Platform 36 и устройство/эмулятор с Android 7.0 (API 24) или новее.

1. Откройте корневую папку проекта в Android Studio и дождитесь Gradle Sync.
2. Выберите конфигурацию `androidApp`.
3. Выберите устройство и нажмите **Run**.

Сборка из терминала Windows:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

APK появится в `androidApp/build/outputs/apk/debug/`.

## Запуск iOS

Требуются macOS, Xcode и JDK 21. Минимальная версия iOS — **18.2**.

1. Откройте `iosApp/iosApp.xcodeproj` в Xcode.
2. Для физического устройства выберите свою команду в **Signing & Capabilities**. При необходимости заполните `TEAM_ID` в `iosApp/Configuration/Config.xcconfig`.
3. Выберите iOS Simulator на Apple Silicon либо физический iPhone/iPad.
4. Нажмите **Run**.

Build Phase **Compile Kotlin Framework** уже вызывает `:shared:embedAndSignAppleFrameworkForXcode`, поэтому `Shared.framework` вручную добавлять не нужно.

### PDFium для iOS

Для PDF reader в репозитории уже лежат готовые PDFium-библиотеки. Ничего скачивать отдельно не нужно:

| Destination                | Библиотека                                                     |
|----------------------------|----------------------------------------------------------------|
| Simulator на Apple Silicon | `iosApp/ThirdParty/pdfium/ios-simulator-arm64/libpdfium.dylib` |
| Физический iPhone/iPad     | `iosApp/ThirdParty/pdfium/ios-arm64/libpdfium.dylib`           |

Перед первым запуском убедитесь, что в target **iosApp → General → Frameworks, Libraries, and Embedded Content** подключён бинарник для выбранного destination с опцией **Embed & Sign**. При переключении между simulator и физическим устройством замените `libpdfium.dylib` на вариант для соответствующей архитектуры.

## Тесты

Общие тесты размещаются рядом с кодом в `commonTest`. Например, Android host-тесты core можно запустить так:

```powershell
.\gradlew.bat :core:testAndroidHostTest
```

iOS simulator-тесты требуют macOS:

```bash
./gradlew :shared:iosSimulatorArm64Test
```
