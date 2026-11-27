/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.location.StreetAddressGenerator;
import io.github.frikit.rdg.generator.user.EmailGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A recipe is {@code rdg-recipe/v3}. Any other recipe version, {@code v2} included, is rejected, and so is a line
 * this library does not read. A recipe that kRandom wrote, format {@code krandom-recipe}, is rejected with a message that says it replays
 * only with the kRandom release that wrote it.
 */
@DisplayName("rdg-recipe/v3, other versions and kRandom recipes")
class GenerationRecipeFormatTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    /** Settings a recipe does not record, which replay rejects. */
    private static final List<String> UNKNOWN_SETTINGS = List.of(
            "address.german-street-layout",
            "advancing-convenience-streams",
            "child-stream-policy",
            "gaussian.strict-math",
            "object.field-stream-policy",
            "object.reseed-built-in-randomizers");

    /** A recipe as kRandom 3.2.0 wrote it for {@code de_DE} and seed 42, with the German street layout on. */
    static final String KRANDOM_V1_RECIPE = """
            format=krandom-recipe
            recipe-version=v1
            library-version=3.2.0
            algorithm=java.util.Random-v1
            seed=42
            locale=de-DE
            clock-instant=2026-07-10T12%3A00%3A00Z
            clock-zone=Z
            profile=custom
            safety-policy=legacy-unclassified
            construction-policy=safe-constructors
            provider-dataset-version=builtin-v1
            setting.address.german-street-layout=true
            setting.string.max=20
            setting.string.min=5
            """;

    private static final String KRANDOM_REJECTION = "Unsupported format: krandom-recipe. kRandom wrote this recipe;"
            + " random-data-generator, which continues kRandom, names its recipes rdg-recipe and does not replay"
            + " kRandom recipes. A krandom-recipe recipe replays only with the kRandom release that its"
            + " library-version line names.";

    private static GeneratorConfig seeded() {
        return GeneratorConfig.builder()
                .seed(42L)
                .locale(Locale.GERMANY)
                .clock(CLOCK)
                .build();
    }

    private static String serialized() {
        return seeded().getGenerationRecipe().orElseThrow().serialize();
    }

    @Test
    @DisplayName("a recipe is rdg-recipe/v3 and has no safety-policy label or unknown setting")
    void writesV3() {
        GenerationRecipe recipe = seeded().getGenerationRecipe().orElseThrow();

        assertEquals("v3", GenerationRecipe.RECIPE_VERSION);
        assertEquals("v3", recipe.getRecipeVersion());
        assertTrue(recipe.serialize().startsWith("format=rdg-recipe\nrecipe-version=v3\n"), recipe.serialize());
        assertFalse(recipe.serialize().contains("\nsafety-policy="), recipe.serialize());
        for (String key : UNKNOWN_SETTINGS) {
            assertFalse(recipe.getSettings().containsKey(key), key);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2", "v4", "v99"})
    @DisplayName("an rdg-recipe of another version is rejected, parsed or built in code")
    void otherVersionsAreRejected(String version) {
        String recipe = serialized().replace("recipe-version=v3", "recipe-version=" + version);
        String rejection = "Unsupported recipe version: " + version + ". This release replays rdg-recipe/v3 recipes"
                + " only: replay it with the release that wrote it, which its library-version line names, or record"
                + " it again with this release.";

        assertEquals(
                rejection,
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(recipe))
                        .getMessage());
        assertEquals(
                rejection,
                assertThrows(
                                IllegalArgumentException.class,
                                () -> GenerationRecipe.builder()
                                        .recipeVersion(version)
                                        .libraryVersion("1.0.0")
                                        .seed(1L)
                                        .build())
                        .getMessage());
    }

    @Test
    @DisplayName("a kRandom recipe of any version, or of none, is rejected with the kRandom release to replay it with")
    void kRandomRecipesNameKRandom() {
        for (String kRandom : List.of(
                serialized().replace("format=rdg-recipe\n", "format=krandom-recipe\n"),
                KRANDOM_V1_RECIPE,
                KRANDOM_V1_RECIPE.replace("recipe-version=v1", "recipe-version=v7"),
                "format=krandom-recipe\n")) {
            assertEquals(
                    KRANDOM_REJECTION,
                    assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(kRandom))
                            .getMessage(),
                    kRandom);
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "address.german-street-layout=true",
                "address.german-street-layout=false",
                "advancing-convenience-streams=true",
                "advancing-convenience-streams=false",
                "child-stream-policy=INDEPENDENT",
                "child-stream-policy=LEGACY",
                "gaussian.strict-math=true",
                "gaussian.strict-math=false",
                "object.field-stream-policy=INDEPENDENT",
                "object.field-stream-policy=LEGACY",
                "object.reseed-built-in-randomizers=true",
                "object.reseed-built-in-randomizers=false"
            })
    @DisplayName("a recipe with a setting this library does not read is rejected at replay, naming the setting")
    void unknownSettingsAreRejected(String line) {
        String key = line.substring(0, line.indexOf('='));
        String recipe = serialized().replace("setting.charset=", "setting." + line + "\nsetting.charset=");
        GenerationRecipe parsed = GenerationRecipe.parse(recipe);

        assertEquals(
                "Unsupported recipe setting: " + key,
                assertThrows(IllegalArgumentException.class, parsed::toGeneratorConfig)
                        .getMessage());
    }

    @Test
    @DisplayName("a recipe with a safety-policy line is rejected when parsed")
    void safetyPolicyLineIsRejected() {
        String recipe =
                serialized().replace("construction-policy=", "safety-policy=legacy-unclassified\nconstruction-policy=");

        assertEquals(
                "Unknown recipe key: safety-policy",
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(recipe))
                        .getMessage());
    }

    @Test
    @DisplayName("a recipe replays the values it was written with")
    void replaysAsRecorded() {
        GeneratorConfig on = seeded();
        GeneratorConfig replayOn = GenerationRecipe.parse(
                        on.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertEquals("Schlossanger 7389", new StreetAddressGenerator(replayOn).generate());
        EmailGenerator emails = new EmailGenerator(replayOn);
        EmailGenerator original = new EmailGenerator(on);
        for (int i = 0; i < 3; i++) {
            assertEquals(original.generateCompanyEmail(), emails.generateCompanyEmail());
        }
        assertEquals(
                on.getGenerationRecipe().orElseThrow().serialize(),
                replayOn.getGenerationRecipe().orElseThrow().serialize());
    }
}
