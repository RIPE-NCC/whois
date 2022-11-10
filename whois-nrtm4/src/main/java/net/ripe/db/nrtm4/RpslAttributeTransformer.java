package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Component;


/**
 * Replaces some attribute values with dummy values to prevent leaking potentially sensitive information
 */
@Component
public class RpslAttributeTransformer {

    private final DummifierNrtm dummifierNrtm;

    private final static int nrtmVersionNumber = 4;

    RpslAttributeTransformer(final DummifierNrtm dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    RpslObject filter(final RpslObject in) {
        return dummifierNrtm.dummify(nrtmVersionNumber, in);
    }

}
