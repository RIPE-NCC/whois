package net.ripe.db.whois.query.rpki;


import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Map;
import java.util.Optional;

import static net.ripe.db.whois.query.rpki.ValidationStatus.INVALID;

public class WhoisRoaChecker extends RpkiRoaChecker {
    public WhoisRoaChecker(final RpkiService rpkiService) {
        super(rpkiService);
    }

    public Roa validateAndGetInvalidRoa(final RpslObject route){
        final Optional<Map.Entry<Roa, ValidationStatus>> roaStatusMap = validateRoas(route)
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == INVALID)
                .findFirst();

        if (roaStatusMap.isEmpty()){
            return null;
        }
        return roaStatusMap.get().getKey();
    }
}
