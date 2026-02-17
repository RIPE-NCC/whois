package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

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
    public void removePassword() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerAfterDummy);
        assertThat(rpslObject.containsAttribute(AttributeType.AUTH), is(false));
    }

    @Test
    public void removeMultiplePasswords() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerWithMultiplePasswords);
        assertThat(rpslObject.findAttributes(AttributeType.AUTH), hasSize(0));
    }

    @Test
    public void removePasswordButNotPgp() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerWithPgp);

        assertThat(rpslObject.findAttributes(AttributeType.AUTH), hasSize(1));
        final RpslAttribute dummyPGP = rpslObject.findAttributes(AttributeType.AUTH).getFirst();
        assertThat(dummyPGP.getCleanValue(), is("PGP-111"));
    }
}
