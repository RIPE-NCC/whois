package net.ripe.db.whois.common.rpki;

import com.google.common.collect.Maps;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpki.ValidationStatus.INVALID;
import static net.ripe.db.whois.common.rpki.ValidationStatus.NOT_FOUND;
import static net.ripe.db.whois.common.rpki.ValidationStatus.VALID;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;

public abstract class AbstractRpkiRoaChecker {

    private final RpkiService rpkiService;

    public AbstractRpkiRoaChecker(final RpkiService rpkiService) {
        this.rpkiService = rpkiService;
    }

    public Map<Roa, ValidationStatus> validateRoas(final RpslObject route) {
        final CIString routePrefix = route.getType() == ROUTE? route.getValueForAttribute(AttributeType.ROUTE) : route.getValueForAttribute(AttributeType.ROUTE6);

        Map<Roa, ValidationStatus> validationResult = Maps.newHashMap();

        final Set<Roa> roas = rpkiService.findRoas(routePrefix.toString());

        final IpInterval<?> prefix = toIpInterval(routePrefix);
        if (!roas.isEmpty()) {
            roas.forEach(roa -> validationResult.put(roa, validate(route, roa, prefix)));
        } else {
            validationResult.put(null, NOT_FOUND);
        }

        return validationResult;
    }

    protected ValidationStatus validate(final RpslObject route, final Roa roa, final IpInterval<?> prefix) {
        final long nonAuthAsn = Asn.parse(route.getValueForAttribute(AttributeType.ORIGIN).toString()).asBigInteger().longValue();
        return prefix.getPrefixLength() <= roa.getMaxLength() &&
                nonAuthAsn != 0 &&
                nonAuthAsn == roa.getAsn()?
                VALID : INVALID;
    }

    private IpInterval<?> toIpInterval(final CIString prefix) {
        return prefix.contains(".")? Ipv4Resource.parse(prefix) : Ipv6Resource.parse(prefix);
    }
}
