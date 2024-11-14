package net.ripe.db.nrtm4.client.dao;

public record NrtmClientVersionInfo(Long id, String source, Long version, String sessionID, NrtmClientDocumentType type
        , long created) {


    public static NrtmClientVersionInfo of(final String source, final Long version, final String sessionID,
                                           final NrtmClientDocumentType type, final long created) {
        return new NrtmClientVersionInfo(0L, source, version, sessionID, type, created);
    }

}
