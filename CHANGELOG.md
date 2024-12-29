# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.7] - 2024-12-29
### :bug: Bug Fixes
- [`05645b5`](https://github.com/SettingDust/lazyyyyy/commit/05645b534b5d0819daa5ab8b41c216ca8722664b) - **faster-mixin**: try to fix concurrent exception with lwjgl *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.6] - 2024-12-29
### :bug: Bug Fixes
- [`dc973ee`](https://github.com/SettingDust/lazyyyyy/commit/dc973ee1984f2874daf2c467544fab4db34bda9a) - **faster-mixin**: working with mixin booster or connector *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.5] - 2024-12-29
### :bug: Bug Fixes
- [`3e036b0`](https://github.com/SettingDust/lazyyyyy/commit/3e036b0206cbc966c6139195c954ed3f108e9804) - **faster-mixin**: work without connector *(commit by [@SettingDust](https://github.com/SettingDust))*

### :zap: Performance Improvements
- [`c8117c0`](https://github.com/SettingDust/lazyyyyy/commit/c8117c0078f7a255701323a6c41d056325ce7004) - **faster-mixin**: faster pack cache & disable modernfix resourcepack perf *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`0070b0b`](https://github.com/SettingDust/lazyyyyy/commit/0070b0b601a5f17b2f0c07755b4c5fda20c189f8) - **pack-resources-cache**: faster pack cache & disable modernfix resourcepack perf *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`69825fc`](https://github.com/SettingDust/lazyyyyy/commit/69825fca7fd4f090a293cc1395608ff383afd91e) - **deps**: bump org.gradle.toolchains.foojay-resolver-convention *(commit by [@dependabot[bot]](https://github.com/apps/dependabot))*


## [0.5.4] - 2024-12-04
### :sparkles: New Features
- [`f4cfecc`](https://github.com/SettingDust/lazyyyyy/commit/f4cfeccc388b1c07dbe66f4f0986f1ac7c6fcc50) - **lazy-entity-renderers**: compat with big brain *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`f79f4b1`](https://github.com/SettingDust/lazyyyyy/commit/f79f4b17079bb9e81590cf763872ce668d8c8230) - **lazy-entity-renderers**: missing remap on mixins *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`13000c6`](https://github.com/SettingDust/lazyyyyy/commit/13000c6654be32efdfc76bc4bf8197e3cda5ab66) - **async-model-baking**: assign location to the debug log *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`00a0d0e`](https://github.com/SettingDust/lazyyyyy/commit/00a0d0e2c743c2ba169bf16fe176c0709c79b694) - **pack-resources-cache**: remove directories without prefix *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.3] - 2024-12-03
### :bug: Bug Fixes
- [`49129d9`](https://github.com/SettingDust/lazyyyyy/commit/49129d99ff94e067e73e15ada17b39772fbed13c) - **lazy-entity-renderers**: the quark fix missin condition *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.2] - 2024-12-03
### :recycle: Refactors
- [`04219fe`](https://github.com/SettingDust/lazyyyyy/commit/04219fe5a5ac48751cd1b8aeaffbe556f4e5c0ea) - **pack-resources-cache**: move cache shared logic to new abstract class *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.1] - 2024-12-03
### :sparkles: New Features
- [`4d0a516`](https://github.com/SettingDust/lazyyyyy/commit/4d0a5166c5e981ca0f4165001f2a3dda2d48199c) - **pack-resources-cache**: add cache for vanilla pack resource *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`593472c`](https://github.com/SettingDust/lazyyyyy/commit/593472c07ea2cfb60c460e0a04cc2759f3623281) - runnable without ModernFix *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`25cedd1`](https://github.com/SettingDust/lazyyyyy/commit/25cedd150b38e682dccad08b787ce77ba44a76a5) - **pack-resources-cache**: respect the roots order for files *(commit by [@SettingDust](https://github.com/SettingDust))*

### :zap: Performance Improvements
- [`4c52270`](https://github.com/SettingDust/lazyyyyy/commit/4c522702547f29d27028257239a710f209b215e6) - **pack-resources-cache**: optimize the cache loader *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.5.0] - 2024-12-02
### :sparkles: New Features
- [`564811f`](https://github.com/SettingDust/lazyyyyy/commit/564811fd4ac9223a97fc9e7445912b1b39228eb6) - **pack-resources-cache**: add cache for file, folder and folder on forge *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`af5b176`](https://github.com/SettingDust/lazyyyyy/commit/af5b176f8635326edd9805bf302cb2e46a36cf09) - **lazy-entity-renderers**: don't add dummy renderer for normal entity *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`1347c9c`](https://github.com/SettingDust/lazyyyyy/commit/1347c9c2a9b96f2e7f04aba4aa34bd80d77306ef) - **yacl-lazy-animated-image**: load the image when render *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`d61f1c8`](https://github.com/SettingDust/lazyyyyy/commit/d61f1c83dc5150d37690fd074d80013bc494a7ab) - **pack-resources-cache**: actually working on Forge and Connector *(commit by [@SettingDust](https://github.com/SettingDust))*

### :zap: Performance Improvements
- [`fbbfef1`](https://github.com/SettingDust/lazyyyyy/commit/fbbfef1118b1b30890e9f6fe8c55c7f666cab6ce) - **pack-resources-cache**: better performance for cache loader *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.4.0] - 2024-11-29
### :sparkles: New Features
- [`df78ca3`](https://github.com/SettingDust/lazyyyyy/commit/df78ca36a2b5a00c23c8150969151ef0689116d0) - avoid duplicates in moremcmeta sprites queue *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`5fb5840`](https://github.com/SettingDust/lazyyyyy/commit/5fb58408f1f7b5d3975ed74fb7b2e9b17967dee3) - **toomanyplayers**: avoid requesting online whitelist in main thread *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`5c604dc`](https://github.com/SettingDust/lazyyyyy/commit/5c604dc2f1d5bf4ce92731987caa8b49f16ce89e) - **axiom**: async init the jackson classes *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`dcb3765`](https://github.com/SettingDust/lazyyyyy/commit/dcb37651d33d7d1ab0f3252841b2c23272b5afb3) - lazy entity renderers working *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`a446c19`](https://github.com/SettingDust/lazyyyyy/commit/a446c197ab0806ab79ed9dda59e75c2ae8ef0b6d) - add lazy block entity renderer *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`0296292`](https://github.com/SettingDust/lazyyyyy/commit/0296292ebd9428906c1c9fbf5435d119e14b49a2) - add lazy player entity renderer *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`a8375cb`](https://github.com/SettingDust/lazyyyyy/commit/a8375cb599419296e468255745c844e686c344cb) - async loading the renderers *(commit by [@SettingDust](https://github.com/SettingDust))*

### :bug: Bug Fixes
- [`0666658`](https://github.com/SettingDust/lazyyyyy/commit/066665800cc7b00e24e77bccd4ef43a4f0fdfc70) - **moremcmeta**: the mixin target is wrong *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`aff83f0`](https://github.com/SettingDust/lazyyyyy/commit/aff83f0b4dbd9aa2a086505d648858dec2638904) - post the player add layer event correctly *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.3.2] - 2024-11-27
### :bug: Bug Fixes
- [`5de07c2`](https://github.com/SettingDust/lazyyyyy/commit/5de07c20ffeb6475dbc71078baf944448ef9d186) - optional esf, etf *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.3.1] - 2024-11-24
### :bug: Bug Fixes
- [`2096d26`](https://github.com/SettingDust/lazyyyyy/commit/2096d26aa902de451a7b2e37a554845d2041365e) - **yacl**: avoid deadlock when loading *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.3.0] - 2024-11-24
### :sparkles: New Features
- [`fbd53aa`](https://github.com/SettingDust/lazyyyyy/commit/fbd53aae161dd5d016223e7faeb984ac5a4fba24) - create entity renderers in async *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`69dd578`](https://github.com/SettingDust/lazyyyyy/commit/69dd5783933bcd5773b29b4454fb067dc70fa67c) - add mixin config *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`3637366`](https://github.com/SettingDust/lazyyyyy/commit/36373667268672e8a9b4e51489ea13c20a118695) - faster `prism` color loader *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`d0669ca`](https://github.com/SettingDust/lazyyyyy/commit/d0669ca6848d0ae19207f594ce77c189db953b56) - async model baking *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`fb5b113`](https://github.com/SettingDust/lazyyyyy/commit/fb5b11378c475a1d8418a94f7628ba5cee926ba4) - **esf**: async sound event *(commit by [@SettingDust](https://github.com/SettingDust))*

### :wrench: Chores
- [`b28572c`](https://github.com/SettingDust/lazyyyyy/commit/b28572ca8ff3e275a90e6232d72a77fccd12f194) - add ignore *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.2.0] - 2024-11-21
### :sparkles: New Features
- [`fa606f1`](https://github.com/SettingDust/lazyyyyy/commit/fa606f1c97e09181ba9ad698499950132487224b) - the hack working *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`642ece8`](https://github.com/SettingDust/lazyyyyy/commit/642ece8c0b14e11791fcfdeb79806c8fc020fa5a) - the faster mixin is working *(commit by [@SettingDust](https://github.com/SettingDust))*

### :recycle: Refactors
- [`58ed1bb`](https://github.com/SettingDust/lazyyyyy/commit/58ed1bb0d3da892f37dd344e503dc8ad706e1771) - use architectury *(commit by [@SettingDust](https://github.com/SettingDust))*


## [0.1.0] - 2024-11-17
### :sparkles: New Features
- [`3d0ddf2`](https://github.com/SettingDust/lazyyyyy/commit/3d0ddf2adddb0faa57b99301b6d7b8e689a5cb3d) - lazyyyyy yacl image loading *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`03652f5`](https://github.com/SettingDust/lazyyyyy/commit/03652f5815961074a5af5eb00001ef4df43b4f31) - yacl image loading lazyyyyy more *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`34fc2da`](https://github.com/SettingDust/lazyyyyy/commit/34fc2da631863181c3c80e558abc630ca453044f) - yacl image loading lazyyyyy from path and gif *(commit by [@SettingDust](https://github.com/SettingDust))*
- [`9177763`](https://github.com/SettingDust/lazyyyyy/commit/91777634e88768e6df3b79258096c919a7aeaf06) - faster kiwi manifest finding *(commit by [@SettingDust](https://github.com/SettingDust))*

[0.1.0]: https://github.com/SettingDust/lazyyyyy/compare/0.0.0...0.1.0
[0.2.0]: https://github.com/SettingDust/lazyyyyy/compare/0.1.0...0.2.0
[0.3.0]: https://github.com/SettingDust/lazyyyyy/compare/0.2.0...0.3.0
[0.3.1]: https://github.com/SettingDust/lazyyyyy/compare/0.3.0...0.3.1
[0.3.2]: https://github.com/SettingDust/lazyyyyy/compare/0.3.1...0.3.2
[0.4.0]: https://github.com/SettingDust/lazyyyyy/compare/0.3.2...0.4.0
[0.5.0]: https://github.com/SettingDust/lazyyyyy/compare/0.4.1...0.5.0
[0.5.1]: https://github.com/SettingDust/lazyyyyy/compare/0.5.0...0.5.1
[0.5.2]: https://github.com/SettingDust/lazyyyyy/compare/0.5.1...0.5.2
[0.5.3]: https://github.com/SettingDust/lazyyyyy/compare/0.5.2...0.5.3
[0.5.4]: https://github.com/SettingDust/lazyyyyy/compare/0.5.3...0.5.4
[0.5.5]: https://github.com/SettingDust/lazyyyyy/compare/0.5.4...0.5.5
[0.5.6]: https://github.com/SettingDust/lazyyyyy/compare/0.5.5...0.5.6
[0.5.7]: https://github.com/SettingDust/lazyyyyy/compare/0.5.6...0.5.7
