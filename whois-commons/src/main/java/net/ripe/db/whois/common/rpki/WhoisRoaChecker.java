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

import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_ORIGIN;
import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID_PREFIX_LENGTH;
import static net.ripe.db.whois.common.rpki.ValidationStatus.VALID;

@Component
public class WhoisRoaChecker extends RpkiRoaChecker {
    public WhoisRoaChecker(final RpkiService rpkiService) {
        super(rpkiService);
    }

    public Map.Entry<Roa, ValidationStatus> validateAndGetInvalidRoa(final RpslObject route){
        final Optional<Map.Entry<Roa, ValidationStatus>> roaStatusMap = validateRoas(route)
                .entrySet()
                .stream()
                .filter(entry -> INVALID.equals(entry.getValue()) || INVALID_PREFIX_LENGTH.equals(entry.getValue()) || INVALID_ORIGIN.equals(entry.getValue()))
                .findFirst();

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

        if (nonAuthAsn != 0 && nonAuthAsn != roa.getAsn()){
            invalidStatus.add(INVALID_ORIGIN);
        }

        if (invalidStatus.isEmpty()){
            return VALID;
        }
        return invalidStatus.size() == 1 ? invalidStatus.get(0) : INVALID;
    }
}
