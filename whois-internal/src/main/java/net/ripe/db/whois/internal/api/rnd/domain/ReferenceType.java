package net.ripe.db.whois.internal.api.rnd.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ReferenceType {
    OUTGOING(0),
    INCOMING(1);

    private final int typeId;

    private static final Map<Integer, ReferenceType> lookup = new HashMap<Integer, ReferenceType>();

    static {
        for (ReferenceType referenceType : EnumSet.allOf(ReferenceType.class))
            lookup.put(referenceType.typeId, referenceType);
    }

    ReferenceType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public static ReferenceType get(int code) {
        return lookup.get(code);
    }
}
