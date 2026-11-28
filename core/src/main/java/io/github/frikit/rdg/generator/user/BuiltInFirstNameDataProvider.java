/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.Locale;

/**
 * Built-in first-name provider backed by classpath name resources.
 */
final class BuiltInFirstNameDataProvider implements FirstNameDataProvider {

    private final Locale locale;
    private final String[] maleNames;
    private final String[] femaleNames;

    BuiltInFirstNameDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        this.maleNames = LocaleTextResourceLoader.load("rdg/names/first_male/" + resourcePrefix + ".txt");
        this.femaleNames = LocaleTextResourceLoader.load("rdg/names/first_female/" + resourcePrefix + ".txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getMaleFirstNames() {
        return maleNames.clone();
    }

    @Override
    public String[] getFemaleFirstNames() {
        return femaleNames.clone();
    }
}
