# AGENTS.md

## Cursor Cloud specific instructions

Gradle (Kotlin DSL) monorepo of Minecraft **Paper 1.21.x** server plugins, **Java 21**.
There is no web app, service, or database — plugins are JARs loaded into a Paper server.
Modules: `plugins-common` (shared lib + the only unit tests), `npcs-paper` (scaffold,
builds standalone), `signs-paper` (needs the private `aero-api` dependency — see below).

### Build / test / lint
- `gradlew` is not executable in this environment (repo files are read-only), so always
  invoke the wrapper via `bash ./gradlew ...` (matches `.github/workflows/ci.yml`).
- Full build + tests (README): `bash ./gradlew build --no-daemon`.
- There is no separate linter; `check`/`test` (JUnit 5 in `plugins-common`) is the gate.
- Buildable without any token: `bash ./gradlew --no-daemon :plugins-common:build :npcs-paper:build`.

### signs-paper needs GitHub Packages access (non-obvious)
- `signs-paper` compiles `compileOnly` against `com.aelion.aero:aero-api` (version pinned by
  `aeroApiVersion` in `gradle.properties`), served from the **private** GitHub Packages repo
  `Aelion-Solutions/aelion-aero`. The default cloud `gh` token has no access → resolving it
  returns HTTP 401 and `:signs-paper:compileJava` fails.
- To build it, provide a classic PAT with `read:packages` for `Aelion-Solutions` and run:
  `GITHUB_TOKEN=<PAT> GITHUB_ACTOR=<github-user> bash ./gradlew build --no-daemon`
  (`settings.gradle.kts` reads env `GITHUB_TOKEN`/`GITHUB_ACTOR`, or `gpr.key`/`gpr.user`).
  Without it, build only the other modules as shown above.

### Running a plugin (how to "run" this project)
- Build the JAR, then drop it into a Paper 1.21.x server's `plugins/` and start the server;
  the plugin logs its version on enable (e.g. `Aelion NPCs enabled (v0.2.0)`).
- Get a Paper server via the current API (the old v2 API is sunset):
  `https://fill.papermc.io/v3/projects/paper/versions/1.21.11/builds/latest`
  (`downloads.server:default.url`). Set `eula=true`, `online-mode=false`, run
  `java -jar paper.jar --nogui`. First start downloads vanilla libraries (needs network).
- `signs-paper` has a hard `depend: [AelionAero]` in its `plugin.yml`, so it will not enable
  without the AelionAero plugin present; full Signs runtime E2E also needs the Aelion Cloud
  panel + a proxy, which are external and not available in this repo.
