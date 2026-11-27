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
  is `rdg.version`. kRandom data packs and schema exports use the old names and need the new ones.
- A seed gives the same values as in kRandom 4.1.0: the seed derivation and stream keys are unchanged. One
  exception: object generation derives a member's stream from its owner's class name, so the members of the
  library's own records (`AddressInfo`, `PersonInfo`, `PostalLocality`, `CountryCodes` and the other `*Info`
  records) take other values when the object engine builds them. Members of your own classes keep their values.
- A kRandom recipe (format `krandom-recipe`, any version) is rejected with a message that says it replays only
  with the kRandom release its `library-version` line names; it is not converted. An `rdg-recipe` of any version
  but `v2` is rejected as unsupported.
- The Jackson, JUnit and Spring Boot modules move to `random-data-generator-java`, and the Kotlin DSL and Kotest
  modules to `random-data-generator-kotlin`.

### Removed

- **Breaking, before 1.0.0:** the four seeding settings that only replayed kRandom 3.x output. Their `true`
  default is now the only behaviour: convenience methods advance their child stream, Gaussian draws use
  `StrictMath`, object generation reseeds the built-in `@Randomizer` generators that a seed can reset, and the
  German locales write the street before the house number. Removed: `GeneratorConfig.Builder`
  `advancingConvenienceStreams`, `strictMathGaussian`, `objectReseedBuiltInRandomizers` and
  `germanStreetLayout`, and the getters `GeneratorConfig.isAdvancingConvenienceStreams`, `isStrictMathGaussian`,
  `isObjectReseedBuiltInRandomizers` and `isGermanStreetLayout`.
- **Breaking, before 1.0.0:** `ChildStreamPolicy`, `GeneratorConfig.Builder.childStreamPolicy` and
  `GeneratorConfig.getChildStreamPolicy`. `forChildStream` always derives a named child stream from a seeded
  configuration, as the `INDEPENDENT` default did.
- **Breaking, before 1.0.0:** the safety-policy label, which nothing read: `GeneratorConfig.Builder.safetyPolicy`,
  `GeneratorConfig.getSafetyPolicy`, `GenerationRecipe.Builder.safetyPolicy`, `GenerationRecipe.getSafetyPolicy`
  and `GenerationRecipe.LEGACY_UNCLASSIFIED_SAFETY_POLICY`. The typed safety policies stay.
- **Breaking, before 1.0.0:** the recipe lines `safety-policy`, `setting.child-stream-policy`,
  `setting.advancing-convenience-streams`, `setting.gaussian.strict-math`,
  `setting.object.reseed-built-in-randomizers` and `setting.address.german-street-layout`. A recipe is still
  `rdg-recipe/v2`; one that carries a `safety-policy` line is rejected when parsed, and one that carries any of the
  settings is rejected at replay with `Unsupported recipe setting`.
- The default seeded output is unchanged: a default configuration gives the values it gave before.

[Unreleased]: https://github.com/frikit/random-data-generator-core/commits/main
