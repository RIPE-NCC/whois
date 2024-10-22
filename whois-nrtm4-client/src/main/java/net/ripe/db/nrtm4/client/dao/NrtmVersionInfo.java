package net.ripe.db.nrtm4.client.dao;

public record NrtmVersionInfo(Long id, String source, Long version, String sessionID, String type
        , long created) {


    public static NrtmVersionInfo of(final String source, final Long version, final String sessionID,
                                     final String type, final long created) {
        return new NrtmVersionInfo(0L, source, version, sessionID, type, created);
    }

}
