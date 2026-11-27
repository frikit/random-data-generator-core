/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.base;

import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.Nullable;

/**
 * Generates nullable booleans (true/false/null).
 */
public final class NullableBooleanGenerator implements Generator<@Nullable Boolean> {

    private final Random random;

    public NullableBooleanGenerator() {
        this(GeneratorConfig.defaults());
    }

    public NullableBooleanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
    }

    @Override
    public @Nullable Boolean generate() {
        int value = random.nextInt(3);
        if (value == 0) {
            return null;
        }
        return value == 1;
    }
}
