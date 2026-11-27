/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.commerce.CommerceGenerator;
import io.github.frikit.rdg.generator.commerce.OrderInfoGenerator;
import io.github.frikit.rdg.generator.commerce.ProductInfoGenerator;
import io.github.frikit.rdg.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.rdg.generator.datetime.OffsetDateTimeGenerator;
import io.github.frikit.rdg.generator.file.FilePathGenerator;
import io.github.frikit.rdg.generator.finance.BankInfoGenerator;
import io.github.frikit.rdg.generator.finance.BankingSafetyPolicy;
import io.github.frikit.rdg.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.rdg.generator.finance.CurrencyGenerator;
import io.github.frikit.rdg.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.rdg.generator.finance.PaymentInfo;
import io.github.frikit.rdg.generator.finance.PaymentInfoGenerator;
import io.github.frikit.rdg.generator.identifier.IsbnGenerator;
import io.github.frikit.rdg.generator.location.AddressInfoGenerator;
import io.github.frikit.rdg.generator.location.StreetAddressGenerator;
import io.github.frikit.rdg.generator.network.HostnameGenerator;
import io.github.frikit.rdg.generator.network.NetworkSafetyPolicy;
import io.github.frikit.rdg.generator.network.URLGenerator;
import io.github.frikit.rdg.generator.network.UriGenerator;
import io.github.frikit.rdg.generator.text.ParagraphGenerator;
import io.github.frikit.rdg.generator.text.SentenceGenerator;
import io.github.frikit.rdg.generator.text.WordGenerator;
import io.github.frikit.rdg.generator.user.AvatarUrlGenerator;
import io.github.frikit.rdg.generator.user.CompanyEmailGenerator;
import io.github.frikit.rdg.generator.user.CompanyInfoGenerator;
import io.github.frikit.rdg.generator.user.ContactInfoGenerator;
import io.github.frikit.rdg.generator.user.EmailGenerator;
import io.github.frikit.rdg.generator.user.FullNameGenerator;
import io.github.frikit.rdg.generator.user.JobInfoGenerator;
import io.github.frikit.rdg.generator.user.MiddleNameGenerator;
import io.github.frikit.rdg.generator.user.PersonInfoGenerator;
import io.github.frikit.rdg.generator.user.ProfileGenerator;
import io.github.frikit.rdg.generator.user.SimpleProfileGenerator;
import io.github.frikit.rdg.generator.user.SocialProfileGenerator;
import io.github.frikit.rdg.generator.user.UsernameGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Composite child streams")
class ChildStreamsTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    /**
     * Fingerprints of the first three values of every composite generator, whose children draw from
     * named child streams, including the convenience methods that keep and advance a child stream
     * ({@code generateCompanyEmail}, the middle-name methods and {@code generatePriceTag}).
     */
    private static final Map<String, String> FINGERPRINTS = Map.ofEntries(
            Map.entry("sentence", "ef455c60ad7bd1c55e42c413"),
            Map.entry("word", "a947c592410a2cd4721ce768"),
            Map.entry("paragraph", "729efacb53f219cd2e44928b"),
            Map.entry("offsetDateTime", "c6bf5ecd053c16335541ea34"),
            Map.entry("url", "945da45b66cfe8ec36823c70"),
            Map.entry("uri", "d6f3ceb244a25781a90ab489"),
            Map.entry("hostname", "140af7f858e34fa1674516f8"),
            Map.entry("companyEmail", "298d21c091a492eda9706aa6"),
            Map.entry("fullName", "f50583b2b93d58ed6df426f5"),
            Map.entry("avatarUrl", "2821714eeab54a71be97163d"),
            Map.entry("username", "400f9408368e35a7aecf65a7"),
            Map.entry("socialProfile", "10d757591832cff3040a753b"),
            Map.entry("middleName", "9ea3d7c9b397f2429bd6499b"),
            Map.entry("addressInfo", "3faf7240ef852e9c91f27722"),
            Map.entry("commerce", "17ce302ae4e7a1fb4764c2b5"),
            Map.entry("email", "26b800511dedab89fbf82b61"),
            Map.entry("orderInfo", "cb61334ddb9aff365cecb314"),
            Map.entry("contactInfo", "045743f457119e34ef0a942d"),
            Map.entry("streetAddress", "1ec7f4fed1d423cc422b036d"),
            Map.entry("personInfo", "00e3fbd744e7620c2711890e"),
            Map.entry("shipmentInfo", "c2aa678dae6f755a0318d71b"),
            Map.entry("profile", "4789d71337858e628b1c3e07"),
            Map.entry("filePath", "27bbf3aa053cd6477449445a"),
            Map.entry("productInfo", "f9cd8efdbe56839b0167edda"),
            Map.entry("jobInfo", "8e65091f58b496238fafc2a5"),
            Map.entry("invoiceInfo", "ba1bd7a22e9b55cc1ef8f19f"),
            Map.entry("companyInfo", "4a8348e0011ca4dd46fa9b87"),
            Map.entry("simpleProfile", "bbe06d5e09be6535ea4af6ed"),
            Map.entry("bankInfo", "79da71fb019e2d16c55c379a"),
            Map.entry("paymentInfo", "03aeb1462e48eb61df9ea5fe"),
            Map.entry("creditCardInfo", "c9a336712de7a0bbbabae13a"),
            Map.entry("fullNameWithMiddle", "dc81d22e69c6347ef888b1a8"),
            Map.entry("fullNameWithInitial", "f30b2286d04ef39834028be2"),
            Map.entry("emailCompany", "6ceac5f8372f721ffa9605ba"),
            Map.entry("priceTag", "2616c2999ecdf06951a2d7d5"));

    @Test
    @DisplayName("composites keep their seeded output")
    void compositesKeepTheirSeededOutput() {
        assertEquals(FINGERPRINTS, fingerprints(baseConfig().build()));
    }

    @Test
    @DisplayName("child streams decorrelate siblings and stay reproducible")
    void childrenDrawFromIndependentStreams() {
        GeneratorConfig config = baseConfig().build();
        PaymentInfo payment = new PaymentInfoGenerator(config).generate();

        assertEquals("PAY-7DC1F9C37F29", payment.paymentNumber());
        assertEquals("INV-F852387C7D39", payment.invoiceNumber());
        assertEquals("ORD-B9D4F25FCB19", payment.orderNumber());
        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
        assertNotEquals(suffix(payment.invoiceNumber()), suffix(payment.orderNumber()));
        assertEquals(payment, new PaymentInfoGenerator(config).generate());
    }

    @Test
    @DisplayName("ISBN generators and nationality names use their own named streams")
    void isbnAndNationalityNamesUseNamedStreams() {
        GeneratorConfig independent = baseConfig().build();
        CommerceGenerator commerce = new CommerceGenerator(independent);

        assertEquals(
                new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, independent.forChildStream("isbn10")).generate(),
                commerce.generateIsbn10());
        assertEquals(
                new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, independent.forChildStream("isbn13")).generate(),
                commerce.generateIsbn13());

        FullNameGenerator.NameOptions italian =
                new FullNameGenerator.NameOptions(false, false, false, false, false, null, "it");
        assertEquals(nationalityNames(independent, italian), nationalityNames(independent, italian));
    }

    private static java.util.List<String> nationalityNames(
            GeneratorConfig config, FullNameGenerator.NameOptions options) {
        FullNameGenerator generator = new FullNameGenerator(config);
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            names.add(generator.generate(options));
        }
        return names;
    }

    @Test
    @DisplayName("seeded child configurations derive named seeds")
    void childConfigurationsDeriveNamedSeeds() {
        GeneratorConfig independent =
                GeneratorConfig.builder().seed("child-streams").build();

        GeneratorConfig invoice = independent.forChildStream("invoice");
        assertEquals(
                GenerationRecipe.deriveChildSeed(independent.getSeed().getAsLong(), "invoice"),
                invoice.getSeed().getAsLong());
        assertTrue(invoice.getStringSeed().isEmpty());
        assertEquals(independent.getLocale(), invoice.getLocale());
        assertNotEquals(invoice.getSeed(), independent.forChildStream("order").getSeed());
        assertThrows(IllegalArgumentException.class, () -> independent.forChildStream(" "));
        assertThrows(NullPointerException.class, () -> independent.forChildStream(null));
    }

    static Stream<Arguments> unseededConfigurations() {
        return Stream.of(
                Arguments.of(Named.of("default source", GeneratorConfig.defaults())),
                Arguments.of(Named.of(
                        "secure random",
                        GeneratorConfig.builder().secureRandom().build())),
                Arguments.of(Named.of(
                        "caller-owned random",
                        GeneratorConfig.builder().random(new Random(7L)).build())),
                Arguments.of(Named.of(
                        "random factory",
                        GeneratorConfig.builder().randomFactory(Random::new).build())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unseededConfigurations")
    @DisplayName("an unseeded configuration passes itself to every child")
    void unseededConfigurationsPassThemselvesToChildren(GeneratorConfig config) {
        assertTrue(config.getSeed().isEmpty());
        assertSame(config, config.forChildStream("invoice"));
        for (String name : new String[] {" ", "two\nlines", "two\rlines"}) {
            IllegalArgumentException error =
                    assertThrows(IllegalArgumentException.class, () -> config.forChildStream(name));
            assertEquals("streamIdentity must be a non-blank single-line value", error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> config.forChildStream(null));
    }

    @Test
    @DisplayName("unseeded children draw from their own sources, and caller-owned randomness is never reseeded")
    void unseededChildrenDrawFromTheirOwnSources() {
        Random shared = new Random(7L);
        GeneratorConfig owned = GeneratorConfig.builder().random(shared).build();
        for (GeneratorConfig config : List.of(
                GeneratorConfig.defaults(),
                GeneratorConfig.builder().secureRandom().build(),
                owned)) {
            PaymentInfo payment = new PaymentInfoGenerator(config).generate();

            assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
            assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
            assertNotEquals(suffix(payment.invoiceNumber()), suffix(payment.orderNumber()));
        }
        assertSame(shared, owned.forChildStream("invoice").createRandom());

        AtomicInteger calls = new AtomicInteger();
        GeneratorConfig repeating = GeneratorConfig.builder()
                .randomFactory(() -> {
                    calls.incrementAndGet();
                    return new Random(7L);
                })
                .build();
        repeating.forChildStream("invoice");
        assertEquals(0, calls.get(), "forChildStream must not call the factory");
        PaymentInfo payment = new PaymentInfoGenerator(repeating).generate();
        assertTrue(calls.get() > 1, "every child calls the factory");
        // A factory that repeats one seed repeats it in every child: its sources are used as they are.
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
    }

    @Test
    @DisplayName("a recipe replays the composites' values")
    void recipesReplayTheValues() {
        GeneratorConfig config = baseConfig().build();
        GeneratorConfig replayed = GenerationRecipe.parse(
                        config.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();

        assertEquals(fingerprints(config), fingerprints(replayed));
    }

    private static GeneratorConfig.Builder baseConfig() {
        return GeneratorConfig.builder()
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .seed(42L)
                .clock(FIXED_CLOCK)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    private static Map<String, String> fingerprints(GeneratorConfig config) {
        Map<String, Function<GeneratorConfig, Supplier<?>>> composites = new LinkedHashMap<>();
        composites.put("sentence", c -> new SentenceGenerator(c)::generate);
        composites.put("word", c -> new WordGenerator(c)::generate);
        composites.put("paragraph", c -> new ParagraphGenerator(c)::generate);
        composites.put("offsetDateTime", c -> new OffsetDateTimeGenerator(c)::generate);
        composites.put("url", c -> new URLGenerator(c)::generate);
        composites.put("uri", c -> new UriGenerator(c)::generate);
        composites.put("hostname", c -> new HostnameGenerator(c)::generate);
        composites.put("companyEmail", c -> new CompanyEmailGenerator(c)::generate);
        composites.put("fullName", c -> new FullNameGenerator(c)::generate);
        composites.put("avatarUrl", c -> new AvatarUrlGenerator(c)::generate);
        composites.put("username", c -> new UsernameGenerator(c)::generate);
        composites.put("socialProfile", c -> new SocialProfileGenerator(c)::generate);
        composites.put("middleName", c -> new MiddleNameGenerator(c)::generate);
        composites.put("addressInfo", c -> new AddressInfoGenerator(c)::generate);
        composites.put("commerce", c -> new CommerceGenerator(c)::generate);
        composites.put("email", c -> new EmailGenerator(c)::generate);
        composites.put("orderInfo", c -> new OrderInfoGenerator(c)::generate);
        composites.put("contactInfo", c -> new ContactInfoGenerator(c)::generate);
        composites.put("streetAddress", c -> new StreetAddressGenerator(c)::generate);
        composites.put("personInfo", c -> new PersonInfoGenerator(c)::generate);
        composites.put("shipmentInfo", c -> new ShipmentInfoGenerator(c)::generate);
        composites.put("profile", c -> new ProfileGenerator(c)::generate);
        composites.put("filePath", c -> new FilePathGenerator(c)::generate);
        composites.put("productInfo", c -> new ProductInfoGenerator(c)::generate);
        composites.put("jobInfo", c -> new JobInfoGenerator(c)::generate);
        composites.put("invoiceInfo", c -> new InvoiceInfoGenerator(c)::generate);
        composites.put("companyInfo", c -> new CompanyInfoGenerator(c)::generate);
        composites.put("simpleProfile", c -> new SimpleProfileGenerator(c)::generate);
        composites.put("bankInfo", c -> new BankInfoGenerator(c)::generate);
        composites.put("paymentInfo", c -> new PaymentInfoGenerator(c)::generate);
        composites.put("creditCardInfo", c -> new CreditCardInfoGenerator(c)::generate);
        composites.put("fullNameWithMiddle", c -> new FullNameGenerator(c)::generateWithMiddleName);
        composites.put("fullNameWithInitial", c -> new FullNameGenerator(c)::generateWithMiddleInitial);
        composites.put("emailCompany", c -> new EmailGenerator(c)::generateCompanyEmail);
        composites.put("priceTag", c -> new CurrencyGenerator(c)::generatePriceTag);

        Map<String, String> result = new LinkedHashMap<>();
        composites.forEach((name, factory) -> {
            Supplier<?> supplier = factory.apply(config);
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                values.append(supplier.get()).append('\n');
            }
            result.put(name, sha256(values.toString()));
        });
        return result;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String suffix(String identifier) {
        return identifier.substring(identifier.indexOf('-') + 1);
    }
}
