/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.provider;

/**
 * Whether a provider's documented output is safe to use as a test fixture.
 *
 * <p>This classification never authorizes using generated values with external services.
 */
public enum ProviderTestSafety {

    /** The provider does not make a test-safety claim. */
    UNCLASSIFIED,

    /** The provider guarantees a non-routable or otherwise designated test fixture. */
    NON_ROUTABLE,

    /** The provider guarantees a documented sandbox fixture that requires its sandbox boundary. */
    OFFICIAL_SANDBOX,

    /** The selected {@code GeneratorConfig} determines test safety. */
    CONFIGURATION_DEPENDENT
}
