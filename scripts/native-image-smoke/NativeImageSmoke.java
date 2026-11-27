/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.smoke;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.Generators;
import io.github.frikit.rdg.generator.extension.ExtensionModule;
import io.github.frikit.rdg.generator.extension.ExtensionModuleContext;
import io.github.frikit.rdg.generator.object.ObjectGenerator;
import io.github.frikit.rdg.generator.provider.ProviderDescriptor;
import java.util.Locale;

/**
 * Representative core fixture used by {@code verify_native_image.sh}.
 */
public final class NativeImageSmoke {

    private NativeImageSmoke() {}

    /**
     * Runs a deterministic core generator.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .locale(Locale.US)
                .install(new SmokeModule())
                .build();
        String name = Generators.ofFullName(config).generate();
        Person person = new ObjectGenerator<>(Person.class, config).generate();
        String extensionValue = Generators.ofProviderHub(config).get("smoke.provider", String.class);
        if (name.isBlank() || person.name().isBlank() || person.age() == 0 || !"extension".equals(extensionValue)) {
            throw new IllegalStateException("Core provider or record generation failed");
        }
        System.out.println("native-image-smoke-passed");
    }

    private record Person(String name, int age) {}

    private static final class SmokeModule implements ExtensionModule {
        @Override
        public String id() {
            return "native-image-smoke";
        }

        @Override
        public void configure(ExtensionModuleContext context) {
            context.registerProvider(ProviderDescriptor.builder("smoke.provider", String.class, config -> "extension")
                    .build());
        }
    }
}
