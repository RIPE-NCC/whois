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
import net.ripe.db.whois.query.rpki.WhoisRoaNonAuthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class RoaInfoDecorator implements ResponseDecorator {

    private final boolean isRoaChecker;

    private final RpkiDataProvider rpkiDataProvider;

    @Autowired
    public RoaInfoDecorator(@Value("${roa.checker.available:false}") boolean isRoaChecker, final RpkiDataProvider dataProvider) {
        this.isRoaChecker = isRoaChecker;
        this.rpkiDataProvider = dataProvider;
    }


    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {
        if (!isRoaChecker || !query.hasRoaValidationFlag()) {
            return input;
        }

        return new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (canProceed(input)) {
                    validateRoa((RpslObject) input, result);
                }
                result.add(input);
            }
        };
    }

    private boolean canProceed(final ResponseObject input){
        return (input instanceof final RpslObject rpslObject) && (rpslObject.getType().equals(ObjectType.ROUTE)
                || rpslObject.getType().equals(ObjectType.ROUTE6));
    }

    private void validateRoa(final RpslObject rpslObject, final Deque<ResponseObject> result){
        final Roa rpkiRoa = new WhoisRoaNonAuthChecker(new RpkiService(rpkiDataProvider)).validateAndGetInvalidRoa(rpslObject);

        if (rpkiRoa != null){
            result.add(new MessageObject(QueryMessages.roaRouteConflicts(rpkiRoa.getAsn())));
        }
    }

}
