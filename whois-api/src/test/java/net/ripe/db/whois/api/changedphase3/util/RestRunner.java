package net.ripe.db.whois.api.changedphase3.util;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class RestRunner extends AbstactScenarioRunner {

    public RestRunner(Context context) {
        super(context);
    }

    public String getProtocolName() {
        return "Rest";
    }

    public void create(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            logEvent("Creating", objectForScenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner?password=123")
                    .request()
                    .post(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);

            logEvent("Created", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            logEvent("Create", exc.getResponse().readEntity(WhoisResources.class));

            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void modify(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = addRemarks(objectForScenario(scenario));

            logEvent("Modifying", objectForScenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .put(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);

            logEvent("Modified", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            logEvent("Modify", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void delete(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .delete(WhoisResources.class);

            logEvent("Deleted", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            logEvent("Delete", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void get(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?unfiltered=true&password=123")
                    .request()
                    .get(WhoisResources.class);
            List<RpslObject> result = context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class);
            assertThat(result, hasSize(1));

            logEvent("Got", result.get(0));

            verifyObject(scenario.getPostCond(), result.get(0));

        } catch (ClientErrorException exc) {
            logEvent("Get", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void search(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            final WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/search?query-string=TESTING-MNT&source=TEST&flags=rB")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            List<RpslObject> searchResults = context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class);
            assertThat(searchResults, hasSize(1));

            logEvent("Search", searchResults.get(0));

            verifyObject(scenario.getPostCond(), searchResults.get(0));

        } catch (ClientErrorException exc) {
            logEvent("Search", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void meta(Scenario scenario) {
        try {
            String result = RestTest.target(context.getRestPort(),
                    "whois/metadata/templates/mntner")
                    .request()
                    .get(String.class);

            if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("<attribute name=\"changed\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\""));
            } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result, not(containsString("<attribute name=\"changed\"")));
            }
        } catch (ClientErrorException exc) {
            logEvent("Meta", exc.getResponse().readEntity(WhoisResources.class));
            assertThat(scenario.getResult(), not(is(Scenario.Result.SUCCESS)));
        }
    }
}
