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
 * Contract for locale-specific marital status values.
 *
 * <p>Built-in providers are loaded from {@code rdg/marital_statuses/<locale>.txt} and served by
 * {@link MaritalStatusDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface MaritalStatusDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies marital status values for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized marital statuses such as {@code "Married"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getMaritalStatuses();
}
