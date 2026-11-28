/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.commerce.CommerceDataRegistry;
import io.github.frikit.rdg.generator.database.DatabaseColumnDataRegistry;
import io.github.frikit.rdg.generator.file.DirectoryNameDataRegistry;
import io.github.frikit.rdg.generator.finance.BankAccountDataRegistry;
import io.github.frikit.rdg.generator.finance.BankNameDataRegistry;
import io.github.frikit.rdg.generator.finance.BankTypeDataRegistry;
import io.github.frikit.rdg.generator.locale.SupportedLocale;
import io.github.frikit.rdg.generator.text.TextWordDataRegistry;
import io.github.frikit.rdg.generator.user.CompanyBuzzwordDataRegistry;
import io.github.frikit.rdg.generator.user.CompanyCatchPhraseDataRegistry;
import io.github.frikit.rdg.generator.user.CompanyNameDataRegistry;
import io.github.frikit.rdg.generator.user.EducationalAttainmentDataRegistry;
import io.github.frikit.rdg.generator.user.IndustryDataRegistry;
import io.github.frikit.rdg.generator.user.JobFieldDataRegistry;
import io.github.frikit.rdg.generator.user.JobTypeDataRegistry;
import io.github.frikit.rdg.generator.user.MaritalStatusDataRegistry;
import io.github.frikit.rdg.generator.user.PositionDataRegistry;
import io.github.frikit.rdg.generator.user.SeniorityDataRegistry;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Read-only contract of the vocabulary registries added by the locale-pattern migration: built-in
 * data is seeded at class load for every supported locale that ships all of the concept's files,
 * lookups fall back from {@code language_COUNTRY} to {@code language}, and unknown or {@code null}
 * locales resolve to nothing. English is intentionally served by each generator's bundled default.
 */
@DisplayName("Vocabulary registry read APIs")
class VocabularyRegistryReadApiTest {

