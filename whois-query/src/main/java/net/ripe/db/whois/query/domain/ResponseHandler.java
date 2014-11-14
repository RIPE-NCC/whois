package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;

public interface ResponseHandler {
    //TODO [TP]: move the API names from the implementation classes to a central place
    String getApi();

    void handle(ResponseObject responseObject);
}
