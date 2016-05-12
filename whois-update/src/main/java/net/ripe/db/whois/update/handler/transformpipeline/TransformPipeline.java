package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformPipeline {

    private final Transformer[] transformers;

    @Autowired
    public TransformPipeline(final Transformer[] transformers) {
        this.transformers = transformers;
        // do we need sorting???
    }

    public RpslObject transform(final RpslObject updatedObject,
                                final Update update,
                            final UpdateContext updateContext,
                            final Action action) {
        RpslObject transformedObject = updatedObject;
        for (Transformer transformer : transformers) {
            transformedObject = transformer.transform(transformedObject, update, updateContext, action);
        }
        return transformedObject;
    }
}
