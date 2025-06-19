package net.ripe.db.whois.rdap;

public enum RdapConformance {
    CIDR_0("cidr0"),
    LEVEL_0("rdap_level_0"),
    NRO_PROFILE_0("nro_rdap_profile_0"),
    FLAT_MODEL("nro_rdap_profile_asn_flat_0"),
    REDACTED("redacted"),
    GEO_FEED_1("geofeed1"),
    RIR_SEARCH_1("rirSearch1"),
    IPS("ips"),
    IP_SEARCH_RESULTS("ipSearchResults"),
    AUTNUMS("autnums"),
    AUTNUM_SEARCH_RESULTS("autnumSearchResults");


    private final String value;

    RdapConformance(final String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


}
