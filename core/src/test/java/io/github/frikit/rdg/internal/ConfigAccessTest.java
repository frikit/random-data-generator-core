/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.ContextualGenerator;
import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigAccess")
class ConfigAccessTest {

    @Test
    @DisplayName("keeps the implementation GeneratorConfig installs and refuses a second one")
    void refusesASecondInstall() {
        GeneratorConfig config = GeneratorConfig.builder().seed(5L).build();
        ConfigAccess.Access other = new ConfigAccess.Access() {
            @Override
            public GeneratorConfig withSeed(GeneratorConfig parent, long seed) {
                return parent;
            }

            @Override
            public boolean isRestoredFromRecipe(GeneratorConfig parent) {
                return true;
            }

            @Override
            public Optional<Generator<?>> getObjectTypeOverride(GeneratorConfig parent, Class<?> type) {
                return Optional.empty();
            }

            @Override
            public Optional<Generator<?>> getObjectFieldOverride(
                    GeneratorConfig parent, Class<?> ownerType, String fieldName) {
                return Optional.empty();
            }

            @Override
            public Optional<ContextualGenerator<?>> getObjectContextualTypeOverride(
                    GeneratorConfig parent, Class<?> type) {
                return Optional.empty();
            }

            @Override
            public Optional<ContextualGenerator<?>> getObjectContextualFieldOverride(
                    GeneratorConfig parent, Class<?> ownerType, String fieldName) {
                return Optional.empty();
            }

            @Override
            public Optional<Generator<?>> getObjectFieldPredicateOverride(GeneratorConfig parent, Field field) {
                return Optional.empty();
            }

            @Override
            public Optional<ContextualGenerator<?>> getObjectContextualFieldPredicateOverride(
                    GeneratorConfig parent, Field field) {
                return Optional.empty();
            }

            @Override
            public boolean shouldObjectExclude(GeneratorConfig parent, Field field) {
                return false;
            }

            @Override
            public boolean isObjectFieldNameExcluded(GeneratorConfig parent, String name) {
                return false;
            }

            @Override
            public ExtensionRegistry getExtensionRegistry(GeneratorConfig parent) {
                return ExtensionRegistry.resolve(List.of());
            }
        };

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> ConfigAccess.install(other));

        assertEquals("ConfigAccess is installed already", error.getMessage());
        assertEquals(9L, ConfigAccess.withSeed(config, 9L).getSeed().getAsLong());
        assertFalse(ConfigAccess.isRestoredFromRecipe(config));
    }

    @Test
    @DisplayName("reads whether a configuration replays a recipe")
    void readsTheRecipeRestoration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(5L).build();
        GeneratorConfig restored = config.getGenerationRecipe().orElseThrow().toGeneratorConfig();

        assertTrue(ConfigAccess.isRestoredFromRecipe(restored));
        assertFalse(
                ConfigAccess.isRestoredFromRecipe(restored.toBuilder().seed(6L).build()));
    }
}
