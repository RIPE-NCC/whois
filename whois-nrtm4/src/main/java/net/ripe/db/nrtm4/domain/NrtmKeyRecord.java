package net.ripe.db.nrtm4.domain;

public record NrtmKeyRecord(long id, byte[] privateKey, byte[] publicKey, boolean isActive, long createdTimestamp, long expires) {

    public static NrtmKeyRecord of(final byte[] privateKey, final byte[] publicKey, final boolean isActive, final long createdTimestamp, final long expires) {
        return new NrtmKeyRecord(0, privateKey, publicKey, isActive, createdTimestamp, expires);
    }
}

