/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ObjectFieldStreamPlannerTest {

    @Test
    void usesNamedStreamsForEverySeededSession() {
        assertTrue(ObjectFieldStreamPlanner.usesNamedChildStreams(42L));
        assertTrue(ObjectFieldStreamPlanner.usesNamedChildStreams(0L));
    }

    @Test
    void keepsTheSharedSequenceWithoutAGenerationSeed() {
        assertFalse(ObjectFieldStreamPlanner.usesNamedChildStreams(null));
    }
}
