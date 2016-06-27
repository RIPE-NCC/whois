package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;

import javax.annotation.CheckForNull;
import java.util.Iterator;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public final class NServer {
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("(?i)^(([A-Z0-9]|[A-Z0-9][A-Z0-9\\-]*[A-Z0-9])\\.)*([A-Z0-9]|[A-Z0-9][A-Z0-9\\-]*[A-Z0-9])$");
    private static final Splitter SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

    private final CIString hostname;
    private final IpInterval ipInterval;

    private NServer(final CIString hostname, final IpInterval ipInterval) {
        this.hostname = hostname;
        this.ipInterval = ipInterval;
    }

    public CIString getHostname() {
        return hostname;
    }

    @CheckForNull
    public IpInterval getIpInterval() {
        return ipInterval;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append(hostname);

        if (ipInterval != null) {
            s.append(' ');

            String glue = ipInterval.toString();
            final int idx = glue.indexOf('/');
            if (idx != -1) {
                glue = glue.substring(0, idx);
            }

            s.append(glue);
        }

        return s.toString();
    }

    public static NServer parse(final CIString value) {
        return parse(value.toString());
    }

    public static NServer parse(final String value) {
        final Iterator<String> iterator = SPLITTER.split(value).iterator();
        if (!iterator.hasNext()) {
            throw new AttributeParseException("No hostname specified", value);
        }

        String hostname = iterator.next();
        if (hostname.endsWith(".")) {
            hostname = hostname.substring(0, hostname.length() - 1);
        }

        if (!HOSTNAME_PATTERN.matcher(hostname).matches()) {
            throw new AttributeParseException("Hostname does not match", value);
        }

        final IpInterval ipInterval;
        if (iterator.hasNext()) {
            try {
                ipInterval = IpInterval.parse(iterator.next());
            } catch (IllegalArgumentException e) {
                throw new AttributeParseException("Invalid ip address", value);
            }

            final int maxPrefix = ipInterval instanceof Ipv4Resource ? 32 : 128;
            if (ipInterval.getPrefixLength() != maxPrefix) {
                throw new AttributeParseException("Not a single address", value);
            }
        } else {
            ipInterval = null;
        }

        if (iterator.hasNext()) {
            throw new AttributeParseException("Too many sections", value);
        }

        return new NServer(ciString(hostname), ipInterval);
    }
}
