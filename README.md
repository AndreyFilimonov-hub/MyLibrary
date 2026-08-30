# MyLibrary

**MyLibrary** — офлайн-приложение для личной библиотеки и чтения книг на Android и iOS. Один Kotlin Multiplatform-модуль содержит основную логику и интерфейс на Compose Multiplatform; нативные проекты нужны только как точки входа и для платформенных интеграций.

Приложение не использует сервер: книги, обложки, настройки и прогресс чтения хранятся локально на устройстве.

## Возможности

### Библиотека

- Импорт EPUB, FB2 и PDF-файлов.
- Извлечение названия, автора и обложки, где это поддерживает формат.
- Создание уменьшенных копий обложек для плавной прокрутки списка.
- Фильтрация всех, избранных и прочитанных книг.
- Отметки «избранное» и «прочитано».
- Удаление записи книги, исходного файла и обложки из локального хранилища.
- Защита от повторного добавления одной и той же книги по SHA-256 хэшу.

### EPUB и FB2 reader

- Постраничная пагинация с учётом размера экрана, шрифта и межстрочного интервала.
- Горизонтальный и вертикальный режимы чтения.
- Настройки размера шрифта, темы и режима чтения.
- Сохранение и восстановление позиции чтения.
- Поиск по книге, сниппеты и переход к найденному слову.
- Подсветка выбранного результата поиска.
- Поддержка встроенных изображений и полноэкранного просмотра.

### PDF reader

- Горизонтальный и вертикальный pager.
- Pinch-to-zoom, перемещение увеличенной страницы с ограничением границ.
- Счётчик текущей страницы.
- Поиск по тексту PDF с частичной выдачей результатов и кэшированием текста страниц.
- Переход к результату и кратковременная подсветка выбранного фрагмента.
- Сохранение прогресса и общих настроек reader.

## Архитектура

Проект разделён на два приложения-точки входа и общий модуль:

```text
androidApp/  Android entry point
iosApp/      SwiftUI entry point и Xcode-проект
shared/      Общий Kotlin Multiplatform-код
```

В `shared` используются слои:

- `data` — парсеры файлов, Room DAO, хранилище книг и обложек, реализации репозиториев;
- `domain` — модели, интерфейсы репозиториев и use case;
- `presentation` — Compose-экраны, ViewModel, UI state и команды;
- `androidMain` / `iosMain` — платформенные реализации PDF-метаданных, базы данных, DataStore, DI и обработки обложек.

Пагинация EPUB/FB2 зависит от параметров Compose (`TextMeasurer`, размеров и плотности экрана), поэтому выполняется отдельным presentation-компонентом `LazyBookPaginator` и отменяется при закрытии reader.

## Технологии

- Kotlin Multiplatform и Kotlin 2.3.
- Compose Multiplatform и Material 3.
- Kotlin Coroutines и Flow.
- Koin для dependency injection.
- Room KMP и SQLite Bundled для локальной БД.
- DataStore Preferences для настроек reader.
- Navigation Compose.
- Coil 3 для обложек.
- FileKit для выбора и работы с файлами на Android и iOS.
- epub4kmp для EPUB, KSoup для FB2/HTML/XML, PDFium для отображения и поиска PDF.
- PDFBox Android для чтения PDF-метаданных на Android.

## Запуск Android

### Требования

- Android Studio.
- JDK 21.
- Android SDK Platform 36.
- Android-устройство с Android 7.0 (API 24) или новее либо эмулятор.

### Через Android Studio

1. Откройте корневую папку проекта.
2. Дождитесь завершения Gradle Sync.
3. Выберите конфигурацию `androidApp`.
4. Выберите эмулятор или физическое устройство и нажмите **Run**.

### Через терминал

macOS/Linux:

```bash
./gradlew :androidApp:assembleDebug
```

Windows:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

APK будет создан в `androidApp/build/outputs/apk/debug/`. Для установки на подключённое устройство можно выполнить `:androidApp:installDebug`.

## Запуск iOS

### Требования

- macOS.
- Xcode 16.2 или новее.
- Установленный JDK 21.
- Для запуска на физическом устройстве — Apple ID и настроенный code signing.

В проекте минимальная поддерживаемая версия iOS — **18.2**.

### Основной способ — через Xcode

1. На macOS откройте `iosApp` в Xcode.
2. Для физического устройства укажите свою команду разработки в **Signing & Capabilities**. При необходимости заполните `TEAM_ID` в `iosApp/Configuration/Config.xcconfig`.
3. Выберите destination:
   - iPhone/iPad Simulator на Apple Silicon — simulator;
   - подключённый iPhone или iPad — physical device.
4. Нажмите **Run**.

В target уже есть Build Phase **Compile Kotlin Framework**. Он запускает Gradle-задачу `:shared:embedAndSignAppleFrameworkForXcode`, которая собирает подходящий `Shared.framework` для выбранного destination. При обычном запуске вручную добавлять framework не требуется.

### PDFium: подключение нативного бинарника

`Shared.framework` вручную добавлять не нужно: его для Xcode собирает Build Phase **Compile Kotlin Framework**.

Для PDF reader в репозитории уже лежат два нативных бинарника PDFium. Ничего скачивать или собирать отдельно не нужно:

| Destination | Файл |
| --- | --- |
| iPhone/iPad Simulator на Apple Silicon | `iosApp/ThirdParty/pdfium/ios-simulator-arm64/libpdfium.dylib` |
| Физический iPhone/iPad | `iosApp/ThirdParty/pdfium/ios-arm64/libpdfium.dylib` |

Перед первым запуском выберите бинарник, соответствующий текущему destination:

1. В Xcode откройте target **iosApp** → **General** → **Frameworks, Libraries, and Embedded Content**.
2. Нажмите **+**, затем **Add Other…** → **Add Files…**.
3. Выберите нужный `libpdfium.dylib` из таблицы.
4. В колонке **Embed** выберите **Embed & Sign**.

Для simulator и физического устройства нельзя использовать один и тот же файл. При смене destination замените подключённый бинарник на второй вариант. Если в списке уже есть `libpdfium.dylib`, удалите старую ссылку перед добавлением подходящего файла.

Xcode автоматически добавит библиотеку в **Link Binary With Libraries** и скопирует её в приложение. Если ошибка `library not found for -lpdfium` сохраняется, проверьте, что в **Build Settings** → **Library Search Paths** указан каталог выбранного бинарника.

## Проверка

Android host-тесты:

```bash
./gradlew :shared:testAndroidHostTest
```

iOS simulator-тесты запускаются только на macOS:

```bash
./gradlew :shared:iosSimulatorArm64Test
```
