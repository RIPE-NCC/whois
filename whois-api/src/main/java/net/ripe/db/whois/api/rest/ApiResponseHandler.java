package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.query.domain.ResponseHandler;

public abstract class ApiResponseHandler implements ResponseHandler {
    @Override
    public String getApi() {
        return "API";
    }
}
