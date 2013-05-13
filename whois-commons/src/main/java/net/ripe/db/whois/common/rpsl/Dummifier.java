package net.ripe.db.whois.common.rpsl;

public interface Dummifier {
    RpslObject dummify(int version, RpslObject rpslObject);

    boolean isAllowed(int version, RpslObject rpslObject);
}
