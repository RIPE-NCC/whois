package net.ripe.db.whois.common.rpki;


import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_ORIGIN;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_PREFIX_AND_ORIGIN;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_PREFIX_LENGTH;
import static net.ripe.db.whois.common.rpki.ValidationStatus.NOT_FOUND;
import static net.ripe.db.whois.common.rpki.ValidationStatus.VALID;

@Component
public class WhoisRoaChecker extends AbstractRpkiRoaChecker {

    public WhoisRoaChecker(final RpkiService rpkiService) {
        super(rpkiService);
    }

    @Nullable
    public Map.Entry<Roa, ValidationStatus> validateAndGetInvalidRoa(final RpslObject route){
        /* This method prioritize VALID roas over INVALID roas. So in case of overlap the VALID ROA will se used.
         This is a common behaviour related to roas */
        final Map<Roa, ValidationStatus> roasStatus = validateRoas(route);
        return roasStatus
                .entrySet()
                .stream()
                .filter(getValidOrNotFoundRoas()).findFirst()
                .or(() -> getInvalidRoas(roasStatus).findFirst())
                .orElse(null);
    }

    @Override
    protected ValidationStatus validate(final RpslObject route, final Roa roa, final IpInterval<?> prefix) {
        final List<ValidationStatus> invalidStatus = Lists.newArrayList();
        final long routeAsn = Asn.parse(route.getValueForAttribute(AttributeType.ORIGIN).toString()).asBigInteger().longValue();
        if (prefix.getPrefixLength() > roa.getMaxLength()){
            invalidStatus.add(INVALID_PREFIX_LENGTH);
        }

        if (routeAsn == 0 || routeAsn != roa.getAsn()){
            invalidStatus.add(INVALID_ORIGIN);
        }

        if (invalidStatus.isEmpty()){
            return VALID;
        }
        return invalidStatus.size() == 1 ? invalidStatus.getFirst() : INVALID_PREFIX_AND_ORIGIN;
    }

    private Predicate<Map.Entry<Roa, ValidationStatus>> getValidOrNotFoundRoas() {
        return entry -> NOT_FOUND.equals(entry.getValue()) || VALID.equals(entry.getValue());
    }

    private Stream<Map.Entry<Roa, ValidationStatus>> getInvalidRoas(Map<Roa, ValidationStatus> roasStatus) {
        return roasStatus.entrySet()
                .stream()
                .filter(other -> INVALID_PREFIX_AND_ORIGIN.equals(other.getValue()) || INVALID_PREFIX_LENGTH.equals(other.getValue()) || INVALID_ORIGIN.equals(other.getValue()))
                .sorted((o1, o2) -> o1.getKey().getPrefix().compareTo(o2.getKey().getPrefix()));
    }
}
