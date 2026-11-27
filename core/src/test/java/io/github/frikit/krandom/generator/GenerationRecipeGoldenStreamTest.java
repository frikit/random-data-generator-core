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
 * kRandom 4.1.0 compatibility pin for object generation. A member's seeded stream is derived from its owner's
 * fully qualified class name, so this test and its fixture keep the binary names they had in kRandom
 * ({@code io.github.frikit.krandom.generator.GenerationRecipeGoldenStreamTest$GoldenPerson}). With them, the
 * object golden stream of {@code io.github.frikit.rdg.generator.GenerationRecipeGoldenStreamTest} must still give
 * the value kRandom 4.1.0 pinned.
 */
@DisplayName("kRandom 4.1.0 compatibility: the object golden stream under its kRandom class name")
class GenerationRecipeGoldenStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
            .seed(24680L)
            .clock(Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
            .stringLength(6, 6)
            .collectionSize(2, 2)
            .build();

    @Test
    @DisplayName("the golden person is the one kRandom 4.1.0 pinned")
    void objectGoldenStream() {
        assertEquals(
                new GoldenPerson("YyauCX", -1_007_300_327),
                new ObjectGenerator<>(GoldenPerson.class, CONFIG).generate());
    }

    record GoldenPerson(String code, int score) {}
}
