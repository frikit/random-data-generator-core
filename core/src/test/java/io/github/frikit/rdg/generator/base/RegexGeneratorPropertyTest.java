/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.base;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.GeneratorConfig;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RegexGenerator's contract, checked over many seeds and patterns: every value matches the pattern under
 * java.util.regex, or construction fails and names why. Each case comes from a fixed-seed {@link Random}, so a
 * failure repeats; its message names the pattern and, for a value, the value.
 */
@DisplayName("RegexGenerator over many seeds and patterns")
class RegexGeneratorPropertyTest {

    private static final List<String> LISTED_PATTERNS = List.of(
            "\\d{3}-\\d{4}",
            "[A-Z]{2,5}",
            "(yes|no)",
            "^[a-z]+$",
            "\\bfoo\\b",
            "[\\w.-]+@example\\.com",
            "(?i)abc",
            "(?i)(yes|no)-[a-c]{2}",
            "a(?i)b|c",
            "(a(?i)b)c",
            "(?i:x)y",
            "(?i)[^a-z]{3}",
            "(?i)\\P{Lower}{2}",
            "(?i)[^\\p{Lu}]\\p{Ll}",
            "\\p{L}+",
            "\\p{Lu}\\p{Ll}{2,8}",
            "[\\p{L}\\d_-]+",
            "\\P{L}{3}",
            "\\p{Nd}{4}",
            "\\p{N}\\p{IsDigit}\\p{IsAlphabetic}",
            "\\p{IsLatin}{2}\\p{Punct}",
            "\\p{Alnum}\\p{Graph}\\p{Print}\\p{Space}\\p{Blank}\\p{XDigit}\\p{ASCII}",
            "\\Qa.b\\E\\d",
            "\\Qab\\E+",
            "[\\Qa-c\\E]{3}",
            "(?i)\\Q(x)\\E[a-c]");

    /** How a rejection starts: a construct this generator cannot honour, or a pattern too large to generate. */
    private static final List<String> REJECTIONS =
            List.of("Unsupported ", "Generated values could be longer", "Generating a value could take");

    private static final List<String> ATOMS = List.of(
            "a",
            "B",
            "é",
            "-",
            " ",
            "_",
            "1",
            ".",
            "\\d",
            "\\w",
            "\\s",
            "\\D",
            "\\W",
            "\\S",
            "[ab]",
            "[a -]",
            "[^a]",
            "[\\d-]",
            "[\\W]",
            "[a-z]",
            "[^\\w.]",
            "\\p{L}",
            "\\p{Lu}",
            "\\P{Alpha}",
            "\\pN",
            "\\p{Punct}",
            "[\\p{Ll}\\d]",
            "[^\\p{Lower}]",
            "\\P{XDigit}",
            "\\p{IsLatin}",
            "\\Qa.\\E",
            "\\Q*\\E",
            "^",
            "$",
            "\\b",
            "\\A",
            "\\z",
            "\\Z",
            // Constructs it rejects
            "\\h",
            "\\p{InGreek}",
            "[a&&b]");

    private static final List<String> ATOM_QUANTIFIERS =
            List.of("", "", "", "", "?", "*", "+", "{2}", "{0,2}", "{1,}", "{0}", "??", "*?", "+?", "{1,2}?");

    // No open-ended group: java.util.regex can take years to check a value against ((\w+)*)*.
    private static final List<String> GROUP_QUANTIFIERS = List.of("", "", "?", "{2}", "{0,2}", "{0}", "??", "{1,2}?");

    private static final List<String> GROUP_OPENINGS = List.of("(", "(?:", "(?i:", "(?-i:");

    // Flags hold to the end of their group. (?s) is rejected.
    private static final List<String> FLAGS = List.of("(?i)", "(?-i)", "(?s)");

    @Test
    @DisplayName("generates only values java.util.regex matches, for every listed pattern and any seed")
    void listedPatternsMatch() {
        Random cases = new Random(20_261_104L);
        for (int i = 0; i < 3_000; i++) {
            String regex = pick(cases, LISTED_PATTERNS);
            long seed = cases.nextLong();
            assertEveryValueMatches(
                    regex,
                    new RegexGenerator(
                            regex, GeneratorConfig.builder().seed(seed).build()));
        }
    }

    @Test
    @DisplayName("rejects a random pattern by name, or generates only values java.util.regex matches")
    void randomPatternsMatchOrAreRejected() {
        Random cases = new Random(20_261_105L);
        int accepted = 0;
        int rejected = 0;
        for (int i = 0; i < 6_000; i++) {
            String regex = randomPattern(cases, 3);
            long seed = cases.nextLong();
            // A minimum length above 5 raises how far *, + and {n,} repeat.
            int minLength = 1 + cases.nextInt(60);
            GeneratorConfig config = GeneratorConfig.builder()
                    .seed(seed)
                    .stringLength(minLength, minLength)
                    .build();
            RegexGenerator generator;
            try {
                generator = new RegexGenerator(regex, config);
            } catch (IllegalArgumentException exception) {
                String message = String.valueOf(exception.getMessage());
                assertTrue(REJECTIONS.stream().anyMatch(message::startsWith), regex + ": " + message);
                rejected++;
                continue;
            }
            accepted++;
            assertEveryValueMatches(regex, generator);
        }
        // Neither branch can have gone quiet.
        assertTrue(accepted > 2_000, "accepted " + accepted);
        assertTrue(rejected > 500, "rejected " + rejected);
    }

    private static String pick(Random random, List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private static String randomPattern(Random random, int depth) {
        StringBuilder pattern = new StringBuilder();
        int alternatives = random.nextInt(4) == 0 ? 2 : 1;
        for (int alternative = 0; alternative < alternatives; alternative++) {
            if (alternative > 0) {
                pattern.append('|');
            }
            int parts = random.nextInt(4);
            for (int part = 0; part < parts; part++) {
                if (depth > 0 && random.nextInt(4) == 0) {
                    pattern.append(pick(random, GROUP_OPENINGS))
                            .append(randomPattern(random, depth - 1))
                            .append(')')
                            .append(pick(random, GROUP_QUANTIFIERS));
                } else if (random.nextInt(8) == 0) {
                    pattern.append(pick(random, FLAGS));
                } else {
                    pattern.append(pick(random, ATOMS)).append(pick(random, ATOM_QUANTIFIERS));
                }
            }
        }
        return pattern.toString();
    }

    private static void assertEveryValueMatches(String regex, RegexGenerator generator) {
        Pattern compiled = Pattern.compile(regex);
        for (int i = 0; i < 20; i++) {
            String value = generator.generate();
            assertTrue(compiled.matcher(value).matches(), "\"" + value + "\" from the pattern " + regex);
        }
    }
}
