package net.ripe.db.whois.api.rest.search;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.api.rest.domain.Flag;
import net.ripe.db.whois.api.rest.domain.Flags;
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

import java.util.List;

import static net.ripe.db.whois.query.QueryFlag.ROA_VALIDATION;

@Component
public class RpkiRoaMessageGenerator implements QueryMessageGenerator {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ROUTE, ObjectType.ROUTE6);
    private final boolean isRoaChecker;

    private final RpkiDataProvider rpkiDataProvider;

    @Autowired
    public RpkiRoaMessageGenerator(@Value("${roa.validator.available:false}") boolean isRoaChecker, final RpkiDataProvider dataProvider) {
        this.isRoaChecker = isRoaChecker;
        this.rpkiDataProvider = dataProvider;
    }

    @Override
    public void generate(final RpslObject rpslObject, final Parameters parameters, final List<String> messages) {
        if (!canProceed(rpslObject, parameters)){
            return;
        }
        validateRoa(rpslObject, messages);
    }

    private boolean canProceed(final RpslObject rpslObject, final Parameters parameters){
        return isRoaChecker && hasRoaValidationFlag(parameters.getFlags()) && TYPES.contains(rpslObject.getType());
    }

    private boolean hasRoaValidationFlag(final Flags flags){
        return flags != null && flags.getFlags().contains(new Flag(ROA_VALIDATION));
    }

    private void validateRoa(final RpslObject rpslObject, final List<String> messages){
        final Roa rpkiRoa = new WhoisRoaChecker(new RpkiService(rpkiDataProvider)).validateAndGetInvalidRoa(rpslObject);

        if (rpkiRoa != null){
            messages.add(QueryMessages.roaRouteConflicts(rpkiRoa.getAsn()).getFormattedText());
        }
    }
}
