package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;

public class NicHandle extends AutoKey {
    private static final Pattern NIC_HDL_PATTERN = Pattern.compile("([a-zA-Z]{1,4})(\\d*)(?:-(.{0,10}))?");

    private static final Set<CIString> STANDARD_SUFFIXES = ciSet(
            "RIPE",
            "ORG",
            "ARIN",
            "LACNIC",
            "RADB",
            "APNIC",
            "RIPN",
            "AFRINIC");

    public static NicHandle parse(final String nicHdl, final CIString source, final Set<CIString> countryCodes) {
        if (StringUtils.startsWithIgnoreCase(nicHdl, "AUTO-")) {
            throw new NicHandleParseException("Primary key generation request cannot be parsed as NIC-HDL: " + nicHdl);
        }

        final Matcher matcher = NIC_HDL_PATTERN.matcher(nicHdl.trim());
        if (!matcher.matches()) {
            throw new NicHandleParseException("Invalid NIC-HDL: " + nicHdl);
        }

        final String characterSpace = matcher.group(1);
        final int index = getIndex(matcher.group(2));
        final String suffix = getSuffix(matcher.group(3), source, countryCodes);

        return new NicHandle(characterSpace, index, suffix);
    }

    private static Integer getIndex(final String indexString) {
        if (indexString.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(indexString);
    }

    private static String getSuffix(final String suffix, final CIString source, final Set<CIString> countryCodes) {
        if (StringUtils.isEmpty(suffix)) {
            return null;
        }

        final CIString suffixToCheck = ciString(suffix.trim());
        if (suffixToCheck.equals(source)) {
            return suffix;
        }

        if (STANDARD_SUFFIXES.contains(suffixToCheck)) {
            return suffix;
        }

        if (countryCodes.contains(suffixToCheck)) {
            return suffix;
        }

        throw new NicHandleParseException("Invalid suffix: " + suffix);
    }

    public NicHandle(final String space, final Integer index, final String suffix) {
        super(space, index, suffix);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder().append(getSpace());

        if (getIndex() != 0) {
            result.append(getIndex());
        }

        if (getSuffix() != null) {
            result.append("-").append(getSuffix());
        }

        return result.toString();
    }
}
