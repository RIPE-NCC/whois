package net.ripe.db.whois.common.rpki;


import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_ORIGIN;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_PREFIX_LENGTH;
import static net.ripe.db.whois.common.rpki.ValidationStatus.NOT_FOUND;
import static net.ripe.db.whois.common.rpki.ValidationStatus.VALID;

@Component
public class WhoisRoaChecker extends RpkiRoaChecker {
    public WhoisRoaChecker(final RpkiService rpkiService) {
        super(rpkiService);
    }

    public Map.Entry<Roa, ValidationStatus> validateAndGetInvalidRoa(final RpslObject route){
        final Map<Roa, ValidationStatus> roasStatus = validateRoas(route);
        final Optional<Map.Entry<Roa, ValidationStatus>> roaStatusMap = roasStatus
                .entrySet()
                .stream()
                .filter(getValidOrNotFoundRoas()).findFirst()
                .or(() -> getInvalidRoas(roasStatus).findFirst());

        if (roaStatusMap.isEmpty()){
            return null;
        }
        return roaStatusMap.get();
    }

    @Override
    protected ValidationStatus validate(final RpslObject route, final Roa roa, final IpInterval<?> prefix) {
        final List<ValidationStatus> invalidStatus = Lists.newArrayList();
        final long nonAuthAsn = Asn.parse(route.getValueForAttribute(AttributeType.ORIGIN).toString()).asBigInteger().longValue();
        if (prefix.getPrefixLength() > roa.getMaxLength()){
            invalidStatus.add(INVALID_PREFIX_LENGTH);
        }

        if (nonAuthAsn == 0 || nonAuthAsn != roa.getAsn()){
            invalidStatus.add(INVALID_ORIGIN);
        }

        if (invalidStatus.isEmpty()){
            return VALID;
        }
        return invalidStatus.size() == 1 ? invalidStatus.get(0) : INVALID;
    }

    private Predicate<Map.Entry<Roa, ValidationStatus>> getValidOrNotFoundRoas() {
        return entry -> NOT_FOUND.equals(entry.getValue()) || VALID.equals(entry.getValue());
    }

    private Stream<Map.Entry<Roa, ValidationStatus>> getInvalidRoas(Map<Roa, ValidationStatus> roasStatus) {
        return roasStatus.entrySet()
                .stream()
                .filter(other -> INVALID.equals(other.getValue()) || INVALID_PREFIX_LENGTH.equals(other.getValue()) || INVALID_ORIGIN.equals(other.getValue()))
                .sorted((o1, o2) -> o1.getKey().getPrefix().compareTo(o2.getKey().getPrefix()));
    }
}
