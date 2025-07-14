package net.ripe.db.nrtm4.domain;

public record NrtmKeyRecord(long id, byte[] privateKey, String pemFormat, boolean isActive, long createdTimestamp, long expires) {

    public static NrtmKeyRecord of(final byte[] privateKey, final String pemFormat, final boolean isActive, final long createdTimestamp, final long expires) {
        return new NrtmKeyRecord(0, privateKey, pemFormat, isActive, createdTimestamp, expires);
    }
}

