package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

public class DummyTransformer implements PipelineTransformer {

    @Override
    public RpslObject transform(final RpslObject rpslObject, final Update update, final UpdateContext updateContext) {
        return rpslObject;
    }
}
