package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.rpki.Roa;
import net.ripe.db.whois.query.rpki.RpkiDataProvider;
import net.ripe.db.whois.query.rpki.RpkiService;
import net.ripe.db.whois.query.rpki.WhoisRoaNonAuthValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class RoaInfoDecorator implements ResponseDecorator {

    private final boolean isRoaValidator;

    private final RpkiDataProvider rpkiDataProvider;

    @Autowired
    public RoaInfoDecorator(@Value("${roa.validator:false}") boolean isRoaValidator, final RpkiDataProvider dataProvider) {
        this.isRoaValidator = isRoaValidator;
        this.rpkiDataProvider = dataProvider;
    }


    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {
        if (!isRoaValidator || !query.hasRoaValidationFlag()) {
            return input;
        }

        return new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof final RpslObject rpslObject)) {
                    result.add(input);
                    return;
                }
                if (!rpslObject.getType().equals(ObjectType.ROUTE) && !rpslObject.getType().equals(ObjectType.ROUTE6)){
                    result.add(input);
                    return;
                }
                validateRoa(rpslObject, result);
                result.add(input);
            }
        };
    }

    private void validateRoa(final RpslObject rpslObject, final Deque<ResponseObject> result){
        final Roa rpkiRoa = new WhoisRoaNonAuthValidator(new RpkiService(rpkiDataProvider)).validateAndGetInvalidRoa(rpslObject);

        if (rpkiRoa != null){
            result.add(new MessageObject(QueryMessages.roaRouteConflicts(rpkiRoa.getAsn())));
        }
    }

}
