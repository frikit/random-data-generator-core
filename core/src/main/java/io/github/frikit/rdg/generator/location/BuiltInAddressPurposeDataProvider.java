/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.generator.user.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in address purposes backed by classpath resources.
 *
 * <p>Entries are loaded from {@code rdg/address_purposes/<locale>.txt}, one per line; blank lines and
 * {@code #} comments are ignored.
 */
final class BuiltInAddressPurposeDataProvider implements AddressPurposeDataProvider {

    private final Locale locale;
    private final List<String> addressPurposes;

    BuiltInAddressPurposeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInAddressPurposeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.addressPurposes =
                List.of(LocaleTextResourceLoader.load("rdg/address_purposes/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAddressPurposes() {
        return addressPurposes;
    }
}
