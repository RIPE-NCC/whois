package net.ripe.db.whois.common.mail;

public enum EmailStatus {

    UNDELIVERABLE("undeliverable"),
    UNSUBSCRIBE("unsubscribe");

    private final String value;

    EmailStatus(final String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


}
