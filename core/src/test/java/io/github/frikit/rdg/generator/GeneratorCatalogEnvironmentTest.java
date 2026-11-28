/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Sweeps every generator factory, so that a new generator is covered without a test of its own: a
 * seeded value must not depend on the JVM's default locale or time zone. One seed cannot reach every
 * branch, so {@link EnvironmentDeterminismTest} keeps its targeted cases.
 */
@Isolated
@DisplayName("Seeded output of the whole generator catalog")
class GeneratorCatalogEnvironmentTest {

    private static final List<Locale> LOCALES_WITHOUT_MIDDLE_NAMES =
            Stream.of("tr-TR", "ar-SA", "hi-IN").map(Locale::forLanguageTag).toList();
    private static final List<Locale> LOCALES_WITHOUT_POSTAL_PLACES =
            Stream.of("tr-TR", "ar-SA", "hi-IN").map(Locale::forLanguageTag).toList();

    private Locale originalLocale;
    private TimeZone originalTimeZone;

    @BeforeEach
    void rememberDefaults() {
        originalLocale = Locale.getDefault();
        originalTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void restoreDefaults() {
        Locale.setDefault(originalLocale);
        TimeZone.setDefault(originalTimeZone);
    }

    @ParameterizedTest(name = "default locale {0}")
    @ValueSource(strings = {"tr-TR", "ar-SA", "hi-IN", "th-TH", "ja-JP-u-ca-japanese", "de-DE"})
    @DisplayName("does not depend on the JVM's default locale")
    void doesNotDependOnTheJvmDefaultLocale(String languageTag) throws Exception {
        Map<String, String> expected = sweepUnderNeutralDefaults();

        Locale.setDefault(Locale.forLanguageTag(languageTag));

        assertEquals(List.of(), differences(expected, sweep()));
    }

    @ParameterizedTest(name = "default time zone {0}")
    @ValueSource(strings = {"Pacific/Kiritimati", "Etc/GMT+12", "America/New_York"})
    @DisplayName("does not depend on the JVM's default time zone")
    void doesNotDependOnTheJvmDefaultTimeZone(String zoneId) throws Exception {
        Map<String, String> expected = sweepUnderNeutralDefaults();

        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));

        assertEquals(List.of(), differences(expected, sweep()));
    }

    @Test
    @DisplayName("covers the catalog, and only the known factories refuse")
    void coversTheCatalogAndOnlyTheKnownFactoriesRefuse() throws Exception {
        Map<String, String> outcomes = sweepUnderNeutralDefaults();

        Set<String> refusals = new TreeSet<>();
        outcomes.forEach((key, outcome) -> {
            if (outcome.startsWith(GeneratorCatalogSweep.REFUSED)) {
                refusals.add(key.replaceFirst("\\[\\d+]$", "") + " "
                        + outcome.substring(GeneratorCatalogSweep.REFUSED.length()));
            }
        });

        assertTrue(
                GeneratorCatalogSweep.FACTORIES.size() > 150,
                "only " + GeneratorCatalogSweep.FACTORIES.size() + " factories found");
        assertEquals(expectedRefusals(), refusals);
    }

    /** What the configuration or the bundled data rules out: nothing else may refuse. */
    private static Set<String> expectedRefusals() {
        Set<String> refusals = new TreeSet<>();
        for (Locale locale : GeneratorCatalogSweep.GENERATION_LOCALES) {
            refusals.add(locale + "/ofEmail.generateFreeEmail IllegalStateException");
            refusals.add(locale + "/ofEmail.generateFreeEmailDomain IllegalStateException");
            refusals.add(locale + "/ofIPv4.generatePrivate IllegalStateException");
            refusals.add(locale + "/ofIPv4.generatePublic IllegalStateException");
            refusals.add(locale + "/ofUniversity.factory UnsupportedOperationException");
        }
        for (Locale locale : LOCALES_WITHOUT_MIDDLE_NAMES) {
            refusals.add(locale + "/ofFullName.generateWithMiddleInitial UnsupportedOperationException");
            refusals.add(locale + "/ofFullName.generateWithMiddleName UnsupportedOperationException");
            refusals.add(locale + "/ofMiddleName.factory UnsupportedOperationException");
        }
        for (Locale locale : LOCALES_WITHOUT_POSTAL_PLACES) {
            refusals.add(locale + "/ofPostalLocality.factory UnsupportedOperationException");
        }
        return refusals;
    }

    private Map<String, String> sweepUnderNeutralDefaults() throws Exception {
        Locale.setDefault(Locale.ROOT);
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        return sweep();
    }

    private static Map<String, String> sweep() throws Exception {
        return GeneratorCatalogSweep.sweep(GeneratorCatalogSweep::describe);
    }

    private static List<String> differences(Map<String, String> expected, Map<String, String> actual) {
        List<String> differences = new ArrayList<>();
        expected.forEach((key, value) -> {
            if (!value.equals(actual.get(key))) {
                differences.add(key + ": " + value + " -> " + actual.get(key));
            }
        });
        return differences.stream().limit(20).toList();
    }
}