    private record ReadApi(
            String name,
            Function<Locale, Boolean> isRegistered,
            Function<Locale, Object> forLocale,
            Set<String> registeredKeys,
            Function<Object, Locale> providerLocale) {

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<ReadApi> readApis() {
        return Stream.of(
                new ReadApi(
                        "industry",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.INDUSTRY, locale),
                        IndustryDataRegistry::forLocale,
                        IndustryDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.IndustryDataProvider) p).getLocale()),
                new ReadApi(
                        "jobField",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.JOB_FIELD, locale),
                        JobFieldDataRegistry::forLocale,
                        JobFieldDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.JobFieldDataProvider) p).getLocale()),
                new ReadApi(
                        "seniority",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.SENIORITY, locale),
                        SeniorityDataRegistry::forLocale,
                        SeniorityDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.SeniorityDataProvider) p).getLocale()),
                new ReadApi(
                        "position",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.POSITION, locale),
                        PositionDataRegistry::forLocale,
                        PositionDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.PositionDataProvider) p).getLocale()),
                new ReadApi(
                        "educationalAttainment",
                        locale -> DataRegistryContext.globalDefault()
                                .isRegistered(Vocabulary.EDUCATIONAL_ATTAINMENT, locale),
                        EducationalAttainmentDataRegistry::forLocale,
                        EducationalAttainmentDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.EducationalAttainmentDataProvider) p).getLocale()),
                new ReadApi(
                        "maritalStatus",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.MARITAL_STATUS, locale),
                        MaritalStatusDataRegistry::forLocale,
                        MaritalStatusDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.MaritalStatusDataProvider) p).getLocale()),
                new ReadApi(
                        "jobType",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.JOB_TYPE, locale),
                        JobTypeDataRegistry::forLocale,
                        JobTypeDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.JobTypeDataProvider) p).getLocale()),
                new ReadApi(
                        "companyName",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.COMPANY_NAME, locale),
                        CompanyNameDataRegistry::forLocale,
                        CompanyNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.CompanyNameDataProvider) p).getLocale()),
                new ReadApi(
                        "companyBuzzword",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.COMPANY_BUZZWORD, locale),
                        CompanyBuzzwordDataRegistry::forLocale,
                        CompanyBuzzwordDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.CompanyBuzzwordDataProvider) p).getLocale()),
                new ReadApi(
                        "companyCatchPhrase",
                        locale -> DataRegistryContext.globalDefault()
                                .isRegistered(Vocabulary.COMPANY_CATCH_PHRASE, locale),
                        CompanyCatchPhraseDataRegistry::forLocale,
                        CompanyCatchPhraseDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.user.CompanyCatchPhraseDataProvider) p).getLocale()),
                new ReadApi(
                        "textWord",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.TEXT_WORD, locale),
                        TextWordDataRegistry::forLocale,
                        TextWordDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.text.TextWordDataProvider) p).getLocale()),
                new ReadApi(
                        "commerce",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.COMMERCE, locale),
                        CommerceDataRegistry::forLocale,
                        CommerceDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.commerce.CommerceDataProvider) p).getLocale()),
                new ReadApi(
                        "databaseColumn",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.DATABASE_COLUMN, locale),
                        DatabaseColumnDataRegistry::forLocale,
                        DatabaseColumnDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.database.DatabaseColumnDataProvider) p).getLocale()),
                new ReadApi(
                        "directoryName",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.DIRECTORY_NAME, locale),
                        DirectoryNameDataRegistry::forLocale,
                        DirectoryNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.file.DirectoryNameDataProvider) p).getLocale()),
                new ReadApi(
                        "bankName",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.BANK_NAME, locale),
                        BankNameDataRegistry::forLocale,
                        BankNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.finance.BankNameDataProvider) p).getLocale()),
                new ReadApi(
                        "bankType",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.BANK_TYPE, locale),
                        BankTypeDataRegistry::forLocale,
                        BankTypeDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.finance.BankTypeDataProvider) p).getLocale()),
                new ReadApi(
                        "bankAccount",
                        locale -> DataRegistryContext.globalDefault().isRegistered(Vocabulary.BANK_ACCOUNT, locale),
                        BankAccountDataRegistry::forLocale,
                        BankAccountDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.rdg.generator.finance.BankAccountDataProvider) p).getLocale()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("seeded locales resolve exactly and through language and fallback-tier variants")
    void seededLocalesResolve(ReadApi api) {
        Locale german = Locale.GERMANY;
        assertTrue(api.isRegistered().apply(german), api.name());
        Object provider = api.forLocale().apply(german);
        assertNotNull(provider, api.name());
        assertEquals(german, api.providerLocale().apply(provider), api.name());

        assertTrue(api.isRegistered().apply(Locale.GERMAN), api.name());
        assertSame(provider, api.forLocale().apply(Locale.GERMAN), api.name());
        assertTrue(api.isRegistered().apply(new Locale("de", "LU")), api.name());
        assertSame(provider, api.forLocale().apply(new Locale("de", "LU")), api.name());

        Object swiss = api.forLocale().apply(SupportedLocale.DE_CH.locale());
        assertNotNull(swiss, api.name());
        assertEquals(SupportedLocale.DE_CH.locale(), api.providerLocale().apply(swiss), api.name());
        assertTrue(api.registeredKeys().containsAll(Set.of("de", "de_DE", "de_CH")), api.name());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("English, null and unknown locales resolve to nothing")
    void unregisteredLocalesResolveToNothing(ReadApi api) {
        for (Locale locale : new Locale[] {Locale.US, Locale.ENGLISH, new Locale("xx", "YY"), new Locale("is")}) {
            assertFalse(api.isRegistered().apply(locale), api.name() + " " + locale);
            assertNull(api.forLocale().apply(locale), api.name() + " " + locale);
        }
        assertFalse(api.isRegistered().apply(null), api.name());
        assertNull(api.forLocale().apply(null), api.name());
        assertFalse(api.registeredKeys().contains("en"), api.name());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("key snapshots are immutable")
    void registeredKeysAreImmutable(ReadApi api) {
        assertThrows(
                UnsupportedOperationException.class, () -> api.registeredKeys().add("xx"));
    }
}
