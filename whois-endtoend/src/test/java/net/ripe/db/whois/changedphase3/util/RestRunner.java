package net.ripe.db.whois.changedphase3.util;

import jakarta.mail.MessagingException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

public class RestRunner extends AbstractScenarioRunner {

    public RestRunner(final Context context) {
        super(context);
    }

    @Override
    public String getProtocolName() {
        return "Rest";
    }

    @Override
    public void create(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            resetMail();

            RpslObject objectForScenario = objectForScenario(scenario);

            logEvent("Creating", objectForScenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner?password=123")
                    .request()
                    .post(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);

            logEvent("Created", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS, whoisResources);

            verifyNotificationEmail(scenario);

        } catch (ClientErrorException exc) {
            logEvent("Create", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
            verifyNotificationEmail(scenario);
        }
    }

    @Override
    public void modify(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            resetMail();

            RpslObject objectForScenario = addRemarks(objectForScenario(scenario));

            logEvent("Modifying", objectForScenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .put(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);

            logEvent("Modified", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS, whoisResources);

            verifyNotificationEmail(scenario);

        } catch (ClientErrorException exc) {
            logEvent("Modify", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void delete(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            resetMail();

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .delete(WhoisResources.class);

            logEvent("Deleted", whoisResources);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS, whoisResources);

            verifyNotificationEmail(scenario);

        } catch (ClientErrorException exc) {
            logEvent("Delete", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
            verifyNotificationEmail(scenario);
        }
    }

    @Override
    public void get(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/test/mntner/TESTING-MNT?unfiltered=true&password=123")
                    .request()
                    .get(WhoisResources.class);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS, whoisResources);

            List<RpslObject> result = context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class);
            assertThat(result, hasSize(1));

            logEvent("Got", result.get(0));

            verifyObject(scenario.getPostCond(), result.get(0));

        } catch (ClientErrorException exc) {
            logEvent("Get", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void search(final Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            final WhoisResources whoisResources = RestTest.target(context.getRestPort(),
                    "whois/search?query-string=TESTING-MNT&source=TEST&flags=rB")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);

            verifyPostCondition(scenario, Scenario.Result.SUCCESS, whoisResources);

            List<RpslObject> searchResults = context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class);
            assertThat(searchResults, hasSize(1));

            logEvent("Search", searchResults.get(0));

            verifyObject(scenario.getPostCond(), searchResults.get(0));

        } catch (ClientErrorException exc) {
            logEvent("Search", exc.getResponse().readEntity(WhoisResources.class));
            verifyPostCondition(scenario, Scenario.Result.FAILURE);
        }
    }

    @Override
    public void meta(final Scenario scenario) {
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

    private void resetMail() {
        context.getMailSenderStub().reset();
    }

    private void verifyNotificationEmail(final Scenario scenario) {
        try {
            if (scenario.getResult() == Scenario.Result.FAILURE &&
                    (scenario.getMethod() == Scenario.Method.MODIFY || scenario.getMethod() == Scenario.Method.DELETE)) {
                verifyMailContents(scenario, context.getMailSenderStub().getMessage("upd-to@ripe.net").getContent().toString());
            }
            if (scenario.getResult() == Scenario.Result.SUCCESS) {
                verifyMailContents(scenario, context.getMailSenderStub().getMessage("mnt-nfy@ripe.net").getContent().toString());
            }
            assertThat(context.getMailSenderStub().anyMoreMessages(), is(false));
        } catch (MessagingException | IOException exc) {
            fail();
        }
    }

    private void verifyMailContents(Scenario scenario, String mailContents) {
        if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
            // note the starting \n
            assertThat(mailContents, containsString("\nchanged:        " + CHANGED_VALUE));
        } else if (scenario.getPostCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
            // note the starting \n
            assertThat(mailContents, not(containsString("\nchanged:")));
        }
    }
}
