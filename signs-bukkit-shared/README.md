# Aelion Signs

Join/status wall signs for Aelion Cloud. Live fleet data and player Connect go
through **Aelion Aero** (`AeroFleetService`) — this plugin does not hold a panel
token.

## Version bands

One JAR per MC range (same cliffs as Aero). See [COMPATIBILITY.md](../docs/COMPATIBILITY.md).

| Band module | MC range |
|-------------|----------|
| `signs-bukkit-1_8` | 1.8 – 1.12.2 |
| `signs-bukkit-1_13` | 1.13 – 1.16.5 |
| `signs-paper-1_17` | 1.17 – 1.20.6 |
| `signs-paper-1_21` | 1.21.x |
| `signs-paper-26` | 26.1 – 26.x |

Shared logic lives in `signs-bukkit-shared`. Band modules are thin entry points.

Build note: compiles against `com.aelion.aero:aero-api` from GitHub Packages
(`aeroApiVersion` in `gradle.properties`). Put credentials in repo-root `.env`
(see `.env.example`). At runtime the API classes come from the Aero plugin JAR —
do not shade `aero-api` into Signs.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/aesign create <group> [filter]` | `aelion.signs.admin` | Register the targeted sign for a server group |
| `/aesign remove` | admin | Unregister targeted sign |
| `/aesign removeall` | admin | Clear all signs |
| `/aesign cleanup [world]` | admin | Remove signs in a world |
| `/aesign reload` | admin | Reload config + `signs.yml` |

## Config

See shared `config.yml` for:

- **animation** — tick rate, sync mode, proximity gate; per-layout `mode` (`loop` / `pingpong`)
- **forcefield** — shape, radius/height, cooldown, particles, sound (legacy `knockback` key still accepted)
- **layouts** — searching / starting / empty / online / full frames + optional `group-layouts`

Placeholders: `%name%`, `%group%`, `%online%`, `%max%`, `%status%`.
Placed signs are stored in `signs.yml`.
