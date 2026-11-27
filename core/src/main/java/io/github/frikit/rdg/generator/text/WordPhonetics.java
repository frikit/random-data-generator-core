/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.text;

/**
 * Locale phonetic profile used by {@link WordGenerator}.
 */
record WordPhonetics(String[] onsets, String[] nuclei, String[] codas) {}
