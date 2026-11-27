---
layout: page
title: Property Testing Integrations
permalink: /guides/property-testing-integrations/
---

# Property Testing Integrations

Use the property-testing module when random-data-generator generators should feed Kotest test data.

## Kotest

Dependency:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.frikit:random-data-generator-kotest-extensions:1.0.0")
}
```

The module brings `kotest-property` transitively at the Kotest version it is built and tested with;
keep your other Kotest modules on that same version.

Usage:

```kotlin
import io.github.frikit.rdg.generator.GeneratorConfig
import io.github.frikit.rdg.generator.user.EmailGenerator
import io.github.frikit.rdg.kotest.rdgArb
import io.kotest.property.checkAll

val emailArb = rdgArb(GeneratorConfig.defaults()) { config ->
    EmailGenerator(config)
}

checkAll(emailArb) { email ->
    require(email.contains("@"))
}
```

`rdgArb(config) { ... }` creates a fresh generator for every Kotest random-source draw. A
failing Kotest seed therefore reproduces the same fixture sequence without sharing mutable
random-data-generator generator state between cases. The legacy `Generator.toArb()`, no-argument
`rdgArb { ... }`, and `rdgObjectArb` bridges are removed in 2.0.0: they reused one mutable
generator and could not support host-controlled replay or parallel property tests.

For object generation:

```kotlin
import io.github.frikit.rdg.generator.GeneratorConfig
import io.github.frikit.rdg.kotest.rdgReplayObjectArb

val userArb = rdgReplayObjectArb<UserDto>(
    GeneratorConfig.builder().seed(42L).build()
)
```

The configuration's seed parents every sample seed (2.6+): for the same Kotest seed, `seed(42L)`
and `seed(43L)` produce different, individually reproducible streams, and an unseeded
configuration behaves like `seed(0L)`. Kotest's seed still selects the samples, so replay needs
both the Kotest seed and the configuration (or its recipe, which records the seed).

## Shrinking for bounded primitives and selections

Bounded primitives and list selections have shrinking-aware adapters:

```kotlin
import io.github.frikit.rdg.kotest.rdgIntArb
import io.github.frikit.rdg.kotest.rdgPickArb

checkAll(rdgIntArb(0, 1000)) { value ->
    require(value < 1000)
}

checkAll(rdgPickArb(listOf("basic", "premium", "enterprise"))) { plan ->
    require(plan.isNotEmpty())
}
```

- `rdgIntArb(min, max)`, `rdgLongArb(min, max)`, and `rdgDoubleArb(min, max)` generate
  random-data-generator's half-open `[min, max)` range, expose the attainable bounds (plus `-1`, `0`, and `1`
  when inside the range) as Kotest edge cases, and shrink only to in-range values.
- `rdgPickArb(source)` picks one element, uses the first element as the edge case, and shrinks
  toward elements earlier in `source`.

Kotest's `RandomSource` owns determinism for these adapters: replaying a failing Kotest seed
reproduces the same values, and a `GeneratorConfig` seed is deliberately not consulted.

## Types that cannot be structurally shrunk

Object fixtures (`rdgReplayObjectArb`), semantic values such as emails, names, addresses, and
identifiers, and any `rdgArb` factory output have no structural shrinker: random-data-generator generates
them as opaque values, so there is no meaningful "smaller" fixture to propose. On failure these
adapters rely on Kotest seed replay instead of shrinking.

The module depends on `random-data-generator-core` transitively.

## Failure Recipes and Supported Versions

`checkAllWithRecipe(config, arb) { ... }` rethrows a failing property with the portable random-data-generator
recipe of the configuration appended below Kotest's own seed report, so a CI failure carries both
replay halves; `rdgKotestRecipe(config)` returns the same value-free recipe directly. That
recipe reads the configuration's clock when the failure is reported, and the appended header says
so; use the overload below for time-sensitive properties.

## Temporal replay with one snapshot (2.3+)

Let `checkAllWithRecipe` take the snapshot (2.6+): pass an Arb factory instead of an Arb. The
clock of `config` is captured once with `snapshotClock()`, the factory builds the Arb from that
snapshot, and the failure recipe records the same instant:

```kotlin
checkAllWithRecipe(GeneratorConfig.builder().seed(42L).build(), { session ->
    rdgArb(session) { sample -> Generator { DateGenerator(sample).future(7) } }
}) { date ->
    // Assert the application contract here.
}
```

An optional `PropTestConfig` argument pins Kotest's seed or iteration count on replay, for example
`checkAllWithRecipe(config, factory, PropTestConfig(seed = 1234L)) { ... }`.

With the Arb-based overload, build the configuration once with `snapshotClock()` before creating
the Arb, and pass the same configuration to `checkAllWithRecipe`:

```kotlin
val session = GeneratorConfig.defaults().snapshotClock()
val arb = rdgArb(session) { sample ->
    Generator { DateGenerator(sample).future(7) }
}
checkAllWithRecipe(session, arb) { date ->
    // Assert the application contract here.
}
```

The snapshot is shared across property samples. Kotest's seed controls sample randomness, while
the random-data-generator recipe records configuration including the captured clock. Snapshotting only in the
failure handler or passing a different configuration to the Arb cannot reproduce temporal data.
