package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.domain.AbuseResources;

public class AbuseContactMapper {

    public static AbuseResources mapAbuseContactError(final String errorMessage) {
        return new AbuseResources(errorMessage);
    }
}
