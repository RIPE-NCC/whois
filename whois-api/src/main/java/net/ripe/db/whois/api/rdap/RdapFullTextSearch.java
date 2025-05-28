package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;

import java.io.IOException;
import java.util.List;

public interface RdapFullTextSearch {

    List<RpslObject> performSearch(final String[] fields, final String term, final String remoteAddr,
                                   final Source source) throws IOException;
}
