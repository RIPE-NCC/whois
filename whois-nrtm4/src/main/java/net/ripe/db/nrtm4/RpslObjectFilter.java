package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;


public class RpslObjectFilter {

    private final DummifierNrtm dummifierNrtm;

    private final static int nrtmVersionNumber = 4;

    RpslObjectFilter(final DummifierNrtm dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    RpslObject filter(final RpslObject in) {
        return dummifierNrtm.dummify(nrtmVersionNumber, in);
    }

}
