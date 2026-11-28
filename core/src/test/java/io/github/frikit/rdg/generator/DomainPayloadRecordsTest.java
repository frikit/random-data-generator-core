/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.rdg.generator.commerce.ProductInfo;
import io.github.frikit.rdg.generator.finance.BankInfo;
import io.github.frikit.rdg.generator.finance.CreditCardInfo;
import io.github.frikit.rdg.generator.location.AddressInfo;
import io.github.frikit.rdg.generator.user.ContactInfo;
import io.github.frikit.rdg.generator.user.JobInfo;
import io.github.frikit.rdg.generator.user.PersonInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Domain payload records")
class DomainPayloadRecordsTest {

    @Test
    @DisplayName("record payloads preserve constructor values")
    void records() {
        AddressInfo address = new AddressInfo(
                "1 Main St, City",
                "1 Main St",
                "1",
                "Main",
                "St",
                "N",
                "Apt 2",
                "City",
                "State",
                "ST",
                "12345",
                "Country",
                "CC");
        ContactInfo contact = new ContactInfo(
                "John", "Doe", "John Doe", "Male", 30, "1234567890", "(123) 456-7890", "john@example.com");
        PersonInfo person = new PersonInfo(contact, address, "jdoe", "pass");
        JobInfo job = new JobInfo("Engineering", "Senior", "Software Engineer", "Full-time", "Engineer");
        BankInfo bank = new BankInfo(
                "1234567890", "021000021", "First National Bank", "Retail Bank", "Checking Account", "deposit");
        CreditCardInfo card = new CreditCardInfo("4111111111111111", "Visa", "12/30", "123");
        ProductInfo product =
                new ProductInfo("Chair", "A nice chair.", "Home", "Wood", "123456789012", "9781234567897");

        assertEquals("City", address.city());
        assertEquals("John", contact.firstName());
        assertEquals("jdoe", person.username());
        assertEquals("Engineering", job.descriptor());
        assertEquals("First National Bank", bank.bankName());
        assertEquals("Visa", card.type());
        assertEquals("Chair", product.name());
    }
}
