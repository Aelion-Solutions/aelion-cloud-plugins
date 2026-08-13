# Aelion Cloud Plugins

First-party Paper/Spigot plugins that complement
[Aelion Cloud](https://github.com/Aelion-Solutions/aelion-cloud).

## Modules

| Module | Release asset | Platform |
|--------|---------------|----------|
| `plugins-common` | (library) | Shared helpers (Java 8) |
| `signs-bukkit-shared` | (library) | Signs logic (Java 8 / Spigot 1.8 API) |
| `signs-bukkit-1_8` | `aelion-signs-bukkit-1_8-<ver>.jar` | MC 1.8–1.12.2 |
| `signs-bukkit-1_13` | `aelion-signs-bukkit-1_13-<ver>.jar` | MC 1.13–1.16.5 |
| `signs-paper-1_17` | `aelion-signs-paper-1_17-<ver>.jar` | MC 1.17–1.20.6 |
| `signs-paper-1_21` | `aelion-signs-paper-1_21-<ver>.jar` | MC 1.21.x |
| `signs-paper-26` | `aelion-signs-paper-26-<ver>.jar` | Paper 26.x |
| `npcs-paper` | `aelion-npcs-<ver>.jar` | Paper 1.21.x (wip) |

## Requirements

- JDK **21** (and **25** for the Paper 26 band)
- Gradle Wrapper (included)
- GitHub Packages credentials for `aero-api` (see `.env.example`)

## Build

```bash
./gradlew build
```
