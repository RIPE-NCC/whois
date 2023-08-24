package net.ripe.db.whois.common.ip;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpExtractor {

    private static final Pattern IPV6_SPLIT_PATTERN = Pattern.compile("" +
            "(" +
            "[\\dA-Fa-f]{1,4}(?::[\\dA-Fa-f]{1,4}){7}" + // basic case when we get full IPv6(2001:0000:130F:0000:0000:09C0:876A:130B)
            "|(?:[\\dA-Fa-f]{1,4}:){1,6}(?::[\\dA-Fa-f]{1,4}){1,6}" + // case when there is a double colon in the IPv6(2a00:1f78::fffe)
            "|(?:[\\dA-Fa-f]{1,4}:){1,6}:" +  // case when the IPv6 finish with double colon(2a00:1f78::)
            "|:(?::[\\dA-Fa-f]{1,4}){1,6}" +  // case when the IPv6 starts with double colon(::123)
            "|::)" + // case when there is just double colon(::/0)
            "((\\/\\d{1,2})|)"); // Prefix could be or not specified, we don't want the prefix in the group 0

    public static Set<String> ipv6FromString(final String string){
        final Set<String> extractedIpv6 = new HashSet<>();
        final Matcher ipv6 = IPV6_SPLIT_PATTERN.matcher(string);
        while (ipv6.find()) {
            extractedIpv6.add(ipv6.group(0));
        }
        return extractedIpv6;
    }
}
