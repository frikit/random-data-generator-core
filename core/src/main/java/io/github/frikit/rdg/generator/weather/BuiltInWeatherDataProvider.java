/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.weather;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in weather-condition names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/weather/<locale>.txt}: one localized condition per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInWeatherDataProvider implements WeatherDataProvider {

    private final Locale locale;
    private final List<String> conditions;

    BuiltInWeatherDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/weather/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInWeatherDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.conditions = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getConditions() {
        return conditions;
    }
}
