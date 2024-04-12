package net.ripe.db.whois.api.rest.search;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.api.rest.domain.RpslMessage;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.common.rpki.Roa;
import net.ripe.db.whois.common.rpki.RpkiDataProvider;
import net.ripe.db.whois.common.rpki.RpkiService;
import net.ripe.db.whois.common.rpki.WhoisRoaChecker;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RpkiRoaMessageGenerator implements RpslMessageGenerator {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);
    private final boolean isEnabled;

    private final WhoisRoaChecker whoisRoaChecker;

    @Autowired
    public RpkiRoaMessageGenerator(@Value("${roa.validator.available:false}") boolean isRoaChecker, final WhoisRoaChecker whoisRoaChecker) {
        this.isEnabled = isRoaChecker;
        this.whoisRoaChecker = whoisRoaChecker;
    }

    @Override
    public RpslMessage proceed(RpslObject rpslObject, Parameters parameters) {
        if (!canProceed(parameters)){
            return null;
        }
        return validateRoa(rpslObject);
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    private boolean canProceed(final Parameters parameters){
        return isEnabled && parameters.getRoaCheck();
    }

    private RpslMessage validateRoa(final RpslObject rpslObject){
        final Roa rpkiRoa = whoisRoaChecker.validateAndGetInvalidRoa(rpslObject);

        if (rpkiRoa == null) {
            return null;
        }

        return new RpslMessage(QueryMessages.roaRouteConflicts(rpkiRoa.getAsn()));

    }
}
