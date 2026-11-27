/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.base.ByteGenerator;
import io.github.frikit.rdg.generator.base.NormalDistributionGenerator;
import io.github.frikit.rdg.generator.finance.CurrencyGenerator;
import io.github.frikit.rdg.generator.finance.MoneyGenerator;
import io.github.frikit.rdg.generator.identifier.UUIDGenerator;
import io.github.frikit.rdg.generator.location.AddressInfo;
import io.github.frikit.rdg.generator.location.AddressInfoGenerator;
import io.github.frikit.rdg.generator.location.StreetAddressGenerator;
import io.github.frikit.rdg.generator.object.ObjectGenerator;
import io.github.frikit.rdg.generator.object.Randomizer;
import io.github.frikit.rdg.generator.schema.Field;
import io.github.frikit.rdg.generator.schema.Schema;
import io.github.frikit.rdg.generator.schema.SchemaValueProvider;
import io.github.frikit.rdg.generator.user.EmailGenerator;
import io.github.frikit.rdg.generator.user.FirstNameGenerator;
import io.github.frikit.rdg.generator.user.FullNameGenerator;
import io.github.frikit.rdg.generator.user.Gender;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded values of the convenience methods that advance their child stream, the street-first layout of the
 * German locales, reseeded built-in randomizers and {@link StrictMath} Gaussian draws, through generators, the
 * session, a schema and object generation.
 *
 * <p>Values that follow the Java runtime are checked against an oracle rather than pinned: a price tag's amount
 * is formatted with the runtime's locale data.
 *
 * <p>Object generation derives a member's stream from its owner's class name, so the {@code object},
 * {@code randomizers} and {@code built-in randomizers} lines follow the names of {@link Resident} and
 * {@link WithRandomizers}, nested in this class. Renaming this class changes those values.
 */
