package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.*;

public interface AttributeParser<T> {
    T parse(String s);

    final class AddressPrefixRangeParser implements AttributeParser<AddressPrefixRange> {
        @Override
        public AddressPrefixRange parse(final String s) {
            return AddressPrefixRange.parse(s);
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
}
