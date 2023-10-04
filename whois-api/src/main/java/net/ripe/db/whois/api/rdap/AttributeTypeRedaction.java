package net.ripe.db.whois.api.rdap;


public enum AttributeTypeRedaction {
    MBRS_BY_REF( "Authenticate members by reference", "No registrant mntner"),
    MNT_DOMAINS( "Authenticate domain objects", "No registrant mntner"),
    MNT_LOWER( "Authenticate more specific resources", "No registrant mntner"),
    MNT_REF( "Authenticate incoming references", "No registrant mntner"),
    MNT_ROUTES( "Authenticate route objects", "No registrant mntner"),

    NOTIFY( "Updates notification e-mail information", "Personal data");

    private String name;

    private String reason;

    AttributeTypeRedaction(final String name, final String reason){
        this.name = name;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }
}
