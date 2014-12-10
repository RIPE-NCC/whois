package net.ripe.db.whois.common.rpsl;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RpslObjectBuilderTest {

    @Test
    public void getAttributes_doesNotDieOnRpslObjectWithNewlines() {
        final String objectString = "" +
                "domain:         10.10.10.in-addr.arpa\n" +
                "descr:          test\n" +
                "\n" +
                "admin-c:        TEST1-RIPE\n" +
                "\n" +
                "tech-c:         TEST1-RIPE\n" +
                "\n" +
                "zone-c:         TEST1-RIPE\n" +
                "\n" +
                "\n" +
                "nserver:        test1.server.nl\n" +
                "nserver:        test2.server.nl\n" +
                "\n" +
                "mnt-by:         KWS-MNT\n" +
                "\n" +
                "changed:        test@ripe.net 20141119\n" +
                "source:         RIPE\n";

        final RpslObject object = new RpslObject(RpslObjectBuilder.getAttributes(objectString));

        assertThat(object.toString(), is(objectString));
    }
}