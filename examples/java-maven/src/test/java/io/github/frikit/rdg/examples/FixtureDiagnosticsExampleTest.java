/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.frikit.rdg.generator.Generators;
import io.github.frikit.rdg.generator.object.FixtureRule;
import io.github.frikit.rdg.generator.object.FixtureRuleKind;
import io.github.frikit.rdg.generator.object.FixtureRuleProblem;
import io.github.frikit.rdg.generator.object.ObjectFaker;
import io.github.frikit.rdg.generator.object.exception.FixtureRuleException;
import org.junit.jupiter.api.Test;

/**
 * {@code ObjectFaker.strict()} checks the fixture rules before anything is generated and reports every
 * problem in one {@link FixtureRuleException}, which names paths and rules, never a value.
 */
class FixtureDiagnosticsExampleTest {

    @Test
    void strictFakerRejectsAShadowedRuleBeforeGenerating() {
        ObjectFaker<UserFixture> faker = new ObjectFaker<>(UserFixture.class)
                .ruleFor(UserFixture::name, Generators.ofFullName())
                .ruleFor(UserFixture::email, Generators.ofEmail())
                .ruleFor(UserFixture::country, Generators.ofCountry())
                // Replaces rule #1, so rule #1 can never supply a value.
                .ruleFor(UserFixture::name, Generators.ofFirstName())
                .strict();

        FixtureRuleException failure = assertThrows(FixtureRuleException.class, faker::generate);

        FixtureRuleProblem problem = failure.getProblems().get(0);
        assertEquals(FixtureRuleProblem.Category.SHADOWED, problem.category());
        assertEquals(FixtureRule.registered(FixtureRuleKind.RULE, "name", 1), problem.rule());
        String report = """
                ObjectFaker for io.github.frikit.rdg.examples.UserFixture has 1 fixture rule problem:
                - SHADOWED rule #1 'name': replaced by rule #4 'name', registered later for the same path""";
        assertEquals(report, failure.getMessage());
    }
}
