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

import io.github.frikit.rdg.generator.GeneratorConfig;
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
