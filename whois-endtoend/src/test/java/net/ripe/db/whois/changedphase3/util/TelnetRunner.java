package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TelnetRunner extends AbstactScenarioRunner {
    public TelnetRunner(Context context) {
        super(context);
    }

    public String getProtocolName() {
        return "Telnet";
    }

    public void search(Scenario scenario) {

        try {

            verifyPreCondition(scenario);

            final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-rB TESTING-MNT");

            if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("changed:        " + CHANGED_VALUE));
            } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result, not(containsString("changed:")));
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void meta(Scenario scenario) {
        try {
            final String result = TelnetWhoisClient.queryLocalhost(QueryServer.port, "-t mntner");

            if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("changed:        [optional]   [multiple]   [ ]"));
            } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result, not(containsString("changed:")));
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

}
