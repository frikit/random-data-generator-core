/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.RandomDataGenerator;
import io.github.frikit.rdg.generator.schema.Field;
import io.github.frikit.rdg.generator.schema.Schema;
import io.github.frikit.rdg.generator.schema.SchemaValueProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded street addresses, address records, the address schema columns and the session's place methods of the
 * Dutch and US locales, and the recipe text of a seeded configuration. The German locales' street-first values
 * are pinned in {@code DefaultFlipPinsTest} and {@code GermanStreetLayoutTest}.
 */
@DisplayName("seeded addresses and recipes keep their values")
class StreetAddressPinsTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    static final List<String> LOCALES = List.of("nl-NL", "en-US");

    static final String PINNED_VALUES = """
                nl-NL street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Reusel, GR 8649 ST, Nederland
                nl-NL info 8680 Boslaan bn. | 8680 Boslaan bn., Helmond, DR 8649 ST, Nederland
                nl-NL schema [{street=3876 Wilhelminastraat gr., info=AddressInfo[address=6924 Waterstraat str., Harderwijk, LI 4023 XQ, Nederland, street=6924 Waterstraat str., streetNumber=6924, streetName=Waterstraat, streetSuffix=str., streetPrefix=, streetUnit=, city=Harderwijk, state=Limburg, stateAbbr=LI, zip=4023 XQ, country=Nederland, countryAbbr=NL]}, {street=1951 Stationsstraat wg., info=AddressInfo[address=770 Lindelaan erf, Veghel, GE 9328 XQ, Nederland, street=770 Lindelaan erf, streetNumber=770, streetName=Lindelaan, streetSuffix=erf, streetPrefix=, streetUnit=, city=Veghel, state=Gelderland, stateAbbr=GE, zip=9328 XQ, country=Nederland, countryAbbr=NL]}]
                nl-NL session 6848 Parallelweg pad | Vierakker | 4692 EG
                en-US street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Renton, WA 98056, United States
                en-US info 8680 Lakeview Holw | 8680 Lakeview Holw, Fontana, CA 92335, United States
                en-US schema [{street=3876 Elm Knl, info=AddressInfo[address=6924 Lincoln Ct, Jersey City, NJ 07310, United States, street=6924 Lincoln Ct, streetNumber=6924, streetName=Lincoln, streetSuffix=Ct, streetPrefix=, streetUnit=, city=Jersey City, state=New Jersey, stateAbbr=NJ, zip=07310, country=United States, countryAbbr=US]}, {street=1951 Cherry Mnr, info=AddressInfo[address=770 Liberty Lndg, Burlington, VT 05401, United States, street=770 Liberty Lndg, streetNumber=770, streetName=Liberty, streetSuffix=Lndg, streetPrefix=, streetUnit=, city=Burlington, state=Vermont, stateAbbr=VT, zip=05401, country=United States, countryAbbr=US]}]
                en-US session 6848 Lincoln Park | Elk Grove | 57692
                """;

    @Test
    @DisplayName("street addresses, address records, schema columns and session places keep their values")
    void seededAddressValuesArePinned() {
        assertEquals(PINNED_VALUES, values(LOCALES));
    }

    @Test
    @DisplayName("the recipe of a seeded configuration keeps its text")
    void defaultRecipeTextIsPinned() {
        assertEquals(
                """
                format=rdg-recipe
                recipe-version=v2
                library-version=development
                algorithm=java.util.Random-v1
                seed=42
                locale=de-DE
                clock-instant=2026-07-10T12%3A00%3A00Z
                clock-zone=Z
                profile=custom
                construction-policy=safe-constructors
                provider-dataset-version=builtin-v1
                setting.banking.safety-policy=DISABLED
                setting.business-tax-identifier.safety-policy=DISABLED
                setting.charset=US-ASCII
                setting.collection.max=10
                setting.collection.min=1
                setting.crypto-address.safety-policy=DISABLED
                setting.email.domain-policy=TEST_SAFE_RESERVED_DOMAINS
                setting.identity-document.safety-policy=DISABLED
                setting.national-id.safety-policy=DISABLED
                setting.network.safety-policy=TEST_SAFE_RESERVED
                setting.object.ignore-errors=false
                setting.object.max-depth=5
                setting.object.null-probability=0.0
                setting.object.optional-empty-probability=0.0
                setting.object.override-default-initialization=false
                setting.object.pool-size=10
                setting.object.requested-unique-fields=-
                setting.object.semantic-mode=RELAXED
                setting.object.unique-fields=email%2Cemailaddress%2Cusername%2Cuserhandle%2Cuuid%2Cguid%2Cid
                setting.object.uniqueness-max-attempts=256
                setting.payment.card-safety-policy=TEST_SAFE_NON_ROUTABLE
                setting.phone-number.safety-policy=TEST_SAFE_WHERE_AVAILABLE
                setting.securities-identifier.safety-policy=DISABLED
                setting.string.max=20
                setting.string.min=5
                """,
                GeneratorConfig.builder()
                        .locale(Locale.GERMANY)
                        .seed(42L)
                        .clock(CLOCK)
                        .build()
                        .getGenerationRecipe()
                        .orElseThrow()
                        .serialize());
    }

    /** The pinned values of the given locales, one line for each value group. */
    static String values(List<String> tags) {
        List<String> out = new ArrayList<>();
        for (String tag : tags) {
            Locale locale = Locale.forLanguageTag(tag);
            GeneratorConfig config = GeneratorConfig.builder()
                    .locale(locale)
                    .seed(42L)
                    .clock(CLOCK)
                    .build();
            StreetAddressGenerator streets = new StreetAddressGenerator(config);
            out.add(tag + " street " + streets.generate() + " | " + streets.generate(false) + " | "
                    + streets.generateFullAddress());
            AddressInfo info = new AddressInfoGenerator(config).generate();
            out.add(tag + " info " + info.street() + " | " + info.address());
            Field field = new Field(config);
            Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
            fields.put("street", field.bind("address.street_address"));
            fields.put("info", field.bind("address.address_info"));
            out.add(tag + " schema " + new Schema(config, fields).nextRecords(2));
            RandomDataGenerator fake = RandomDataGenerator.of(
                    GeneratorConfig.builder().seed("pins").locale(locale).build());
            out.add(tag + " session " + fake.streetAddress() + " | " + fake.city() + " | " + fake.postalCode());
        }
        return String.join("\n", out) + "\n";
    }
}
