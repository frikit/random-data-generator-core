/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.rdg.generator.finance.FinancialTermDataRegistry;
import io.github.frikit.rdg.generator.location.CityDataRegistry;
import io.github.frikit.rdg.generator.location.CountryDataRegistry;
import io.github.frikit.rdg.generator.location.StateDataRegistry;
import io.github.frikit.rdg.generator.location.StreetAddressDataRegistry;
import io.github.frikit.rdg.generator.measurement.MeasurementDataRegistry;
import io.github.frikit.rdg.generator.user.BloodTypeDataRegistry;
import io.github.frikit.rdg.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.rdg.generator.user.HobbyDataRegistry;
import io.github.frikit.rdg.generator.user.NationalityDataRegistry;
import io.github.frikit.rdg.generator.user.PronounDataRegistry;
import io.github.frikit.rdg.generator.user.TitleDataRegistry;
import io.github.frikit.rdg.generator.weather.WeatherDataRegistry;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Read-only contract of the global registries after the kRandom v2 removal of process-wide mutation:
 * built-ins are pre-seeded at class load, lookups fall back from exact locale to language, and
 * unknown or null locales resolve to nothing. Assertions derive from each registry's own seeded
 * keys because the built-in locale sets differ per data family.
 */
@DisplayName("Global registry read APIs")
class GlobalRegistryReadApiTest {

    private record ReadApi(
            String name,
            Function<Locale, Boolean> isRegistered,
            Function<Locale, Object> forLocale,
            Set<String> registeredKeys) {}

    private static java.util.List<ReadApi> readApis() {
        return java.util.List.of(
                new ReadApi(
                        "weather",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.WEATHER, locale),
                        WeatherDataRegistry::forLocale,
                        WeatherDataRegistry.registeredKeys()),
                new ReadApi(
                        "restaurantType",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.RESTAURANT_TYPE, locale),
                        RestaurantTypeDataRegistry::forLocale,
                        RestaurantTypeDataRegistry.registeredKeys()),
                new ReadApi(
                        "financialTerm",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.FINANCIAL_TERM, locale),
                        FinancialTermDataRegistry::forLocale,
                        FinancialTermDataRegistry.registeredKeys()),
                new ReadApi(
                        "measurement",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.MEASUREMENT, locale),
                        MeasurementDataRegistry::forLocale,
                        MeasurementDataRegistry.registeredKeys()),
                new ReadApi(
                        "pronoun",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.PRONOUN, locale),
                        PronounDataRegistry::forLocale,
                        PronounDataRegistry.registeredKeys()),
                new ReadApi(
                        "hobby",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.HOBBY, locale),
                        HobbyDataRegistry::forLocale,
                        HobbyDataRegistry.registeredKeys()),
                new ReadApi(
                        "nationality",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.NATIONALITY, locale),
                        NationalityDataRegistry::forLocale,
                        NationalityDataRegistry.registeredKeys()),
                new ReadApi(
                        "chineseZodiac",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.CHINESE_ZODIAC, locale),
                        ChineseZodiacDataRegistry::forLocale,
                        ChineseZodiacDataRegistry.registeredKeys()),
                new ReadApi(
                        "title",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.TITLE, locale),
                        TitleDataRegistry::forLocale,
                        TitleDataRegistry.registeredKeys()),
                new ReadApi(
                        "bloodType",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.BLOOD_TYPE, locale),
                        BloodTypeDataRegistry::forLocale,
                        BloodTypeDataRegistry.registeredKeys()),
                new ReadApi(
                        "country",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.COUNTRY, locale),
                        CountryDataRegistry::forLocale,
                        CountryDataRegistry.registeredKeys()),
                new ReadApi(
                        "city",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.CITY, locale),
                        CityDataRegistry::forLocale,
                        CityDataRegistry.registeredKeys()),
                new ReadApi(
                        "state",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.STATE, locale),
                        StateDataRegistry::forLocale,
                        StateDataRegistry.registeredKeys()),
                new ReadApi(
                        "streetAddress",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.STREET_ADDRESS, locale),
                        StreetAddressDataRegistry::forLocale,
                        StreetAddressDataRegistry.registeredKeys()));
    }

    private static Locale seededCountryLocale(ReadApi api) {
        String key = api.registeredKeys().stream()
                .filter(k -> k.contains("_"))
                .sorted()
                .findFirst()
                .orElseThrow(() -> new AssertionError(api.name() + " seeded no country-level key"));
        String[] parts = key.split("_", 2);
        return new Locale(parts[0], parts[1]);
    }

    @Test
    @DisplayName("seeded locales resolve by exact key and by language fallback")
    void builtInsResolveWithFallback() {
        for (ReadApi api : readApis()) {
            Locale seeded = seededCountryLocale(api);

            assertTrue(api.isRegistered().apply(seeded), api.name());
            assertNotNull(api.forLocale().apply(seeded), api.name());

            Locale unknownRegion = new Locale(seeded.getLanguage(), "ZZ");
            assertTrue(api.isRegistered().apply(unknownRegion), api.name());
            assertNotNull(api.forLocale().apply(unknownRegion), api.name());

            Locale languageOnly = new Locale(seeded.getLanguage());
            assertTrue(api.isRegistered().apply(languageOnly), api.name());
            assertNotNull(api.forLocale().apply(languageOnly), api.name());
        }
    }

    @Test
    @DisplayName("null and unknown locales resolve to nothing")
    void nullAndUnknownLocalesResolveToNothing() {
        for (ReadApi api : readApis()) {
            assertFalse(api.isRegistered().apply(null), api.name());
            assertNull(api.forLocale().apply(null), api.name());

            Locale unknown = new Locale("xx", "YY");
            assertFalse(api.isRegistered().apply(unknown), api.name());
            assertNull(api.forLocale().apply(unknown), api.name());
        }
    }

    @Test
    @DisplayName("key snapshots pair every seeded country key with its language fallback")
    void registeredKeySnapshotsContainLanguageFallbacks() {
        for (ReadApi api : readApis()) {
            Locale seeded = seededCountryLocale(api);
            assertTrue(
                    api.registeredKeys().contains(seeded.getLanguage()),
                    () -> api.name() + " must seed the language fallback for " + seeded);
        }
    }
}
