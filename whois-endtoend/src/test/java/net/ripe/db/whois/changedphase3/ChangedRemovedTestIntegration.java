package net.ripe.db.whois.changedphase3;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static net.ripe.db.whois.changedphase3.util.Scenario.Builder.given;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.CREATE;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.DELETE;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.EVENT_;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.GET___;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.META__;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.MODIFY;
import static net.ripe.db.whois.changedphase3.util.Scenario.Method.SEARCH;
import static net.ripe.db.whois.changedphase3.util.Scenario.Mode.NEW_MODE;
import static net.ripe.db.whois.changedphase3.util.Scenario.Mode.OLD_MODE;
import static net.ripe.db.whois.changedphase3.util.Scenario.ObjectStatus.OBJ_DOES_NOT_EXIST_____;
import static net.ripe.db.whois.changedphase3.util.Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED__;
import static net.ripe.db.whois.changedphase3.util.Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED;
import static net.ripe.db.whois.changedphase3.util.Scenario.Protocol.MAILUPD;
import static net.ripe.db.whois.changedphase3.util.Scenario.Protocol.NRTM___;
import static net.ripe.db.whois.changedphase3.util.Scenario.Protocol.REST___;
import static net.ripe.db.whois.changedphase3.util.Scenario.Protocol.SYNCUPD;
import static net.ripe.db.whois.changedphase3.util.Scenario.Protocol.TELNET_;
import static net.ripe.db.whois.changedphase3.util.Scenario.Req.NOT_APPLIC__;
import static net.ripe.db.whois.changedphase3.util.Scenario.Req.NO_CHANGED__;
import static net.ripe.db.whois.changedphase3.util.Scenario.Req.WITH_CHANGED;
import static net.ripe.db.whois.changedphase3.util.Scenario.Result.FAILED;
import static net.ripe.db.whois.changedphase3.util.Scenario.Result.SUCCESS;


@Category(IntegrationTest.class)
public class ChangedRemovedTestIntegration extends AbstractChangedPhase3IntegrationTest {

    @BeforeClass
    public static void beforeClass() {

        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("feature.toggle.changed.attr.available", "false");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("feature.toggle.changed.attr.available");
    }

    //@Ignore
    @Test
    public void new_mode_rest_test() {
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(REST___, CREATE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(REST___, CREATE, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST___, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST___, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(REST___, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(REST___, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST___, DELETE, NOT_APPLIC__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(REST___, DELETE, NOT_APPLIC__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST___, SEARCH, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(REST___, SEARCH, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST___, GET___, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_WITH_CHANGED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(REST___, GET___, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(REST___, META__, NOT_APPLIC__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
    }

    @Ignore
    @Test
    public void new_mode_telnet_test() {
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(TELNET_, SEARCH, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(TELNET_, SEARCH, NOT_APPLIC__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(TELNET_, META__, NOT_APPLIC__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
    }

    //@Ignore
    @Test
    public void new_mode_syncupdates_test() {
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(SYNCUPD, CREATE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(SYNCUPD, CREATE, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(SYNCUPD, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(SYNCUPD, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, DELETE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, DELETE, NO_CHANGED__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(SYNCUPD, DELETE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(SYNCUPD, DELETE, NO_CHANGED__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
    }

    @Ignore
    @Test
    public void new_mode_mailupdates_test() {
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(MAILUPD, CREATE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(MAILUPD, CREATE, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(MAILUPD, MODIFY, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(MAILUPD, MODIFY, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, DELETE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, DELETE, NO_CHANGED__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(MAILUPD, DELETE, WITH_CHANGED).then(FAILED).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(MAILUPD, DELETE, NO_CHANGED__).then(SUCCESS, OBJ_DOES_NOT_EXIST_____).run(context);
    }

    @Ignore
    @Test
    public void new_mode_nrtm_test() {
        // create
        given(NEW_MODE, OBJ_DOES_NOT_EXIST_____).when(NRTM___, EVENT_, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

        // modify
        given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(NRTM___, EVENT_, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);
        given(NEW_MODE, OBJ_EXISTS_NO_CHANGED__).when(NRTM___, EVENT_, NO_CHANGED__).then(SUCCESS, OBJ_EXISTS_NO_CHANGED__).run(context);

    }
}
