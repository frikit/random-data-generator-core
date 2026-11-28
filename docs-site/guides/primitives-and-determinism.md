---
layout: page
title: Primitives and Determinism
permalink: /guides/primitives-and-determinism/
---

# Primitives and Determinism

## Primitive generators

```java
int n = Generators.ofInt(10, 99).generate();
double p = Generators.ofDouble(0.0, 1.0).generate();
String token = Generators.ofString(
        StringGenerator.builder()
                .length(16)
                .charGenerator(CharGenerator.alphanumeric())
).generate();
```

## Reproducible sequences

Build generators from a seeded `GeneratorConfig`. Each generator created from the same seeded
configuration starts from the same random state:

```java
GeneratorConfig seeded = GeneratorConfig.builder().seed(123L).build();
IntGenerator a = Generators.ofInt(1, 100, seeded);
IntGenerator b = Generators.ofInt(1, 100, seeded);

assert a.generate() == b.generate();
assert a.generate() == b.generate();
```

Generator instances hold mutable random state. Treat them as per-test or per-thread objects, not shared
singletons. Share `GeneratorConfig` when you want common defaults, then create fresh generators from it
inside each test, fixture factory, or worker thread.

## Composition

`Generator.map(...)` transforms values lazily, and `Generator.filter(...)` retries until a generated value matches.
Filters are bounded by default, so an impossible predicate fails instead of hanging a test run. Raise or lower the cap
with `filter(predicate, maxAttempts)` when the predicate is deliberately rare:

```java
Generator<String> evenLabel = Generators.ofInt(0, 100)
        .filter(n -> n % 2 == 0)
        .map(n -> "even-" + n);

Generator<Integer> rare = Generators.ofInt(0, 1_000_000)
        .filter(n -> n == 42, 100_000);
```

## Selection helpers

```java
List<String> colors = List.of("red", "green", "blue");
String one = Generators.pick(colors).generate();
List<String> two = Generators.pickSet(colors, 2).generate();
List<String> shuffled = Generators.shuffle(colors).generate();
```

## Explicit replay snapshots (2.3+)

A seed does not freeze time. For temporal fixtures, capture the clock before generation and use
that same configuration for the recipe printed on failure:

```java
GeneratorConfig session = GeneratorConfig.builder().seed(42L).build().snapshotClock();
DateGenerator dates = new DateGenerator(session);
LocalDate fixture = dates.future(7);
String recipe = session.getGenerationRecipe().orElseThrow().serializeForDiagnostics();
```

`snapshotClock()` captures the configured instant and zone. The original configuration keeps its
live clock. Reuse the snapshot across the generation session; taking a new snapshot after failure
cannot recover an earlier clock. Custom callbacks and registries still do not yield a portable
recipe. Changes made to an injected builder must be included in the actual configuration you use
for generation and diagnostics.

## Object member streams

With a numeric or textual seed, each member of a generated object draws from its own stream, derived by
name from the seed, and so does each provider-backed semantic generator, such as the one for `email`. That
holds whatever the configuration installs: custom rules, exclusions and modules. An override of one field
then leaves unrelated fields on their streams, and a module that registers a semantic provider does not
shift the members that do not use it. Dependent rules and semantic relationships may intentionally change
related values, such as an email derived from a name; arbitrary callbacks remain responsible for their own
state. A configuration with rules, exclusions or modules still returns no portable recipe.

An unseeded configuration, or one with a caller-owned `Random`, has no seed to derive from, so its members
draw from one shared sequence.

