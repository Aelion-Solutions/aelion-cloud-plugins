# Aelion Cloud Plugins

First-party Paper/Spigot plugins that complement
[Aelion Cloud](https://github.com/Aelion-Solutions/aelion-cloud) but are **not**
Aero (panel control / `/ae` lives in
[aelion-aero](https://github.com/Aelion-Solutions/aelion-aero)).

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
| `npcs-paper` | `aelion-npcs-<ver>.jar` | Paper 1.21.x (scaffold) |

Band selection for Cloud: [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md).

## Requirements

- JDK **21** (and **25** for the Paper 26 band)
- Gradle Wrapper (included)
- GitHub Packages credentials for `aero-api` (see `.env.example`)

## Build

```bash
./gradlew build
```

## Release

Same flow as Aero: release-please on `main` → approve Release PR → tag + GitHub
Release → CI uploads NPCs + all Signs band JARs. See [docs/RELEASE.md](docs/RELEASE.md).

## Cloud delivery

The panel fetches these JARs from this repo’s GitHub Releases (same
`AERO_GITHUB_TOKEN` / Admin → Plugins path as Aero). Details:
[docs/CLOUD_INTEGRATION.md](docs/CLOUD_INTEGRATION.md).

## Runtime (Signs)

Signs **depends** on **Aelion Aero** on the same server. Fleet snapshots and
Connect use `AeroFleetService`. Install the matching Aero band for the host MC
version. See `signs-bukkit-shared` config and band READMEs under each module.
