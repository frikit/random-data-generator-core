/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.weather;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of weather-condition names.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder#register(io.github.frikit.rdg.generator.Vocabulary, io.github.frikit.rdg.generator.LocaleDataProvider)}
 * to add or override the weather vocabulary for any locale. {@link WeatherDataRegistry} holds the
 * built-in vocabulary served by
 * {@link io.github.frikit.rdg.generator.DataRegistryContext#globalDefault()}.
 */
public interface WeatherDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies condition names for.
     */
    Locale getLocale();

    /**
     * The localized weather-condition names; must be non-empty.
     */
    List<String> getConditions();
}