An `ObjectFaker` root rule is such a customization: `ruleFor(path, generator)` on a root member installs a
field override, so the faker generates without a recipe, but the other seeded members keep their values.
`ObjectFaker.ruleForKeepingRecipe(path, generator)` installs the same rule and also keeps the
configuration's recipe. See
[Root rules that keep the recipe]({{ '/guides/object-generation/' | relative_url }}#root-rules-that-keep-the-recipe-32).

A seeded object costs more to generate than an unseeded one, because each member receives its own
resolver stream. Benchmark your own fixture before bulk-generation adoption.

### Composite child streams

Composite value generators such as `PaymentInfoGenerator`, `OrderInfoGenerator`, or
`PersonInfoGenerator` build child generators from their configuration. Each child receives
`config.forChildStream(name)`. With a numeric or textual seed, that is a copy whose seed is
`GenerationRecipe.deriveChildSeed(seed, name)` for a stable per-child stream name, so siblings draw
from independent streams and the seed still reproduces them: the payment, invoice, and order
identifiers of one seeded `PaymentInfo` are unrelated, such as `PAY-7DC1F9C37F29`,
`INV-F852387C7D39`, and `ORD-B9D4F25FCB19` for seed 42. Custom composites follow the same contract by
passing `config.forChildStream("childName")` to each child they construct.

Without a seed there is nothing to derive from, and `forChildStream` returns the configuration
unchanged. Each child then takes its own source from `createRandom()`: a fresh unseeded `Random`, or a
fresh `SecureRandom` with `secureRandom()`, so siblings still draw independent values. A caller-owned
`random(...)` instance is shared by every child, so the children draw their values in turn from that
one sequence. A `randomFactory(...)` is called once for each child, and its sources are used as they
are, so a factory that returns the same seeded source every time correlates the children again.
Neither is reseeded.

Separate generators built from one seeded configuration are not children of each other: each starts
from the root seed, so two `Generators.ofInt(1, 40, config)` instances return the same values, and
generators of different types draw from the same random sequence. Give each generator its own stream
by passing `config.forChildStream(name)`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed("checkout-fixtures")
    .build();

Generator<String> firstNames = Generators.ofFirstName(config.forChildStream("firstName"));
Generator<String> lastNames = Generators.ofLastName(config.forChildStream("lastName"));
```

`RandomDataGenerator` does this for you: each of its methods has a stream derived from the seed and the
method's name.

## Convenience streams, Gaussian draws and randomizers

- **Convenience methods advance their child stream.** `EmailGenerator.generateCompanyEmail()`, the
  `FullNameGenerator` methods with a middle name or initial, and `CurrencyGenerator.generatePriceTag`
  keep one child generator, so each call gives the next value: seed 42 gives
  `aharris@advancedassociatesco.test`, then `juliagarcia@advancedresourcesltd.test`, from
  `generateCompanyEmail()`. A reseed starts the child again.
- **Gaussian draws use `StrictMath`.** `NormalDistributionGenerator` draws with the `StrictMath` forms
  of `log`, `sin` and `cos`, which give the same bits everywhere, so a seed gives the same Gaussian
  values on every JVM and CPU. The `Math` forms, which Java lets differ in the last bit between
  platforms, are not used.
- **Object generation reseeds built-in randomizers.** Object generation reseeds a `Seedable`
  `@Randomizer` for every object from that object's stream. The built-in generators that are not
  `Seedable` but that a seeded `Schema` reseeds are reseeded the same way, so they replay from the seed
  and its recipe: `FullNameGenerator`, `FirstNameGenerator`, `LastNameGenerator`, `UsernameGenerator`,
  `EmailGenerator`, `PhoneNumberGenerator`, `StreetAddressGenerator`, `AddressInfoGenerator`,
  `CityGenerator`, `StateGenerator`, `PostalCodeGenerator`, `CountryGenerator`, `CurrencyGenerator`,
  `MoneyGenerator`, `UUIDGenerator`, `WordGenerator`, `SentenceGenerator` and `ParagraphGenerator`.
  Other built-ins that are not `Seedable`, such as `MiddleNameGenerator` or `CompanyEmailGenerator`,
  and your own generators that are not `Seedable`, keep their own source.

A recipe is `rdg-recipe/v3`. It records none of these, because none of them can be turned off. A
recipe that carries a setting this release does not read, such as `setting.child-stream-policy`, is
rejected at replay with `Unsupported recipe setting: child-stream-policy`, rather than replayed with
other values.
