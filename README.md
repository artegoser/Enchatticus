# Enchatticus

Server-side Fabric 26.2 chat channels with optional LuckPerms integration.

## Behaviour

- `Hello` -> local chat (default radius: 64 blocks)
- `!Hello` -> global chat
- `/me takes out an ID` -> local emote
- `/me !declares an emergency` -> global emote
- `/say`, `/msg`, `/tell`, `/w`, team chat and other command messages keep vanilla behaviour
- LuckPerms' resolved `prefix` (and optionally `suffix`) is inserted before the player name

Clients do not need Enchatticus installed.

## Configuration

The first launch creates `config/enchatticus.json`.

The channel labels themselves are separate settings: `localPrefix` and `globalPrefix`.

Available placeholders in all four chat formats:

- `{channel}` - configured local/global channel prefix
- `{prefix}` - resolved LuckPerms prefix
- `{name}` - player name
- `{suffix}` - resolved LuckPerms suffix
- `{message}` - message/action text

Formatting supports legacy Minecraft `&` codes, for example `&6`, `&l`, `&r`, plus `&#RRGGBB` hex colors. Player message text is inserted literally, so players cannot inject formatting codes through chat.

Reload the configuration without restarting:

```text
/enchatticus reload
```

## LuckPerms

LuckPerms is optional. If it is installed, Enchatticus reads the player's fully resolved cached meta prefix.

Example police group prefix:

```text
/lp group police meta setprefix 100 "&9👮 &r"
```

A player inheriting the `police` group will then automatically get that icon/prefix in local chat, global chat and `/me`.

LuckPerms is not bundled into this mod; install the Fabric LuckPerms mod separately.
