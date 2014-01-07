package net.ripe.db.rcdummifier;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RcDatabaseDummifierTest {
    final RpslObject mntnerWithPgp = RpslObject.parse("mntner: NINJA\n" +
            "auth: PGP-111\n" +
            "source: test");

    final RpslObject mntnerAfterDummy = RpslObject.parse("mntner: NINJA\n" +
            "auth: md5-pw mwhahaha\n" +
            "source: test");

    @Test
    public void hasPasswordTest() {
        assertThat(RcDatabaseDummifier.DatabaseObjectProcessor.hasPassword(mntnerAfterDummy), is(true));
        assertThat(RcDatabaseDummifier.DatabaseObjectProcessor.hasPassword(mntnerWithPgp), is(false));
    }

    @Test
    public void replacePasswordTest() {
        RpslObject rpslObject = RcDatabaseDummifier.DatabaseObjectProcessor.replaceWithMntnerNamePassword(mntnerAfterDummy);
        RpslAttribute authAttr = rpslObject.findAttribute(AttributeType.AUTH);
        assertThat(PasswordHelper.authenticateMd5Passwords(authAttr.getCleanValue().toString(), "NINJA"), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void replacePgpPasswordTest() {
        RcDatabaseDummifier.DatabaseObjectProcessor.replaceWithMntnerNamePassword(mntnerWithPgp);
    }
}
