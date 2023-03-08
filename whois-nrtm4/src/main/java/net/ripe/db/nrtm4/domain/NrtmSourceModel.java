package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import net.ripe.db.whois.common.domain.CIString;


public class NrtmSourceModel {

    private final long id;
    @JsonValue
    private final CIString source;

    private NrtmSourceModel(final String source) {
        id = 0L;
        this.source = CIString.ciString(source);
    }

    public NrtmSourceModel(final long id, final CIString source) {
        this.id = id;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public CIString getSource() {
        return source;
    }

}
