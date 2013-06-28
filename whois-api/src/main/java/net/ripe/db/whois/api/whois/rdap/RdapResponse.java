package net.ripe.db.whois.api.whois.rdap;

import java.util.ArrayList;
import java.util.List;

public class RdapResponse {

    private String[] rdapConformance = new String[] { "rdap_level_0", "lunarNic_level_0" };
    private List entities;

    public RdapResponse() {}

    public String[] getRdapConformance () {
        return rdapConformance;
    }

    public void addEntity(Object entity) {
        if (entities == null) {
            entities = new ArrayList();
        }
        entities.add(entities);
    }

    public List<Object> getEntities() {
        return entities;
    }


}
