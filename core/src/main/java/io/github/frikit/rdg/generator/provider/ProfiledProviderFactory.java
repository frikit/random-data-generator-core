/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.provider;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.GeneratorProfile;

/**
 * Creates provider instances using generator configuration and profile metadata.
 */
@FunctionalInterface
public interface ProfiledProviderFactory {

    /**
     * Creates a provider instance.
     *
     * @param profile selected profile metadata
     * @param config  generator config with locale/seed
     * @return provider instance
     */
    Object create(GeneratorProfile profile, GeneratorConfig config);
}
