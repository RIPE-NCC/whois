package net.ripe.db.whois.api.changedphase3;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class OldModeTest extends AbstractChangedPhase3Test {


    @Test
    public void old_mode_rest_test() {
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.REST, Scenario.Method.CREATE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.REST, Scenario.Method.CREATE, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.DELETE).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.DELETE).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.SEARCH).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.REST, Scenario.Method.SEARCH).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.REST, Scenario.Method.META).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
    }

    @Test
    public void old_mode_telnet_test() {
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.TELNET, Scenario.Method.SEARCH).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.TELNET, Scenario.Method.SEARCH).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.TELNET, Scenario.Method.META).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
    }

    @Test
    public void old_mode_syncupdates_test() {
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.SYNCUPD, Scenario.Method.CREATE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.SYNCUPD, Scenario.Method.CREATE, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.DELETE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.DELETE, Scenario.Req.NO_CHANGED).then(Scenario.Result.FAILED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.DELETE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.FAILED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.SYNCUPD, Scenario.Method.DELETE, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();

    }

    @Test
    public void old_mode_mailupdates_test() {
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.MAILUPD, Scenario.Method.CREATE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_NO_EXISTS).when(Scenario.Protocol.MAILUPD, Scenario.Method.CREATE, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.MODIFY, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.MODIFY, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();

        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.DELETE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.DELETE, Scenario.Req.NO_CHANGED).then(Scenario.Result.FAILED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.DELETE, Scenario.Req.WITH_CHANGED).then(Scenario.Result.FAILED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.MAILUPD, Scenario.Method.DELETE, Scenario.Req.NO_CHANGED).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_NO_EXISTS).run();

    }

    @Test
    public void old_mode_nrtm_test() {
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).when(Scenario.Protocol.NRTM, Scenario.Method.EVENT).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_WITH_CHANGED).run();
        Scenario.Builder.given(Scenario.Mode.OLD_MODE, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).when(Scenario.Protocol.TELNET, Scenario.Method.EVENT).then(Scenario.Result.SUCCESS, Scenario.ObjectStatus.OBJ_EXISTS_NO_CHANGED).run();
    }

}
