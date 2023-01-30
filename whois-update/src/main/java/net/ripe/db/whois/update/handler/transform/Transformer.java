package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

public interface Transformer {

    RpslObject transform(final RpslObject rpslObject,
                         final Update update,
                         final UpdateContext updateContext,
                         final Action action);
}
