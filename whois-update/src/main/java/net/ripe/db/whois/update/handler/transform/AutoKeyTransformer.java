package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.AutoKeyResolver;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutoKeyTransformer implements Transformer {
    private final AutoKeyResolver autoKeyResolver;

    @Autowired
    public AutoKeyTransformer(final AutoKeyResolver autoKeyResolver) {
        this.autoKeyResolver = autoKeyResolver;
    }

    @Override
    public RpslObject transform(final RpslObject rpslObject, final Update update, final UpdateContext updateContext, final Action action) {
        return autoKeyResolver.resolveAutoKeys(rpslObject, update, updateContext, action);
    }
}
