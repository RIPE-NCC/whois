package net.ripe.db.nrtm4.domain;

public record NrtmVersionInfo(Long id, NrtmSource source, Long version, String sessionID, NrtmDocumentType type,
                              Integer lastSerialId, long created) {

}
