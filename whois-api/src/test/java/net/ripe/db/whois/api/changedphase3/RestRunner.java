package net.ripe.db.whois.api.changedphase3;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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

            //System.err.println("Creating:" + objectForScenario);
            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner?password=123")
                    .request()
                    .post(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);
            //System.err.println("Created:" + context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class));

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
//            WhoisResources whoisResources = exc.getResponse().readEntity(WhoisResources.class);
//            for(ErrorMessage em: whoisResources.getErrorMessages() ) {
//                System.err.println(em);
//            }
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void modify(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            RpslObject objectForScenario = objectForScenario(scenario);

            //System.err.println("Modifying:" + objectForScenario);
            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .put(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objectForScenario),
                                    MediaType.APPLICATION_XML),
                            WhoisResources.class);
            //System.err.println("Modified:" + context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class));

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void delete(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .delete(WhoisResources.class);
            //System.err.println("Deleted:" + context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class));

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void get(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .get(WhoisResources.class);
            //System.err.println("Got:" + context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class));

            verifyPostCondition(scenario, Scenario.Result.SUCCESS);

        } catch (ClientErrorException exc) {
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void search(Scenario scenario) {
        try {
            verifyPreCondition(scenario);

            final WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/search?query-string=TESTING-MNT&source=TEST&flags=r")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            List<RpslObject> searchResult = context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class);

            // verify response
            assertThat(searchResult, hasSize(1));

        } catch (ClientErrorException exc) {
            WhoisResources whoisResources = exc.getResponse().readEntity(WhoisResources.class);
//            for(ErrorMessage em: whoisResources.getErrorMessages() ) {
//                System.err.println(em);
//            }
            verifyPostCondition(scenario, Scenario.Result.FAILED);
        }
    }

    public void meta(Scenario scenario) {
        try {
            String result = RestTest.target(context.getRestPort(), "whois/metadata/templates/mntner")
                .request()
                .get(String.class);
            //System.err.println("Meta:" + result);

            if( scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result, containsString("<attribute name=\"changed\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\""));
            } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__){
                assertThat(result, not(containsString("<attribute name=\"changed\"")));
            }
        } catch (ClientErrorException exc) {
            assertThat(scenario.getResult(), not(is(Scenario.Result.SUCCESS)));
        }
    }

    public RpslObject objectForScenario(final Scenario scenario) {
        RpslObject obj = null;
        if (scenario.getReq() == Scenario.Req.WITH_CHANGED) {
            obj = MNTNER_WITH_CHANGED();
        } else if (scenario.getReq() == Scenario.Req.NO_CHANGED__) {
            obj = MNTNER_WITHOUT_CHANGED();
        }
        return obj;
    }

    private void verifyPreCondition(final Scenario scenario) {
        verifyState(scenario.getPreCond());
    }

    private void verifyPostCondition(final Scenario scenario, Scenario.Result actualResult) {
        assertThat(actualResult, is(scenario.getResult()));
        verifyState(scenario.getPostCond());
    }

    private void verifyState(Scenario.ObjectStatus objectState) {
        RpslObject result = fetchObject();

        if (objectState == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
            assertThat(result, is(nullValue()));
        } else {
            assertThat(result, is(notNullValue()));
            if (objectState == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result.containsAttribute(AttributeType.CHANGED), is(true));
            } else if (objectState == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result.containsAttribute(AttributeType.CHANGED), is(false));
            }
        }
    }

    private RpslObject fetchObject() {
        try {
            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?unfiltered=true&password=123")
                    .request()
                    .get(WhoisResources.class);
            //System.err.println("Fetch:" + context.getWhoisObjectMapper().mapWhoisObjects(whoisResources.getWhoisObjects(), FormattedClientAttributeMapper.class));

            return context.getWhoisObjectMapper().map(whoisResources.getWhoisObjects().get(0), FormattedClientAttributeMapper.class);
        } catch (NotFoundException exc) {
            // swallow
        }
        return null;
    }


}
