module io.github.frikit.rdg.examples.jpms.open {
    requires io.github.frikit.rdg;

    opens io.github.frikit.rdg.examples.jpms.openconsumer to
            io.github.frikit.rdg;
}
