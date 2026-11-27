# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- random-data-generator-core starts from the kRandom 4.1.0 core (`io.github.frikit:krandom-core`), whose history
  is in the [kRandom changelog](https://github.com/frikit/krandom/blob/main/CHANGELOG.md). It is published as
  `io.github.frikit:random-data-generator-core` and `io.github.frikit:random-data-generator-bom`, and its Java
  package and JPMS module are `io.github.frikit.rdg` instead of `io.github.frikit.krandom`.
- Renamed types: `Krandom` is `RandomDataGenerator`, and the extension types `KRandomModule`,
  `KRandomModuleContext` and `KRandomExtensionRegistry` are `ExtensionModule`, `ExtensionModuleContext` and
  `ExtensionRegistry`.
- Renamed contracts: the built-in data resources are under `rdg/` instead of `krandom/`, a local data pack's
  manifest is `rdg-data-pack.properties`, a recipe's format is `rdg-recipe` (still version `v2`), a schema export's
  safety extension is `x-rdg-safety`, and the system property that supplies a development build's library version
  is `rdg.version`. kRandom recipes, data packs and schema exports use the old names and need the new ones.
- A seed gives the same values as in kRandom 4.1.0: the seed derivation and stream keys are unchanged. One
  exception: object generation derives a member's stream from its owner's class name, so the members of the
  library's own records (`AddressInfo`, `PersonInfo`, `PostalLocality`, `CountryCodes` and the other `*Info`
  records) take other values when the object engine builds them. Members of your own classes keep their values.
- A kRandom recipe (format `krandom-recipe`) is rejected with a message that says how to convert it: a
  `krandom-recipe/v2` recipe replays once its first line reads `format=rdg-recipe`, and a v1 recipe needs the
  kRandom 3.x release it records. An `rdg-recipe` of any version but `v2` is rejected as unsupported.
- The Jackson, JUnit and Spring Boot modules move to `random-data-generator-java`, and the Kotlin DSL and Kotest
  modules to `random-data-generator-kotlin`.

[Unreleased]: https://github.com/frikit/random-data-generator-core/commits/main
