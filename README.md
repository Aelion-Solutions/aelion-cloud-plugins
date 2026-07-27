# Aelion Cloud Plugins

Paper plugins that complement [Aelion Cloud](https://github.com/Aelion-Solutions/aelion-cloud)
but are **not** Aero (panel control / `/ae` lives in
[aelion-aero](https://github.com/Aelion-Solutions/aelion-aero)).

## Status

Scaffold only. NPCs and Signs load and log their version; panel-driven features come later.

## Modules

| Module | Release asset | Platform |
|--------|---------------|----------|
| `plugins-common` | (library) | Shared version helpers |
| `npcs-paper` | `aelion-npcs-<version>.jar` | Paper 1.21.x |
| `signs-paper` | `aelion-signs-<version>.jar` | Paper 1.21.x |

Asset names must stay aligned with the panel catalog in
`aelion-cloud` (`backend/src/services/plugins/catalog.ts`).

## Requirements

- JDK **21**
- Gradle Wrapper (included)

## Build

```bash
./gradlew build
```

Plugin JARs:

- `npcs-paper/build/libs/aelion-npcs-<version>.jar`
- `signs-paper/build/libs/aelion-signs-<version>.jar`

## Release

Same flow as Aero: release-please on `main` → approve Release PR → tag + GitHub
Release → CI uploads both JARs. See [docs/RELEASE.md](docs/RELEASE.md).

## Cloud delivery

The panel fetches these JARs from this repo’s GitHub Releases (same
`AERO_GITHUB_TOKEN` / Admin → Plugins path as Aero). Details:
[docs/CLOUD_INTEGRATION.md](docs/CLOUD_INTEGRATION.md).

## Runtime (Signs)

Signs depends on **Aelion Aero**. Fleet snapshots and Connect use
`com.aelion.aero.api.AeroFleetService` registered by Aero Paper — see
`signs-paper/README.md`.

Compile against GitHub Packages `com.aelion.aero:aero-api` (pin `aeroApiVersion`).
Resolve with `GITHUB_TOKEN` / `read:packages`.
