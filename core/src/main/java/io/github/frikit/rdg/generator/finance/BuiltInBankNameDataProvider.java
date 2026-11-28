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
 * Built-in fictional bank names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/bank_names/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInBankNameDataProvider implements BankNameDataProvider {

    private final Locale locale;
    private final List<String> bankNames;

    BuiltInBankNameDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInBankNameDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.bankNames = List.of(LocaleTextResourceLoader.load("rdg/bank_names/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getBankNames() {
        return bankNames;
    }
}
