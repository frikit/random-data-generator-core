/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.finance;

/**
 * GoFakeit-style credit card payload.
 *
 * @param number card number
 * @param type   card provider/type
 * @param exp    expiration date
 * @param cvv    security code
 */
public record CreditCardInfo(String number, String type, String exp, String cvv) {}
