/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.object;

import io.github.frikit.rdg.generator.Generator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a generator class to use for a specific field or record component.
 *
 * <p>The referenced generator must have either:
 * <ul>
 *   <li>a no-arg constructor, or</li>
 *   <li>a constructor that matches the declared {@link RandomizerArgument} list.</li>
 * </ul>
 *
 * <p>One instance is created per annotated field for the lifetime of a top-level generator, so a
 * stateful generator such as a sequence keeps its state across generated objects. In a seeded
 * configuration, generators implementing {@link io.github.frikit.rdg.generator.Seedable} are
 * reseeded for every object from that object's seeded stream, so they replay from the seed and its
 * recipe. The built-in generators that are not {@code Seedable} but that a seed can reset to a new
 * build, the ones a seeded {@code Schema} reseeds, such as {@code FirstNameGenerator},
 * {@code EmailGenerator} and {@code UuidGenerator}, are reseeded the same way. Other generators, such as
 * {@code MiddleNameGenerator}, {@code CompanyEmailGenerator} or a custom one that is not {@code Seedable},
 * control their own randomness, which seeded replay cannot reproduce.
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Randomizer {

    /**
     * Generator class used to produce the field/component value.
     */
    Class<? extends Generator<?>> value();
}
