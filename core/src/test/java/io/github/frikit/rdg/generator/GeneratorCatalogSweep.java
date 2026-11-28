/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import io.github.frikit.rdg.generator.finance.BankingSafetyPolicy;
import io.github.frikit.rdg.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.rdg.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.rdg.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.rdg.generator.user.nationalid.NationalIdSafetyPolicy;
import jakarta.validation.constraints.Email;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * The sweep over the whole generator catalog that {@link GeneratorCatalogEnvironmentTest} and
 * {@link GeneratorCatalogSeededBaselineTest} share: every static factory of {@link Generators} that takes one
 * {@link GeneratorConfig} and returns a generator, built with a fixed seed and clock in each of six locales,
 * and the first {@value #DRAWS} values of each of its {@code generate*()} methods.
 */
final class GeneratorCatalogSweep {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
    static final List<Locale> GENERATION_LOCALES = Stream.of("en-US", "en-GB", "de-DE", "tr-TR", "ar-SA", "hi-IN")
            .map(Locale::forLanguageTag)
            .toList();
    static final int DRAWS = 3;
    static final String REFUSED = "refused: ";
    /** The name under which the sweep records the object engine's {@link FixtureShape}. */
    static final String OBJECT = "object";

    static final List<Method> FACTORIES = Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getParameterCount() == 1 && method.getParameterTypes()[0] == GeneratorConfig.class)
            .filter(method -> Generator.class.isAssignableFrom(method.getReturnType()))
            .sorted(Comparator.comparing(Method::getName))
            .toList();

    private GeneratorCatalogSweep() {}

    /**
     * Sweeps the catalog, drawing all {@code generate*()} methods of a factory's generator, in name order, from
     * one instance. Each key is {@code <locale>/<factory>.<method>[<draw>]}, or {@code <locale>/<factory>.factory}
     * for a factory that refuses the configuration; each value is {@code describe} of the drawn value, or
     * {@value #REFUSED} and the exception's simple name.
     */
    static Map<String, String> sweep(Function<@Nullable Object, String> describe) throws Exception {
        return sweep(describe, false);
    }

    /**
     * Sweeps the catalog as {@link #sweep} does, except that each {@code generate*()} method draws from a fresh
     * generator built from the same configuration, so that one method's values do not depend on the others.
     */
    static Map<String, String> sweepEachMethodFresh(Function<@Nullable Object, String> describe) throws Exception {
        return sweep(describe, true);
    }

    private static Map<String, String> sweep(Function<@Nullable Object, String> describe, boolean freshPerMethod)
            throws Exception {
        Map<String, String> outcomes = new TreeMap<>();
        for (Locale locale : GENERATION_LOCALES) {
            for (Method factory : FACTORIES) {
                sweepFactory(outcomes, locale, factory, describe, freshPerMethod);
            }
            Object shape =
                    Generators.ofObject(FixtureShape.class, config(locale)).generate();
            outcomes.put(locale + "/" + OBJECT + ".generate[0]", describe.apply(shape));
        }
        return outcomes;
    }

    private static void sweepFactory(
            Map<String, String> outcomes,
            Locale locale,
            Method factory,
            Function<@Nullable Object, String> describe,
            boolean freshPerMethod)
            throws Exception {
        String prefix = locale + "/" + factory.getName() + ".";
        Object generator;
        try {
            generator = factory.invoke(null, config(locale));
        } catch (InvocationTargetException refused) {
            outcomes.put(
                    prefix + "factory", REFUSED + refused.getCause().getClass().getSimpleName());
            return;
        }
        for (Method method : generateMethods(generator)) {
            method.setAccessible(true);
            Object drawnFrom = freshPerMethod ? factory.invoke(null, config(locale)) : generator;
            for (int draw = 0; draw < DRAWS; draw++) {
                outcomes.put(prefix + method.getName() + "[" + draw + "]", outcome(method, drawnFrom, describe));
            }
        }
    }

    private static List<Method> generateMethods(Object generator) {
        return Arrays.stream(generator.getClass().getMethods())
                .filter(method -> method.getName().startsWith("generate"))
                .filter(method -> method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isBridge())
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    private static String outcome(Method method, Object generator, Function<@Nullable Object, String> describe)
            throws IllegalAccessException {
        try {
            return describe.apply(method.invoke(generator));
        } catch (InvocationTargetException refused) {
            return REFUSED + refused.getCause().getClass().getSimpleName();
        }
    }

    static GeneratorConfig config(Locale locale) {
        return GeneratorConfig.builder()
                .seed(42L)
                .locale(locale)
                .clock(CLOCK)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .businessTaxIdentifierSafetyPolicy(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .securitiesIdentifierSafetyPolicy(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();
    }

    /** A value's own content: a date, for example, without the zone its {@code toString} prints it in. */
    static String describe(@Nullable Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().toString();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().toString();
        }
        if (value instanceof Date date) {
            return Long.toString(date.getTime());
        }
        if (value instanceof Calendar calendar) {
            return calendar.getClass().getSimpleName() + ":" + calendar.getTimeInMillis() + ":"
                    + calendar.getTimeZone().getID();
        }
        if (value instanceof Object[] array) {
            return Arrays.deepToString(array);
        }
        if (value != null && value.getClass().isArray()) {
            return Arrays.deepToString(new Object[] {value});
        }
        return String.valueOf(value);
    }

    /**
     * The object the sweep generates in each locale. Its fully qualified name is part of the catalog baseline
     * ({@link GeneratorCatalogSeededBaselineTest}), because the object engine derives each member's stream from the
     * owner class name: moving or renaming it changes the {@code object.generate} lines.
     */
    record FixtureShape(
            String firstName,
            String lastName,
            @Email String email,
            String companyName,
            String companyEmail,
            UUID id,
            int age,
            BigDecimal amount,
            LocalDate birthDate,
            Instant createdAt,
            List<String> tags,
            Set<String> labels,
            Map<String, Integer> scores) {}
}
