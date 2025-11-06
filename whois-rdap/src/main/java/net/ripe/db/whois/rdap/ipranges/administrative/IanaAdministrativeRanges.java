package net.ripe.db.whois.rdap.ipranges.administrative;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.AttributeType.INETNUM;

@Component
public class IanaAdministrativeRanges {

    private static final Logger LOGGER = LoggerFactory.getLogger(IanaAdministrativeRanges.class);
    private static final String TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE = "2002-06-25T14:19:09Z";

    private final ReservedResources reservedResources;

    final private List<IanaRecord> ianaRecords;

    @Autowired
    public IanaAdministrativeRanges(final IanaRangeXmlLoader ianaRangeXmlLoader, final ReservedResources reservedResources) {
        this.reservedResources = reservedResources;
        ianaRecords = ianaRangeXmlLoader.getIanaRecords();
    }

    @Nullable
    public RpslObject getRipeAdministrativeRange(final String prefix) {

        final IanaRecord ianaRecord = getIanaRecord(prefix);

        if (ianaRecord == null) return null;

        LOGGER.info("Retrieving ripe  administrative range for {}", ianaRecord.getPrefix() + ":" + ianaRecord.getRdap().getServer());

        if(!isRipeAdministrativeRange(prefix)) return null;

        return new RpslObjectBuilder().append(new RpslAttribute( (ianaRecord.getPrefix() instanceof Ipv4Resource) ? INETNUM : INET6NUM, ianaRecord.getPrefix().toString()))
                        .append(new RpslAttribute(AttributeType.NETNAME, "RIPE-NCC-MANAGED-ADDRESS-BLOCK"))
                        .append(new RpslAttribute(AttributeType.STATUS, InetnumStatus.ALLOCATED_UNSPECIFIED.toString()))
                        .append(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_CREATED_CHANGED_ADMINISTRATIVE))
                        .append(new RpslAttribute(AttributeType.SOURCE, "RIPE"))
                        .get();
    }

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
    public URI getOtherRirRedirectUri(final String prefix) {
        final IanaRecord ianaRecord = getIanaRecord(prefix);

        if ( ianaRecord == null ) return null;
        if(isRipeAdministrativeRange(prefix)) return null;

        return URI.create(String.format("%s/ip/%s", ianaRecord.getRdap().getServer(), prefix)).normalize();
    }
}
