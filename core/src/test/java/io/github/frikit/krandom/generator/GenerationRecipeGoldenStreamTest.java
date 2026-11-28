/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.object.ObjectGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regression pin of the object golden stream for one record of a {@code String} and an {@code int}. A member's
 * seeded stream is derived from its owner's fully qualified class name, so this test and its fixture keep the
 * binary names they had in kRandom
 * ({@code io.github.frikit.krandom.generator.GenerationRecipeGoldenStreamTest$GoldenPerson}). This record still
 * gives the value kRandom 4.1.0 pinned, but that is not a promise: generated objects can take other values than
 * in kRandom 4.1.0.
 */
@DisplayName("the object golden stream of a two-member record under its kRandom class name")
class GenerationRecipeGoldenStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
            .seed(24680L)
            .clock(Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
            .stringLength(6, 6)
            .collectionSize(2, 2)
            .build();

    @Test
    @DisplayName("the golden person keeps its pinned value")
    void objectGoldenStream() {
        assertEquals(
                new GoldenPerson("YyauCX", -1_007_300_327),
                new ObjectGenerator<>(GoldenPerson.class, CONFIG).generate());
    }

    record GoldenPerson(String code, int score) {}
}
