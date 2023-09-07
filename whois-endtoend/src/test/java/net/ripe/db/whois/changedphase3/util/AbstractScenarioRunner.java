package net.ripe.db.whois.changedphase3.util;

import com.google.common.collect.Iterables;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractScenarioRunner implements ScenarioRunner {
    protected static final RpslObject TEST_OBJECT = RpslObject.parse("" +
            "mntner:        TESTING-MNT\n" +
            "descr:         Test maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        upd-to@ripe.net\n" +
            "mnt-nfy:       mnt-nfy@ripe.net\n" +
            "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
            "mnt-by:        OWNER-MNT\n" +
            "mnt-by:        TESTING-MNT\n" +
            "created:       2010-11-12T13:14:15Z\n" +
            "source:        TEST\n");
    protected static String CHANGED_VALUE = "test@ripe.net 20121016";
    protected Context context;

    public AbstractScenarioRunner(final Context context) {
        this.context = context;
    }

    @Override
    public void before(final Scenario scenario) {
        // delete if exists
        if (doesObjectExist()) {
            doDelete("TESTING-MNT");
        }

        // create if needed
        if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
            doCreate(MNTNER_WITH_CHANGED());
        } else if (scenario.getPreCond() == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
            doCreate(MNTNER_WITHOUT_CHANGED());
        }
    }

    @Override
    public void after(final Scenario scenario) {}

    protected RpslObject MNTNER_WITHOUT_CHANGED() {
        return TEST_OBJECT;
    }

    protected RpslObject MNTNER_WITH_CHANGED() {
        return new RpslObjectBuilder(TEST_OBJECT)
                // Create more than one changed attribute
                .addAttributeSorted(new RpslAttribute(AttributeType.CHANGED, CHANGED_VALUE))
                .addAttributeSorted(new RpslAttribute(AttributeType.CHANGED, CHANGED_VALUE))
                .get();
    }

    protected RpslObject addRemarks(final RpslObject obj) {
        return new RpslObjectBuilder(obj)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "My remark"))
                .get();
    }

    private boolean doesObjectExist() {
        boolean exists = false;
        try {
            RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT")
                    .request()
                    .get(WhoisResources.class);
            exists = true;
        } catch (NotFoundException nf) {
            // swallow
        }
        return exists;
    }


    private void doDelete(final String uid) {
        try {

            RestTest.target(context.getRestPort(), "whois/test/mntner/" + uid + "?password=123")
                    .request()
                    .delete(WhoisResources.class);
        } catch (ClientErrorException exc) {
            throw exc;
        }
    }

    private void doCreate(final RpslObject obj) {
        context.getDatabaseHelper().addObject(obj);
    }

    protected RpslObject objectForScenario(final Scenario scenario) {
        switch (scenario.getReq()) {
            case NO_CHANGED__:
                return MNTNER_WITHOUT_CHANGED();
            case WITH_CHANGED:
                return MNTNER_WITH_CHANGED();
            default:
                return null;
        }
    }

    protected void verifyPreCondition(final Scenario scenario) {
        if(Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____ == scenario.getPreCond()) {
            final RpslObject result = fetchObjectViaRestApi();
            verifyObject(scenario.getPreCond(), result);
        } else {
            final RpslObject rpslObject = context.getDatabaseHelper().lookupObject(ObjectType.MNTNER, "TESTING-MNT");
            verifyObject(scenario.getPreCond(), rpslObject);
        }
    }

    protected void verifyPostCondition(final Scenario scenario, final Scenario.Result actualResult) {
        assertThat(actualResult, is(scenario.getResult()));
        verifyObject(scenario.getPostCond(), fetchObjectViaRestApi());
    }

    protected void verifyPostCondition(final Scenario scenario, final Scenario.Result actualResult, final WhoisResources whoisResources) {

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final Iterable<Attribute> changed = Iterables.filter(whoisResources.getWhoisObjects().get(0).getAttributes(), input -> input.getName().equals("changed"));
        switch (scenario.getReq()) {
            case WITH_CHANGED:
            case NO_CHANGED__:
                assertThat(changed, emptyIterable());
                break;
            case NOT_APPLIC__:
                break;
        }
        verifyPostCondition(scenario, actualResult);
    }

    protected void verifyPostCondition(final Scenario scenario, final Scenario.Result actualResult, final String respText) {
        if( scenario.getReq() != Scenario.Req.NOT_APPLIC__ ) {
            assertThat(respText, not(containsString("\nchanged:")));
        }
        verifyPostCondition(scenario, actualResult);
    }

    protected void verifyObject(final Scenario.ObjectStatus objectState, final RpslObject result) {
        if (objectState == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
            assertThat(result, is(nullValue()));
        } else {
            assertThat(result, is(notNullValue()));
            if (objectState == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result.containsAttribute(AttributeType.CHANGED), is(true));
                List<RpslAttribute> attrs = result.findAttributes(AttributeType.CHANGED);
                assertThat(attrs,hasSize(2));
                assertThat(attrs.get(0).getValue().trim(), is(CHANGED_VALUE));
                assertThat(attrs.get(1).getValue().trim(), is(CHANGED_VALUE));
            } else if (objectState == Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__) {
                assertThat(result.containsAttribute(AttributeType.CHANGED), is(false));
            }
        }
    }

    private RpslObject fetchObjectViaRestApi() {
        try {
            WhoisResources whoisResources = RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?unfiltered=true&password=123")
                    .request()
                    .get(WhoisResources.class);
            logEvent("Fetch", whoisResources);
            return context.getWhoisObjectMapper().map(whoisResources.getWhoisObjects().get(0), FormattedClientAttributeMapper.class);
        } catch (NotFoundException exc) {
            // swallow
        }
        return null;
    }

    protected void logEvent(final String msg, final String result) {
        if (context.isDebug()) {
            System.err.println(msg + ":" + result);
        }
    }

    protected void logEvent(final String msg, final RpslObject rpslObject) {
        if (context.isDebug()) {
            System.err.println(msg + ":" + rpslObject);
        }
    }

    protected void logEvent(final String msg, final WhoisResources whoisResources) {
        if (context.isDebug()) {
            for (WhoisObject obj : whoisResources.getWhoisObjects()) {
                System.err.println(msg + ":" + context.getWhoisObjectMapper().map(obj, FormattedClientAttributeMapper.class));
            }
            for (ErrorMessage em : whoisResources.getErrorMessages()) {
                System.err.println("Error:" + msg + ":" + em);
            }
        }
    }

    interface Updater {
        void update(final RpslObject obj);
    }

    protected void createObjectViaApi(final RpslObject obj) {
        try {
            RestTest.target(context.getRestPort(), "whois/test/mntner?password=123")
                    .request()
                    .post(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, obj), MediaType.APPLICATION_XML), WhoisResources.class);
        } catch (ClientErrorException exc) {
            logEvent("Create-to-trigger-nrtm-event", exc.getResponse().readEntity(WhoisResources.class));
            throw exc;
        }
    }

    protected void modifyObjectViaApi(final RpslObject obj) {
        try {
            final RpslObject objAdjusted = addRemarks(obj);
            RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .put(Entity.entity(context.getWhoisObjectMapper().mapRpslObjects(FormattedClientAttributeMapper.class, objAdjusted), MediaType.APPLICATION_XML), WhoisResources.class);
        } catch (ClientErrorException exc) {
            logEvent("Modify-to-trigger-nrtm-event", exc.getResponse().readEntity(WhoisResources.class));
            throw exc;
        }
    }

    protected void deleteObjectViaApi(final RpslObject obj) {
        try {
            RestTest.target(context.getRestPort(), "whois/test/mntner/TESTING-MNT?password=123")
                    .request()
                    .delete(WhoisResources.class);
        } catch (ClientErrorException exc) {
            logEvent("Delete-to-trigger-nrtm-event", exc.getResponse().readEntity(WhoisResources.class));
            throw exc;
        }
    }
}
