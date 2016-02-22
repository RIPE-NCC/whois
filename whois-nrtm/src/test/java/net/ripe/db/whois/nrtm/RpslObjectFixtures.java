package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class RpslObjectFixtures {

    public static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "description: creation\n" +
            "source: TEST");

    public static final RpslObject MNTNER_UPDATED = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "description: modification\n" +
            "source: TEST");


    public static final RpslObject TEST1_MNT = RpslObject.parse("" +
            "mntner: TEST1-MNT\n" +
            "description: first\n" +
            "source: TEST");

    public static final RpslObject TEST2_MNT = RpslObject.parse("" +
            "mntner: TEST2-MNT\n" +
            "description: second\n" +
            "source: TEST");

}
