package net.ripe.db.whois.api.rest.search;

import net.ripe.db.whois.api.rest.domain.InfoMessage;
import net.ripe.db.whois.api.rest.domain.InfoMessages;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.common.rpsl.RpslObject;

public interface QueryMessageGenerator {

    InfoMessage generate(final RpslObject rpslObject, final Parameters parameters);
}
