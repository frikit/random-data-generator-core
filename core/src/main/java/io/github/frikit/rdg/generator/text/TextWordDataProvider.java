/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.text;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific text vocabulary words.
 *
 * <p>Built-in providers are loaded from {@code rdg/text/words/<locale>.txt} and served by
 * {@link TextWordDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface TextWordDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies text vocabulary words for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized vocabulary words used to compose text blocks; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getWords();
}
