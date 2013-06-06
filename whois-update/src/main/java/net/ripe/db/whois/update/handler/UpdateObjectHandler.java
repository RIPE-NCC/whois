package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

public interface UpdateObjectHandler {
    void execute(PreparedUpdate update, UpdateContext updateContext);

    /**
     * Validate the update using all compatible business rule validators.
     *
     * @param update        update to validate.
     * @param updateContext update context.
     * @return true if validation was successful.
     */
    boolean validateBusinessRules(PreparedUpdate update, UpdateContext updateContext);
}
