/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.user;

/**
 * GoFakeit-style job payload.
 *
 * @param descriptor job descriptor/domain
 * @param level      job seniority level
 * @param title      job position title
 * @param type       employment type
 * @param profession profession/job name
 */
public record JobInfo(String descriptor, String level, String title, String type, String profession) {}
