/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import io.github.frikit.rdg.generator.locale.SupportedLocale;
import java.util.List;
import java.util.Locale;

/**
 * Built-in postal places, read from {@code rdg/postal_places/<locale>.txt} with each region
 * resolved against the same locale's {@code rdg/states/<locale>_states.txt}.
 */
final class BuiltInPostalPlaceDataProvider implements PostalPlaceDataProvider {

    private final Locale locale;
    private final List<PostalPlace> places;

    BuiltInPostalPlaceDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        StateResourceLoader.StateData regions =
                StateResourceLoader.load("rdg/states/" + supportedLocale.canonicalResourcePrefix() + "_states.txt");
        this.places = PostalPlaceResourceLoader.load(resourcePath(supportedLocale), regions);
    }

    static String resourcePath(SupportedLocale supportedLocale) {
        return "rdg/postal_places/" + supportedLocale.canonicalResourcePrefix() + ".txt";
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<PostalPlace> getPlaces() {
        return places;
    }
}
