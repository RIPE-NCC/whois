package net.ripe.db.nrtm4.client.scheduler;

public record NrtmVersionInfo(Long id, String source, Long version, String sessionID, NrtmDocumentType type
        , long created) {


    public static NrtmVersionInfo of(final String source, final Long version, final String sessionID,
                                     final NrtmDocumentType type, final long created) {
        return new NrtmVersionInfo(0L, source, version, sessionID, type, created);
    }


}
