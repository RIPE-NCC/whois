package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseDummifierJmxTest {
    final RpslObject mntnerWithPgp = RpslObject.parse(
            "mntner: NINJA\n" +
            "auth: PGP-111\n" +
            "source: test");

    final RpslObject mntnerAfterDummy = RpslObject.parse(
            "mntner: NINJA\n" +
            "auth: md5-pw mwhahaha\n" +
            "source: test");

    final RpslObject mntnerWithMultiplePasswords = RpslObject.parse(
            "mntner: NINJA\n" +
            "auth: md5-pw mwhahaha\n" +
            "auth: md5-pw minime\n" +
            "source: test");

    @Test
    public void replacePassword() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerAfterDummy);
        final RpslAttribute authAttr = rpslObject.findAttribute(AttributeType.AUTH);
        assertThat(PasswordHelper.authenticateMd5Passwords(authAttr.getCleanValue().toString(), "NINJA"), is(true));
    }

    @Test
    public void replaceMultiplePasswords() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerWithMultiplePasswords);
        assertThat(rpslObject.findAttributes(AttributeType.AUTH), hasSize(1));

        final RpslAttribute authAttr = rpslObject.findAttribute(AttributeType.AUTH);
        assertThat(PasswordHelper.authenticateMd5Passwords(authAttr.getCleanValue().toString(), "NINJA"), is(true));
    }

    @Test
    public void replacePasswordButNotPgp() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerWithPgp);

        assertThat(rpslObject.findAttributes(AttributeType.AUTH), hasSize(2));
        final RpslAttribute authAttr = rpslObject.findAttributes(AttributeType.AUTH).get(0);
        assertThat(PasswordHelper.authenticateMd5Passwords(authAttr.getCleanValue().toString(), "NINJA"), is(true));
    }
}
