package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class TelnetRunner extends AbstractScenarioRunner {
    public TelnetRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Telnet";
    }

    @Override
    public void search(final Scenario scenario) {

        try {

            verifyPreCondition(scenario);

            final String result = TelnetWhoisClient.queryLocalhost(context.getQueryServer().getPort(), "-rB TESTING-MNT");

            if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("changed:        " + CHANGED_VALUE));
            } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result, not(containsString("changed:")));
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void meta(final Scenario scenario) {
        try {
            final String result = TelnetWhoisClient.queryLocalhost(context.getQueryServer().getPort(), "-t mntner");

            if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("changed:        [optional]   [multiple]   [ ]"));
            } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result, not(containsString("changed:")));
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

}
