/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
/**
 * Core random-data-generator generators, object fixtures, providers, and schema support.
 */
module io.github.frikit.rdg {
    // Optional: constraint-aware generation activates only when the consumer provides the API.
    requires static jakarta.validation;
    requires java.sql;
    requires org.objenesis;
    requires org.slf4j;
    requires static transitive org.jspecify;

    uses io.github.frikit.rdg.generator.object.ObjectConstructionAdapter;

    exports io.github.frikit.rdg.generator;
    exports io.github.frikit.rdg.generator.algorithms;
    exports io.github.frikit.rdg.generator.base;
    exports io.github.frikit.rdg.generator.color;
    exports io.github.frikit.rdg.generator.commerce;
    exports io.github.frikit.rdg.generator.database;
    exports io.github.frikit.rdg.generator.datapack;
    exports io.github.frikit.rdg.generator.datetime;
    exports io.github.frikit.rdg.generator.extension;
    exports io.github.frikit.rdg.generator.failure;
    exports io.github.frikit.rdg.generator.file;
    exports io.github.frikit.rdg.generator.finance;
    exports io.github.frikit.rdg.generator.games.coin;
    exports io.github.frikit.rdg.generator.games.dice;
    exports io.github.frikit.rdg.generator.identifier;
    exports io.github.frikit.rdg.generator.locale;
    exports io.github.frikit.rdg.generator.location;
    exports io.github.frikit.rdg.generator.measurement;
    exports io.github.frikit.rdg.generator.namespace;
    exports io.github.frikit.rdg.generator.network;
    exports io.github.frikit.rdg.generator.object;
    exports io.github.frikit.rdg.generator.object.exception;
    exports io.github.frikit.rdg.generator.provider;
    exports io.github.frikit.rdg.generator.report;
    exports io.github.frikit.rdg.generator.schema;
    exports io.github.frikit.rdg.generator.selection;
    exports io.github.frikit.rdg.generator.system;
    exports io.github.frikit.rdg.generator.tech;
    exports io.github.frikit.rdg.generator.text;
    exports io.github.frikit.rdg.generator.user;
    exports io.github.frikit.rdg.generator.user.nationalid;
    exports io.github.frikit.rdg.generator.vehicle;
    exports io.github.frikit.rdg.generator.weather;
}
