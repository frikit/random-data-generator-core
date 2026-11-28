/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Pins the seeded output of the whole generator catalog to a committed baseline, so that an accidental change to
 * any built-in generator's values fails the build with the factory, method and locale named.
 *
 * <p>The values are those of {@link GeneratorCatalogSweep#sweepEachMethodFresh}: every factory of
 * {@link Generators} that takes one {@link GeneratorConfig}, in six locales, with a fixed seed and clock, and the
 * first {@value GeneratorCatalogSweep#DRAWS} values of each {@code generate*()} method, each method drawn from a
 * fresh generator, so that a new method adds lines and changes none. Each line of {@value #BASELINE} is
 * {@code <factory>.<method> <locale> <digest>}, or {@code <factory>.factory <locale> <digest>} for a factory that
 * refuses the locale, the digest being the first 12 bytes of the SHA-256 of those values.
 *
 * <p>Values that follow the Java runtime's TZDB or CLDR data rather than the seed, as {@code VERSIONING.md}
 * documents, are left out ({@link #RUNTIME_VALUES}) or masked ({@link #NAMES_THE_LOCALE_COUNTRY}). Floating-point
 * values are compared by their bits, because {@code Double.toString} and {@code Float.toString} print some values
 * differently from Java 19 on.
 *
 * <p>Running the test with {@code -P}{@value #WRITE_PROPERTY}{@code =true} rewrites the baseline instead of
 * comparing with it. That is allowed only (a) when the comparison reports nothing but {@code new:} lines, for a new
 * factory or {@code generate*()} method; (b) for a reviewed seeded-output change that the changelog lists; or (c)
 * for a change of this test itself, such as a new {@link #RUNTIME_VALUES} exclusion, a new
 * {@link #NAMES_THE_LOCALE_COUNTRY} mask, or a change of the sweep (moving or renaming
 * {@link GeneratorCatalogSweep.FixtureShape}, for example), where the pull request says why no seeded output
 * changed. A {@code changed:} or {@code missing:} line needs (b) or (c). See
 * {@code docs/development/determinism-and-compatibility.md}.
 */
@Isolated
@DisplayName("Seeded output of the whole generator catalog against its committed baseline")
class GeneratorCatalogSeededBaselineTest {

    static final String BASELINE = "rdg-baseline/catalog-fingerprints.txt";
    static final String WRITE_PROPERTY = "rdg.baseline.write";
    private static final String REGENERATE =
            "./gradlew :core:test --tests '*GeneratorCatalogSeededBaselineTest' -P" + WRITE_PROPERTY + "=true";
    private static final String HEADER = String.join(
            "\n",
            "# Seeded-output baseline of the generator catalog, compared by GeneratorCatalogSeededBaselineTest.",
            "# One line per factory, generate*() method and locale: <factory>.<method> <locale> <digest>, or",
            "# <factory>.factory for a factory that refuses the locale. The digest is the first 12 bytes of the",
            "# SHA-256 of the method's first values from a fresh generator (seed 42, fixed clock), as the test",
            "# computes them.",
            "#",
            "# A changed, missing or new line fails the test. Regenerate with",
            "#   " + REGENERATE,
            "# only",
            "#   (a) when the test reports nothing but new: lines, for a new factory or generate*() method;",
            "#   (b) for a reviewed seeded-output change that CHANGELOG.md lists with the values it affects; or",
            "#   (c) for a change of the test itself: a new RUNTIME_VALUES exclusion or NAMES_THE_LOCALE_COUNTRY",
            "#       mask, or a change of the sweep, such as moving or renaming GeneratorCatalogSweep.FixtureShape;",
            "#       the pull request says why no seeded output changed.",
            "# A changed: or missing: line needs (b) or (c); otherwise find its cause, see",
            "# docs/development/determinism-and-compatibility.md. Do not edit this file by hand.",
            "");

    /**
     * Masked in the values of these factories: the configured locale's country name, which comes from the
     * runtime's CLDR data ({@code Locale.getDisplayCountry}) for addresses in a locale with a country.
     */
    private static final Set<String> NAMES_THE_LOCALE_COUNTRY = Set.of(
            "ofAddressInfo",
            "ofCompanyInfo",
            "ofInvoiceInfo",
            "ofOrderInfo",
            "ofPaymentInfo",
            "ofPersonInfo",
            "ofProfile",
            "ofShipmentInfo",
            "ofSimpleProfile",
            "ofStreetAddress");

    private static final String RUNTIME_COUNTRY_NAME = "<runtime country name>";
    /** How many mismatching lines the failure message lists before it only counts the rest. */
    private static final int LISTED_MISMATCHES = 50;
    /** How many characters of a changed line's current value the failure message shows. */
    private static final int SHOWN_VALUE_LENGTH = 200;

    /** Values that follow the Java runtime's time-zone or locale data rather than the seed. */
    private static final List<RuntimeValue> RUNTIME_VALUES = List.of(
            new RuntimeValue("ofZoneId", "*", any(), "draws from ZoneId.getAvailableZoneIds(), the runtime's TZDB"),
            new RuntimeValue(
                    "ofTimeZoneObject", "*", any(), "draws from TimeZone.getAvailableIDs(), the runtime's TZDB"),
            new RuntimeValue(
                    "ofZonedDateTime",
                    "*",
                    any(),
                    "draws its zone from the runtime's TZDB, and the zone's offset follows TZDB rules"),
            new RuntimeValue(
                    "ofCalendar",
                    "*",
                    any(),
                    "draws its zone from the runtime's TZDB, and the zone's offset follows TZDB rules"),
            new RuntimeValue(
                    "ofTimezone",
                    "generateOffset",
                    any(),
                    "the current offset of a zone follows the runtime's TZDB rules"),
            new RuntimeValue(
                    "ofTimezone",
                    "generateUtcOffset",
                    any(),
                    "the current offset of a zone follows the runtime's TZDB rules"),
            new RuntimeValue(
                    "ofTimezone",
                    "*",
                    locale -> Generators.ofTimezone(GeneratorCatalogSweep.config(locale))
                            .localeZones()
                            .isEmpty(),
                    "in a locale without its own zones every method draws its zone from"
                            + " ZoneId.getAvailableZoneIds(), the runtime's TZDB"),
            new RuntimeValue(
                    "ofMoney", "*", any(), "formats with NumberFormat.getCurrencyInstance, the runtime's CLDR"),
            new RuntimeValue(
                    "ofCurrency",
                    "generatePriceTag",
                    any(),
                    "formats with NumberFormat.getCurrencyInstance, the runtime's CLDR"),
            new RuntimeValue(
                    "ofLocalDate",
                    "generateMonthName",
                    any(),
                    "Month.getDisplayName gives the runtime's CLDR month names"));

    private Locale originalLocale;
    private TimeZone originalTimeZone;

    @BeforeEach
    void neutralDefaults() {
        originalLocale = Locale.getDefault();
        originalTimeZone = TimeZone.getDefault();
        Locale.setDefault(Locale.ROOT);
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @AfterEach
    void restoreDefaults() {
        Locale.setDefault(originalLocale);
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    @DisplayName("every factory method keeps its seeded output in every locale")
    void everyFactoryKeepsItsSeededOutput() throws Exception {
        Map<String, List<String>> drawn = drawnValues();
        Map<String, String> actual = fingerprints(drawn);
        if (Boolean.getBoolean(WRITE_PROPERTY)) {
            Path written = write(actual);
            Assumptions.abort(
                    "wrote " + actual.size() + " lines to " + written + "; review the diff before committing");
        }

        Map<String, String> expected = readBaseline();
        List<String> mismatches = mismatches(expected, actual, drawn);

        assertTrue(mismatches.isEmpty(), () -> failure(mismatches));
    }

    @Test
    @DisplayName("every exclusion and mask still matches swept values, and no unmasked factory holds the country name")
    void everyExclusionMatchesASweptMethod() throws Exception {
        Set<String> swept = new TreeSet<>();
        Set<String> namingTheCountry = new TreeSet<>();
        Set<String> unmasked = new TreeSet<>();
        GeneratorCatalogSweep.sweepEachMethodFresh(GeneratorCatalogSweep::describe)
                .forEach((key, value) -> {
                    SweptValue sweptValue = SweptValue.parse(key);
                    swept.add(sweptValue.factory() + "." + sweptValue.method());
                    swept.add(sweptValue.factory() + ".*");
                    if (!masked(sweptValue, value).equals(value)) {
                        namingTheCountry.add(
                                sweptValue.factory() + " " + sweptValue.locale().toLanguageTag());
                    }
                    // A record that holds an address prints the runtime's country name as country=<name>.
                    if (!NAMES_THE_LOCALE_COUNTRY.contains(sweptValue.factory())
                            && value.contains("country=" + runtimeCountryName(sweptValue.locale()))) {
                        unmasked.add(sweptValue.factory() + " holds the runtime's country name in "
                                + sweptValue.locale().toLanguageTag() + ": add it to NAMES_THE_LOCALE_COUNTRY");
                    }
                });
        Set<String> stale = new TreeSet<>();
        for (RuntimeValue exclusion : RUNTIME_VALUES) {
            if (!swept.contains(exclusion.factory() + "." + exclusion.method())) {
                stale.add(exclusion.factory() + "." + exclusion.method());
            }
        }
        for (String factory : NAMES_THE_LOCALE_COUNTRY) {
            for (Locale locale : GeneratorCatalogSweep.GENERATION_LOCALES) {
                if (!namingTheCountry.contains(factory + " " + locale.toLanguageTag())) {
                    stale.add(factory + " " + locale.toLanguageTag() + " names no country");
                }
            }
        }

        assertEquals(Set.of(), stale);
        assertEquals(Set.of(), unmasked);
    }

    /**
     * {@code <factory>.<method> <locale>} to the method's drawn values in that locale, each as
     * {@code <method>[<draw>]=<value>}, in sorted order.
     */
    static Map<String, List<String>> drawnValues() throws Exception {
        Map<String, List<String>> values = new TreeMap<>();
        GeneratorCatalogSweep.sweepEachMethodFresh(GeneratorCatalogSeededBaselineTest::describe)
                .forEach((key, value) -> {
                    SweptValue swept = SweptValue.parse(key);
                    if (RUNTIME_VALUES.stream().noneMatch(exclusion -> exclusion.covers(swept))) {
                        values.computeIfAbsent(
                                        swept.factory() + "." + swept.method() + " "
                                                + swept.locale().toLanguageTag(),
                                        line -> new ArrayList<>())
                                .add(swept.draw() + "=" + masked(swept, value));
                    }
                });
        return values;
    }

    /** Each line of the drawn values to the digest of its values. */
    static Map<String, String> fingerprints(Map<String, List<String>> drawn) {
        Map<String, String> fingerprints = new TreeMap<>();
        drawn.forEach((line, values) -> {
            StringBuilder text = new StringBuilder();
            values.forEach(value -> text.append(value).append('\n'));
            fingerprints.put(line, sha256(text.toString()));
        });
        return fingerprints;
    }

    /** As the sweep describes a value, except that a floating-point value is described by its bits. */
    private static String describe(@Nullable Object value) {
        if (value instanceof Double number) {
            return "double:" + Long.toHexString(Double.doubleToLongBits(number));
        }
        if (value instanceof Float number) {
            return "float:" + Integer.toHexString(Float.floatToIntBits(number));
        }
        return GeneratorCatalogSweep.describe(value);
    }

    private static String masked(SweptValue swept, String value) {
        if (!NAMES_THE_LOCALE_COUNTRY.contains(swept.factory())) {
            return value;
        }
        return value.replace(runtimeCountryName(swept.locale()), RUNTIME_COUNTRY_NAME);
    }

    /** The locale's country name as the runtime's CLDR data gives it, and as the address generators print it. */
    private static String runtimeCountryName(Locale locale) {
        return new Locale("", locale.getCountry()).getDisplayCountry(locale);
    }

    private static List<String> mismatches(
            Map<String, String> expected, Map<String, String> actual, Map<String, List<String>> drawn) {
        List<String> mismatches = new ArrayList<>();
        expected.forEach((line, digest) -> {
            String found = actual.get(line);
            if (found == null) {
                mismatches.add("missing: " + line);
            } else if (!found.equals(digest)) {
                StringBuilder changed =
                        new StringBuilder("changed: " + line + " (baseline " + digest + ", now " + found + ")");
                for (String value : drawn.get(line)) {
                    changed.append("\n      now ").append(shortened(value));
                }
                mismatches.add(changed.toString());
            }
        });
        actual.forEach((line, digest) -> {
            if (!expected.containsKey(line)) {
                mismatches.add("new:     " + line + " " + digest);
            }
        });
        return mismatches;
    }

    private static String failure(List<String> mismatches) {
        StringBuilder message = new StringBuilder()
                .append(mismatches.size())
                .append(" lines of seeded output differ from ")
                .append(BASELINE)
                .append(".\nA changed: or missing: line is a change of seeded output: find its cause, see")
                .append(" docs/development/determinism-and-compatibility.md. Regenerate the baseline with\n  ")
                .append(REGENERATE)
                .append("\nonly (a) when every line is new:, for a new factory or generate*() method; (b) for a")
                .append(" reviewed seeded-output change that CHANGELOG.md lists; or (c) for a change of the test")
                .append(" itself (a new exclusion or mask, or a change of the sweep), where the pull request says why")
                .append(" no seeded output changed. changed: and missing: lines need (b) or (c).\nThe lines:");
        mismatches.stream()
                .limit(LISTED_MISMATCHES)
                .forEach(mismatch -> message.append("\n  ").append(mismatch));
        if (mismatches.size() > LISTED_MISMATCHES) {
            message.append("\n  ... and ")
                    .append(mismatches.size() - LISTED_MISMATCHES)
                    .append(" more");
        }
        return message.toString();
    }

    private static String shortened(String value) {
        String oneLine = value.replace("\n", "\\n");
        return oneLine.length() <= SHOWN_VALUE_LENGTH ? oneLine : oneLine.substring(0, SHOWN_VALUE_LENGTH) + "...";
    }

    private static Map<String, String> readBaseline() throws IOException {
        Map<String, String> baseline = new TreeMap<>();
        try (InputStream stream =
                GeneratorCatalogSeededBaselineTest.class.getClassLoader().getResourceAsStream(BASELINE)) {
            assertNotNull(stream, BASELINE + " is missing; create it with " + REGENERATE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                int digest = line.lastIndexOf(' ');
                baseline.put(line.substring(0, digest), line.substring(digest + 1));
            }
        }
        return baseline;
    }

    private static Path write(Map<String, String> fingerprints) throws IOException {
        StringBuilder text = new StringBuilder(HEADER);
        fingerprints.forEach(
                (line, digest) -> text.append(line).append(' ').append(digest).append('\n'));
        Path file = Path.of(System.getProperty("rdg.rootDir"), "core", "src", "test", "resources")
                .resolve(BASELINE);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text, StandardCharsets.UTF_8);
        return file;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Predicate<Locale> any() {
        return locale -> true;
    }

    /** One key of the sweep: {@code <locale>/<factory>.<method>[<draw>]}, or {@code <locale>/<factory>.factory}. */
    private record SweptValue(Locale locale, String factory, String method, String draw) {

        static SweptValue parse(String key) {
            int slash = key.indexOf('/');
            int dot = key.indexOf('.', slash);
            String localeName = key.substring(0, slash);
            Locale locale = GeneratorCatalogSweep.GENERATION_LOCALES.stream()
                    .filter(candidate -> candidate.toString().equals(localeName))
                    .findFirst()
                    .orElseThrow();
            String draw = key.substring(dot + 1);
            int bracket = draw.indexOf('[');
            String method = bracket < 0 ? draw : draw.substring(0, bracket);
            return new SweptValue(locale, key.substring(slash + 1, dot), method, draw);
        }
    }

    /**
     * A swept value left out of the baseline, because it follows the Java runtime. {@code method} is a method name,
     * or {@code *} for every method of the factory.
     */
    private record RuntimeValue(String factory, String method, Predicate<Locale> locales, String reason) {

        boolean covers(SweptValue value) {
            return factory.equals(value.factory())
                    && (method.equals("*") || method.equals(value.method()))
                    && locales.test(value.locale());
        }
    }
}
