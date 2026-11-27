/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

import io.github.frikit.rdg.generator.LocaleDataProvider;
import java.util.Locale;

/**
 * Contract for a locale-specific gender-label data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.rdg.generator.DataRegistryContext.Builder#register(io.github.frikit.rdg.generator.Vocabulary, io.github.frikit.rdg.generator.LocaleDataProvider)}
 * to extend or override gender labels for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .register(Vocabulary.GENDER, new GenderDataProvider() {
 *         public Locale getLocale() { return Locale.forLanguageTag("ko-KR"); }
 *         public String getMaleLabel()   { return "남성"; }
 *         public String getFemaleLabel() { return "여성"; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.forLanguageTag("ko-KR"))
 *     .registryContext(context)
 *     .build();
 * GenderGenerator gen = new GenderGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link GenderDataRegistry} from
 * {@link io.github.frikit.rdg.generator.locale.SupportedLocale}.
 */
public interface GenderDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies labels for.
     */
    Locale getLocale();

    /**
     * Localized label for the male gender (e.g., {@code "Male"}, {@code "Homme"}, {@code "男性"}).
     */
    String getMaleLabel();

    /**
     * Localized label for the female gender (e.g., {@code "Female"}, {@code "Femme"}, {@code "女性"}).
     */
    String getFemaleLabel();
}
