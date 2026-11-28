/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.internal;

import io.github.frikit.rdg.generator.ContextualGenerator;
import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

/**
 * What one random-data-generator package may ask of {@link GeneratorConfig} that its public API does not offer.
 *
 * <p>This package is not exported from the {@code io.github.frikit.rdg} module, so it is not public API:
 * VERSIONING.md lets it change in any release. {@link GeneratorConfig} installs the implementation when its
 * class is initialized, which has happened by the time anything holds a configuration to pass here.
 */
public final class ConfigAccess {

    /**
     * The reads, the object-engine lookups and the copy that {@link GeneratorConfig} gives the library's other
     * packages.
     */
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

        /**
         * The root type override for object generation.
         *
         * @param config configuration to read
         * @param type   type to look up
         * @return the override, or empty
         */
        Optional<Generator<?>> getObjectTypeOverride(GeneratorConfig config, Class<?> type);

        /**
         * The root field override for object generation.
         *
         * @param config    configuration to read
         * @param ownerType type that declares the field
         * @param fieldName field name
         * @return the override, or empty
         */
        Optional<Generator<?>> getObjectFieldOverride(GeneratorConfig config, Class<?> ownerType, String fieldName);

        /**
         * The root contextual type override for object generation.
         *
         * @param config configuration to read
         * @param type   type to look up
         * @return the override, or empty
         */
        Optional<ContextualGenerator<?>> getObjectContextualTypeOverride(GeneratorConfig config, Class<?> type);

        /**
         * The root contextual field override for object generation.
         *
         * @param config    configuration to read
         * @param ownerType type that declares the field
         * @param fieldName field name
         * @return the override, or empty
         */
        Optional<ContextualGenerator<?>> getObjectContextualFieldOverride(
                GeneratorConfig config, Class<?> ownerType, String fieldName);

        /**
         * The first root predicate field override that matches the field.
         *
         * @param config configuration to read
         * @param field  field to match
         * @return the override, or empty
         */
        Optional<Generator<?>> getObjectFieldPredicateOverride(GeneratorConfig config, Field field);

        /**
         * The first root contextual predicate field override that matches the field.
         *
         * @param config configuration to read
         * @param field  field to match
         * @return the override, or empty
         */
        Optional<ContextualGenerator<?>> getObjectContextualFieldPredicateOverride(GeneratorConfig config, Field field);

        /**
         * Whether the root configuration excludes the field from object generation.
         *
         * @param config configuration to read
         * @param field  field to test
         * @return {@code true} if a field or type exclusion matches
         */
        boolean shouldObjectExclude(GeneratorConfig config, Field field);

        /**
         * Whether the root configuration excludes the member name from object generation.
         *
         * @param config configuration to read
         * @param name   member name
         * @return {@code true} if the name is excluded
         */
        boolean isObjectFieldNameExcluded(GeneratorConfig config, String name);
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

    /**
     * The type-level override that {@link GeneratorConfig.Builder#objectOverride(Class, Generator)} set on the
     * root configuration for object generation.
     *
     * @param config configuration to read
     * @param type   type to look up
     * @return the override, or empty
     */
    public static Optional<Generator<?>> getObjectTypeOverride(GeneratorConfig config, Class<?> type) {
        return installed().getObjectTypeOverride(config, type);
    }

    /**
     * The field-level override set on the root configuration for object generation, keyed by the owner's
     * fully-qualified class name and the field name.
     *
     * @param config    configuration to read
     * @param ownerType type that declares the field
     * @param fieldName field name
     * @return the override, or empty
     */
    public static Optional<Generator<?>> getObjectFieldOverride(
            GeneratorConfig config, Class<?> ownerType, String fieldName) {
        return installed().getObjectFieldOverride(config, ownerType, fieldName);
    }

    /**
     * The contextual type-level override set on the root configuration for object generation.
     *
     * @param config configuration to read
     * @param type   type to look up
     * @return the override, or empty
     */
    public static Optional<ContextualGenerator<?>> getObjectContextualTypeOverride(
            GeneratorConfig config, Class<?> type) {
        return installed().getObjectContextualTypeOverride(config, type);
    }

    /**
     * The contextual field-level override set on the root configuration for object generation.
     *
     * @param config    configuration to read
     * @param ownerType type that declares the field
     * @param fieldName field name
     * @return the override, or empty
     */
    public static Optional<ContextualGenerator<?>> getObjectContextualFieldOverride(
            GeneratorConfig config, Class<?> ownerType, String fieldName) {
        return installed().getObjectContextualFieldOverride(config, ownerType, fieldName);
    }

    /**
     * The first predicate field override on the root configuration that matches {@code field}.
     *
     * @param config configuration to read
     * @param field  field to match
     * @return the override, or empty
     * @throws NullPointerException if {@code field} is {@code null}
     */
    public static Optional<Generator<?>> getObjectFieldPredicateOverride(GeneratorConfig config, Field field) {
        return installed().getObjectFieldPredicateOverride(config, field);
    }

    /**
     * The first contextual predicate field override on the root configuration that matches {@code field}.
     *
     * @param config configuration to read
     * @param field  field to match
     * @return the override, or empty
     * @throws NullPointerException if {@code field} is {@code null}
     */
    public static Optional<ContextualGenerator<?>> getObjectContextualFieldPredicateOverride(
            GeneratorConfig config, Field field) {
        return installed().getObjectContextualFieldPredicateOverride(config, field);
    }

    /**
     * Whether a field or type exclusion of the root configuration excludes {@code field} from object generation.
     *
     * @param config configuration to read
     * @param field  field to test
     * @return {@code true} if the field is excluded
     * @throws NullPointerException if {@code field} is {@code null}
     */
    public static boolean shouldObjectExclude(GeneratorConfig config, Field field) {
        return installed().shouldObjectExclude(config, field);
    }

    /**
     * Whether {@link GeneratorConfig.Builder#objectExcludeField(String)} excluded {@code name}. Unlike
     * {@link #shouldObjectExclude(GeneratorConfig, Field)}, this answers for members that no field backs, such as
     * constructor parameters.
     *
     * @param config configuration to read
     * @param name   member name
     * @return {@code true} if the name is excluded
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static boolean isObjectFieldNameExcluded(GeneratorConfig config, String name) {
        return installed().isObjectFieldNameExcluded(config, name);
    }

    private static Access installed() {
        return Objects.requireNonNull(access, "GeneratorConfig is not initialized");
    }
}
