/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.Locale;

/**
 * Built-in suffix provider backed by classpath suffix resources.
 *
 * <p>Suffixes are loaded from {@code rdg/suffixes/<locale>.txt} (one suffix per line).
 */
final class BuiltInSuffixDataProvider implements SuffixDataProvider {

    private final Locale locale;
    private final String[] suffixes;

    BuiltInSuffixDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.suffixes = LocaleTextResourceLoader.load("rdg/suffixes/" + supportedLocale.resourcePrefix() + ".txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getSuffixes() {
        return suffixes.clone();
    }
}
