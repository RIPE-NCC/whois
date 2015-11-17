package net.ripe.db.whois.changedphase3.util;

public interface ScenarioRunner {
    void before(final Scenario scenario);

    void after(final Scenario scenario);

    String getProtocolName();

    void create(final Scenario scenario);

    void modify(final Scenario scenario);

    void delete(final Scenario scenario);

    void get(final Scenario scenario);

    void search(final Scenario scenario);

    void event(final Scenario scenario);

    void meta(final Scenario scenario);

}
