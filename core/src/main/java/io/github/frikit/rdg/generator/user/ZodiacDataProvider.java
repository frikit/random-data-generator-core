/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific set of Western zodiac sign names.
 *
 * <p>The date boundaries of the tropical zodiac are universal; only the sign <em>names</em> differ by
 * language. Implement this interface and register an instance with
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder#register(io.github.frikit.rdg.generator.Vocabulary, io.github.frikit.rdg.generator.LocaleDataProvider)}
 * to add or override validated localized sign names for one configuration.
 * {@link ZodiacDataRegistry} holds the built-in sign names served by
 * {@link io.github.frikit.rdg.generator.DataRegistryContext#globalDefault()}.
 *
 * <p>{@link #getSigns()} must return exactly 12 names in canonical zodiac order, starting with the
 * Aries sign: {@code [Aries, Taurus, Gemini, Cancer, Leo, Virgo, Libra, Scorpio, Sagittarius,
 * Capricorn, Aquarius, Pisces]}.
 *
 */
public interface ZodiacDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies sign names for.
     */
    Locale getLocale();

    /**
     * The 12 localized sign names in canonical zodiac order (Aries first).
     */
    List<String> getSigns();
}
