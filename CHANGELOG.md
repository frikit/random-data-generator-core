# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- random-data-generator-core starts from the kRandom 4.1.0 core (`io.github.frikit:krandom-core`), whose history
  is in the [kRandom changelog](https://github.com/frikit/krandom/blob/main/CHANGELOG.md). It is published as
  `io.github.frikit:random-data-generator-core` and `io.github.frikit:random-data-generator-bom`, and its Java
  package and JPMS module are `io.github.frikit.rdg` instead of `io.github.frikit.krandom`.
- Renamed types: `Krandom` is `RandomDataGenerator`, and the extension types `KRandomModule` and
  `KRandomModuleContext` are `ExtensionModule` and `ExtensionModuleContext`. `KRandomExtensionRegistry` has no
  public counterpart (see `GeneratorConfig.getExtensionRegistry` under Removed).
- **Breaking, before 1.0.0:** `URLGenerator` is `UrlGenerator`, its option record `URLGenerator.URLOptions` is
  `UrlGenerator.UrlOptions`, and `UUIDGenerator` is `UuidGenerator`, in the camel case of their factories
  `Generators.ofUrl()` and `ofUuid()` and of the other generators, such as `UriGenerator` and `IbanGenerator`.
  The factories, provider keys and seeded values are unchanged, with one exception: a `UrlOptions` that object
  generation fills, as a generated object or a member of one, takes other seeded values, because the streams of
  its components follow its class name. The IP generators keep their names (`IPGenerator`, `ofIP()`).
- Renamed contracts: the built-in data resources are under `rdg/` instead of `krandom/`, a local data pack's
  manifest is `rdg-data-pack.properties`, a recipe's format is `rdg-recipe` (version `v3`, below), a schema export's
  safety extension is `x-rdg-safety`, and the system property that supplies a development build's library version
  is `rdg.version`. kRandom data packs and schema exports use the old names and need the new ones.
- A seed gives generators and `RandomDataGenerator` methods the same values as in kRandom 4.1.0: the seed
  derivation and stream keys are unchanged. Generated objects can take other values than in kRandom 4.1.0: object
  generation derives every member's stream and every semantic provider's seed by name (see `ObjectFieldStreamPolicy`
  under Removed), and a member's stream follows its owner's class name, which is in `io.github.frikit.rdg` for the
  library's own records (`AddressInfo`, `PersonInfo`, `PostalLocality`, `CountryCodes` and the other `*Info`
  records).
- A kRandom recipe (format `krandom-recipe`, any version) is rejected with a message that says it replays only
  with the kRandom release its `library-version` line names; it is not converted. An `rdg-recipe` of any version
  but `v3`, `v2` included, is rejected as unsupported.
- The Jackson, JUnit and Spring Boot modules move to `random-data-generator-java`, and the Kotlin DSL and Kotest
  modules to `random-data-generator-kotlin`.
- **Breaking, before 1.0.0, changes generated objects:** with a caller-owned `Random`
  (`GeneratorConfig.Builder.random`) or a `randomFactory` and no seed, object generation reseeds a `@Randomizer`
  generator that a seed would reseed (a `Seedable` one, or a built-in such as `UuidGenerator`, `FirstNameGenerator`
  or `EmailGenerator`) for every object, with a seed drawn from the sequence the object's other members draw from,
  just before the generator first gives the member's value. Such a member kept the unseeded source of its own
  constructor and took other values on every launch, while the other members repeated; now
  `random(new Random(42))` repeats them too. That draw shifts the later values of the shared sequence: the members
  generated after it and every later object take other values than before, in such a configuration only. A
  `@Randomizer` that cannot be reseeded, or that is not used, as on an `Optional` member, draws nothing, and seeded,
  unseeded and `secureRandom()` configurations keep their values.
- **Breaking, before 1.0.0, changes generated objects:** a `@Randomizer` on a field that a constructor argument
  fills now gives that argument, where the class is compiled with `-parameters` or a construction adapter names
  the parameter, and the field is the one the parameter initializes (the nearest field with its name and declared
  type, as for Bean Validation). It was
  ignored, and the argument was generated as an unannotated one. Only objects with such a field change: with a seed
  their other members keep their values, and with a caller-owned `Random` the later values of the sequence shift.

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
  derives a stream for each member, which is slower than one shared sequence; the last measurement is the
  [2.3.0 benchmark record](docs/benchmarks/2.3.0/README.md). The generation report names such a member's seed
  source `DERIVED_STREAM` where it said `EXPLICIT_SEED`.
