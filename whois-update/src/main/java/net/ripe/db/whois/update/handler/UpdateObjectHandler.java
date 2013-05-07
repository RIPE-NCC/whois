package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.Set;

public interface UpdateObjectHandler {
    void execute(PreparedUpdate update, UpdateContext updateContext);

    Set<ObjectType> getSupportedTypes();
}
