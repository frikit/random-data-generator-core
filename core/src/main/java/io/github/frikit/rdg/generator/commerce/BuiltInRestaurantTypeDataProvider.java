/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.commerce;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in restaurant cuisine/type names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/restaurant_types/<locale>.txt}: one localized type per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInRestaurantTypeDataProvider implements RestaurantTypeDataProvider {

    private final Locale locale;
    private final List<String> types;

    BuiltInRestaurantTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/restaurant_types/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInRestaurantTypeDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.types = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getTypes() {
        return types;
    }
}