@DisplayName("seeded child streams, street layout, randomizers and Gaussian draws keep their values")
class DefaultFlipPinsTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    static final List<String> LOCALES = List.of("de", "de-DE", "de-AT", "de-CH", "nl-NL", "en-US");

    static final String VALUES = """
            company emails [aharris@advancedassociatesco.test, juliagarcia@advancedresourcesltd.test, edavis@alpharesourcescorp.test]
            middle names [Caroline Hazel Tran, Steven Audrey Mitchell, Olivia Amanda Taylor, Carter L. Green, Stephanie J. Reyes, Hunter P. Bell]
            session mnelson@example.org | Walter Ross
            de street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland
            de info Tulpenpfad 8680 | Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de session Beethovenring 7072
            de object Ayse, Domal. 855, Parkbr. 9051 | Tim, Beethoveng. 4164, Bismarckpl. 2993
            de-DE street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland
            de-DE info Tulpenpfad 8680 | Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de-DE schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-DE session Beethovenring 7072
            de-DE object Ayse, Mozartwall 7448, Parkbr. 9051 | Tim, Waldaue 2922, Bismarckpl. 2993
            de-AT street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Rostock, Baden-Württemberg 9342, Österreich
            de-AT info Tulpenpfad 8680 | Tulpenpfad 8680, Oldenburg, Rheinland-Pfalz 9342, Österreich
            de-AT schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-AT session Beethovenring 7072
            de-AT object Ayse, Mozartwall 7448, Parkbr. 9051 | Tim, Waldaue 2922, Bismarckpl. 2993
            de-CH street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Rostock, Baden-Württemberg 9342, Schweiz
            de-CH info Tulpenpfad 8680 | Tulpenpfad 8680, Oldenburg, Rheinland-Pfalz 9342, Schweiz
            de-CH schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-CH session Beethovenring 7072
            de-CH object Ayse, Mozartwall 7448, Parkbr. 9051 | Tim, Waldaue 2922, Bismarckpl. 2993
            nl-NL street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Reusel, GR 8649 ST, Nederland
            nl-NL info 8680 Boslaan bn. | 8680 Boslaan bn., Helmond, DR 8649 ST, Nederland
            nl-NL schema [{street=3876 Wilhelminastraat gr., email=mariusveenstra@example.net}, {street=1951 Stationsstraat wg., email=charlotte_vanschaik@example.com}]
            nl-NL session 7072 Hoofdstraat wg.
            nl-NL object Merel, 7448 Julianastraat pad, 9051 Stationsstraat ln. | Ivo, 2922 Wilhelminastraat erf, 2993 Waterstraat sgl.
            en-US street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Renton, WA 98056, United States
            en-US info 8680 Lakeview Holw | 8680 Lakeview Holw, Fontana, CA 92335, United States
            en-US schema [{street=3876 Elm Knl, email=landonhill@example.net}, {street=1951 Cherry Mnr, email=savannah_edwards@example.com}]
            en-US session 7072 Fairview Pt
            en-US object Piper, 7448 Washington Anx, 9051 Cherry Jct | Charles, 2922 Elm Hl, 2993 Lincoln Blvd
            randomizers -126 1320854254 QSPIiiDvTKHfe
            randomizers -42 -1375916062 xMRrSmzG
            randomizers -56 -946313064 ExwAN
            built-in randomizers [a974fd65-841d-4fb8-8acc-667159bc1446 Easton 382 Park Jct, 9bd35bf1-b02d-4679-8575-2f54fa87f018 Serenity 7862 Franklin Fwy, df6740a1-5bca-4f3e-8560-2e904e4907cf Ivy 3208 Lincoln Ests]
            gaussian 10 [-0.0388436147345816, 0.7916654062154486, 0.08786661673846564, 2.3761061677475346] [99.41734577898127, 111.87498109323172]
            gaussian 14 [0.7299233329370594, -0.31054186561011643, -1.0265431925416346, 1.0495360551026751] [110.94884999405589, 95.34187201584825]
            gaussian 42 [-0.3249011602434719, -0.7283863934921454, -0.25959749624431383, 1.5110439411230536] [95.12648259634793, 89.07420409761782]
            """;

    @Test
    @DisplayName("a seeded configuration gives the pinned values")
    void pinnedValues() {
        assertEquals(VALUES, values(UnaryOperator.identity()));
    }

    @Test
    @DisplayName("a configuration replayed from its recipe gives the pinned values")
    void replayedValues() {
        assertEquals(
                VALUES,
                values(config -> GenerationRecipe.parse(
                                config.getGenerationRecipe().orElseThrow().serialize())
                        .toGeneratorConfig()));
    }

    @Test
    @DisplayName("each price tag is the next amount of the money stream")
    void priceTags() {
        for (long seed = 0; seed < 50; seed++) {
            GeneratorConfig config = seeded(seed, "en-US", UnaryOperator.identity());
            CurrencyGenerator tags = new CurrencyGenerator(config);
            MoneyGenerator money = new MoneyGenerator(config.forChildStream("money"));
            for (int i = 0; i < 4; i++) {
                assertEquals(money.generate(), tags.generatePriceTag(), "seed " + seed);
            }
        }
    }

    @Test
    @DisplayName("Gaussian draws use StrictMath")
    void gaussianDraws() {
        for (long seed = 0; seed < 200; seed++) {
            assertEquals(
                    boxMuller(seed),
                    new NormalDistributionGenerator(seeded(seed, "en-US", UnaryOperator.identity())).generateList(6),
                    "seed " + seed);
        }
    }

    static GeneratorConfig seeded(long seed, String tag, UnaryOperator<GeneratorConfig> settings) {
        return settings.apply(GeneratorConfig.builder()
                .seed(seed)
                .locale(Locale.forLanguageTag(tag))
                .clock(CLOCK)
                .build());
    }

    /**
     * The pinned values of configurations that {@code settings} makes from seeded ones, one line for each group.
     * The built-in randomizers replay, and the Gaussian values are the same on every platform, so both are pinned.
     */
    static String values(UnaryOperator<GeneratorConfig> settings) {
        List<String> out = new ArrayList<>();
        GeneratorConfig us = seeded(42L, "en-US", settings);
        EmailGenerator emails = new EmailGenerator(us);
        List<String> companyEmails = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            companyEmails.add(emails.generateCompanyEmail());
        }
        out.add("company emails " + companyEmails);
        FullNameGenerator names = new FullNameGenerator(us);
        out.add("middle names "
                + List.of(
                        names.generateWithMiddleName(),
                        names.generateWithMiddleName(),
                        names.generateWithMiddleName(Gender.FEMALE),
                        names.generateWithMiddleInitial(),
                        names.generateWithMiddleInitial(),
                        names.generateWithMiddleInitial(Gender.MALE)));
        GeneratorConfig session = seeded(42L, "en-US", settings);
        RandomDataGenerator generator = RandomDataGenerator.of(session);
        out.add("session " + generator.email() + " | " + generator.fullName());

        for (String tag : LOCALES) {
            GeneratorConfig config = seeded(42L, tag, settings);
            StreetAddressGenerator streets = new StreetAddressGenerator(config);
            out.add(tag + " street " + streets.generate() + " | " + streets.generate(false) + " | "
                    + streets.generateFullAddress());
            AddressInfo info = new AddressInfoGenerator(config).generate();
            out.add(tag + " info " + info.street() + " | " + info.address());
            Field field = new Field(config);
            Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
            fields.put("street", field.bind("address.street_address"));
            fields.put("email", field.bind("person.email"));
            out.add(tag + " schema " + new Schema(config, fields).nextRecords(2));
            out.add(tag + " session " + RandomDataGenerator.of(config).streetAddress());
            ObjectGenerator<Resident> residents = new ObjectGenerator<>(Resident.class, config);
            out.add(tag + " object " + residents.generate() + " | " + residents.generate());
        }

        ObjectGenerator<WithRandomizers> objects = new ObjectGenerator<>(WithRandomizers.class, us);
        for (int i = 0; i < 3; i++) {
            WithRandomizers value = objects.generate();
            out.add("randomizers " + value.seedable + " " + value.plain + " " + value.text);
        }
        out.add("built-in randomizers " + builtIns(settings));
        for (long seed : List.of(10L, 14L, 42L)) {
            out.add("gaussian " + seed + " "
                    + new NormalDistributionGenerator(seeded(seed, "en-US", settings)).generateList(4) + " "
                    + new NormalDistributionGenerator(100.0, 15.0, seeded(seed, "en-US", settings)).generateList(2));
        }
        return String.join("\n", out) + "\n";
    }

    private static List<String> builtIns(UnaryOperator<GeneratorConfig> settings) {
        ObjectGenerator<WithRandomizers> objects =
                new ObjectGenerator<>(WithRandomizers.class, seeded(42L, "en-US", settings));
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WithRandomizers value = objects.generate();
            values.add(value.uuid + " " + value.first + " " + value.street);
        }
        return values;
    }

    /** Box-Muller over the seed's own {@link Random}, with {@link StrictMath}. */
    private static List<Double> boxMuller(long seed) {
        Random random = new Random(seed);
        List<Double> values = new ArrayList<>();
        while (values.size() < 6) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            while (u1 == 0.0) {
                u1 = random.nextDouble();
            }
            double angle = 2.0 * Math.PI * u2;
            double magnitude = StrictMath.sqrt(-2.0 * StrictMath.log(u1));
            values.add(0.0 + magnitude * StrictMath.cos(angle) * 1.0);
            values.add(0.0 + magnitude * StrictMath.sin(angle) * 1.0);
        }
        return values;
    }

    public static class Resident {
        public String firstName;
        public String street;
        public String streetAddress;

        @Override
        public String toString() {
            return firstName + ", " + street + ", " + streetAddress;
        }
    }

    public static class WithRandomizers {
        @Randomizer(ByteGenerator.class)
        public byte seedable;

        @Randomizer(UUIDGenerator.class)
        public UUID uuid;

        @Randomizer(FirstNameGenerator.class)
        public String first;

        @Randomizer(StreetAddressGenerator.class)
        public String street;

        public int plain;
        public String text;
    }
}
