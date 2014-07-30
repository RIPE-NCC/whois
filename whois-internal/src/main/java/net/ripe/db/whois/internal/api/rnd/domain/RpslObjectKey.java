package net.ripe.db.whois.internal.api.rnd.domain;

public class RpslObjectKey {
    private Integer objectType;
    private String pkey;

    public RpslObjectKey() {}

    public RpslObjectKey(final Integer objectType, final String pkey) {
        this.objectType = objectType;
        this.pkey = pkey;
    }

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(final Integer objectType) {
        this.objectType = objectType;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(final String pkey) {
        this.pkey = pkey;
    }

    @Override
    public String toString() {
        return objectType + "::" + pkey;
    }
}
