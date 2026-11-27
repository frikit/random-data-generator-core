/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import static org.junit.jupiter.api.Assertions.*;

import io.github.frikit.rdg.generator.GenerationRecipe;
import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.extension.ExtensionModule;
import io.github.frikit.rdg.generator.extension.ExtensionModuleContext;
import io.github.frikit.rdg.generator.provider.ProviderDescriptor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IndependentFieldStreamsTest {
    public static class Fixture {
        public String name;
        public int age;
        public Child child;
        public List<Integer> values;
    }

    public static class Child {
        public String name;
        public int age;
    }

    public record RecordFixture(String name, int age, List<Integer> values) {}

    private GeneratorConfig legacy() {
        return GeneratorConfig.builder()
                .seed(42)
                .clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                .build();
    }

    private GeneratorConfig independent() {
        return legacy().toBuilder()
                .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                .build();
    }

    @Test
    void unrelatedFieldsRetainTheirStreamsAcrossOverridesExclusionsAndModules() {
        GeneratorConfig config = independent();
        ExtensionModule emptyModule = new ExtensionModule() {
            public String id() {
                return "unrelated";
            }

            public void configure(ExtensionModuleContext context) {}
        };
        for (GeneratorConfig changed : List.of(
                config.toBuilder()
                        .objectOverride(Fixture.class, "name", () -> "fixed")
                        .build(),
                config.toBuilder().objectExcludeField("name").build(),
                config.toBuilder().install(emptyModule).build(),
                config.toBuilder()
                        .objectOverride(Child.class, "name", () -> "nested")
                        .build())) {
            ObjectGenerator<Fixture> plain = new ObjectGenerator<>(Fixture.class, config);
            ObjectGenerator<Fixture> custom = new ObjectGenerator<>(Fixture.class, changed);
            for (int i = 0; i < 5; i++) {
                Fixture expected = plain.generate();
                Fixture actual = custom.generate();
                assertEquals(expected.age, actual.age);
                assertEquals(expected.child.age, actual.child.age);
                assertEquals(expected.values, actual.values);
            }
            assertTrue(changed.getGenerationRecipe().isEmpty(), "custom configuration must not claim portable replay");
        }
    }

    public enum Color {
        RED,
        GREEN,
        BLUE,
        BLACK,
        WHITE,
        ORANGE,
        PURPLE
    }

    public static class SemanticFixture {
        public List<Integer> values;
        public Color color;
        public Child child;
        public int score;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final ExtensionModule UNRELATED_SEMANTIC_PROVIDER = new ExtensionModule() {
        public String id() {
            return "unrelated.semantic";
        }

        public void configure(ExtensionModuleContext context) {
            context.registerProvider(ProviderDescriptor.builder(
                            "unrelated.flavor", (Class) Generator.class, cfg -> (Generator<String>) () -> "vanilla")
                    .semanticKeys("favoriteflavor")
                    .build());
        }
    };

    @Test
    void unrelatedSemanticProvidersDoNotShiftIndependentMemberStreams() {
        for (ObjectGenerationSemanticMode mode :
                List.of(ObjectGenerationSemanticMode.RELAXED, ObjectGenerationSemanticMode.STRICT)) {
            GeneratorConfig config =
                    independent().toBuilder().objectSemanticMode(mode).build();
            GeneratorConfig withModule =
                    config.toBuilder().install(UNRELATED_SEMANTIC_PROVIDER).build();
            ObjectGenerator<SemanticFixture> plain = new ObjectGenerator<>(SemanticFixture.class, config);
            ObjectGenerator<SemanticFixture> custom = new ObjectGenerator<>(SemanticFixture.class, withModule);
            for (int i = 0; i < 5; i++) {
                SemanticFixture expected = plain.generate();
                SemanticFixture actual = custom.generate();
                assertEquals(expected.values, actual.values, mode + " values");
                assertEquals(expected.color, actual.color, mode + " color");
                assertEquals(expected.score, actual.score, mode + " score");
                assertEquals(expected.child.name, actual.child.name, mode + " child.name");
                assertEquals(expected.child.age, actual.child.age, mode + " child.age");
            }
        }
    }

    @Test
    void recordsKeepIndependentStreamsAndDifferentRecordIndices() {
        GeneratorConfig config = independent();
        List<RecordFixture> original = new ObjectGenerator<>(RecordFixture.class, config).generateList(5);
        List<RecordFixture> custom = new ObjectGenerator<>(
                        RecordFixture.class,
                        config.toBuilder()
                                .objectOverride(RecordFixture.class, "name", () -> "fixed")
                                .build())
                .generateList(5);
        assertEquals(
                original.stream().map(RecordFixture::age).toList(),
                custom.stream().map(RecordFixture::age).toList());
        assertEquals(
                original.stream().map(RecordFixture::values).toList(),
                custom.stream().map(RecordFixture::values).toList());
        assertTrue(original.stream().map(RecordFixture::age).distinct().count() > 1);
    }

    @Test
    void defaultsAndExistingRecipesRetainLegacyBehavior() {
        GeneratorConfig config = legacy();
        assertEquals(ObjectFieldStreamPolicy.LEGACY, config.getObjectFieldStreamPolicy());
        GenerationRecipe old = config.getGenerationRecipe().orElseThrow();
        assertFalse(old.getSettings().containsKey("object.field-stream-policy"));
        assertEquals(ObjectFieldStreamPolicy.LEGACY, old.toGeneratorConfig().getObjectFieldStreamPolicy());
        List<RecordFixture> baseline = new ObjectGenerator<>(RecordFixture.class, config).generateList(5);
        assertEquals(baseline, new ObjectGenerator<>(RecordFixture.class, old.toGeneratorConfig()).generateList(5));
        assertEquals(baseline, new ObjectGenerator<>(RecordFixture.class, independent()).generateList(5));
        int original = new ObjectGenerator<>(Fixture.class, config).generate().age;
        int custom = new ObjectGenerator<>(
                        Fixture.class,
                        config.toBuilder()
                                .objectOverride(Fixture.class, "name", () -> "fixed")
                                .build())
                .generate()
                .age;
        assertNotEquals(original, custom, "legacy non-portable configurations retain their existing sequence");
    }

    @Test
    void recipeRoundTripPreservesExplicitPolicyWithoutChangingAlgorithm() {
        GenerationRecipe recipe = independent().getGenerationRecipe().orElseThrow();
        assertEquals("INDEPENDENT", recipe.getSettings().get("object.field-stream-policy"));
        assertEquals(GenerationRecipe.ALGORITHM, recipe.getAlgorithm());
        GeneratorConfig replay = GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();
        assertEquals(
                ObjectFieldStreamPolicy.INDEPENDENT, replay.toBuilder().build().getObjectFieldStreamPolicy());
        assertEquals(
                new ObjectGenerator<>(RecordFixture.class, independent()).generateList(5),
                new ObjectGenerator<>(RecordFixture.class, replay).generateList(5));
        assertThrows(
                IllegalArgumentException.class,
                () -> GenerationRecipe.builder()
                        .setting("object.field-stream-policy", "UNKNOWN")
                        .build()
                        .toGeneratorConfig());
    }

    @Test
    void independentStreamsRequireAnOwnedSeedAndRejectNullPolicy() {
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().objectFieldStreamPolicy(null));
        assertThrows(
                IllegalStateException.class,
                () -> GeneratorConfig.builder()
                        .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                        .build());
        assertThrows(
                IllegalStateException.class,
                () -> GeneratorConfig.builder()
                        .random(new Random(42))
                        .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                        .build());
        assertEquals(
                ObjectFieldStreamPolicy.INDEPENDENT,
                GeneratorConfig.builder()
                        .seed("text")
                        .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                        .build()
                        .getObjectFieldStreamPolicy());
    }
}
