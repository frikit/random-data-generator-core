/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.GeneratorConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidGeneratorTest {

    @Test
    void testDefaultConstructor() {
        UuidGenerator gen = new UuidGenerator();
        assertNotNull(gen);
    }

    @Test
    void testGenerateV4() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV4();

        assertNotNull(uuid);
        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerate() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generate();

        assertNotNull(uuid);
        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateString() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateString();

        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV4String() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateV4String();

        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV7() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV7();
        assertNotNull(uuid);
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV7UsesConfiguredClock() {
        Instant instant = Instant.ofEpochMilli(0x0123456789ABL);
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .clock(Clock.fixed(instant, ZoneOffset.UTC))
                .build();

        UUID uuid = new UuidGenerator(config).generateV7();

        assertEquals(instant.toEpochMilli(), uuid.getMostSignificantBits() >>> 16);
    }

    @Test
    void testGenerateV7ConsumesRandomBytes() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
                .build();
        UuidGenerator generator = new UuidGenerator(config);

        assertNotEquals(generator.generateV7(), generator.generateV7());
    }

    @Test
    void testGenerateV7String() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateV7String();
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5WithName() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5("example.com");

        assertNotNull(uuid);
        assertEquals(5, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV5String() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateV5String("example.com");

        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-5[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5WithNamespace() {
        UuidGenerator gen = new UuidGenerator();
        UUID namespace = UuidGenerator.getDnsNamespace();
        UUID uuid = gen.generateV5(namespace, "example.com");

        assertNotNull(uuid);
        assertEquals(5, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV5StringWithNamespace() {
        UuidGenerator gen = new UuidGenerator();
        UUID namespace = UuidGenerator.getUrlNamespace();
        String uuidStr = gen.generateV5String(namespace, "https://example.com");

        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-5[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5IsDeterministic() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid1 = gen.generateV5("example.com");
        UUID uuid2 = gen.generateV5("example.com");

        assertEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateV5DifferentNames() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid1 = gen.generateV5("example.com");
        UUID uuid2 = gen.generateV5("different.com");

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateV5DifferentNamespaces() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid1 = gen.generateV5(UuidGenerator.getDnsNamespace(), "example.com");
        UUID uuid2 = gen.generateV5(UuidGenerator.getUrlNamespace(), "example.com");

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testSeededV4Generation() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        UuidGenerator generator = new UuidGenerator(config);

        UUID uuid = generator.generateV4();

        assertEquals(UUID.fromString("d6209f5c-31b3-4183-a2a9-d8ee7807c8ea"), uuid);
    }

    @Test
    void testSeededGenerate() {
        GeneratorConfig config = GeneratorConfig.builder().seed(67890L).build();
        UuidGenerator gen1 = new UuidGenerator(config);
        UuidGenerator gen2 = new UuidGenerator(config);

        UUID uuid1 = gen1.generate();
        UUID uuid2 = gen2.generate();

        assertEquals(uuid1, uuid2);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new UuidGenerator(null));
    }

    @Test
    void testGenerateV5NullNameThrowsException() {
        UuidGenerator gen = new UuidGenerator();
        assertThrows(NullPointerException.class, () -> gen.generateV5((String) null));
    }

    @Test
    void testGenerateV5NullNamespaceThrowsException() {
        UuidGenerator gen = new UuidGenerator();
        assertThrows(NullPointerException.class, () -> gen.generateV5((UUID) null, "example.com"));
    }

    @Test
    void testGenerateV5NullNameWithNamespaceThrowsException() {
        UuidGenerator gen = new UuidGenerator();
        UUID namespace = UuidGenerator.getDnsNamespace();
        assertThrows(NullPointerException.class, () -> gen.generateV5(namespace, null));
    }

    @Test
    void testMessageDigestInvalidAlgorithmThrows() {
        assertThrows(RuntimeException.class, () -> UuidGenerator.messageDigest("NOT_A_REAL_ALGO"));
    }

    @Test
    void testGenerateMultipleV4UUIDs() {
        UuidGenerator gen = new UuidGenerator();
        Set<UUID> uuids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            uuids.add(gen.generateV4());
        }

        // All UUIDs should be unique
        assertEquals(1000, uuids.size());
    }

    @Test
    void testV4Format() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateV4String();

        // Check format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
        // where y is 8, 9, a, or b
        String[] parts = uuidStr.split("-");
        assertEquals(5, parts.length);
        assertEquals(8, parts[0].length());
        assertEquals(4, parts[1].length());
        assertEquals(4, parts[2].length());
        assertEquals(4, parts[3].length());
        assertEquals(12, parts[4].length());

        // Check version 4
        assertTrue(parts[2].startsWith("4"));

        // Check variant (10xx in binary, so 8, 9, a, or b in hex)
        char variantChar = parts[3].charAt(0);
        assertTrue(variantChar == '8' || variantChar == '9' || variantChar == 'a' || variantChar == 'b');
    }

    @Test
    void testV5Format() {
        UuidGenerator gen = new UuidGenerator();
        String uuidStr = gen.generateV5String("example.com");

        // Check format: xxxxxxxx-xxxx-5xxx-yxxx-xxxxxxxxxxxx
        String[] parts = uuidStr.split("-");
        assertEquals(5, parts.length);

        // Check version 5
        assertTrue(parts[2].startsWith("5"));

        // Check variant
        char variantChar = parts[3].charAt(0);
        assertTrue(variantChar == '8' || variantChar == '9' || variantChar == 'a' || variantChar == 'b');
    }

    @Test
    void testGetDnsNamespace() {
        UUID namespace = UuidGenerator.getDnsNamespace();
        assertNotNull(namespace);
        assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", namespace.toString());
    }

    @Test
    void testGetUrlNamespace() {
        UUID namespace = UuidGenerator.getUrlNamespace();
        assertNotNull(namespace);
        assertEquals("6ba7b811-9dad-11d1-80b4-00c04fd430c8", namespace.toString());
    }

    @Test
    void testV5KnownVectorDns() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5(UuidGenerator.getDnsNamespace(), "www.example.com");

        assertEquals(UUID.fromString("2ed6657d-e927-568b-95e1-2665a8aea6a2"), uuid);
    }

    @Test
    void testV5Consistency() {
        UuidGenerator gen1 = new UuidGenerator();
        UuidGenerator gen2 = new UuidGenerator();

        UUID uuid1 = gen1.generateV5("test");
        UUID uuid2 = gen2.generateV5("test");

        assertEquals(uuid1, uuid2);
    }

    @Test
    void testV4Randomness() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid1 = gen.generateV4();
        UUID uuid2 = gen.generateV4();

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateStringMatchesGenerate() {
        GeneratorConfig config = GeneratorConfig.builder().seed(99999L).build();
        UuidGenerator gen = new UuidGenerator(config);

        UUID uuid = gen.generate();

        GeneratorConfig config2 = GeneratorConfig.builder().seed(99999L).build();
        UuidGenerator gen2 = new UuidGenerator(config2);
        String uuidStr = gen2.generateString();

        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV5EmptyString() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5("");

        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testV5SpecialCharacters() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5("example.com/path?query=value&special=!@#$%");

        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testV5Unicode() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5("例え.com");

        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testMultipleSeededGenerators() {
        long seed = 54321L;
        GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();

        UuidGenerator gen1 = new UuidGenerator(config);
        UuidGenerator gen2 = new UuidGenerator(config);
        UuidGenerator gen3 = new UuidGenerator(config);

        UUID uuid1 = gen1.generate();
        UUID uuid2 = gen2.generate();
        UUID uuid3 = gen3.generate();

        assertEquals(uuid1, uuid2);
        assertEquals(uuid2, uuid3);
    }

    @Test
    void testV5StringMatchesV5() {
        UuidGenerator gen = new UuidGenerator();
        UUID uuid = gen.generateV5("test-name");
        String uuidStr = gen.generateV5String("test-name");

        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV5WithCustomNamespaceStringMatchesUUID() {
        UuidGenerator gen = new UuidGenerator();
        UUID namespace = UuidGenerator.getUrlNamespace();

        UUID uuid = gen.generateV5(namespace, "test-name");
        String uuidStr = gen.generateV5String(namespace, "test-name");

        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV4StringMatchesV4() {
        GeneratorConfig config = GeneratorConfig.builder().seed(111L).build();
        UuidGenerator gen = new UuidGenerator(config);

        UUID uuid = gen.generateV4();

        GeneratorConfig config2 = GeneratorConfig.builder().seed(111L).build();
        UuidGenerator gen2 = new UuidGenerator(config2);
        String uuidStr = gen2.generateV4String();

        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testDnsNamespaceIsConstant() {
        UUID ns1 = UuidGenerator.getDnsNamespace();
        UUID ns2 = UuidGenerator.getDnsNamespace();

        assertEquals(ns1, ns2);
    }

    @Test
    void testUrlNamespaceIsConstant() {
        UUID ns1 = UuidGenerator.getUrlNamespace();
        UUID ns2 = UuidGenerator.getUrlNamespace();

        assertEquals(ns1, ns2);
    }
}
