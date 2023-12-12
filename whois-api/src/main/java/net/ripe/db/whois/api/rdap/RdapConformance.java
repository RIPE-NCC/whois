package net.ripe.db.whois.api.rdap;

public enum RdapConformance {
    CIDR_0("cidr0"),
    LEVEL_0("rdap_level_0"),
    NRO_PROFILE_0("nro_rdap_profile_0"),
    FLAT_MODEL("nro_rdap_profile_asn_flat_0"),
    REDACTED("redacted"),
    GEO_FEED_V1("geofeedv1");


    private final String value;

    RdapConformance(final String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


}
