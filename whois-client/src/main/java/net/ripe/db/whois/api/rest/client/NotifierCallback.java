package net.ripe.db.whois.api.rest.client;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;

import java.util.List;

public interface NotifierCallback {
    void notify(List<ErrorMessage> messages);
}
