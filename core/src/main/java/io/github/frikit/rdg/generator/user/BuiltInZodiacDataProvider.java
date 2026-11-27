/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.List;
import java.util.Locale;

/**
 * Built-in zodiac sign names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/zodiac/<locale>.txt}: one localized sign name per line, in
 * canonical zodiac order (Aries first). Blank lines and {@code #} comments are ignored.
 */
final class BuiltInZodiacDataProvider implements ZodiacDataProvider {

    private final Locale locale;
    private final List<String> signs;

    BuiltInZodiacDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/zodiac/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInZodiacDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.signs = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getSigns() {
        return signs;
    }
}
