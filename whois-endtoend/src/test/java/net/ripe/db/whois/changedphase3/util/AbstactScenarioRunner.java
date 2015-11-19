package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class AbstactScenarioRunner implements ScenarioRunner {
    protected static final RpslObject TEST_OBJECT = RpslObject.parse("" +
            "mntner:        TESTING-MNT\n" +
            "descr:         Test maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        upd-to@ripe.net\n" +
            "mnt-nfy:       mnt-nfy@ripe.net\n" +
            "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
            "mnt-by:        OWNER-MNT\n" +
            "mnt-by:        TESTING-MNT\n" +
            "source:        TEST\n");
    protected static String CHANGED_VALUE = "test@ripe.net 20121016";
    protected Context context;

    public AbstactScenarioRunner(final Context context) {
        this.context = context;
    }

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


    public void after(final Scenario scenario) {
    }

    public void create(final Scenario scenario) {
        throw new UnsupportedOperationException("Create method not supported for protocol " + getProtocolName());
    }

    public void modify(final Scenario scenario) {
        throw new UnsupportedOperationException("Modify method not supported for protocol " + getProtocolName());
    }

    public void delete(final Scenario scenario) {
        throw new UnsupportedOperationException("Delete method not supported for protocol " + getProtocolName());
    }

    public void get(final Scenario scenario) {
        throw new UnsupportedOperationException("Get method not supported for protocol " + getProtocolName());
    }

    public void search(Scenario scenario) {
        throw new UnsupportedOperationException("Search method not supported for protocol " + getProtocolName());
    }

    public void event(final Scenario scenario) {
        throw new UnsupportedOperationException("Event method not supported for protocol " + getProtocolName());
    }

    public void meta(final Scenario scenario) {
        throw new UnsupportedOperationException("Meta method not supported for protocol " + getProtocolName());
    }


    protected RpslObject MNTNER_WITHOUT_CHANGED() {
        return TEST_OBJECT;
    }

    protected RpslObject MNTNER_WITH_CHANGED() {
        return new RpslObjectBuilder(TEST_OBJECT)
                .addAttributeSorted(new RpslAttribute(AttributeType.CHANGED, CHANGED_VALUE))
                .get();
    }

    protected RpslObject addRemarks(final RpslObject obj) {
        return new RpslObjectBuilder(obj)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "My remark"))
                .get();
    }

    protected boolean doesObjectExist() {
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


    protected void doDelete(final String uid) {
        try {

            RestTest.target(context.getRestPort(), "whois/test/mntner/" + uid + "?password=123")
                    .request()
                    .delete(WhoisResources.class);
        } catch (ClientErrorException exc) {
            throw exc;
        }
    }

    protected void doCreate(final RpslObject obj) {
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

    public void verifyObject(final Scenario.ObjectStatus objectState, final RpslObject result) {
        if (objectState == Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____) {
            assertThat(result, is(nullValue()));
        } else {
            assertThat(result, is(notNullValue()));
            if (objectState == Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED) {
                assertThat(result.containsAttribute(AttributeType.CHANGED), is(true));
                assertThat(result.findAttribute(AttributeType.CHANGED).getValue().trim(), is(CHANGED_VALUE.trim()));
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

}
