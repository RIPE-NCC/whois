package net.ripe.db.whois.api.changedphase3;

public interface ScenarioRunner {
    void before(Scenario scenario);

    void after(Scenario scenario);

    String getProtocolName();

    void create(Scenario scenario);

    void modify(Scenario scenario);

    void delete(Scenario scenario);

    void get(Scenario scenario);

    void search(Scenario scenario);

    void event(Scenario scenario);

    void meta(Scenario scenario);

}
