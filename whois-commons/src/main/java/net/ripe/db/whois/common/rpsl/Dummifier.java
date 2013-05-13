package net.ripe.db.whois.common.rpsl;

public interface Dummifier {

    public RpslObject dummify(int version, RpslObject rpslObject);
    public boolean isAllowed(int version, RpslObject rpslObject);
}
