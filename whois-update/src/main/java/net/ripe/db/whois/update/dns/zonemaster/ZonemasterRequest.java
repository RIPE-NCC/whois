package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.core.Response;

interface ZonemasterRequest {
    ObjectNode json();
    Response execute();
}
