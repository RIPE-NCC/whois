package net.ripe.db.nrtm4.domain;

public record NrtmVersionInfo(Long id, NrtmSource source, Long version, String sessionID, NrtmDocumentType type,
                              Integer lastSerialId, long created) {


    public static NrtmVersionInfo of(final NrtmSource source, final Long version, final String sessionID, final NrtmDocumentType type,
                                     final Integer lastSerialId, final long created) {
        return new NrtmVersionInfo(0L, source, version, sessionID, type, lastSerialId, created);
    }


}
