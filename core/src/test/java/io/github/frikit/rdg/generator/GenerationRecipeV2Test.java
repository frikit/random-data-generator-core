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
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A recipe is {@code rdg-recipe/v2}: it records the four seeding settings that are on by default, on or off,
 * and replay requires them. Any other recipe version is rejected. A recipe that kRandom wrote, format
 * {@code krandom-recipe}, is rejected with a message that says how to convert it.
 */
@DisplayName("rdg-recipe/v2, other versions and kRandom recipes")
class GenerationRecipeV2Test {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static final List<String> FOUR = List.of(
            "address.german-street-layout",
            "advancing-convenience-streams",
            "gaussian.strict-math",
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
            + " random-data-generator, which continues kRandom, names its recipes rdg-recipe. ";

    private static GeneratorConfig seeded(UnaryOperator<GeneratorConfig.Builder> settings) {
        return settings.apply(GeneratorConfig.builder()
                        .seed(42L)
                        .locale(Locale.GERMANY)
                        .clock(CLOCK))
                .build();
    }

    private static String serializedV2() {
        return seeded(UnaryOperator.identity())
                .getGenerationRecipe()
                .orElseThrow()
                .serialize();
    }

    @Test
    @DisplayName("a recipe is rdg-recipe/v2 and records the four settings, on by default")
    void writesV2WithTheFourSettings() {
        GenerationRecipe recipe =
                seeded(UnaryOperator.identity()).getGenerationRecipe().orElseThrow();

        assertEquals("v2", GenerationRecipe.RECIPE_VERSION);
        assertEquals("v2", recipe.getRecipeVersion());
        assertTrue(recipe.serialize().startsWith("format=rdg-recipe\nrecipe-version=v2\n"), recipe.serialize());
        for (String key : FOUR) {
            assertEquals("true", recipe.getSettings().get(key), key);
        }
        GenerationRecipe off =
                seeded(DefaultFlipPinsTest.ALL_OFF).getGenerationRecipe().orElseThrow();
        for (String key : FOUR) {
            assertEquals("false", off.getSettings().get(key), key);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v3", "v99"})
    @DisplayName("an rdg-recipe of another version is rejected, parsed or built in code")
    void otherVersionsAreRejected(String version) {
        String recipe = serializedV2().replace("recipe-version=v2", "recipe-version=" + version);

        assertEquals(
                "Unsupported recipe version: " + version,
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(recipe))
                        .getMessage());
        assertEquals(
                "Unsupported recipe version: " + version,
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
    @DisplayName("a kRandom v2 recipe is rejected with how to convert it, and replays once converted")
    void kRandomV2RecipeConverts() {
        String recipe = serializedV2();
        String kRandom = recipe.replace("format=rdg-recipe\n", "format=krandom-recipe\n");

        assertEquals(
                KRANDOM_REJECTION
                        + "A krandom-recipe/v2 recipe, from kRandom 4.x, replays once its first line reads"
                        + " format=rdg-recipe; the members of a library record such as AddressInfo then take other"
                        + " values than in kRandom.",
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(kRandom))
                        .getMessage());

        GeneratorConfig converted = GenerationRecipe.parse(
                        kRandom.replace("format=krandom-recipe\n", "format=rdg-recipe\n"))
                .toGeneratorConfig();
        assertEquals(
                new StreetAddressGenerator(seeded(UnaryOperator.identity())).generate(),
                new StreetAddressGenerator(converted).generate());
    }

    @Test
    @DisplayName("a kRandom v1 recipe is rejected with the kRandom release to replay it with")
    void kRandomV1RecipeNamesKRandom() {
        assertEquals(
                KRANDOM_REJECTION
                        + "A krandom-recipe/v1 recipe, from kRandom 3.x or earlier, replays only with the kRandom"
                        + " release that its library-version line names.",
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(KRANDOM_V1_RECIPE))
                        .getMessage());
    }

    @Test
    @DisplayName("a kRandom recipe of another version, or of none, is told that only v2 converts")
    void otherKRandomRecipesDoNotConvert() {
        String onlyV2 = KRANDOM_REJECTION
                + "Only a krandom-recipe/v2 recipe converts, by changing its first line to format=rdg-recipe.";

        assertEquals(
                onlyV2,
                assertThrows(
                                IllegalArgumentException.class,
                                () -> GenerationRecipe.parse(
                                        KRANDOM_V1_RECIPE.replace("recipe-version=v1", "recipe-version=v7")))
                        .getMessage());
        assertEquals(
                onlyV2,
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse("format=krandom-recipe\n"))
                        .getMessage());
    }

    @Test
    @DisplayName("a v2 recipe without one of the four settings is rejected rather than replayed with a default")
    void v2RequiresTheFourSettings() {
        String recipe = serializedV2();
        for (String key : FOUR) {
            GenerationRecipe without = GenerationRecipe.parse(recipe.replace("setting." + key + "=true\n", ""));
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, without::toGeneratorConfig);
            assertEquals("Recipe requires setting: " + key, error.getMessage());

            GenerationRecipe bad = GenerationRecipe.parse(recipe.replace(key + "=true", key + "=on"));
            assertEquals(
                    "Recipe setting '" + key + "' must be true or false",
                    assertThrows(IllegalArgumentException.class, bad::toGeneratorConfig)
                            .getMessage());
        }
    }

    @Test
    @DisplayName("a v2 recipe replays each setting as recorded, so the values with the four off replay as such")
    void v2ReplaysAsRecorded() {
        GeneratorConfig off = seeded(DefaultFlipPinsTest.ALL_OFF);
        GeneratorConfig replayOff = GenerationRecipe.parse(
                        off.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertFalse(replayOff.isAdvancingConvenienceStreams());
        assertFalse(replayOff.isStrictMathGaussian());
        assertFalse(replayOff.isObjectReseedBuiltInRandomizers());
        assertFalse(replayOff.isGermanStreetLayout());
        assertEquals(new StreetAddressGenerator(off).generate(), new StreetAddressGenerator(replayOff).generate());
        assertEquals("7389 Schloss Anger", new StreetAddressGenerator(replayOff).generate());

        GeneratorConfig on = seeded(UnaryOperator.identity());
        GeneratorConfig replayOn = GenerationRecipe.parse(
                        on.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertTrue(replayOn.isAdvancingConvenienceStreams());
        assertTrue(replayOn.isStrictMathGaussian());
        assertTrue(replayOn.isObjectReseedBuiltInRandomizers());
        assertTrue(replayOn.isGermanStreetLayout());
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
