package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.List;

public interface SingleUpdateHandler {
    boolean supportAll(List<Update> updates);

    void handle(Origin origin, Keyword keyword, Update update, UpdateContext updateContext);
}
