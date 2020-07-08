package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

public class AuthoritativeResourceJsonLoader extends AbstractAuthoritativeResourceLoader {

    AuthoritativeResourceJsonLoader(final Logger logger) {
        super(logger);
    }

    public AuthoritativeResource load(final JsonNode root) {
        handleAllocations(root.get("asnResources"), "asn");
        handleFreeResources(root.get("asnResources"), "asn");
        handleReservedResources(root.get("asnResources"), "asn");

        handleAssignments(root.get("ipv4Resources"), "ipv4");
        handleAllocations(root.get("ipv4Resources"), "ipv4");
        handleFreeResources(root.get("ipv4Resources"), "ipv4");
        handleReservedResources(root.get("ipv4Resources"), "ipv4");

        handleAssignments(root.get("ipv6Resources"), "ipv6");
        handleAllocations(root.get("ipv6Resources"), "ipv6");
        handleFreeResources(root.get("ipv6Resources"), "ipv6");
        handleReservedResources(root.get("ipv6Resources"), "ipv6");

        return new AuthoritativeResource(autNums, ipv4Space, ipv6Space);
    }

    private void handleAllocations(final JsonNode parent, final String type) {
        parent.get("allocations").forEach(allocation -> {
            handleResource("ripe", allocation.get("countryCode").asText(), type, allocation.get("start").asText(), allocation.get("value").asText(), "allocated", "ripe");
        });
    }

    private void handleFreeResources(final JsonNode parent, final String type) {
        parent.get("freeResources").forEach(allocation -> {
            handleResource("ripe", "", type, allocation.get("start").asText(), allocation.get("value").asText(), "available", "ripe");
        });
    }

    private void handleReservedResources(final JsonNode parent, final String type) {
        parent.get("reservedResources").forEach(allocation -> {
            handleResource("ripe", "", type, allocation.get("start").asText(), allocation.get("value").asText(), "reserved", "ripe");
        });
    }

    private void handleAssignments(final JsonNode parent, final String type) {
        parent.get("assignments").forEach(allocation -> {
            handleResource("ripe", allocation.get("countryCode").asText(), type, allocation.get("start").asText(), allocation.get("value").asText(), "assigned", "ripe");
        });
    }

}
