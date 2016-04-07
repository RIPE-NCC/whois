package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformPipeline {

    private final PipelineTransformer[] transformers;

    @Autowired
    public TransformPipeline(final PipelineTransformer[] transformers) {
        this.transformers = transformers;
        // do we need sorting???
    }

    public Update transform(final Update update, final UpdateContext updateContext) {
        RpslObject transformedObject = update.getSubmittedObject();
        for (PipelineTransformer transformer : transformers) {
            transformedObject = transformer.transform(transformedObject, update, updateContext);
        }
        return new Update(update.getParagraph(), update.getOperation(), update.getDeleteReasons(), transformedObject);
    }
}
