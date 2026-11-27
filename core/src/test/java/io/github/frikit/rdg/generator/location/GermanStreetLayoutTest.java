/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.DataRegistryContext;
import io.github.frikit.rdg.generator.GenerationRecipe;
import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.RandomDataGenerator;
import io.github.frikit.rdg.generator.Vocabulary;
import io.github.frikit.rdg.generator.schema.Field;
import io.github.frikit.rdg.generator.schema.Schema;
import io.github.frikit.rdg.generator.schema.SchemaValueProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("the German locales write the street, then the house number, and no other locale does")
class GermanStreetLayoutTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static final Pattern GERMAN_LINE = Pattern.compile("\\S+ [1-9][0-9]{0,3}");

    private static GeneratorConfig config(String tag) {
        return GeneratorConfig.builder()
                .locale(Locale.forLanguageTag(tag))
                .seed(42L)
                .clock(CLOCK)
                .build();
    }

    @Test
    @DisplayName("seeded with 42, the German street lines read as German writes them")
    void germanLinesAreAsGermanWritesThem() {
        StreetAddressGenerator streets = new StreetAddressGenerator(config("de-DE"));

        assertEquals("Schlossanger 7389", streets.generate());
        assertEquals("Rosenstrasse 9180", streets.generate(false));
        assertEquals("Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland", streets.generateFullAddress());

        AddressInfo info = new AddressInfoGenerator(config("de-DE")).generate();
        assertEquals("Tulpenpfad 8680", info.street());
        assertEquals("Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland", info.address());
        assertEquals("8680", info.streetNumber());
        assertEquals("Tulpen", info.streetName());
        assertEquals("Pfad", info.streetSuffix());
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "de-DE", "de-AT", "de-CH", "de-LU"})
    @DisplayName("each German locale lays out its draws street first")
    void eachGermanLocaleLaysOutItsDrawsStreetFirst(String tag) {
        StreetAddressGenerator germanLayout = new StreetAddressGenerator(config(tag));
        StreetAddressGenerator parts = new StreetAddressGenerator(config(tag));
        assertTrue(germanLayout.streetFirst(), tag);
        for (int i = 0; i < 200; i++) {
            boolean shortSuffix = i % 2 == 0;
            String number = parts.generateStreetAddressNumber();
            String name = parts.generateStreetName();
            String suffix = parts.generateStreetSuffix(shortSuffix);
            String line = germanLayout.generate(shortSuffix);
            assertEquals(german(name, suffix) + " " + number, line, tag);
            assertTrue(GERMAN_LINE.matcher(line).matches(), line);
        }
        String germanFull = germanLayout.generateFullAddress();
        assertTrue(
                GERMAN_LINE
                        .matcher(germanFull.substring(0, germanFull.indexOf(',')))
                        .matches(),
                germanFull);

        AddressInfoGenerator germanInfo = new AddressInfoGenerator(config(tag));
        for (int i = 0; i < 50; i++) {
            AddressInfo de = germanInfo.generate();
            String street = german(de.streetName(), de.streetSuffix()) + " " + de.streetNumber();
            assertEquals(street, de.street());
            assertTrue(de.address().startsWith(street + ", "), de.address());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"nl-NL", "nl-BE", "en-US", "en-DE", "fr-CH", "it-IT", "sv-SE"})
    @DisplayName("every other locale writes the house number first")
    void otherLocalesWriteTheNumberFirst(String tag) {
        StreetAddressGenerator streets = new StreetAddressGenerator(config(tag));
        StreetAddressGenerator parts = new StreetAddressGenerator(config(tag));
        assertFalse(streets.streetFirst(), tag);
        for (int i = 0; i < 20; i++) {
            String number = parts.generateStreetAddressNumber();
            String name = parts.generateStreetName();
            String suffix = parts.generateStreetSuffix(true);
            assertEquals(number + " " + name + " " + suffix, streets.generate(), tag);
        }
        AddressInfoGenerator infos = new AddressInfoGenerator(config(tag));
        for (int i = 0; i < 20; i++) {
            AddressInfo info = infos.generate();
            assertTrue(info.street().startsWith(info.streetNumber() + " " + info.streetName() + " "), info.toString());
        }
    }

    @Test
    @DisplayName("the session, the schema columns and the object engine's address record follow it")
    void sessionAndSchemaFollowIt() {
        GeneratorConfig config = config("de-DE");
        assertTrue(GERMAN_LINE
                .matcher(RandomDataGenerator.of(config).streetAddress())
                .matches());

        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("street", field.bind("address.street_address"));
        fields.put("info", field.bind("address.address_info"));
        for (Map<String, Object> row : new Schema(config, fields).nextRecords(20)) {
            assertTrue(GERMAN_LINE.matcher((String) row.get("street")).matches(), row.toString());
            assertTrue(
                    GERMAN_LINE
                            .matcher(((AddressInfo) row.get("info")).street())
                            .matches(),
                    row.toString());
        }
    }

    @Test
    @DisplayName("a recipe replays it")
    void recipeReplaysIt() {
        GeneratorConfig config = config("de-DE");
        GeneratorConfig replay = GenerationRecipe.parse(
                        config.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        StreetAddressGenerator original = new StreetAddressGenerator(config);
        StreetAddressGenerator replayed = new StreetAddressGenerator(replay);
        for (int i = 0; i < 20; i++) {
            assertEquals(original.generate(), replayed.generate());
        }
    }

    @Test
    @DisplayName("a child stream keeps it")
    void childStreamKeepsIt() {
        assertTrue(new StreetAddressGenerator(config("de-DE").forChildStream("street")).streetFirst());
        assertTrue(new StreetAddressGenerator(config("de-DE").toBuilder().build()).streetFirst());
    }

    @Test
    @DisplayName("joins a registered German name and type as one word")
    void joinsRegisteredGermanData() {
        StreetAddressDataProvider streets = new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return Locale.GERMANY;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] {"Linden"};
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] {"Al."};
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] {"Allee"};
            }
        };
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .registryContext(DataRegistryContext.builder()
                        .isolated()
                        .register(Vocabulary.STREET_ADDRESS, streets)
                        .build())
                .seed(5L)
                .build();
        StreetAddressGenerator generator = new StreetAddressGenerator(config);

        assertTrue(generator.generate(false).matches("Lindenallee [1-9][0-9]{0,3}"));
        assertTrue(generator.generate(true).matches("Lindenal\\. [1-9][0-9]{0,3}"));
    }

    /** The street of a German line: the name and its type as one word, the type starting in lower case. */
    private static String german(String name, String type) {
        return name + type.substring(0, 1).toLowerCase(Locale.ROOT) + type.substring(1);
    }
}
