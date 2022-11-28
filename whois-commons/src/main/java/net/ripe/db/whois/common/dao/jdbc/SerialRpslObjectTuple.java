package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.RpslObjectModel;
import net.ripe.db.whois.common.dao.Serial;


public class SerialRpslObjectTuple {

    private final Serial serial;
    private final RpslObjectModel rpslObjectModel;

    public SerialRpslObjectTuple(
        final Serial serial,
        final RpslObjectModel rpslObjectModel
    ) {
        this.serial = serial;
        this.rpslObjectModel = rpslObjectModel;
    }

    public Serial getSerial() {
        return serial;
    }

    public RpslObjectModel getRpslObjectModel() {
        return rpslObjectModel;
    }


}
