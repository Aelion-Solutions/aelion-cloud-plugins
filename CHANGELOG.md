# Changelog

## [0.3.1](https://github.com/Aelion-Solutions/aelion-cloud-plugins/compare/v0.3.0...v0.3.1) (2026-07-28)


### Bug Fixes

* **signs:** assign walls without joinable-only gate ([d58dbfe](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/d58dbfe2644f5c99355ddc162373ed376dba68c0))
* **signs:** idle layout skip claimed members ([877a770](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/877a770b4a6b074b82ce5c9c7167779223870bf1))
* **signs:** wall assignment + member visibility filters ([d7a275b](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/d7a275be47d28c51de5858cd776780d3e9855ad3))

## [0.3.0](https://github.com/Aelion-Solutions/aelion-cloud-plugins/compare/v0.2.0...v0.3.0) (2026-07-27)


### Features

* **release:** attach signs-compat.yml to GitHub Releases ([5f78dc2](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/5f78dc2510a71c0532217aad36c2777df5551421))
* **release:** attach signs-compat.yml to GitHub Releases ([b61193a](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/b61193a5099dca4a5327fcc26f174d6c5f938d6d))
* **signs:** ship MC version bands 1.8-26 ([e3121a4](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/e3121a4ac1c080b98dd8c9b971e67be1e6bf2e37))
* **signs:** ship MC version bands 1.8-26 ([760b1b0](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/760b1b09d2b4f733ee0b2e6eeee6e3b27f2d4162))


### Bug Fixes

* **signs:** don't advance per-sign animation tick outside animation task ([a927169](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/a9271698fd780b2135fb24a2ec04c79c159886ea))
* **signs:** only advance per-sign tick when a frame is actually drawn ([ff8ea70](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/ff8ea70ab044d1b3c093af5d00743f2d5f5836b4))

## [0.2.0](https://github.com/Aelion-Solutions/aelion-cloud-plugins/compare/v0.1.0...v0.2.0) (2026-07-27)


### Features

* resolve aero-api from GitHub Packages ([4073044](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/4073044a146a179821d47d2e32e0a848688575d5))
* scaffold cloud plugins monorepo with Signs wall ([365626a](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/365626af422a681110987b360669656686a176fa))


### Bug Fixes

* **build:** pin aero-api to published 0.3.0 ([0de1cd9](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/0de1cd9d730a1d8335af6a8c3f59e8b3a5a19315))
* **ci:** auth GitHub Packages for aero-api resolve ([06648ed](https://github.com/Aelion-Solutions/aelion-cloud-plugins/commit/06648edae8c62c4df8c9582a006e1b1ebbe0a7ae))

## [0.1.0] (unreleased)

### Features

- Scaffold Gradle multi-module repo (`plugins-common`, `npcs-paper`, `signs-paper`)
- Release-please + CI uploading `aelion-npcs-*.jar` and `aelion-signs-*.jar`
