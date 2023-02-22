package net.ripe.db.nrtm4.domain;

import net.ripe.db.whois.common.domain.CIString;


public class NrtmSourceModel {

    private final long id;
    private final CIString source;

    public NrtmSourceModel(final long id, final CIString source) {
        this.id = id;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public CIString getName() {
        return source;
    }

}
