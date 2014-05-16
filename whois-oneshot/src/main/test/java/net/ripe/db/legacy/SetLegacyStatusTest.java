package net.ripe.db.legacy;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

public class SetLegacyStatusTest {

    SetLegacyStatus subject;

    @Before
    public void setup() {
        subject = new SetLegacyStatus(null, null, null, null, true);
    }

    @Test
    public void testSetLegacyStatusWithoutHmMnt() {
        subject.setLegacyStatus(RpslObject.parse("" +
                "inetnum:        192.109.254.0 - 192.109.255.255\n" +
                "netname:        PLASMA-LAN\n" +
                "descr:          Consulting und Service fuer Software\n" +
                "country:        DE\n" +
                "admin-c:        HS-RIPE\n" +
                "tech-c:         HS-RIPE\n" +
                "status:         ASSIGNED PI\n" +
                "notify:         hsc@de.uu.net\n" +
                "notify:         Schaefer@Wei.de\n" +
                "mnt-by:         RIPE-NCC-END-MNT\n" +
                "mnt-lower:      RIPE-NCC-END-MNT\n" +
                "mnt-by:         PLASMA-MNT\n" +
                "mnt-routes:     PLASMA-MNT\n" +
                "mnt-domains:    PLASMA-MNT\n" +
                "mnt-by:         UUNETDE-I\n" +
                "mnt-routes:     UUNETDE-I\n" +
                "mnt-domains:    UUNETDE-I\n" +
                "changed:        cp@deins.Informatik.Uni-Dortmund.DE 19920904\n" +
                "changed:        ripe-dbm@ripe.net 19990706\n" +
                "changed:        hsc@de.uu.net 20010207\n" +
                "changed:        eho@de.uu.net 20010525\n" +
                "changed:        hostmaster@ripe.net 20120524\n" +
                "source:         RIPE"));
    }

    @Test
    public void testSetLegacyStatusWithHmMnt() {
        subject.setLegacyStatus(RpslObject.parse("" +
                "inetnum:        128.16.0.0 - 128.16.255.255\n" +
                "netname:        UCL-CS-ETHER\n" +
                "org:            ORG-UCL4-RIPE\n" +
                "descr:          University College London\n" +
                "descr:          Information Systems,  Gower Street\n" +
                "descr:          London, WC1E 6BT\n" +
                "country:        GB\n" +
                "admin-c:        PK\n" +
                "tech-c:         JA863-RIPE\n" +
                "status:         EARLY-REGISTRATION\n" +
                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                "mnt-by:         JANET-HOSTMASTER\n" +
                "mnt-lower:      JANET-HOSTMASTER\n" +
                "mnt-routes:     JANET-HOSTMASTER\n" +
                "changed:        hostmaster@arin.net 19970101\n" +
                "changed:        hostmaster@arin.net 20001201\n" +
                "changed:        er-transfer@ripe.net 20040405\n" +
                "changed:        hostmaster@ripe.net 20120803\n" +
                "changed:        hostmaster@ripe.net 20120820\n" +
                "changed:        ipaddress@ja.net 20120821\n" +
                "source:         RIPE"));

    }
}
