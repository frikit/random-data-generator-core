/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.benchmarks;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Benchmark workloads")
class BenchmarkWorkloadTest {

    @Test
    @DisplayName("Instancio reuses one prebuilt model held in benchmark state")
    void instancioUsesPrebuiltModel() {
        CompetitorObjectBenchmark benchmark = new CompetitorObjectBenchmark();
        CompetitorObjectBenchmark.InstancioState state = new CompetitorObjectBenchmark.InstancioState();

        assertNotNull(state.model);
        Object model = state.model;
        BenchmarkFixtures.ComparableUser user = benchmark.instancioObject(state);

        assertNotNull(user.firstName);
        assertSame(model, state.model, "the model must be built once in @State, not per invocation");
    }
}
