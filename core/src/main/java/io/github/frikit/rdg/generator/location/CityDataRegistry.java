/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for locale-specific city name providers.
 *
 * <p>This read-only registry holds the built-in city data for {@link CityGenerator} and backs
 * {@link io.github.frikit.rdg.generator.DataRegistryContext#globalDefault()}.
 *
 * <p>Built-in support is auto-loaded at class init from
 * {@link io.github.frikit.rdg.generator.locale.SupportedLocale}.
 *
 * <p>Custom providers registered via {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder} override built-in data
 * for the same locale and enable support for additional locales within that context.
 */
public final class CityDataRegistry {

    private static final Map<String, CityDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            registerProvider(new BuiltInCityDataProvider(supportedLocale));
        }
    }

    private CityDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the provider for the given locale.
     *
     * @param locale the locale
     * @return the provider, or {@code null} if none is registered or locale is null
     */
    public static @org.jspecify.annotations.Nullable CityDataProvider forLocale(Locale locale) {
        return RegistryLookup.findWithFallback(providers, locale);
    }

    /**
     * Returns the set of all registered locale keys (for diagnostics).
     *
     * @return unmodifiable view of registered keys
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }

    private static void registerProvider(CityDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
    }
}
