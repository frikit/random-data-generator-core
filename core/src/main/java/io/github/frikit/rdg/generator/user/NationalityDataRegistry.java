/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Global registry mapping locales to {@link NationalityDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.rdg.generator.locale.SupportedLocale} that has a
 * {@code rdg/nationalities/<locale>.txt} resource. Locales without a file fall back to the
 * bundled default (English) demonyms. Custom providers can be added via
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder}.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class NationalityDataRegistry {

    private static final ConcurrentHashMap<String, NationalityDataProvider> REGISTRY = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "rdg/nationalities/" + supportedLocale.resourcePrefix() + ".txt";
            if (NationalityDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInNationalityDataProvider(supportedLocale));
            }
        }
    }

    private NationalityDataRegistry() {}

    /**
     * Returns the best-matching provider for the given locale, or {@code null} if none is registered.
     */
    public static @Nullable NationalityDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            NationalityDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    private static void putProvider(NationalityDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
