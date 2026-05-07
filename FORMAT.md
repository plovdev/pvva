# .PVVA Format Description

**PVVA** - PornViewer Video Adapter.
Бинарный формат, который представляет плагин для парсинга сайтов-источников для [PornViewer](https://github.com/anton-1488/PornViewer).

Основная структура файла - `.json` конфиги и `.parser(yaml based)` файлы.

## Общая структура файла

HEADER
- `PVVAHeader fields`
- `metadata`
- `etc`
  BODY:
- `plugin.json`
- `/configs`
    - `resource-config.json` + CRC32 checksum
    - `other configs(http-config.json, captcha-config.json, etc.)` + CRC32 checksum
- `/parsers`
    - `main-parser.parser` + CRC32 checksum
    - `other .parser` files, which should be included in build + CRC32 checksum

## Детальный разбор структуры

### HEADER 50 bytes:

Заголовок генерируется из `build.xml` файла.

```pvva-mark
--------------------------------------------------------------------
Magic number(PVVA)                                        // 4bytes
File version                                              // 1byte
File flag                                                 // 1byte
Build ID                                                  // 4bytes

plugin id length                                          // 1byte
MIN_APP_SUPPORTED_VERSION                                 // 4bytes
MAX_APP_SUPPORTED_VERSION                                 // 4bytes

json size                                                 // 4bytes
Plugin ID                                                 // nbytes
--------------------------------------------------------------------

# BODY:

plugin.json                                               // nbytes

-------------
CHUNK-ID length                                           // 1 byte
CHUNK-SIZE                                                // 4byte
CHUNK-ID                                                  // nbyte
CHUNK-CONTENT                                             // nbytes
-------------
```

## Пояснения

`File Version` - версия формата
`File flag` - модификация/возможности данного плагина.
Существуют следующие флаги:

1. `0` - Стандартный плагин. Содержит в себе только .json и .parser файлы. Подходит для безопасного публичного распространения
2. `1` - Расширенный `.groovy` скриптами(не официальная поддержка). Дает возможность упаковывать и распаковывать скрипты для сложного сценария парсинга. Groovy выполняется в изолированной песочнице
3. `2` - Расширенный `groovy(xGroovy)` - скрипты, которые работают без ограничений. Только в режиме разработчика. Не подходит для распространения.

`Build ID` - id сборки плагина. Необходима для проверки версий в автообновлениях.
Current BuildID < Available BuildID ? load update : not load update.

`Plugin ID` - **уникальный** и **понятный** id плагина в системе PornViewer.
Это постоянное значение. Нужно для идентификации плагина в приложении.

`MIN/MAX_APP_SUPPORTED_VERSION` - минимальная и максимальная версия приложения, в пределах которой плагин будет работать отлично.
`Reversed` - зарезервированное место для доп. полей и расширенных форматов.
`Json Size` - размер `plugin.json` - файл-описание плагина для пользователя и приложения.

### Тело файла

В начале идет `plugin.json` - текстовый файл-описание плагина.
Обязательные и дополнительный поля plugin.json:
1. Блок commons(Обязательный) - общие сведения о плагине:
    - `title` - название плагина.
    - `version` - dev версия плагина.
    - `descryption` - описание плагина.
2. Блок auto-update(Дополнительный) - проверка автообновления плагина
    - `url` - URL endpoint для проверки плагина. На сервере должна работать утилита pvva-server.
    - `sign-required` - требуется ли проверка подписи для плагина.
3. Блок legal(Дополнительный) - лицензия, автор, и ссылки.
    - `author` - Имя автора плагина.
    - `author-page` - страница разработчика плагина.
    - `license` - url лицензии плагина.
    - `homepage` - домашняя страница плагина. Обычно ссылка на исходный код.

Затем идут чанки(файл-блоки). Это `.json`, `.parser`, и, если включено `.groovy`.

`CHUNK-ID length` - Длина имени файла.
`CHUNK-ID` - имя файла.
`CHUNK-SIZE` - размер данных файла.
`CHUNK-CONTENT` - сам файл.
`CHUNK-CRC32` - контрольная сумма проверки целостности файл-блока(всего).

## build.xml In Action:

Build.xml - файл на основе которого собирается плагин.
Начинается с корневого элемента `<plugin>`