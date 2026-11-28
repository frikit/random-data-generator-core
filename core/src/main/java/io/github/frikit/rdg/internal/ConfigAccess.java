/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.internal;

import io.github.frikit.rdg.generator.GeneratorConfig;
import java.util.Objects;

/**
 * What one random-data-generator package may ask of {@link GeneratorConfig} that its public API does not offer.
 *
 * <p>This package is not exported from the {@code io.github.frikit.rdg} module, so it is not public API:
 * VERSIONING.md lets it change in any release. {@link GeneratorConfig} installs the implementation when its
 * class is initialized, which has happened by the time anything holds a configuration to pass here.
 */
public final class ConfigAccess {

    /** The reads and the copy that {@link GeneratorConfig} gives the library's other packages. */
    public interface Access {

        /**
         * The configuration that {@code config.toBuilder().seed(seed).build()} makes.
         *
         * @param config configuration to copy
         * @param seed   numeric seed of the copy
         * @return the copy
         */
        GeneratorConfig withSeed(GeneratorConfig config, long seed);

        /**
         * Whether the configuration replays a recipe and keeps the recipe's seed.
         *
         * @param config configuration to read
         * @return {@code true} for a configuration that replays a recipe
         */
        boolean isRestoredFromRecipe(GeneratorConfig config);
    }

    private static volatile @org.jspecify.annotations.Nullable Access access;

    private ConfigAccess() {}

    /**
     * Installs the implementation; {@link GeneratorConfig} calls this once, from its static initializer.
     *
     * @param implementation the implementation
     * @throws IllegalStateException if an implementation is installed already
     */
    public static synchronized void install(Access implementation) {
        Objects.requireNonNull(implementation, "implementation must not be null");
        if (access != null) {
            throw new IllegalStateException("ConfigAccess is installed already");
        }
        access = implementation;
    }

    /**
     * The configuration that {@code config.toBuilder().seed(seed).build()} makes: for a configuration without
     * modules, a field-by-field copy, the one {@link GeneratorConfig#forChildStream(String)} makes for a child
     * stream, without the builder's two copies of every collection.
     *
     * @param config configuration to copy
     * @param seed   numeric seed of the copy
     * @return the copy
     */
    public static GeneratorConfig withSeed(GeneratorConfig config, long seed) {
        return installed().withSeed(config, seed);
    }

    /**
     * Whether the configuration was restored from a replay recipe with
     * {@link io.github.frikit.rdg.generator.GenerationRecipe#toGeneratorConfig()} and keeps the recipe's seed.
     * A new seed set through {@link GeneratorConfig#toBuilder()} makes it an ordinary seeded configuration.
     *
     * @param config configuration to read
     * @return {@code true} for a configuration that replays a recipe
     */
    public static boolean isRestoredFromRecipe(GeneratorConfig config) {
        return installed().isRestoredFromRecipe(config);
    }

    private static Access installed() {
        return Objects.requireNonNull(access, "GeneratorConfig is not initialized");
    }
}
