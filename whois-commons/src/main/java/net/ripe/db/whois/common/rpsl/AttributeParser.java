package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.*;
import net.ripe.db.whois.common.exception.AsBlockParseException;

public interface AttributeParser<T> {
    T parse(String s);

    static final class AddressPrefixRangeParser implements AttributeParser<AddressPrefixRange> {
        @Override
        public AddressPrefixRange parse(final String s) {
            return AddressPrefixRange.parse(s);
        }
    }

    static final class AutNumParser implements AttributeParser<AutNum> {
        @Override
        public AutNum parse(final String s) {
            return AutNum.parse(s);
        }
    }

    static final class AsBlockParser implements AttributeParser<AsBlockRange> {
        @Override
        public AsBlockRange parse(final String s) {
            // TODO: [AH] this test should be done at the query search key sanitizer, and asblock should be range only
            if (s.indexOf('-') == -1) {
                throw new AsBlockParseException("invalid asblock");
            }
            return AsBlockRange.parse(s);
        }
    }

    static final class AsSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.ASSET, s);
        }
    }

    static final class RouteSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.ROUTESET, s);
        }
    }

    static final class FilterSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.FILTERSET, s);
        }
    }

    static final class PeeringSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.PEERINGSET, s);
        }
    }

    static final class RtrSetParser implements AttributeParser<SetObject> {
        @Override
        public SetObject parse(final String s) {
            return SetObject.parse(SetObject.Type.RTRSET, s);
        }
    }

    static final class ChangedParser implements AttributeParser<Changed> {
        @Override
        public Changed parse(final String s) {
            return Changed.parse(s);
        }
    }

    static final class DomainParser implements AttributeParser<Domain> {
        @Override
        public Domain parse(final String s) {
            return Domain.parse(s);
        }
    }

    static final class Ipv4ResourceParser implements AttributeParser<Ipv4Resource> {
        @Override
        public Ipv4Resource parse(final String s) {
            return Ipv4Resource.parse(s);
        }
    }

    static final class Ipv6ResourceParser implements AttributeParser<Ipv6Resource> {
        @Override
        public Ipv6Resource parse(final String s) {
            return Ipv6Resource.parse(s);
        }
    }

    static final class RouteResourceParser implements AttributeParser<Ipv4Resource> {
        @Override
        public Ipv4Resource parse(final String s) {
            if (s.contains("-") || !s.contains("/")) {
                throw new IllegalArgumentException("Only prefix notation is supported");
            }

            return Ipv4Resource.parse(s);
        }
    }

    static final class Route6ResourceParser implements AttributeParser<Ipv6Resource> {
        @Override
        public Ipv6Resource parse(final String s) {
            if (s.contains("-") || !s.contains("/")) {
                throw new IllegalArgumentException("Only prefix notation is supported");
            }

            return Ipv6Resource.parse(s);
        }
    }

    static final class MntRoutesParser implements AttributeParser<MntRoutes> {
        @Override
        public MntRoutes parse(final String s) {
            return MntRoutes.parse(s);
        }
    }

    static final class NServerParser implements AttributeParser<NServer> {
        @Override
        public NServer parse(final String s) {
            return NServer.parse(s);
        }
    }
}
