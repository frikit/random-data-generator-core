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
 * Registry for locale-specific street-address providers.
 */
public final class StreetAddressDataRegistry {

    private static final Map<String, StreetAddressDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            registerProvider(new BuiltInStreetAddressDataProvider(supportedLocale));
        }
    }

    private StreetAddressDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns provider for locale.
     */
    public static @org.jspecify.annotations.Nullable StreetAddressDataProvider forLocale(Locale locale) {
        return RegistryLookup.findWithFallback(providers, locale);
    }

    /**
     * Returns registered key set.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }

    private static void registerProvider(StreetAddressDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
    }
}
