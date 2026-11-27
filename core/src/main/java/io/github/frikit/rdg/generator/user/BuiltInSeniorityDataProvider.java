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
 * Built-in job seniority labels backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/seniorities/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInSeniorityDataProvider implements SeniorityDataProvider {

    private final Locale locale;
    private final List<String> seniorities;

    BuiltInSeniorityDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInSeniorityDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.seniorities = List.of(LocaleTextResourceLoader.load("rdg/seniorities/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getSeniorities() {
        return seniorities;
    }
}
