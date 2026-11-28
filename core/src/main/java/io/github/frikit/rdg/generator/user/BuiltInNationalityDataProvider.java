/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in nationality demonyms backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/nationalities/<locale>.txt}: one localized demonym per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInNationalityDataProvider implements NationalityDataProvider {

    private final Locale locale;
    private final List<String> nationalities;

    BuiltInNationalityDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/nationalities/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInNationalityDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.nationalities = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getNationalities() {
        return nationalities;
    }
}
