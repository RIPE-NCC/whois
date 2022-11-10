package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class RpslAttributeTransformerTest {

    private final RpslAttributeTransformer rpslAttributeTransformer = new RpslAttributeTransformer(new DummifierNrtm());

    @Test
    public void test_filtering_works() {
        final String inetnumStr = "" +
                "inetnum:         193.0.0.0 - 193.0.7.255\n" +
                "netname:         RIPE-NCC\n" +
                "descr:           RIPE Network Coordination Centre\n" +
                "org:             ORG-RIEN1-RIPE\n" +
                "descr:           Amsterdam, Netherlands\n" +
                "remarks:         Used for RIPE NCC infrastructure.\n" +
                "country:         NL\n" +
                "admin-c:         BRD-RIPE\n" +
                "tech-c:          OPS4-RIPE\n" +
                "status:          ASSIGNED PA\n" +
                "mnt-by:          RIPE-NCC-MNT\n" +
                "created:         2003-03-17T12:15:57Z\n" +
                "last-modified:   2017-12-04T14:42:31Z\n" +
                "source:          RIPE";
        final String expectedStr = "" +
                "inetnum:        193.0.0.0 - 193.0.7.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          RIPE Network Coordination Centre\n" +
                "org:            ORG-RIEN1-RIPE\n" +
                "descr:          Amsterdam, Netherlands\n" +
                "remarks:        Used for RIPE NCC infrastructure.\n" +
                "country:        NL\n" +
                "admin-c:        DUMY-RIPE\n" +
                "tech-c:         DUMY-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         RIPE-NCC-MNT\n" +
                "created:        2003-03-17T12:15:57Z\n" +
                "last-modified:  2017-12-04T14:42:31Z\n" +
                "source:         RIPE\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n";
        final RpslObject inetnumRpsl = RpslObject.parse(inetnumStr);

        final RpslObject actual = rpslAttributeTransformer.filter(inetnumRpsl);
        final RpslObject expected = RpslObject.parse(expectedStr);

        assertThat(actual, is(expected));
    }

}
