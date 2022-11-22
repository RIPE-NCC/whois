package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Component;

import static net.ripe.db.nrtm4.Constants.NRTM_VERSION;


/**
 * Replaces some attribute values with dummy values to prevent leaking potentially sensitive information
 */
@Component
public class RpslObjectTransformer {

    private final DummifierNrtm dummifierNrtm;

    RpslObjectTransformer(final DummifierNrtm dummifierNrtm) {
        this.dummifierNrtm = dummifierNrtm;
    }

    RpslObject filter(final RpslObject in) {
        // do we need to convert latin1 to utf8? nrtm db is utf8 (coz we serve json as utf8)
        return dummifierNrtm.dummify(NRTM_VERSION, in);
    }

}
