package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;

public interface ResponseHandler {
    String getApi();

    void handle(ResponseObject responseObject);
}
