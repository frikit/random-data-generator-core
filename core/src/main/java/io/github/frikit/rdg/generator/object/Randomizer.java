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
 * <p>On a field that a constructor argument fills, the argument uses it: the field that a constructor
 * parameter initializes is the nearest field with the parameter's name and declared type, on the class or a
 * superclass, so the class must be compiled with {@code -parameters}. Without parameter names the
 * annotation applies only where the field is set after construction.
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
 *
 * <p>A configuration with a caller-owned {@link java.util.Random} or a random factory and no seed reseeds
 * the same generators for every object, with a seed drawn from the sequence the object's other members
 * draw from, just before the generator first gives the member's value, so the same caller sequence repeats
 * them. A member whose generator is not used, such as an {@code Optional} member, draws no seed. An unseeded
 * or secure configuration reseeds none.
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
