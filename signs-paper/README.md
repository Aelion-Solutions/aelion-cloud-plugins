# Aelion Signs

Paper join/status wall signs for Aelion Cloud. Live fleet data and player Connect
go through **Aelion Aero** (`AeroFleetService`) — this plugin does not hold a panel token.

## Requirements

- Paper 1.21.x
- **Aelion Aero** on the same server (`depend: [AelionAero]`) with panel URL + token
- Proxy with BungeeCord plugin messaging / Velocity legacy Connect support

Build note: `signs-paper` compileOnly uses `libs/aero-common-api.jar` (copy from
`aelion-aero` `aero-common` jar after building Aero). At runtime the API classes
come from the Aero plugin JAR.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/aesign create <group> [filter]` | `aelion.signs.admin` | Register the targeted sign for a server group |
| `/aesign remove` | admin | Unregister targeted sign |
| `/aesign removeall` | admin | Clear all signs |
| `/aesign cleanup [world]` | admin | Remove signs in a world |
| `/aesign reload` | admin | Reload config + `signs.yml` |

## Config

See `config.yml` for layouts (`searching` / `starting` / `empty` / `online` / `full`),
knockback, and optional per-group layout overrides. Placed signs are stored in `signs.yml`.

Placeholders: `%name%`, `%group%`, `%online%`, `%max%`, `%status%`.
