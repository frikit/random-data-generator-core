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

/**
 * Generates single decimal digits as strings.
 */
public final class DigitGenerator implements Generator<String> {

    private final Random random;

    public DigitGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DigitGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return String.valueOf(random.nextInt(10));
    }

    /**
     * Generates a non-zero digit from 1..9.
     */
    public String generateNonZero() {
        return String.valueOf(random.nextInt(1, 10));
    }
}
