package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.stream.Collectors;

public class DropChangedTransformer implements PipelineTransformer {

    @Override
    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext,
                                final Action action) {
        return new RpslObject(rpslObject.getAttributes()
                .stream()
                .filter(attribute ->
                        !attribute.getType().equals(AttributeType.CHANGED))
                .collect(Collectors.toList()));
    }
}
