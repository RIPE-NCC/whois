package net.ripe.db.whois.common.mail;

public enum EmailStatusType {

    UNDELIVERABLE("undeliverable"),
    UNSUBSCRIBE("unsubscribe");

    private final String value;

    EmailStatusType(final String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}