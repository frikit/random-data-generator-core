/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.commerce;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of restaurant cuisine/type names.
 *
 * <p>Implement this interface and register an instance with
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder#register(io.github.frikit.rdg.generator.Vocabulary, io.github.frikit.rdg.generator.LocaleDataProvider)}
 * to add or override restaurant-type vocabulary for one configuration.
 * {@link RestaurantTypeDataRegistry} holds the built-in vocabulary served by
 * {@link io.github.frikit.rdg.generator.DataRegistryContext#globalDefault()}.
 */
public interface RestaurantTypeDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies restaurant types for.
     */
    Locale getLocale();

    /**
     * The localized restaurant cuisine/type names; must be non-empty.
     */
    List<String> getTypes();
}
