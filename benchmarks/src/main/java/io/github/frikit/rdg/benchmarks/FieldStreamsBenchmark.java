/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.benchmarks;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.object.ObjectFieldStreamPolicy;
import io.github.frikit.rdg.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.rdg.generator.object.ObjectGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/**
 * Compare legacy output and opt-in field streams on the same workload, both structurally and with
 * the default {@code RELAXED} field-name semantics that ordinary callers get.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class FieldStreamsBenchmark {
    @Param({"LEGACY", "INDEPENDENT"})
    public String policy;

    @Param({"false", "true"})
    public boolean customized;
    /** {@code STRUCTURAL_ONLY} isolates stream planning; {@code RELAXED} is the default mode. */
    @Param({"STRUCTURAL_ONLY", "RELAXED"})
    public String semanticMode;

    private ObjectGenerator<Fixture> generator;

    @Setup
    public void setup() {
        GeneratorConfig.Builder config = GeneratorConfig.builder()
                .seed(42)
                .clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
                .objectSemanticMode(ObjectGenerationSemanticMode.valueOf(semanticMode))
                .objectFieldStreamPolicy(ObjectFieldStreamPolicy.valueOf(policy));
        if (customized) config.objectOverride(Fixture.class, "name", () -> "fixed");
        generator = new ObjectGenerator<>(Fixture.class, config.build());
    }

    @Benchmark
    public Fixture generate() {
        return generator.generate();
    }

    public record Fixture(String name, int age, List<Integer> values) {}
}
