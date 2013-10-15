package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class AclServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AclServiceHelper.class);

    public static final Charset DEFAULT_CHARACTER_ENCODING = StandardCharsets.UTF_8;

    private AclServiceHelper() {
    }

    public static IpInterval<?> getNormalizedPrefix(final String prefix) {
        final IpInterval<?> ipInterval = IpInterval.parse(prefix);

        if (ipInterval instanceof Ipv6Resource && ipInterval.getPrefixLength() != 64) {
            throw new IllegalArgumentException("IPv6 must be a /64 prefix range");
        }

        if (ipInterval instanceof Ipv4Resource && ipInterval.getPrefixLength() != 32) {
            throw new IllegalArgumentException("IPv4 must be a single address");
        }

        return ipInterval;
    }

    public static String getDecodedPrefix(String prefix, String characterEncoding) {
        try {
            Charset charset = DEFAULT_CHARACTER_ENCODING;
            if (characterEncoding != null){
                charset = Charset.forName(characterEncoding);
            }
            return URLDecoder.decode(prefix, charset.name());
        } catch (UnsupportedEncodingException | RuntimeException e) {
            LOGGER.info("Unsupported Encoding: {}. Unable to decode prefix: {}.", characterEncoding, prefix);
        }
        return prefix;
    }
}
