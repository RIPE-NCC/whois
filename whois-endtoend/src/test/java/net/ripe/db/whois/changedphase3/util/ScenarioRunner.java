package net.ripe.db.whois.changedphase3.util;

public interface ScenarioRunner {
    String getProtocolName();

    void before(final Scenario scenario);

    void after(final Scenario scenario);

    default void create(final Scenario scenario) {
        throw new UnsupportedOperationException("Create method not supported for protocol " + getProtocolName());
    }

    default void modify(final Scenario scenario) {
        throw new UnsupportedOperationException("Modify method not supported for protocol " + getProtocolName());
    }

    default void delete(final Scenario scenario) {
        throw new UnsupportedOperationException("Delete method not supported for protocol " + getProtocolName());
    }

    default void get(final Scenario scenario) {
        throw new UnsupportedOperationException("Get method not supported for protocol " + getProtocolName());
    }

    default void search(Scenario scenario) {
        throw new UnsupportedOperationException("Search method not supported for protocol " + getProtocolName());
    }

    default void meta(final Scenario scenario) {
        throw new UnsupportedOperationException("Meta method not supported for protocol " + getProtocolName());
    }

}
