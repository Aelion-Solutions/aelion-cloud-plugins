# Aelion Cloud Plugins

First-party Paper/Spigot plugins that complement [Aelion Cloud](https://github.com/Aelion-Solutions/aelion-cloud) and expand the servers with more Cloud features.

In normal cases you dont have to download these manually. This repo is for transparency.

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

## Build
Building is not documented, so this repo is for viewing the code to make sure its safe for you.
Releases build artifacts and packages automatically, if you want to build it yourself for whatever reason you should understand how.

## Cloud Installation - How it works

Aelion Cloud AEpi / Panel connects to this repo via HTTP/S and fetches the latest released compat yaml file to detewrmine what to download.
Based on server requirements it caches and installs the plugin(s) automatically.
