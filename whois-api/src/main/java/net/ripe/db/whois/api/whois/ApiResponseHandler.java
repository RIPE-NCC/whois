package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.query.domain.ResponseHandler;

abstract class ApiResponseHandler implements ResponseHandler {
    @Override
    public String getApi() {
        return "API";
    }
}
