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
 * Global registry mapping locales to {@link ZodiacDataProvider} instances.
 *
 * <p>At class-load time it is seeded with every built-in
 * {@link io.github.frikit.rdg.generator.locale.SupportedLocale} that <em>has</em> a
 * {@code rdg/zodiac/<locale>.txt} resource. Locales without a file are intentionally not
 * registered, so {@link ZodiacGenerator} falls back to the bundled default (English) names. Custom
 * providers are registered per configuration via {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder}.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null} (the caller handles the missing case).
 */
public final class ZodiacDataRegistry {

    private static final ConcurrentHashMap<String, ZodiacDataProvider> REGISTRY = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "rdg/zodiac/" + supportedLocale.resourcePrefix() + ".txt";
            if (ZodiacDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInZodiacDataProvider(supportedLocale));
            }
        }
    }

    private ZodiacDataRegistry() {}

    /**
     * Returns the best-matching provider for the given locale, or {@code null} if none is registered.
     */
    public static @Nullable ZodiacDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            ZodiacDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    private static void putProvider(ZodiacDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
