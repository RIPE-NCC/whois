package net.ripe.db.whois.api.changedphase3;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static net.ripe.db.whois.api.changedphase3.Scenario.Method.CREATE;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.DELETE;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.EVENT;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.GET;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.META;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.MODIFY;
import static net.ripe.db.whois.api.changedphase3.Scenario.Method.SEARCH;
import static net.ripe.db.whois.api.changedphase3.Scenario.Mode.NEW_MODE;
import static net.ripe.db.whois.api.changedphase3.Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED;
import static net.ripe.db.whois.api.changedphase3.Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED;
import static net.ripe.db.whois.api.changedphase3.Scenario.ObjectStatus.OBJ_NO_EXISTS;
import static net.ripe.db.whois.api.changedphase3.Scenario.Protocol.MAILUPD;
import static net.ripe.db.whois.api.changedphase3.Scenario.Protocol.NRTM;
import static net.ripe.db.whois.api.changedphase3.Scenario.Protocol.REST;
import static net.ripe.db.whois.api.changedphase3.Scenario.Protocol.SYNCUPD;
import static net.ripe.db.whois.api.changedphase3.Scenario.Protocol.TELNET;
import static net.ripe.db.whois.api.changedphase3.Scenario.Req.NO_CHANGED;
import static net.ripe.db.whois.api.changedphase3.Scenario.Req.WITH_CHANGED;
import static net.ripe.db.whois.api.changedphase3.Scenario.Result.FAILED;
import static net.ripe.db.whois.api.changedphase3.Scenario.Result.SUCCESS;


@Category(IntegrationTest.class)
public class NewModeTest extends AbstractChangedPhase3Test {

    @Test
    public void new_mode_rest_test() {
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(REST, CREATE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(REST, CREATE, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(REST, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(REST, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST, DELETE).then(SUCCESS, OBJ_NO_EXISTS).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(REST, DELETE).then(SUCCESS, OBJ_NO_EXISTS).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST, SEARCH).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(REST, SEARCH).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(REST, GET).then(SUCCESS, OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(REST, GET).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(REST, META).then(SUCCESS, OBJ_NO_EXISTS).run();
    }

    @Test
    public void new_mode_telnet_test() {
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(TELNET, SEARCH).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(TELNET, SEARCH).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(TELNET, META).then(SUCCESS, OBJ_NO_EXISTS).run();
    }

    @Test
    public void new_mode_syncupdates_test() {
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(SYNCUPD, CREATE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(SYNCUPD, CREATE, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(SYNCUPD, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(SYNCUPD, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, DELETE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(SYNCUPD, DELETE, NO_CHANGED).then(SUCCESS, OBJ_NO_EXISTS).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(SYNCUPD, DELETE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(SYNCUPD, DELETE, NO_CHANGED).then(SUCCESS, OBJ_NO_EXISTS).run();
    }

    @Test
    public void new_mode_mailupdates_test() {
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(MAILUPD, CREATE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_NO_EXISTS).when(MAILUPD, CREATE, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(MAILUPD, MODIFY, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(MAILUPD, MODIFY, NO_CHANGED).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, DELETE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(MAILUPD, DELETE, NO_CHANGED).then(SUCCESS, OBJ_NO_EXISTS).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(MAILUPD, DELETE, WITH_CHANGED).then(FAILED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(MAILUPD, DELETE, NO_CHANGED).then(SUCCESS, OBJ_NO_EXISTS).run();
    }

    @Test
    public void new_mode_nrtm_test() {
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_WITH_CHANGED).when(NRTM, EVENT).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(NEW_MODE, OBJ_EXISTS_NO_CHANGED).when(TELNET, EVENT).then(SUCCESS, OBJ_EXISTS_NO_CHANGED).run();
    }

}
