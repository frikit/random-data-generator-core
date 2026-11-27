/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.object.ObjectFaker;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * kRandom 4.1.0 compatibility pin for the named member streams of object generation. A member's seeded stream
 * is derived from its owner's fully qualified class name, so this test and its fixtures keep the binary names
 * they had in kRandom ({@code io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest$Member} and
 * {@code $Address}). With them, seed 42 must still give the two members that kRandom 4.1.0 pinned.
 */
@DisplayName("kRandom 4.1.0 compatibility: named member streams under their kRandom class names")
class ObjectEngineDefaultsPinTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    public static class Address {
        public String street;
        public String city;
        public int zip;
    }

    public static class Member {
        public String name;
        public String nickname;
        public int age;
        public Address address;
        public String email;
    }

    private static String show(Member member) {
        return member.name + "|" + member.nickname + "|" + member.age + "|" + member.email + "|" + member.address.street
                + "/" + member.address.city + "/" + member.address.zip;
    }

    @Test
    @DisplayName("seed 42 gives the two members that kRandom 4.1.0 pinned")
    void namedMemberStreams() {
        ObjectFaker<Member> faker = new ObjectFaker<>(
                Member.class, GeneratorConfig.builder().seed(42L).clock(CLOCK).build());

        assertEquals(
                List.of(
                        "rjyXYwkzybng|aQvyppOmBjDTPRIitB|51|angelwalker@example.net|4315 Brookside Cres/Indianapolis/766875267",
                        "oAERm|sDANx|72|adams.scarlett@example.com|8731 Forest Pike/Orange/-1958311597"),
                List.of(show(faker.generate()), show(faker.generate())));
    }
}
