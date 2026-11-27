/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.Locale;

/**
 * Built-in title provider backed by classpath title resources.
 *
 * <p>Honorific titles are loaded from {@code rdg/titles/<locale>.txt} (one title per line).
 */
final class BuiltInTitleDataProvider implements TitleDataProvider {

    private final Locale locale;
    private final String[] titles;

    BuiltInTitleDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.titles = LocaleTextResourceLoader.load("rdg/titles/" + supportedLocale.resourcePrefix() + ".txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getTitles() {
        return titles.clone();
    }
}
