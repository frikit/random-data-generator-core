/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in pronoun sets backed by classpath resources.
 *
 * <p>Sets are loaded from {@code rdg/pronouns/<locale>.txt}: one {@code subject/object} set per
 * line. Blank lines and {@code #} comments are ignored.
 */
final class BuiltInPronounDataProvider implements PronounDataProvider {

    private final Locale locale;
    private final List<String> sets;

    BuiltInPronounDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "rdg/pronouns/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInPronounDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.sets = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getPronounSets() {
        return sets;
    }
}
