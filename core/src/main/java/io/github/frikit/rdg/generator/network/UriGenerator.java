/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.network;

import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates URI strings (URL-form URIs).
 */
public final class UriGenerator implements Generator<String> {

    private final Random random;
    private final UrlGenerator urlGenerator;

    public UriGenerator() {
        this(GeneratorConfig.defaults());
    }

    public UriGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public UriGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.urlGenerator = new UrlGenerator(effective.forChildStream("uRL"));
    }

    @Override
    public String generate() {
        if (random.nextBoolean()) {
            return urlGenerator.generateWithPathAndQuery();
        }
        return urlGenerator.generateWithPath();
    }

    /**
     * Generates a URI with path but without query parameters.
     *
     * @return URI string
     */
    public String generateWithPath() {
        return urlGenerator.generateWithPath();
    }

    /**
     * Generates a URI with path and query parameters.
     *
     * @return URI string
     */
    public String generateWithQuery() {
        return urlGenerator.generateWithPathAndQuery();
    }
}
