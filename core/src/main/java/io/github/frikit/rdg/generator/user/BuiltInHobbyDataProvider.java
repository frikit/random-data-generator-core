/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.List;
import java.util.Locale;

/**
 * Built-in hobby names backed by classpath resources.
 *
 * <p>Names are loaded from {@code rdg/hobbies/<locale>.txt}: one localized hobby per line. Blank
 * lines and {@code #} comments are ignored.
 */
final class BuiltInHobbyDataProvider implements HobbyDataProvider {

    private final Locale locale;
    private final List<String> hobbies;

    BuiltInHobbyDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/hobbies/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInHobbyDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.hobbies = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getHobbies() {
        return hobbies;
    }
}
