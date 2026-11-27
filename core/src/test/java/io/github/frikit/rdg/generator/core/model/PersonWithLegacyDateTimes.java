/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.core.model;

/**
 * POJO with legacy Java date/time fields used to verify
 * {@link io.github.frikit.rdg.generator.object.ObjectGenerator} support.
 */
public class PersonWithLegacyDateTimes {

    private java.util.Date createdAt;
    private java.sql.Date bornOn;
    private java.sql.Time wakeUpTime;
    private java.sql.Timestamp updatedAt;

    public PersonWithLegacyDateTimes() {}

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public java.sql.Date getBornOn() {
        return bornOn;
    }

    public java.sql.Time getWakeUpTime() {
        return wakeUpTime;
    }

    public java.sql.Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
