/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.file;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in directory names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/directory_names/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInDirectoryNameDataProvider implements DirectoryNameDataProvider {

    private final Locale locale;
    private final List<String> directoryNames;

    BuiltInDirectoryNameDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInDirectoryNameDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.directoryNames = List.of(LocaleTextResourceLoader.load("rdg/directory_names/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getDirectoryNames() {
        return directoryNames;
    }
}
