package net.ripe.db.nrtm4.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;


public record RpslObjectData(int objectId, int sequenceId, RpslObject rpslObject) {

}
