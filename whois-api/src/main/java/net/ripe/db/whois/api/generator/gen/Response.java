package net.ripe.db.whois.api.generator.gen;


import nl.grol.whois.data.model.Inetnum;

public class Response {
    boolean status;
    String errorMessage;

    public boolean hasError() {
        return this.status == false;
    }

    public boolean hasInetNum() {
        return false;
    }

    public Inetnum getInetNum() {
        return null;
    }

}
