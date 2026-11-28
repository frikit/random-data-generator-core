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
 * Built-in employment type values backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/job_types/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInJobTypeDataProvider implements JobTypeDataProvider {

    private final Locale locale;
    private final List<String> jobTypes;

    BuiltInJobTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInJobTypeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.jobTypes = List.of(LocaleTextResourceLoader.load("rdg/job_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getJobTypes() {
        return jobTypes;
    }
}
