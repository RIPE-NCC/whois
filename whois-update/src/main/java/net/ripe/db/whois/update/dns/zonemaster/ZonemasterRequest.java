package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.ObjectNode;

interface ZonemasterRequest {
    ObjectNode json();
}
