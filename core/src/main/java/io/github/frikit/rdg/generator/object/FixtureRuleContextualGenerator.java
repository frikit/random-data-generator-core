/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import io.github.frikit.rdg.generator.ContextualGenerator;
import io.github.frikit.rdg.generator.GenerationContext;

/**
 * A root {@link ObjectFaker} context rule's generator, tagged with the rule as {@link FixtureRuleGenerator}
 * tags a plain one.
 *
 * @param delegate the rule's generator
 * @param rule     the rule, by kind, path and registration number
 * @param <T>      the generated type
 */
record FixtureRuleContextualGenerator<T>(ContextualGenerator<T> delegate, FixtureRule rule)
        implements ContextualGenerator<T> {

    @Override
    public T generate(GenerationContext context) {
        return delegate.generate(context);
    }
}
