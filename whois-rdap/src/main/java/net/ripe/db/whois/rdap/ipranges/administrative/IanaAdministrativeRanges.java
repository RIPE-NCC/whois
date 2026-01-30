package net.ripe.db.whois.rdap.ipranges.administrative;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.AttributeType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INETNUM;

@Component
public class IanaAdministrativeRanges {

    private static final Logger LOGGER = LoggerFactory.getLogger(IanaAdministrativeRanges.class);
    private static final String TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE = "2002-06-25T14:19:09Z";

    private final ReservedResources reservedResources;

    final private List<IanaRecord> ianaRecords;

    private static final String SOURCE_NAMESERVER = "pri.authdns.ripe.net";

    private static final String RIPE_NCC_NETNAME = "RIPE-NCC-MANAGED-ADDRESS-BLOCK";

    private final NestedIntervalMap<Ipv4Resource, Domain> ipv4RipeDelegatedReverseZones;

    private final NestedIntervalMap<Ipv6Resource, Domain> ipv6RipeDelegatedReverseZones;

    private final String source;

    @Autowired
    public IanaAdministrativeRanges(final IanaRangeXmlLoader ianaRangeXmlLoader,
                                    final ReservedResources reservedResources,
                                    final NestedIntervalMap<Ipv4Resource, Domain> ipv4RipeDelegatedReverseZones,
                                    final NestedIntervalMap<Ipv6Resource, Domain> ipv6RipeDelegatedReverseZones,
                                    @Value("${whois.source}") final String source) {
        this.reservedResources = reservedResources;
        this.ipv4RipeDelegatedReverseZones = ipv4RipeDelegatedReverseZones;
        this.ipv6RipeDelegatedReverseZones = ipv6RipeDelegatedReverseZones;
        this.source = source;
        ianaRecords = ianaRangeXmlLoader.getIanaRecords();
    }

    @Nullable
    public RpslObject getRipeAdministrativeRange(final String prefix) {

        final IanaRecord ianaRecord = getIanaRecord(prefix);

        if (ianaRecord == null) return null;

        if(!isRipeAdministrativeRange(prefix)) return null;

        return new RpslObjectBuilder().append(new RpslAttribute( (ianaRecord.getPrefix() instanceof Ipv4Resource) ? INETNUM : INET6NUM, ianaRecord.getPrefix().toString()))
                        .append(new RpslAttribute(AttributeType.NETNAME, RIPE_NCC_NETNAME))
                        .append(new RpslAttribute(AttributeType.STATUS, InetnumStatus.ALLOCATED_UNSPECIFIED.toString()))
                        .append(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.SOURCE, this.source))
                        .get();
    }

    @Nullable
    public RpslObject getRipeAdministrativeDomain(final String reverseIp) {

        final IanaRecord ianaRecord = getIanaRecord(reverseIp);

        if (ianaRecord == null) return null;

        if(!isRipeAdministrativeRange(reverseIp)) return null;

        return new RpslObjectBuilder().append(new RpslAttribute(DOMAIN, getRipeDelegatedReverseZone(reverseIp)))
                .append(new RpslAttribute(AttributeType.NSERVER, SOURCE_NAMESERVER))
                .append(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                .append(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                .append(new RpslAttribute(AttributeType.SOURCE, this.source))
                .get();
    }

    private CIString getRipeDelegatedReverseZone(final String reverseIp) {
        final Domain domain = (reverseIp.indexOf(':') == -1) ?
                ipv4RipeDelegatedReverseZones.findExactOrFirstLessSpecific(Ipv4Resource.parse(reverseIp)).getFirst() :
                ipv6RipeDelegatedReverseZones.findExactOrFirstLessSpecific(Ipv6Resource.parse(reverseIp)).getFirst();

        if (IpInterval.isIANADefaultBlock(CIString.ciString(domain.getReverseIp().toString()))){
            throw new IllegalStateException("IANA default block cannot be delegated to RIPE NCC. Fix the configuration");
        }

        return domain.getValue();
    }

    @Nullable
    private IanaRecord getIanaRecord(final String prefix) {
        final IpInterval<?> interval;
        try {
            interval = IpInterval.parse(prefix);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("{} is not a valid prefix, skipping...", prefix);
            return null;
        }

        if(reservedResources.isBogon(prefix)) return null;

        return ianaRecords.stream()
                .filter(record -> interval.getClass().equals(record.getPrefix().getClass())
                                                    && record.getPrefix().contains(interval)
                ).findAny().orElse(null);
    }

    public boolean isRipeAdministrativeRange(final String prefix) {
        final IanaRecord ianaRecord = getIanaRecord(prefix);
        if (ianaRecord == null) return false;

        return ianaRecord.getRdap().getServer().contains("rdap.db.ripe.net");
    }

    @Nullable
    public URI getOtherRirRedirectUri(final String prefix, final String domainKey) {
        final IanaRecord ianaRecord = getIanaRecord(prefix);

        if ( ianaRecord == null ) return null;
        if(isRipeAdministrativeRange(prefix)) return null;

        if (StringUtils.isEmpty(domainKey)){
            return URI.create(String.format("%s/ip/%s", ianaRecord.getRdap().getServer(), prefix)).normalize();
        }
        return URI.create(String.format("%s/domain/%s", ianaRecord.getRdap().getServer(), domainKey)).normalize();
    }
}