- **Breaking, before 1.0.0:** public types and members that only the library itself used. The built-in national
  ID providers are package-private, except `UsNationalIdProvider`, `EsNationalIdProvider` and
  `BrNationalIdProvider`, which keep their public format options; for any other locale,
  `DataRegistryContext.globalDefault().provider(Vocabulary.NATIONAL_ID, locale)` returns the built-in provider.
  `finance.CurrencyDetails`, which no generator returned, is removed, and `user.LocaleTextResourceLoader` moves to
  the unexported `io.github.frikit.rdg.internal` package. Package-private: `GeneratorConfig.isRestoredFromRecipe`
  (a generation report's `SeedSource.RECIPE` says the same), `FixtureRuleKind.isRegisteredOnObjectFaker`, and the
  constructors,
  the `random` field and the `validate` method of `AbstractBoundedGenerator`, which only the numeric generators in
  `io.github.frikit.rdg.generator.base` extend. `DataRegistryContext.Builder`, `CharGenerator.Builder` and
  `StringGenerator.Builder` have private constructors (their `builder()` methods create them), and the nested
  `Builder` of the package-private `ObjectGeneratorConfig` is no longer declared public. Seeded output is unchanged.
- **Breaking, before 1.0.0:** the static `isRegistered(Locale)` of the 41 built-in registries (the 40
  `*DataRegistry` classes and `NationalIdRegistry`). The registries keep `forLocale` and `registeredKeys`, and
  `DataRegistryContext.globalDefault().isRegistered(Vocabulary.X, locale)` gives the same answer.
- **Breaking, before 1.0.0:** `GenerationRecipe.builder()` and the whole `GenerationRecipe.Builder` are
  package-private. A recipe comes from `GeneratorConfig.getGenerationRecipe()` or from its text with
  `GenerationRecipe.parse`, and `toGeneratorConfig()` replays either. Seeded output is unchanged.
- **Breaking, before 1.0.0:** the object engine's lookups on `GeneratorConfig`, which take a type, a field or a
  name and apply the engine's rules: `getObjectTypeOverride`, `getObjectFieldOverride`,
  `getObjectContextualTypeOverride`, `getObjectContextualFieldOverride`, `getObjectFieldPredicateOverride`,
  `getObjectContextualFieldPredicateOverride`, `shouldObjectExclude` and `isObjectFieldNameExcluded` are
  package-private. The `GeneratorConfig.Builder` methods that set the overrides and exclusions stay, and object
  generation applies them as before. Seeded output is unchanged.
- **Breaking, before 1.0.0:** `GeneratorConfig.getExtensionRegistry()` is package-private, and the type it
  returned, `extension.ExtensionRegistry` (kRandom's `KRandomExtensionRegistry`), moves to the unexported
  `io.github.frikit.rdg.internal` package. Modules are still installed with `GeneratorConfig.Builder.install` and
  resolved, with the same collision checks, when the configuration is built. Seeded output is unchanged.
- **Breaking, before 1.0.0:** the `get…()` methods that drew a new random value on every call, as if they were
  properties. Removed, use the `generate…()` method that gave the same value: `DomainGenerator.getTLD`
  (`generateTld`), `CreditCardGenerator.getCvv` (`generateCvv`) and `getExpirationDate` (`generateExpiration`),
  `CurrencyGenerator.getName`, `getSymbol` and `getNumericCode`, with or without a `Locale`
  (`generateCurrencyName`, `generateCurrencySymbol` and `generateCurrencyNumericCode`, with the same parameters),
  `EmailGenerator.getFreeEmailProvider` (`generateFreeEmailDomain`), and `UrlGenerator.getProtocol`
  (`generateProtocol`) and `getQueryString` (`generateQuery`). Renamed, as they had no such twin:
  `DomainGenerator.getPopularTLD` is `generatePopularTld`, `CardExpirationGenerator.getMonth` and `getYear`, with
  all their overloads, are `generateMonth` and `generateYear` with the same parameters, and `UrlGenerator.getPath` is
  `generatePath`, and `MbtiGenerator.withNickname`, which drew a value but read like a configuring method, is
  `generateWithNickname`. `DomainGenerator.getLocaleTLD`, which returns the locale's TLD without drawing, is
  `getLocaleTld`. Every remaining method draws as before, so seeded output is unchanged.

[Unreleased]: https://github.com/frikit/random-data-generator-core/commits/main
