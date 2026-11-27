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
  manifest is `rdg-data-pack.properties`, a recipe's format is `rdg-recipe` (version `v3`, below), a schema export's
  safety extension is `x-rdg-safety`, and the system property that supplies a development build's library version
  is `rdg.version`. kRandom data packs and schema exports use the old names and need the new ones.
- A seed gives generators and `RandomDataGenerator` methods the same values as in kRandom 4.1.0: the seed
  derivation and stream keys are unchanged. Generated objects do not keep their kRandom 4.1.0 values: object
  generation derives every member's stream and every semantic provider's seed by name (see `ObjectFieldStreamPolicy`
  under Removed), and a member's stream follows its owner's class name, which is in `io.github.frikit.rdg` for the
  library's own records (`AddressInfo`, `PersonInfo`, `PostalLocality`, `CountryCodes` and the other `*Info`
  records).
- A kRandom recipe (format `krandom-recipe`, any version) is rejected with a message that says it replays only
  with the kRandom release its `library-version` line names; it is not converted. An `rdg-recipe` of any version
  but `v3`, `v2` included, is rejected as unsupported.
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
  `setting.object.reseed-built-in-randomizers` and `setting.address.german-street-layout`. A recipe that carries
  a `safety-policy` line is rejected when parsed, and one that carries any of the settings is rejected at replay
  with `Unsupported recipe setting`. Removing these settings, `ChildStreamPolicy` and the label leaves the default
  seeded output as it was.
- **Breaking, before 1.0.0, changes seeded objects:** `ObjectFieldStreamPolicy`,
  `GeneratorConfig.Builder.objectFieldStreamPolicy`, `GeneratorConfig.getObjectFieldStreamPolicy` and the recipe
  line `setting.object.field-stream-policy`. Every seeded object generation now does what `INDEPENDENT` did: each
  member draws from its own stream, derived by name from the seed, even when the configuration has custom rules,
  exclusions or modules, and the seed of each provider-backed semantic generator, such as the one for `email`, is
  derived from its key instead of drawn in registry order. So a module that registers a semantic provider no longer
  shifts the other members, and an `ObjectFaker` root `ruleFor` no longer changes the other seeded members. Seeded
  generated objects take other values: in the default `RELAXED` and in `STRICT` semantic mode for every seeded
  configuration, and in `STRUCTURAL_ONLY` mode for one without a portable recipe. Generators, `RandomDataGenerator`
  methods and schemas keep their values. An unseeded configuration, or one with a caller-owned `Random`, still
  draws its members from one shared sequence. Since the derivation changes, a recipe is now `rdg-recipe/v3`; a `v2`
  recipe is rejected as unsupported, and one that carries `setting.object.field-stream-policy` is rejected at
  replay with `Unsupported recipe setting`. A seeded configuration with custom rules, exclusions or modules now
  derives a stream for each member, which is slower than one shared sequence: the last measurement, on kRandom
  2.3.0, gave about a third of the throughput for such a fixture.

[Unreleased]: https://github.com/frikit/random-data-generator-core/commits/main
