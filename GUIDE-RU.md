# Enchatticus — настройка

## 1. Что делает мод

Обычное сообщение отправляется в локальный чат:

```text
Привет
```

Глобальный чат вызывается первым символом `!`:

```text
!Привет всему серверу
```

`/me` использует ту же семантику:

```text
/me достал рацию
/me !объявил общую тревогу
```

`/say`, `/msg`, `/tell`, `/w` Enchatticus не перехватывает.

После изменения `config/enchatticus.json` применяй конфиг командой:

```text
/enchatticus reload
```

## 2. Основной конфиг чата

```json
"chat": {
  "localRadius": 64.0,
  "globalTrigger": "!",
  "globalAcrossDimensions": true,
  "localPrefix": "&8[Л] &r",
  "globalPrefix": "&6[Г] &r",
  "localFormat": "{channel}{badges}{prefix}{name}{suffix}&7: &f{message}",
  "globalFormat": "{channel}{badges}{prefix}{name}{suffix}&7: &f{message}",
  "localMeFormat": "{channel}* {badges}{prefix}{name}{suffix} &7{message}",
  "globalMeFormat": "{channel}* {badges}{prefix}{name}{suffix} &f{message}"
}
```

`localRadius` — радиус локального чата в блоках.

`globalTrigger` — строка в начале сообщения, переключающая его в глобальный канал.

`globalAcrossDimensions: true` — глобальный чат видят игроки во всех измерениях. При `false` — только в текущем измерении отправителя.

`localPrefix` и `globalPrefix` — подписи каналов.

Доступные placeholders:

- `{channel}` — `[Л]` или `[Г]`;
- `{badges}` — все активные интерактивные префиксы/значки игрока;
- `{prefix}` — обычный resolved prefix LuckPerms;
- `{suffix}` — обычный resolved suffix LuckPerms;
- `{name}` — ник;
- `{message}` — текст сообщения.

Поддерживаются цвета `&0`…`&f`, форматирование `&l`, `&o`, `&n`, `&m`, `&r` и HEX вида `&#55AAFF`.

Сам `{message}` вставляется как обычный текст: игрок не сможет написать `&c` и самостоятельно покрасить сообщение.

## 3. Обычный prefix LuckPerms

Если оставить:

```json
"luckPerms": {
  "usePrefix": true,
  "useSuffix": false
}
```

то `{prefix}` берётся из стандартного metadata LuckPerms.

Например:

```text
/lp group admin meta setprefix 100 "&c[ADMIN] &r"
```

Получится примерно:

```text
[Л] [ADMIN] Nick: сообщение
```

Это именно обычный LuckPerms prefix. Обычно LuckPerms выбирает один итоговый prefix согласно своей meta-конфигурации.

## 4. Несколько интерактивных префиксов одновременно

Для нескольких ролей используется массив `badges`.

Например:

```json
"badges": [
  {
    "id": "police",
    "text": "&9👮 &r",
    "hover": "&9Полицейский\n&7Сотрудник полиции\n&eНажми, чтобы написать игроку",
    "group": "police",
    "permission": "",
    "clickCommand": "/msg {name} ",
    "clickAction": "suggest",
    "showInChat": true,
    "showInTab": true
  },
  {
    "id": "medic",
    "text": "&c✚ &r",
    "hover": "&cМедик\n&7Медицинская служба",
    "group": "medic",
    "permission": "",
    "clickCommand": "",
    "clickAction": "suggest",
    "showInChat": true,
    "showInTab": true
  }
]
```

Если игрок состоит одновременно в `police` и `medic`, будут показаны оба значка в том порядке, в котором они стоят в JSON.

### Поля badge

`id` — внутреннее уникальное имя.

`text` — отображаемый префикс. Пробел после значка лучше добавить прямо сюда.

`hover` — текст при наведении в чате. Можно использовать переносы `\n` и цвета.

`group` — LuckPerms-группа. Учитывается наследование групп.

`permission` — дополнительная LuckPerms permission-node.

Если заданы одновременно `group` и `permission`, игрок должен удовлетворять обоим условиям.

Если оба поля пустые, badge показывается всем.

`showInChat` и `showInTab` независимо включают отображение значка в чате и TAB.

## 5. Команда по клику на значок

Пример:

```json
"clickCommand": "/msg {name} ",
"clickAction": "suggest"
```

`{name}` заменяется на ник владельца префикса.

Варианты `clickAction`:

- `suggest` — подставить команду в строку чата, но не выполнять;
- `run` — выполнить команду сразу от имени кликнувшего игрока;
- `copy` — скопировать строку в буфер обмена.

