# Signs compatibility matrix

Source of truth: [`compat/signs-compat.yml`](../compat/signs-compat.yml).

Signs ships **one JAR per platform version band**, matching [Aelion Aero](https://github.com/Aelion-Solutions/aelion-aero)’s band model.
Product version (e.g. `0.1.0`) is independent of the Minecraft API band.

## Backend bands

| Artifact | MC range | Compile API | JVM bytecode |
|----------|----------|-------------|--------------|
| `aelion-signs-bukkit-1_8` | 1.8 – 1.12.2 | Spigot 1.8.8 | Java 8 |
| `aelion-signs-bukkit-1_13` | 1.13 – 1.16.5 | Spigot 1.13.2 | Java 8 |
| `aelion-signs-paper-1_17` | 1.17 – 1.20.x | Paper 1.17.1 | Java 16 |
| `aelion-signs-paper-1_21` | 1.21.x | Paper 1.21.x | Java 21 |
| `aelion-signs-paper-26` | 26.1 – 26.x | Paper 26.x | Java 25 |

All bands include:

- Layout frames + animation (`loop` / `pingpong`)
- Forcefield (formerly knockback)
- Fleet assignment via `AeroFleetService`
- `/aesign` admin commands

**Deprecated:** the old release asset name `aelion-signs-<version>.jar` is replaced by banded names above (e.g. `aelion-signs-paper-1_21-<version>.jar`).

## Cloud selection

On provision / plugin install the panel:

1. Fetches the GitHub Release for the Signs product version (latest, or pin when per-package pins exist).
2. Loads that release’s `signs-compat.yml` (release asset, or Contents API fallback at the tag).
3. Maps instance `software` to a family (`paper`, `spigot`, …) and parses Minecraft version.
4. Finds the **unique** matching row in that version’s matrix.
5. Installs the matching GitHub Release asset by filename prefix.
6. If no row matches → fail with a clear unsupported error (do not guess).

Proxies are not supported (`COMPAT_SIGNS_PROXY`).

## Future plugins (NPCs et al.)

When adding another first-party plugin:

1. Shared library module + thin band modules (same cliffs as Aero/Signs).
2. `compat/<product>-compat.yml` with `schemaVersion: 1` backends rows.
3. Publish the YAML as a release asset; wire cloud catalog `compat` metadata + matrix resolver.
4. Release assets named `<artifact>-<productVersion>.jar`.

NPCs remains a single Paper 1.21 stub until it leaves scaffold.
