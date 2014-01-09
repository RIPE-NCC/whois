package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;

import java.util.List;

public interface NotifierCallback {

    void notify(ErrorMessage message);

    void notify(List<ErrorMessage> messages);
}
