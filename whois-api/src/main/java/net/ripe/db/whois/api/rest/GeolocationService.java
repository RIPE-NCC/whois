package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.domain.GeolocationAttributes;
import net.ripe.db.whois.api.rest.domain.Language;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Location;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Component
@Path("/geolocation")
public class GeolocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationService.class);
    private static final String SERVICE_NAME = "geolocation-finder";

    // TODO: [ES] don't hard-code environment specific URL
    private static final String LOOKUP_URL = "http://rest.db.ripe.net/lookup";

    private static final Set<InetnumStatus> STOP_AT_STATUS_IPV4 = Sets.immutableEnumSet(
            InetnumStatus.ASSIGNED_PI,
            InetnumStatus.ASSIGNED_ANYCAST,
            InetnumStatus.ALLOCATED_PA,
            InetnumStatus.ALLOCATED_ASSIGNED_PA,
            InetnumStatus.ALLOCATED_UNSPECIFIED);

    private static final Set<Inet6numStatus> STOP_AT_STATUS_IPV6 = Sets.immutableEnumSet(
            Inet6numStatus.ASSIGNED_PI,
            Inet6numStatus.ASSIGNED_ANYCAST,
            Inet6numStatus.ALLOCATED_BY_RIR);

    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public GeolocationService(final Ipv4Tree ipv4Tree, final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    /**
     * @param ipkey IPv4 or IPv6 address
     * @return Returns geolocation information for the specified address.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public WhoisResources geolocation(
            @Context final HttpServletRequest request,
            @QueryParam(value = "ipkey") final String ipkey) {

        List<Language> languages = null;
        Location location = null;

        if (ipkey == null) {
            throw new IllegalArgumentException("ipkey is required");
        }

        try {
            final IpInterval interval = IpInterval.parse(ipkey);
            try {
                for (IpEntry ipEntry : Lists.reverse(lookupEntries(interval))) {
                    final RpslObject rpslObject = lookup(ipEntry);
                    final RpslObject orgObject = lookupOrg(rpslObject);

                    if (location == null) {
                        location = getLocation(rpslObject);
                        if (location == null) {
                            location = getLocation(orgObject);
                        }
                    }

                    if (languages == null) {
                        languages = getLanguages(rpslObject);
                        if (languages == null) {
                            languages = getLanguages(orgObject);
                        }
                    }

                    if (location != null && languages != null) {
                        break;
                    }

                    if (isStopStatus(rpslObject)) {
                        break;
                    }
                }
            } catch (EmptyResultDataAccessException ignored) {
                // stop looking
            }
        }
        catch (IllegalArgumentException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("No inetnum/inet6num resource has been found")
                            .build());
        }

        if (languages == null && location == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("No geolocation data was found for the given ipkey: " + ipkey)
                            .build());
        }

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setService(new Service(SERVICE_NAME));
        whoisResources.setGeolocationAttributes(new GeolocationAttributes(location, languages));
        whoisResources.setLink(Link.create(RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    private List<? extends IpEntry> lookupEntries(final IpInterval interval) {
        return switch (interval) {
            case Ipv4Resource ipv4Resource -> ipv4Tree.findExactAndAllLessSpecific(ipv4Resource);
            case Ipv6Resource ipv6Resource -> ipv6Tree.findExactAndAllLessSpecific(ipv6Resource);
            case null -> throw new IllegalStateException();
        };
    }

    private RpslObject lookup(final IpEntry ipEntry) {
        return rpslObjectDao.getById(ipEntry.getObjectId());
    }

    @Nullable
    private RpslObject lookupOrg(final RpslObject rpslObject) {
        if (!rpslObject.containsAttribute(AttributeType.ORG)) {
            return null;
        }
        final CIString orgName = rpslObject.getValueForAttribute(AttributeType.ORG);
        return rpslObjectDao.getByKey(ObjectType.ORGANISATION, orgName.toString());
    }

    @Nullable
    private Location getLocation(@Nullable final RpslObject rpslObject) {
        if (rpslObject == null || !rpslObject.containsAttribute(AttributeType.GEOLOC)) {
            return null;
        }
        final String value = rpslObject.getValueForAttribute(AttributeType.GEOLOC).toString();
        final Link link = Link.create(LOOKUP_URL, rpslObject);
        return new Location(value, link);
    }

    @Nullable
    private List<Language> getLanguages(@Nullable final RpslObject rpslObject) {
        if (rpslObject == null || !rpslObject.containsAttribute(AttributeType.LANGUAGE)) {
            return null;
        }
        final List<Language> languages = Lists.newArrayList();
        final Link link = Link.create(LOOKUP_URL, rpslObject);
        for (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.LANGUAGE)) {
            final String value = rpslAttribute.getCleanValue().toString();
            languages.add(new Language(value, link));
        }
        return languages;
    }

    private boolean isStopStatus(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case INETNUM:
            {
                final CIString status = rpslObject.getValueForAttribute(AttributeType.STATUS);
                return STOP_AT_STATUS_IPV4.contains(InetnumStatus.getStatusFor(status));
            }
            case INET6NUM:
            {
                final CIString status = rpslObject.getValueForAttribute(AttributeType.STATUS);
                return STOP_AT_STATUS_IPV6.contains(Inet6numStatus.getStatusFor(status));
            }
            default:
                throw new IllegalArgumentException("not an inetnum or inet6num");
        }
    }
}
