---
layout: page
title: FAQ
permalink: /faq/
---

# FAQ

## Which module should I use?

Use `random-data-generator-core` for generation APIs. Add `random-data-generator-jackson` for Jackson 3 serialization support,
`random-data-generator-junit` for the JUnit seed extension, `random-data-generator-spring-boot-starter` for Spring Boot,
`random-data-generator-kotest-extensions` for Kotest adapters, or `random-data-generator-kotlin-dsl` for Kotlin fixture rules.
Import `random-data-generator-bom:1.0.0` to align multiple random-data-generator modules.

## How do I keep generated data stable between test runs?

Build and reuse a seeded configuration, for example
`GeneratorConfig.builder().seed(42L).build()`.

## How do I apply locale consistently?

Build one `GeneratorConfig` with `locale(...)` and pass it to all locale-aware generators in the fixture pipeline.

## How do I generate structured records?

Use `Field` to bind provider names and `Schema` to generate single records or batches.

## How do I extend providers dynamically?

Use an explicitly installed, configuration-scoped `ExtensionModule` when the same contribution must
work through provider lookup, schemas, safety metadata, and semantic object fields. Use
`ProviderHub.register(...)` only for a short-lived hub-local provider.

## Can Kotlin or Scala consume the library?

Yes. Kotlin and Scala can use the Java `random-data-generator-core` artifact directly. Kotlin projects can also use `random-data-generator-kotlin-dsl` for typed fixture rules, while Kotest projects can use `random-data-generator-kotest-extensions` for property-testing adapters. See [Kotlin DSL]({{ '/guides/kotlin-dsl/' | relative_url }}) and [Property Testing Integrations]({{ '/guides/property-testing-integrations/' | relative_url }}).
