/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

/** Selects the random-stream model used for one object-generation session. */
final class ObjectFieldStreamPlanner {

    private ObjectFieldStreamPlanner() {}

    /**
     * Whether each member draws from its own stream, derived by name from the generation seed. Every seeded
     * session does, whatever rules, exclusions or modules the configuration has; an unseeded session draws
     * its members from one shared sequence, since it has no seed to derive from.
     */
    static boolean usesNamedChildStreams(@org.jspecify.annotations.Nullable Long generationSeed) {
        return generationSeed != null;
    }
}
