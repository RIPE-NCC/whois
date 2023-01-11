package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;


public record ObjectChangeData(
    int objectId,
    int sequenceId,
    Operation operation,
    RpslObject rpslObject) {

}
