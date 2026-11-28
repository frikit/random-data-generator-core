/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.Generator;
import io.github.frikit.rdg.generator.GeneratorConfig;
import io.github.frikit.rdg.generator.network.UrlGenerator;
import java.util.Locale;
import java.util.Objects;

/**
 * Generates locale-aware extended user profiles.
 */
public final class ProfileGenerator implements Generator<UserProfile> {

    private final SimpleProfileGenerator simpleProfileGenerator;
    private final CompanyNameGenerator companyNameGenerator;
    private final ProfessionGenerator professionGenerator;
    private final UrlGenerator urlGenerator;

    public ProfileGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ProfileGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public ProfileGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.simpleProfileGenerator = new SimpleProfileGenerator(effective.forChildStream("simpleProfile"));
        this.companyNameGenerator = new CompanyNameGenerator(effective.forChildStream("companyName"));
        this.professionGenerator = new ProfessionGenerator(effective.forChildStream("profession"));
        this.urlGenerator = new UrlGenerator(effective.forChildStream("uRL"));
    }

    /**
     * Generates a simple-profile view.
     *
     * @return simple profile
     */
    public SimpleProfile generateSimpleProfile() {
        return simpleProfileGenerator.generate();
    }

    @Override
    public UserProfile generate() {
        SimpleProfile base = simpleProfileGenerator.generate();
        return new UserProfile(
                base.username(),
                base.name(),
                base.sex(),
                base.address(),
                base.mail(),
                base.birthdate(),
                companyNameGenerator.generate(),
                professionGenerator.generate(),
                urlGenerator.generate());
    }
}
