package net.ripe.db.whois.api.autocomplete;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AutocompleteSearch {

    List<Map<String, Object>> search(final String queryString, final Set<AttributeType> queryAttributes, final Set<AttributeType> responseAttributes, final Set<ObjectType> objectTypes) throws IOException;
}
