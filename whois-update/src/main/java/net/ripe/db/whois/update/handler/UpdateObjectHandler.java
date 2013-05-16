package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

public interface UpdateObjectHandler {
    void execute(PreparedUpdate update, UpdateContext updateContext);
}
