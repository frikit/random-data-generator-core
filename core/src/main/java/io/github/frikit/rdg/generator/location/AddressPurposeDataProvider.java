/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific address purposes, what an address is used for in a record.
 *
 * <p>Built-in providers are loaded from {@code rdg/address_purposes/<locale>.txt} and served by
 * {@link AddressPurposeDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface AddressPurposeDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies address purposes for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized address purposes such as {@code "Billing address"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAddressPurposes();
}
