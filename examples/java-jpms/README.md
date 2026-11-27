# Java JPMS consumer

This Gradle composite verifies Java 21 named-module behavior against the locally published
`random-data-generator-core` artifact, whose module is `io.github.frikit.rdg`.

- `open-consumer` proves safe constructor and mutable-field access with a qualified `opens` clause.
- `closed-consumer` proves that a missing `opens` clause fails with the exact actionable directive.

Run it through `../../scripts/verify_examples_local.sh`, or after publishing the requested version to Maven
local:

```bash
../../gradlew -PrdgVersion=1.0.0-SNAPSHOT verifyJpms
```

The named-module consumers of the Jackson and JUnit integrations live in `random-data-generator-java`.
