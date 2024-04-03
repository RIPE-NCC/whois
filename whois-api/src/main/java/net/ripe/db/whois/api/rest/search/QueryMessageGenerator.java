package net.ripe.db.whois.api.rest.search;

import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

public interface QueryMessageGenerator {

    void generate(final RpslObject rpslObject, final Parameters parameters, final List<String> messages);
}
