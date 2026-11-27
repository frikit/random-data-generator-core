/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.extension.ExtensionModule;
import io.github.frikit.rdg.generator.extension.ExtensionModuleContext;
import io.github.frikit.rdg.generator.provider.ProviderDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("semantic extension provider resolution")
class BuiltInProviderResolverExtensionTest {

    @Test
    @DisplayName("extension aliases resolve and unknown names fail")
    void extensionAliasesAndUnknownNames() {
        GeneratorConfig config =
                GeneratorConfig.builder().install(new AliasModule()).build();

        Generator<?> generator = BuiltInProviderResolver.generator("extension_alias", config);

        assertEquals("extension", generator.generate());
        assertThrows(
                IllegalArgumentException.class, () -> BuiltInProviderResolver.generator("missing.extension", config));
    }

    private static final class AliasModule implements ExtensionModule {

        @Override
        public String id() {
            return "test.alias";
        }

        @Override
        public void configure(ExtensionModuleContext context) {
            context.registerProvider(ProviderDescriptor.builder(
                            "extension.provider", FixedGenerator.class, config -> new FixedGenerator())
                    .aliases("extension_alias")
                    .build());
        }
    }

    private static final class FixedGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "extension";
        }
    }
}
