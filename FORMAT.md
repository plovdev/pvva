# .PVVA Format Description

**PVVA** - PornViewer Video Adapter.
Бинарный формат, который представляет плагин для парсинга сайтов-источников
для [PornViewer](https://pornviewer.foundation).

Основная структура файла - `.json` конфиги и `.lua` скрипты.

## Общая структура файла

HEADER:

- `PVVAHeader fields`
- `metadata`
- `etc`

TABLE:

- `tableSize`
- `entriesCount`
- `List of entries`
    - `entry id len`
    - `entry offset`
    - `entry id`

BODY:

- `plugin.json`
- `/configs`
    - `resource-config.json`
    - `other configs(http-config.json, captcha-config.json, etc.)`
- `/sscripts`
    - `main-parser.parser`
    - `other .parser` files, which should be included in build
- `/resources`
    - user files. Should be included into build in build.xml <include> section.

## Детальный разбор структуры

### HEADER 28 bytes:

Заголовок генерируется из `build.xml` файла, и файлов проекта.

```pvva-mark
--------------------------------------------------------------------
Magic number(PVVA)                                        // 4bytes
File version                                              // 1byte
File flag                                                 // 1byte
Contains signature                                        // 1byte
Build ID                                                  // 4bytes

plugin id length                                          // 1byte
MIN_APP_SUPPORTED_VERSION                                 // 4bytes
MAX_APP_SUPPORTED_VERSION                                 // 4bytes

json size                                                 // 4bytes
table offset                                              // 4bytes
Plugin ID                                                 // nbytes
--------------------------------------------------------------------

# BODY:

plugin.json                                               // nbytes

-------------
CHUNK-TYPE                                                // 4bytes
CHUNK-SIZE                                                // 4byte
CHUNK-ID length                                           // 1 byte
CHUNK-ID                                                  // nbyte
CHUNK-CONTENT(compressed by zlib format)                  // nbytes
-------------
Plugin signature                                          // 64bytes
```

## Пояснения

`File Version` - версия формата
`File flag` - модификация/возможности данного плагина.
Существуют следующие флаги:

1. `0` - Стандартный плагин. Содержит в себе только .json и .parser файлы. Подходит для безопасного публичного
   распространения

`Contains signature` - Содержит ли файл подпись, которой он подписан?

`Build ID` - id сборки плагина. Необходима для проверки версий в автообновлениях.
Current BuildID < Available BuildID ? load update : not load update.

`MIN/MAX_APP_SUPPORTED_VERSION` - минимальная и максимальная версия приложения, в пределах которой плагин будет работать
отлично.
`Json Size` - размер `plugin.json` - файл-описание плагина для пользователя и приложения.

`Table offset` - **Абсолютное** смещение до таблицы смещений записей в BODY.

`Plugin ID` - **уникальный** и **понятный** id плагина в системе PornViewer.
Это постоянное значение. Нужно для идентификации плагина в приложении.

### Entries Offset Table

Таблица смещений для быстрого доступа к любой записи в BODY.
Структура таблицы проста:
Заголовок

- `table size(2 bytes)` - размер тела таблицы.
- `entries count(1 byte)` - количество записей в BODY.
  Таблица может вмещать только 128 записей для быстрого доступа.
  Если по какой то причине ваш плагин содержит больше записей, то в таблицу запишутся только первые 127 записей.

Тело(Список записей)

- `entry id length(1 byte)` - Длина ID записи(chunk id).
- `entry offset(4 byte)` - **Абсолютное** смещение до конкретного чанка.
- `entry(chunk) id` - Уникальный ID записи(чанка).

### Тело файла

В начале идет `plugin.json` - текстовый файл-описание плагина.
Обязательные и дополнительный поля plugin.json:

1. Блок commons(Обязательный) - общие сведения о плагине:
    - `title` - название плагина.
    - `version` - dev версия плагина.
    - `descryption` - описание плагина.
2. Блок auto-update(Дополнительный) - проверка автообновления плагина
    - `url` - URL endpoint для проверки плагина.
3. Блок legal(Дополнительный) - лицензия, автор, и ссылки.
    - `author` - Имя автора плагина.
    - `developerId` - Уникальный id разработчика. Обязательное поле в блоке legal.
    - `author-page` - страница разработчика плагина.
    - `license` - url лицензии плагина.
    - `homepage` - домашняя страница плагина. Обычно ссылка на исходный код.

Затем идут чанки(файл-блоки). Это `.json`, `.lua` и тд.

`CHUNK-TYPE` - Тип чанка(SYSTEM, CONFIG, etc.)
`CHUNK-SIZE` - размер данных чанка.
`CHUNK-ID length` - Длина имени чанка.
`CHUNK-ID` - имя чанка.
`CHUNK-CONTENT` - сам чанк.

В самом конце плагина возможно поле signature которое является цифровой подписью плагина.