package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import org.apache.commons.lang.math.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservedAutnum {

    private final List<LongRange> reservedAsnumbers;

    @Autowired
    public ReservedAutnum(@Value("${whois.reserved.as.numbers:}") final String reservedAsNumbers) {
        this.reservedAsnumbers = parseReservedAsNumbers(reservedAsNumbers);
    }

    public boolean isReservedAsNumber(Long asn) {
        for (LongRange range : this.reservedAsnumbers) {
            if (range.containsLong(asn)) {
                return true;
            }
            if (asn < range.getMinimumLong()) {
                break;
            }
        }
        return false;
    }

    private List<LongRange> parseReservedAsNumbers(final String reservedAsNumbers) {
        final List<LongRange> parsedAsNumbers = Lists.newArrayList();

        for (String reservedAsNumber : reservedAsNumbers.split(",")) {
            if (reservedAsNumber.contains("-")) {
                String[] startEnd = reservedAsNumber.split("-");
                parsedAsNumbers.add(new LongRange(Long.parseLong(startEnd[0]), Long.parseLong(startEnd[1])));
            } else {
                parsedAsNumbers.add(new LongRange(Long.parseLong(reservedAsNumber), Long.parseLong(reservedAsNumber)));
            }
        }
        return parsedAsNumbers;
    }
}
