package net.ripe.db.whois.common.domain.attrs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class MntRoutes {
    private static final Pattern MNT_ROUTES_PATTERN = Pattern.compile("(?i)^([A-Z](?:[A-Z0-9_-]){1,80})(?:$|[ ]+\\{(.*)\\}[ ]*$)");
    private static final Splitter ADDRESS_PREFIX_RANGES_SPLITTER = Splitter.onPattern(",| ").trimResults().omitEmptyStrings();

    private final CIString maintainer;
    private final boolean anyRange;
    private final List<AddressPrefixRange> addressPrefixRanges;

    public MntRoutes(final String maintainer, final boolean anyRange, final List<AddressPrefixRange> addressPrefixRanges) {
        this.maintainer = ciString(maintainer);
        this.anyRange = anyRange;
        this.addressPrefixRanges = Collections.unmodifiableList(Lists.newArrayList(addressPrefixRanges));
    }

    public CIString getMaintainer() {
        return maintainer;
    }

    public boolean isAnyRange() {
        return anyRange || addressPrefixRanges.isEmpty();
    }

    public List<AddressPrefixRange> getAddressPrefixRanges() {
        return addressPrefixRanges;
    }

    public static MntRoutes parse(final CIString value) {
        return parse(value.toString());
    }

    public static MntRoutes parse(final String value) {
        final Matcher matcher = MNT_ROUTES_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final String maintainer = matcher.group(1);

        final List<AddressPrefixRange> addressPrefixRanges = Lists.newArrayList();
        int anyRangeCount = 0;

        final String addressPrefixRangesString = matcher.group(2);
        if (addressPrefixRangesString != null) {
            final Iterable<String> rangeStrings = ADDRESS_PREFIX_RANGES_SPLITTER.split(addressPrefixRangesString);
            for (final String rangeString : rangeStrings) {
                if (rangeString.equalsIgnoreCase("any")) {
                    anyRangeCount++;
                } else {
                    addressPrefixRanges.add(AddressPrefixRange.parse(rangeString));
                }
            }
        }

        if (anyRangeCount > 1 || (anyRangeCount == 1 && addressPrefixRanges.size() > 0)) {
            throw new AttributeParseException("ANY can only occur as a single value", value);
        }

        return new MntRoutes(maintainer, anyRangeCount > 0, addressPrefixRanges);
    }
}
