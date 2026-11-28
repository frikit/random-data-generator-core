/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.measurement;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Global registry mapping locales to {@link MeasurementDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.rdg.generator.locale.SupportedLocale} that has a
 * {@code rdg/measurement/<locale>.txt} resource. Locales without a file fall back to the bundled
 * default (English) units. Custom providers can be added via
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder}.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class MeasurementDataRegistry {

    private static final ConcurrentHashMap<String, MeasurementDataProvider> REGISTRY = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "rdg/measurement/" + supportedLocale.resourcePrefix() + ".txt";
            if (MeasurementDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInMeasurementDataProvider(supportedLocale));
            }
        }
    }

    private MeasurementDataRegistry() {}

    /**
     * Returns the best-matching provider for the given locale, or {@code null} if none is registered.
     */
    public static @Nullable MeasurementDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            MeasurementDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void putProvider(MeasurementDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
