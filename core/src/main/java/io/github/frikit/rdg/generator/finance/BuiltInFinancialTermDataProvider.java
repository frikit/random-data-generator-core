/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.finance;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in financial-term names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/financial_terms/<locale>.txt}: one localized term per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInFinancialTermDataProvider implements FinancialTermDataProvider {

    private final Locale locale;
    private final List<String> terms;

    BuiltInFinancialTermDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/financial_terms/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInFinancialTermDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.terms = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getTerms() {
        return terms;
    }
}
