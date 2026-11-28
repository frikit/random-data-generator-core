/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.internal.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in address types backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/address_types/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInAddressTypeDataProvider implements AddressTypeDataProvider {

    private final Locale locale;
    private final List<String> addressTypes;

    BuiltInAddressTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInAddressTypeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.addressTypes = List.of(LocaleTextResourceLoader.load("rdg/address_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAddressTypes() {
        return addressTypes;
    }
}
