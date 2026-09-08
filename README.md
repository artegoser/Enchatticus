# Enchatticus

Server-side Fabric 26.2 chat mod with local/global channels, `/me`, LuckPerms integration, interactive role badges and TAB formatting.

## Chat semantics

- `Привет` -> local chat.
- `!Привет` -> global chat.
- `/me достал рацию` -> local emote.
- `/me !объявил тревогу` -> global emote.
- `/say` remains vanilla and is not intercepted.
- `/msg`, `/tell`, `/w` remain vanilla/private and are not intercepted.

## Config

The config is created at `config/enchatticus.json`.

Important placeholders:

- `{channel}` - local/global channel prefix.
- `{badges}` - zero or more interactive badges from `badges`.
- `{prefix}` - LuckPerms resolved meta prefix.
- `{suffix}` - LuckPerms resolved meta suffix.
- `{name}` - player name.
- `{message}` - message text (chat formats only).

Colors support legacy `&` codes (`&a`, `&l`, `&r`, etc.) and hex `&#RRGGBB`.
Player-written message text is inserted as plain text, so players cannot inject formatting through chat messages.

### Example

See `enchatticus.example.json` for a complete example.

```json
{
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
  },
  "tab": {
    "enabled": true,
    "format": "{badges}{prefix}{name}{suffix}"
  }
}
```

## LuckPerms: rank prefix vs badges

Enchatticus intentionally has two mechanisms.

`{prefix}` is the normal LuckPerms resolved meta prefix. LuckPerms selects the effective prefix according to its meta stacking/priority rules. This is useful for one main rank such as Admin, Moderator, VIP, etc.

`{badges}` is Enchatticus' multi-prefix system. Every matching badge is rendered, so one player can have several role icons at once. Badges can be activated by an inherited LuckPerms group, a LuckPerms permission node, or both.

Example group badge:

```json
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
}
```

A player inherits this badge when they inherit the `police` group, including through parent groups.

If both `group` and `permission` are non-empty, both conditions must pass. If both are empty, the badge is shown to everyone.

Badges are displayed in the same order as they appear in the JSON array.

### Badge click actions

- `suggest` - places the command in the player's chat input; safest default.
- `run` - runs the command immediately when clicked.
- `copy` - copies the configured string to clipboard.

`{name}` and `{id}` can be used in `clickCommand`. Hover text supports `{name}`, `{command}` and `{id}`.

## TAB

TAB formatting is controlled separately:

```json
"tab": {
  "enabled": true,
  "format": "{badges}{prefix}{name}{suffix}"
}
```

The same role badges and LuckPerms prefix/suffix can therefore be kept consistent between chat and the player list.

`showInTab: false` hides an individual badge from TAB while keeping it in chat. `showInChat: false` does the opposite.

TAB refreshes when the config is reloaded and when LuckPerms recalculates user/group data.

## Permissions

Default nodes:

- `enchatticus.chat.local` - send local chat. Default: allowed.
- `enchatticus.chat.global` - send global chat. Default: allowed.
- `enchatticus.chat.me` - use `/me` through Enchatticus. Default: allowed.
- `enchatticus.command.reload` - use `/enchatticus reload`. Default: vanilla gamemaster/operator level unless LuckPerms explicitly decides otherwise.

The node names and the three chat defaults are configurable under `permissions`.

LuckPerms `true`/`false` overrides the configured default. If LuckPerms has no decision for a chat node, Enchatticus uses `localChatDefault`, `globalChatDefault` or `meDefault`.

Examples:

```text
/lp group default permission set enchatticus.chat.local true
/lp group default permission set enchatticus.chat.global true
/lp group default permission set enchatticus.chat.me true
/lp group muted permission set enchatticus.chat.local false
/lp group muted permission set enchatticus.chat.global false
/lp group admin permission set enchatticus.command.reload true
```

Wildcards such as `enchatticus.chat.*` can be managed by LuckPerms in the normal way.

## Typical role setup

Create or use a LuckPerms group:

```text
/lp creategroup police
/lp user PlayerName parent add police
```

Then add a badge with `"group": "police"` to `enchatticus.json` and run:

```text
/enchatticus reload
```

No LuckPerms meta prefix is required for the badge. If you also set a normal LuckPerms prefix, it is rendered through `{prefix}` in addition to the badge:

```text
/lp group police meta setprefix 100 "&9[POLICE] &r"
```

If that duplicates your badge, either remove `{prefix}` from the formats or set:

```json
"luckPerms": {
  "usePrefix": false,
  "useSuffix": false
}
```

## Build

Requires Java 25 and Fabric Loader 0.19.3 or newer.

```bash
./gradlew build
```

The remapped JAR is written to `build/libs/`.
