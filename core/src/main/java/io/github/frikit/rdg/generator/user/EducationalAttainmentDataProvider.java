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
 * Contract for locale-specific educational attainment levels.
 *
 * <p>Built-in providers are loaded from {@code rdg/educational_attainments/<locale>.txt} and served by
 * {@link EducationalAttainmentDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface EducationalAttainmentDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies educational attainment levels for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized attainment levels such as {@code "Master's Degree"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getEducationalAttainments();
}
