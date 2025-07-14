package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.ImmutableSet;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.attrs.AddressPrefixRange;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Changed;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.rpsl.attrs.DsRdata;
import net.ripe.db.whois.common.rpsl.attrs.IPAddress;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import net.ripe.db.whois.common.rpsl.attrs.NServer;
import net.ripe.db.whois.common.rpsl.attrs.SetObject;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

public interface AttributeParser<T> {
    // TODO: [AH] should operate on CIString, not String
    T parse(String s);

    final class AddressPrefixRangeParser implements AttributeParser<AddressPrefixRange> {
        @Override
        public AddressPrefixRange parse(final String s) {
            return AddressPrefixRange.parse(s);
        }
    }

    final class IPAddressParser implements AttributeParser<IPAddress> {
        @Override
        public IPAddress parse(final String s) {
            return IPAddress.parse(s);
        }
    }

    final class AutNumParser implements AttributeParser<AutNum> {
        @Override
        public AutNum parse(final String s) {
            return AutNum.parse(s);
        }
    }

    final class AsBlockParser implements AttributeParser<AsBlockRange> {
        @Override
        public AsBlockRange parse(final String s) {
            return AsBlockRange.parse(s);
        }
    }

    final class AsSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.ASSET, s);
        }
    }

    final class RouteSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.ROUTESET, s);
        }
    }

    final class FilterSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.FILTERSET, s);
        }
    }

    final class PeeringSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.PEERINGSET, s);
        }
    }

    final class RtrSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.RTRSET, s);
        }
    }

    final class ChangedParser implements AttributeParser<Changed> {
        @Override
        public Changed parse(final String s) {
            return Changed.parse(s);
        }
    }

    final class DsRdataParser implements AttributeParser<DsRdata> {
        @Override
        public DsRdata parse(final String s) {
            return DsRdata.parse(s);
        }
    }

    final class DomainParser implements AttributeParser<Domain> {
        @Override
        public Domain parse(final String s) {
            return Domain.parse(s);
        }
    }

    final class Ipv4ResourceParser implements AttributeParser<Ipv4Resource> {
        @Override
        public Ipv4Resource parse(final String s) {
            return Ipv4Resource.parse(s);
        }
    }

    final class Ipv6ResourceParser implements AttributeParser<Ipv6Resource> {
        @Override
        public Ipv6Resource parse(final String s) {
            return Ipv6Resource.parse(s);
        }
    }

    final class RouteResourceParser implements AttributeParser<Ipv4Resource> {
        @Override
        public Ipv4Resource parse(final String s) {
            if (s.contains("-") || !s.contains("/")) {
                throw new IllegalArgumentException("Only prefix notation is supported");
            }

            return Ipv4Resource.parse(s);
        }
    }

    final class Route6ResourceParser implements AttributeParser<Ipv6Resource> {
        @Override
        public Ipv6Resource parse(final String s) {
            if (s.contains("-") || !s.contains("/")) {
                throw new IllegalArgumentException("Only prefix notation is supported");
            }

            return Ipv6Resource.parse(s);
        }
    }

    final class MntRoutesParser implements AttributeParser<MntRoutes> {
        @Override
        public MntRoutes parse(final String s) {
            return MntRoutes.parse(s);
        }
    }

    final class NServerParser implements AttributeParser<NServer> {
        @Override
        public NServer parse(final String s) {
            return NServer.parse(s);
        }
    }

    final class NameParser implements AttributeParser {
        private static final Pattern NAME = Pattern.compile("(?i)[a-z][a-z0-9_-]{0,78}[a-z0-9]");
        private static final Set<String> RESERVED = ImmutableSet.of(
                "ANY", "AS-ANY", "RS-ANY", "PEERAS", "AND", "OR", "NOT",
                "ATOMIC", "FROM", "TO", "AT", "ACTION", "ACCEPT", "ANNOUNCE",
                "EXCEPT", "REFINE", "NETWORKS", "INTO", "INBOUND", "OUTBOUND");

        @Override
        public String parse(final String s) {
            if (!StringUtils.isBlank(s) &&
                    NAME.matcher(s).matches() &&
                    ! RESERVED.contains(s.toUpperCase())) {
                return s;
            }
            throw new AttributeParseException("Unexpected parse result", s);
        }
    }

    final class EmailParser implements AttributeParser<InternetAddress> {

        // The maxmimum length of an email address according to RFC 5321 is (local-part = 64) + '@' + (domain = 255) octets
        private static final int MAXIMUM_LENGTH = 320;

        @Override
        public InternetAddress parse(final String s) {
            final InternetAddress[] parsed;
            try {
                parsed = InternetAddress.parse(s);
            } catch (AddressException e) {
                throw new AttributeParseException(String.format("Illegal address (%s)", e.getMessage()), s);
            }

            if (parsed.length != 1) {
                throw new AttributeParseException("Illegal address", s);
            }

            try {
                parsed[0].validate();
            } catch (AddressException e) {
                throw new AttributeParseException(String.format("Invalid address (%s)", e.getMessage()), s);
            }

            final String address = parsed[0].getAddress();
            final String localPart = address.substring(0, address.indexOf('@'));

            if (address.length() > MAXIMUM_LENGTH) {
                throw new AttributeParseException(String.format("Address length %d is greater than the maximum supported length %d",
                        address.length(), MAXIMUM_LENGTH), s);
            }

            if (!StandardCharsets.US_ASCII.newEncoder().canEncode(localPart)) {
                // only convert non-ASCII characters in domain part to punycode
                throw new AttributeParseException("Address contains non ASCII characters (%s)", s);
            }

            return parsed[0];
        }

    }

}
