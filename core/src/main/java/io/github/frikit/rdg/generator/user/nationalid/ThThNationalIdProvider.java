/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Thai national ID style identifiers — 13 digits in {@code X-XXXX-XXXXX-XX-X} format.
 *
 * <p>The first digit is 1–9 and the others are random, so they do not necessarily encode a real
 * registrar office, and no check digit is computed.
 */
final class ThThNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return new Locale("th", "TH");
    }

    @Override
    public String generate(Random random) {
        return String.format(
                Locale.ROOT,
                "%d-%04d-%05d-%02d-%d",
                random.nextInt(9) + 1,
                random.nextInt(10_000),
                random.nextInt(100_000),
                random.nextInt(100),
                random.nextInt(10));
    }
}
