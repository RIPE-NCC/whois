package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
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


    @Test
    public void doNotReplacePGP() {
        final RpslObject rpslObject = DatabaseDummifierJmx.DatabaseObjectProcessor.replaceAuthAttributes(mntnerWithPgp);

        assertThat(rpslObject.findAttributes(AttributeType.AUTH), hasSize(1));
        final RpslAttribute authAttr = rpslObject.findAttributes(AttributeType.AUTH).getFirst();
        assertThat(PasswordHelper.authenticateMd5Passwords(authAttr.getCleanValue().toString(), "NINJA"), is(false));
        assertThat(authAttr.getCleanValue(), is("PGP-111"));
    }
}
