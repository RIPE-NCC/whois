package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class SyncUpdateRunner extends AbstractScenarioRunner {
    public SyncUpdateRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "SyncUpdate";
    }

    @Override
    public void create(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            logEvent("Creating", objectForScenario);

            String result = RestTest.target(context.getSyncUpdatePort(), "whois/syncupdates/test")
                    .request()
                    .post(Entity.entity("DATA=" +
                                            SyncUpdateUtils.encode(objectForScenario + "password: 123\n") + "&NEW=yes",
                                    MediaType.valueOf("application/x-www-form-urlencoded")),
                            String.class);

            logEvent("Created", result);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(result, containsString("Create SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, result);
            } else {
                assertThat(result, containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void modify(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = addRemarks(objectForScenario(scenario));

            logEvent("Modifying", objectForScenario);

            String result = RestTest.target(context.getSyncUpdatePort(), "whois/syncupdates/test")
                    .request()
                    .post(Entity.entity("DATA=" +
                                            SyncUpdateUtils.encode(objectForScenario + "password: 123\n") + "&NEW=no",
                                    MediaType.valueOf("application/x-www-form-urlencoded")),
                            String.class);

            logEvent("Modified", result);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(result, containsString("Modify SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, result);
            } else {
                assertThat(result, containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (Exception exc) {
            exc.printStackTrace();
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void delete(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            logEvent("Deleting", objectForScenario);

            String result = RestTest.target(context.getSyncUpdatePort(), "whois/syncupdates/test")
                    .request()
                    .post(Entity.entity("DATA=" +
                                            SyncUpdateUtils.encode(objectForScenario +
                                                    "delete: My delete reason\n" +
                                                    "password: 123\n"),
                                    MediaType.valueOf("application/x-www-form-urlencoded")),
                            String.class);

            logEvent("Deleted", result);

            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                assertThat(result, containsString("Delete SUCCEEDED: [mntner] TESTING-MNT"));
                verifyPostCondition(scenario, Scenario.Result.SUCCESS, result);
            } else {
                assertThat(result, containsString("***Error:"));
                verifyPostCondition(scenario, Scenario.Result.FAILURE);
            }

        } catch (Exception exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }
}
