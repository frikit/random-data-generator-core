# random-data-generator-core

[![tests + coverage](https://github.com/frikit/random-data-generator-core/actions/workflows/continuous-integration-workflow.yml/badge.svg)](https://github.com/frikit/random-data-generator-core/actions/workflows/continuous-integration-workflow.yml)
[![codecov](https://codecov.io/github/frikit/random-data-generator-core/graph/badge.svg)](https://codecov.io/github/frikit/random-data-generator-core)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

random-data-generator is a seeded, locale-aware random and fake-data generation toolkit for Java 17 and later. This
repository holds its core: the Java engine and API, the locale data, the seed and recipe contracts and the bill of
materials. The integrations live in their own repositories:

| Repository | What it adds |
|:---|:---|
| `random-data-generator-core` (this one) | Generators, object generation, schemas, provider hub, locale data |
| `random-data-generator-java` | JUnit, Jackson 3 and Spring Boot integrations |
| `random-data-generator-kotlin` | Kotlin DSL and Kotest extensions |
| `random-data-generator-scala` | Scala 3 API with ScalaTest and MUnit integrations |

random-data-generator continues kRandom (`io.github.frikit:krandom-*`), which is frozen at 4.1.0. The core starts
from the kRandom 4.1.0 code under new coordinates and a new package, `io.github.frikit.rdg`.

## Modules

| Module | Purpose |
|:---|:---|
| `bom` | Maven/Gradle bill of materials (`random-data-generator-bom`) for aligning the family's versions |
| `core` | The engine (`random-data-generator-core`): generators, object generation, schema DSL, provider hub |
| `benchmarks` | JMH and macro-profile workloads, including competitor comparisons; not published |
| `examples/` | Java consumer examples for Gradle and Maven that use `core` directly |
| `docs-site/` | Public documentation site source |
| `docs/` | Release, benchmark, contribution, migration and development documentation |

## Current status

- No version is released yet; the first release will be `1.0.0`. Until then the API and seeded output may change;
  current repository development line: `1.0.0-SNAPSHOT`.
- Every published module runs on Java 17 or later; building the repository needs JDK 21 or later.
- CI runs the tests on Java 17, Java 21 and Java 25, with separate GraalVM native-image and critical-path mutation
  jobs.
- Local quality checks are standardized through `./scripts/pre_commit_check.sh`.
- Published to Maven Central under `io.github.frikit`. See [`docs/release-runbook.md`](docs/release-runbook.md) for
  the release process.

## What the core covers

- Primitive and numeric generators
- Text, lorem-style content, and provider-token templates
- Date/time, timezone, and legacy `Calendar` values
- Network and internet generators
- Locale-aware user/address data plus random supported `Locale` values
- Finance and identifier formats
- Geohash and coordinate-adjacent location data
- File/system/version values
- Selection combinators such as `pick`, `shuffle`, `weighted`, `repeat`, and `unique`
- Reflection-based object graph generation
- Schema-like record generation with `Field` and `Schema`
- Extensible provider lookup with `ProviderHub`

## Quick usage

```java
import io.github.frikit.rdg.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String email = Generators.ofEmail().generate();
String label = Generators.ofProviderTemplate("{firstname}-##").generate();
```

## Choosing an API

- Use `RandomDataGenerator.seeded("checkout-fixtures", Locale.UK)` for seeded fixture values in tests: one object
  that returns plain values (`fake.firstName()`, `fake.birthday(25, 80)`, `fake.postalCode()`), with an independent
  stream per method.
- Use scalar generators like `Generators.ofInt(...)`, `Generators.ofEmail()`, or `Generators.ofCity()` when you need
  a few direct values.
- Use the domain namespaces — `Generators.person()`, `Generators.finance()`, `Generators.location()`,
  `Generators.network()`, `Generators.text()`, `Generators.commerce()`, `Generators.identifier()`,
  `Generators.datetime()` — when you want a discoverable, IDE-friendly entry point per domain (each also accepts a
  `GeneratorConfig`).
- Use `ObjectGenerator<T>` when you want an existing DTO / record populated with realistic defaults.
- Use `ObjectFaker<T>` when you need explicit fixture rules or typed nested overrides, and `ObjectFaker.strict()` to
  report root properties without a rule and unused, ambiguous or shadowed rules before generating. Use immutable
  `ObjectModel<T>` values for reusable, composable fixture definitions.
- Set `GeneratorConfig.Builder.generationReportListener(...)` to see how object generation produced each value, its
  seed source, recipe settings, provider, matched rule, constraints, safety policy and fallback decisions, without
  the value itself.
- Use `Field` + `Schema` when you want row-style payloads or formatted export output such as JSONL, JSON, CSV, XML,
  SQL, YAML, or TOML.

The public docs include a dedicated guide: [Choosing an API](docs-site/guides/choosing-an-api.md).

## Performance

Checked-in benchmark reports are stored in [`docs/benchmarks/`](docs/benchmarks/); the
[methodology](docs/benchmarks/METHODOLOGY.md) separates structural and semantic workloads and defines the
publication protocol and regression budgets. The current reports were measured on the kRandom 4.x engine that this
core starts from.

## Randomness model

Unseeded generators use the JDK's fast `Random` by default, which is appropriate for fixture and fake-data
generation. Use `GeneratorConfig.builder().seed(...)` when output must be reproducible,
`GeneratorConfig.builder().random(myRandom)` when a client owns the PRNG instance, `randomFactory(...)` for a fresh
custom source per generator, or `secureRandom()` when a consumer explicitly needs a `SecureRandom` source. These
source choices are mutually exclusive; `build()` rejects mixed configurations instead of applying call-order
precedence.

`Generator.filter(predicate)` is bounded by default and throws if no generated value matches after 10,000 attempts.
Use `filter(predicate, maxAttempts)` when a domain-specific predicate is intentionally rare.

## Concurrency and determinism

A `Generator` instance is **not thread-safe**: it holds a single mutable PRNG. Sharing one instance across threads
is memory-safe (the backing `Random` is synchronized) but interleaves the random sequence — which destroys
reproducibility — and serializes callers on the PRNG. The rule for multi-threaded services is **one generator
instance per thread**:

```java
// Each thread builds its own generator from a shared, immutable config.
GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).build();
Generator<String> emails = Generators.threadLocal(() -> Generators.ofEmail(config));
```

`Generators.threadLocal(Supplier)` gives each thread its own instance from the supplied factory. What *is* safe to
share:

- **`GeneratorConfig`** has immutable value fields and is safe to share when its caller-owned `Random`, factory,
  registry, and other extension objects are also used according to their own concurrency contracts. A configured
  caller-owned `Random` remains shared mutable state.
- **`ProviderHub`** registration is thread-safe; complete all registration before sharing the hub for lookups.
- **`ObjectGenerator` / `ObjectFaker` / `Schema`** are stateful per generation call and must be confined to one
  thread (one instance per thread).

**Determinism.** A fixed seed reproduces output only under **single-threaded** use of a given instance — concurrent
calls race on PRNG call order. For reproducible per-thread data, derive a distinct seed per thread (e.g.
`baseSeed ^ threadId`) and build one seeded config per thread. Object generation does not depend on the JVM's
unspecified reflection order: fields are populated in a name-sorted order. Seeded output is not guaranteed to be
identical across JDK builds, however: values drawn from JDK-supplied data — time-zone IDs from the JDK's tzdata
(`ZoneIdGenerator`, `TimezoneGenerator`, zoned date-times), ISO country lists, and locale display names and date
formats — follow the running JDK, so pin the JDK version when fixtures must match byte-for-byte across machines.

## Object semantic aliases

`ObjectGenerator` resolves common field names such as `firstName`, `email`, and `createdAt` through semantic
providers. Projects with their own vocabulary can extend that lookup with a `SemanticFieldRegistry`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectSemanticRegistry(SemanticFieldRegistry.defaults().toBuilder()
        .alias("email", "contactMail")
        .build())
    .build();
```

## Build and verify locally

Ensure `java -version` reports Java 21+ before running the local checks.

```bash
./gradlew clean build
./scripts/pre_commit_check.sh
./scripts/verify_examples_local.sh
```

`pre_commit_check.sh` runs formatting, Markdown, link, and documentation-fact checks, compilation, module
boundaries, release SBOM validation, Javadoc, tests, critical-path mutation testing, and the exact core coverage
gate, and fails fast when Java 21+ is not active. `--fast` reuses up-to-date test results and skips mutation testing
and SBOM validation while iterating.

`verify_examples_local.sh` publishes the `random-data-generator-*` artifacts of this repository to Maven local and
runs the consumer examples against that local snapshot. The Java + Gradle example also runs on Java 17 when Gradle
detects a JDK 17 (`./gradlew javaToolchains`); CI sets `RDG_REQUIRE_JAVA17=true`, which fails the run without one.

## Install

Module coordinates (group `io.github.frikit`):

| Artifact | Java module name |
|:---|:---|
| `io.github.frikit:random-data-generator-bom` | — (Maven/Gradle platform) |
| `io.github.frikit:random-data-generator-core` | `io.github.frikit.rdg` (explicit descriptor) |

The coordinates below are those of the coming first release; until it is on Maven Central, build from source or
publish locally with `./gradlew publishToMavenLocal -PreleaseVersion=<version>`.

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.frikit:random-data-generator-core:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.frikit:random-data-generator-core:1.0.0'
}
```

### Gradle with the BOM

```kotlin
dependencies {
    implementation(platform("io.github.frikit:random-data-generator-bom:1.0.0"))
    implementation("io.github.frikit:random-data-generator-core")
}
```

### Maven

```xml
<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>random-data-generator-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Examples

Consumer examples live in [`examples/`](examples/). They are test-based examples rather than runnable demo apps,
and they verify Maven-local consumption of the core:

- Java + Gradle
- Java + Maven

The Kotlin, Scala and integration-module examples live in the language repositories.

## Versioning

random-data-generator follows Semantic Versioning from its first release: only a major release removes or breaks
public API, and minor and patch releases keep seeded output the same unless a fix restores a documented contract.
[VERSIONING.md](VERSIONING.md) defines the public API, the deprecation rule, the determinism contract, and the
platform requirements; check the [changelog](CHANGELOG.md) before upgrading.

## Getting help

- [GitHub Issues](https://github.com/frikit/random-data-generator-core/issues) for bug reports and feature requests
- [FAQ](docs-site/faq.md) for common questions

## Docs

- Public docs source: [`docs-site/`](docs-site/)
- Maintained project docs: [`docs/`](docs/)
- Migration guides from other libraries: [`docs/migration/`](docs/migration/)
