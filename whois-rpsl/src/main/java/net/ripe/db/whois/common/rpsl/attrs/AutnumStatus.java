package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.domain.CIString;

public enum AutnumStatus {
    ASSIGNED, LEGACY, OTHER;

    private CIString ciName;

    private AutnumStatus() {
        this.ciName = CIString.ciString(toString());
    }

    public CIString getCIName() {
        return ciName;
    }
}
