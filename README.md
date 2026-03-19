# Vanish

Simple Vanish plugin for Paper 1.21.x with:
- `/v` and `/v <player>`
- fake quit/join messages when toggling vanish
- PlaceholderAPI placeholders for online counters
- Chatty compatibility (hides vanilla join/quit for vanished players)
- server list ping online counter fix (vanished players are hidden)

## Requirements

- Paper or Purpur `1.21.x`
- Java `17+`

Optional:
- PlaceholderAPI (for `%vanish_online%`, `%vanish_vanished%`)
- LuckPerms (for easy permission management)
- Chatty (compatibility already included)

## Commands

- `/v` - toggle vanish
- `/v <player>` - enable vanish and teleport to player
- `/vanishreload` (alias: `/vr`) - reload config

## Permissions

- `vanish.use` - use `/v`
- `vanish.see` - see vanished players
- `vanish.reload` - use `/vanishreload`

## Placeholders

- `%vanish_online%` - online count minus vanished online players
- `%vanish_vanished%` - number of online vanished players

## Features

- Vanished players are hidden in tab and from regular players.
- Online list in server ping (multiplayer menu) excludes vanished players.
- Vanished state is kept after reconnect.
- Real join/quit messages are hidden for vanished players.
- On rejoin while vanished, moderator gets a reminder message.
- Hex color support in config messages: `&#RRGGBB`.

## Build

```bash
mvn -U -DskipTests package
```

Built jar:
`target/Vanish-1.0.0.jar`

## Default Config

See: `src/main/resources/config.yml`

## Compatibility Notes

- Main target: Paper/Purpur `1.21.x`.
- Older versions are not guaranteed.