Для административных или опасных действий лучше использовать `suggest`, а не `run`.

В `hover` доступны `{name}`, `{id}` и `{command}`.

Например:

```json
"hover": "&9Полицейский\n&7Игрок: &f{name}\n&eКоманда: &f{command}"
```

## 6. Управление ролями через LuckPerms

Создать группу полицейских:

```text
/lp creategroup police
```

Выдать игроку роль:

```text
/lp user Nick parent add police
```

Снять:

```text
/lp user Nick parent remove police
```

Если badge содержит:

```json
"group": "police"
```

то отдельный `meta setprefix` для этого badge не нужен.

Можно вместо группы привязать badge к permission:

```json
{
  "id": "police",
  "text": "&9👮 &r",
  "hover": "&9Полицейский",
  "group": "",
  "permission": "enchatticus.badge.police",
  "clickCommand": "",
  "clickAction": "suggest",
  "showInChat": true,
  "showInTab": true
}
```

А затем:

```text
/lp group police permission set enchatticus.badge.police true
```

Так можно полностью контролировать значки через LuckPerms permission-ноды.

## 7. TAB

```json
"tab": {
  "enabled": true,
  "format": "{badges}{prefix}{name}{suffix}"
}
```

Например в TAB может отображаться:

```text
👮 ✚ [VIP] Nick
```

где `👮` и `✚` — несколько Enchatticus badges, а `[VIP]` — обычный LuckPerms `{prefix}`.

После `/enchatticus reload` TAB обновляется сразу. Мод также подписан на перерасчёт user/group data LuckPerms и обновляет display name игроков.

Vanilla TAB не является обычным кликабельным чат-компонентом во время игры, поэтому hover/click предназначены прежде всего для сообщений чата; в TAB гарантируется отображение текста префиксов.

## 8. Permissions Enchatticus

По умолчанию используются:

```text
enchatticus.chat.local
enchatticus.chat.global
enchatticus.chat.me
enchatticus.command.reload
```

Локальный, глобальный чат и `/me` разрешены обычным игрокам по умолчанию.

`/enchatticus reload` без явного решения LuckPerms доступен только vanilla gamemaster/operator level.

### Разрешить стандартной группе весь чат

```text
/lp group default permission set enchatticus.chat.local true
/lp group default permission set enchatticus.chat.global true
/lp group default permission set enchatticus.chat.me true
```

Это необязательно при стандартных defaults, но удобно, если хочешь явно хранить всё в LuckPerms.

### Запретить глобальный чат группе

```text
/lp group localonly permission set enchatticus.chat.global false
```

### Полностью заглушить группу

```text
/lp group muted permission set enchatticus.chat.local false
/lp group muted permission set enchatticus.chat.global false
/lp group muted permission set enchatticus.chat.me false
```

### Разрешить reload админам

```text
/lp group admin permission set enchatticus.command.reload true
```

Можно использовать стандартные wildcard-ноды LuckPerms, например:

```text
/lp group admin permission set enchatticus.chat.* true
```

В конфиге сами названия permission-нод можно изменить:

```json
"permissions": {
  "localChat": "enchatticus.chat.local",
  "globalChat": "enchatticus.chat.global",
  "me": "enchatticus.chat.me",
  "reload": "enchatticus.command.reload",
  "localChatDefault": true,
  "globalChatDefault": true,
  "meDefault": true
}
```

Если LuckPerms явно возвращает `true` или `false`, это решение имеет приоритет над `*Default`.

## 9. Рекомендуемая схема

Для простого сервера я бы разделил данные так:

- обычный LuckPerms `{prefix}` — главный ранг: `ADMIN`, `MOD`, `VIP`;
- Enchatticus `{badges}` — дополнительные роли: полиция, медик, журналист, организация, профессия и т.п.;
- LuckPerms группы/permissions — источник того, какие badges активны;
- `localPrefix` / `globalPrefix` — обозначение канала.

Например игрок одновременно может выглядеть так:

```text
[Л] 👮 ✚ [VIP] Nick: сообщение
```

## 10. Если prefix дублируется

Если у `police` уже есть обычный LuckPerms meta-prefix `👮`, и одновременно такой же Enchatticus badge, значок будет два раза.

Либо не задавай `meta setprefix` роли `police`, либо отключи обычный prefix:

```json
"luckPerms": {
  "usePrefix": false,
  "useSuffix": false
}
```

либо убери `{prefix}` из нужных `format`/`tab.format`.

## 11. Сборка

Нужны Java 25, Minecraft 26.2 и Fabric Loader 0.19.3+.

```bash
./gradlew build
```

Готовый JAR будет в:

```text
build/libs/
```
