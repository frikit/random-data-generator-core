/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.measurement;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.generator.user.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in measurement-unit names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/measurement/<locale>.txt}: one localized unit per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInMeasurementDataProvider implements MeasurementDataProvider {

    private final Locale locale;
    private final List<String> units;

    BuiltInMeasurementDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/measurement/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInMeasurementDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.units = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getUnits() {
        return units;
    }
}
