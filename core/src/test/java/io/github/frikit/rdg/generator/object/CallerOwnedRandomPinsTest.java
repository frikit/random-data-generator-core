/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.base.ByteGenerator;
import io.github.frikit.rdg.generator.identifier.UuidGenerator;
import io.github.frikit.rdg.generator.user.FirstNameGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the generated objects of a caller-owned {@link Random} and of a random factory, whose output the
 * versioning policy covers like seeded output. Every member draws from the caller's one sequence, so one more or
 * one fewer draw anywhere changes every value after it: a changed line here is a change of output.
 */
class CallerOwnedRandomPinsTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-09-26T10:00:00Z"), ZoneOffset.UTC);

    public static class Counter implements Generator<Long> {
        private long next = 1;

        @Override
        public Long generate() {
            return next++;
        }
    }

    /** Fields are generated in name order: firstName, id, plain, plainId, seedable, sequence. */
    public static class Fixture {
        @Randomizer(FirstNameGenerator.class)
        public String firstName;

        @Randomizer(UuidGenerator.class)
        public UUID id;

        public int plain;
        public UUID plainId;

        @Randomizer(ByteGenerator.class)
        public byte seedable;

        @Randomizer(Counter.class)
        public long sequence;
    }

    private static List<String> objects(GeneratorConfig config) {
        ObjectGenerator<Fixture> generator = new ObjectGenerator<>(Fixture.class, config);
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Fixture value = generator.generate();
            lines.add(value.firstName + " " + value.id + " " + value.plain + " " + value.plainId + " " + value.seedable
                    + " " + value.sequence);
        }
        return lines;
    }

    @Test
    @DisplayName("random(new Random(42)) gives these objects, randomizer members included")
    void callerOwnedRandom() {
        GeneratorConfig config = GeneratorConfig.builder()
                .random(new Random(42L))
                .locale(Locale.US)
                .clock(FIXED)
                .build();

        assertEquals(
                List.of(
                        "Sadie 03c82938-ed74-4c1f-a536-8f0d0fddca02 1325939940 4bbb2bf1-839d-4e46-ad85-2cb5be6a61aa -25 1",
                        "Brooklyn 77e24208-23ae-4db3-9324-1fba0281a3d1 -1329611232 1100b276-5457-48c3-8f40-6cc8e3a188ff -98 2",
                        "Audrey 18cb9b75-0e6a-48b3-9dfd-c3f49753e4ff 1660292060 05835ccc-16ad-482d-8c47-8a26f4502798 85 3"),
                objects(config));
    }

    @Test
    @DisplayName("randomFactory(() -> new Random(42)) gives these objects, randomizer members included")
    void repeatingRandomFactory() {
        GeneratorConfig config = GeneratorConfig.builder()
                .randomFactory(() -> new Random(42L))
                .locale(Locale.US)
                .clock(FIXED)
                .build();

        // Each object's members draw from a new source in the same state, so the objects are equal but for
        // the counter, which keeps its own state.
        assertEquals(
                List.of(
                        "Sadie 03c82938-ed74-4c1f-a536-8f0d0fddca02 1325939940 359d41ba-f78a-4e0d-a1bb-e7ae28c0450c 116 1",
                        "Sadie 03c82938-ed74-4c1f-a536-8f0d0fddca02 1325939940 359d41ba-f78a-4e0d-a1bb-e7ae28c0450c 116 2",
                        "Sadie 03c82938-ed74-4c1f-a536-8f0d0fddca02 1325939940 359d41ba-f78a-4e0d-a1bb-e7ae28c0450c 116 3"),
                objects(config));
    }
}
