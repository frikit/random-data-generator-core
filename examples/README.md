# random-data-generator-core examples

This folder contains test-based examples that consume `io.github.frikit:random-data-generator-core` directly,
including strong encapsulation from Java 21 named modules. The Kotlin, Scala and integration-module examples live in
the language repositories (`random-data-generator-java`, `-kotlin` and `-scala`).

Default example version: `1.0.0-SNAPSHOT`

Version strategy:

- `1.0.0-SNAPSHOT` is the repo-local default for Maven-local verification.
- CI injects the version under test instead of editing example build files.
- published-release checks should pass an explicit version with the existing property knobs rather than changing the embedded defaults.

For repo-local verification, publish the current workspace artifact to Maven local first:

```bash
./scripts/verify_examples_local.sh
```

That script:

- verifies Java 21+
- publishes the `random-data-generator-*` artifacts of this repository to Maven local
- runs the example test suites against the local artifact
- runs the Java + Gradle example again on Java 17, the oldest Java that random-data-generator runs on, when
  Gradle detects a JDK 17; it skips that run otherwise, unless `RDG_REQUIRE_JAVA17=true`, as CI sets
- verifies both successful qualified `opens` and actionable missing-`opens` behavior from named modules

To point the examples at a different artifact version, set `RDG_VERSION` or the build-tool-specific property:

- Gradle: `-PrdgVersion=<version>`
- Maven: `-Drdg.version=<version>`

## Matrix

Each example's README lists what its tests show.

- Java + Gradle: `examples/java-gradle` — the `Generators` factories, `ObjectModel`, scoped locale data,
  `ObjectFaker.strict()` fixture diagnostics, the generation report, and sealed interfaces
- Java + Maven: `examples/java-maven` — the `Generators` factories, `ObjectModel`,
  `ObjectFaker.strict()` fixture diagnostics, the generation report, and sealed interfaces
- Java 21 + Gradle named modules: `examples/java-jpms` — qualified `opens` for object generation, and the
  failure without it

## Run commands

```bash
# Verify every example against the locally published snapshot
./scripts/verify_examples_local.sh
```
