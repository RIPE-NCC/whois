package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

public interface SingleUpdateHandler {
    void handle(Origin origin, Keyword keyword, Update update, UpdateContext updateContext);
}
