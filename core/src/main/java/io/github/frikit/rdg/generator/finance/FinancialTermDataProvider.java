/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.finance;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of financial-term names.
 *
 * <p>Implement this interface and register an instance with
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder#register(io.github.frikit.rdg.generator.Vocabulary, io.github.frikit.rdg.generator.LocaleDataProvider)}
 * to add or override financial-term vocabulary for one configuration.
 * {@link FinancialTermDataRegistry} holds the built-in vocabulary served by
 * {@link io.github.frikit.rdg.generator.DataRegistryContext#globalDefault()}.
 */
public interface FinancialTermDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies financial terms for.
     */
    Locale getLocale();

    /**
     * The localized financial-term names; must be non-empty.
     */
    List<String> getTerms();
}
