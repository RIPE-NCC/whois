package net.ripe.db.emails;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find contact email addresses for Legacy address space.
 *
 * Legacy address space is defined by the prefixes listed in the filename specified (and including more specifics).
 *
 *  First log the distinct e-mail attributes of all tech-c attributes of these resources.
 *
 *  Then log the distinct upd-to attributes of the maintainers of the resource organisations.
 */
public class LegacySpaceContactEmails {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySpaceContactEmails.class);

    private static final Splitter COMMA_SEPARATED_LIST_SPLITTER = Splitter.on(',');

    public static final String ARG_FILENAME = "filename";
    public static final String ARG_REST_API_URL = "rest-api-url";
    public static final String ARG_SOURCE = "source";

    private RestClient restClient;
    private String filename;

    public LegacySpaceContactEmails(final String filename, final String restApiUrl, final String source) {
        this.restClient = createRestClient(restApiUrl, source);
        this.filename = filename;
    }

    public static void main(String[] args) throws IOException {
        final OptionSet options = setupOptionParser().parse(args);
        new LegacySpaceContactEmails(
                (String) options.valueOf(ARG_FILENAME),
                (String) options.valueOf(ARG_REST_API_URL),
                (String) options.valueOf(ARG_SOURCE)).execute();
    }

    public void execute() throws IOException {
        final Set<RpslObject> legacyInetnums = Sets.newHashSet();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line;
        while ((line = reader.readLine()) != null) {
            RpslObject exactMatch = null;
            if (line.contains(",")) {
                final Ipv4Resource resource = createIpv4ResourceFromCommaSeparatedList(line);
                exactMatch = searchExactMatch(resource);
            } else {
                if (line.contains("/")) {
                    final Ipv4Resource resource = createIpv4Resource(line);
                    exactMatch = searchExactMatch(resource);
                } else {
                    LOGGER.info("Skipping line: {}", line);
                }
            }

            try {
                if (exactMatch != null) {
                    legacyInetnums.add(exactMatch);
                    legacyInetnums.addAll(searchAllMoreSpecificMatch(exactMatch.getKey().toString()));
                }
            } catch (Exception e) {
                LOGGER.error("Exception on {}", (exactMatch != null ? exactMatch.getKey() : null), e);
            }
        }

        LOGGER.info("Found {} legacy inetnum objects", legacyInetnums.size());

        logTechCEmails(findTechCs(legacyInetnums));

        logUpdTo(findResourceOrganisationMaintainers(legacyInetnums));
    }

    // tech-c email addresses

    private Set<CIString> findTechCs(final Set<RpslObject> rpslObjects) {
        final Set<CIString> techCs = Sets.newHashSet();
        for (RpslObject rpslObject : rpslObjects) {
            final CIString techc = rpslObject.getValueOrNullForAttribute(AttributeType.TECH_C);
            if (techc != null) {
                techCs.add(techc);
            }
        }
        return techCs;
    }

    private void logTechCEmails(final Set<CIString> techCs) {
        for (CIString techc : techCs) {
            final RpslObject techCObject = searchPersonRole(techc.toString());
            if (techCObject != null) {
                for (CIString email : techCObject.getValuesForAttribute(AttributeType.E_MAIL)) {
                    LOGGER.info(email.toString());
                }
            }
        }
    }

    // resource organisation maintainers upd-to addresses

    private Set<RpslObject> findResourceOrganisationMaintainers(final Set<RpslObject> inetnums) {
        final Map<CIString, RpslObject> maintainers = Maps.newHashMap();
        for (RpslObject inetnum : inetnums) {
            final CIString orgId = inetnum.getValueOrNullForAttribute(AttributeType.ORG);
            if (orgId != null) {
                final RpslObject org = lookup(ObjectType.ORGANISATION, orgId.toString());
                if (org != null) {
                    for (CIString mntnerName : org.getValuesForAttribute(AttributeType.MNT_BY)) {
                        if (!maintainers.containsKey(mntnerName)) {
                            final RpslObject mntner = lookup(ObjectType.MNTNER, mntnerName.toString());
                            if (mntner != null) {
                                maintainers.put(mntnerName, mntner);
                            }
                        }
                    }

                    for (CIString mntnerName : org.getValuesForAttribute(AttributeType.MNT_LOWER)) {
                        if (!maintainers.containsKey(mntnerName)) {
                            final RpslObject mntner = lookup(ObjectType.MNTNER, mntnerName.toString());
                            if (mntner != null) {
                                maintainers.put(mntnerName, mntner);
                            }
                        }
                    }
                }
            }
        }

        return Sets.newHashSet(Iterables.transform(maintainers.entrySet(), new Function<Map.Entry<CIString, RpslObject>, RpslObject>() {
            @Override
            public RpslObject apply(@Nullable Map.Entry<CIString, RpslObject> input) {
                return input.getValue();
            }
        }));
    }

    private void logUpdTo(final Set<RpslObject> rpslObjects) {
        final Set<CIString> emails = Sets.newHashSet();

        for (RpslObject rpslObject : rpslObjects) {
            emails.addAll(rpslObject.getValuesForAttribute(AttributeType.UPD_TO));
        }

        for (CIString email : emails) {
            LOGGER.info(email.toString());
        }
    }

    // rest API calls

    private RestClient createRestClient(final String restApiUrl, final String source) {
        final RestClient restClient = new RestClient(restApiUrl, source);
        restClient.setWhoisObjectMapper(
                new WhoisObjectMapper(
                        restApiUrl,
                        new AttributeMapper[]{
                                new FormattedClientAttributeMapper(),
                                new DirtyClientAttributeMapper()
                        }
                )
        );
        return restClient;
    }

    @Nullable
    private RpslObject searchExactMatch(final Ipv4Resource ipv4Resource) {
        final Collection<RpslObject> objects;
        try {
            objects = restClient
                    .request()
                    .addParam("query-string", ipv4Resource.toRangeString())
                    .addParams("type-filter", ObjectType.INETNUM.getName())
                    .addParams("flags", QueryFlag.NO_REFERENCED.getName(), QueryFlag.EXACT.getName(), QueryFlag.NO_FILTERING.getName())
                    .search();
        } catch (RestClientException | ProcessingException e) {
            LOGGER.warn("Unable to retrieve exact match for inetnum: {}\nreason: {}", ipv4Resource.toRangeString(), e.toString());
            return null;
        }

        switch (objects.size()) {
            case 0:
                LOGGER.warn("inetnum {} not found in database", ipv4Resource.toRangeString());
                return null;
            case 1:
                // always expect only one result
                final RpslObject rpslObject = objects.iterator().next();

                final RpslAttribute inetnum = rpslObject.findAttribute(AttributeType.INETNUM);
                if (!inetnum.getCleanValue().equals(ipv4Resource.toRangeString())) {
                    LOGGER.warn("Search result from database: {} does not match concatenated inetnum: {}, skipping.", inetnum.getCleanValue().toString(), ipv4Resource.toString());
                    return null;
                }

                return rpslObject;

            default:
                LOGGER.warn("More than one match for {} found in database, skipping", ipv4Resource.toRangeString());
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<RpslObject> searchAllMoreSpecificMatch(final String inetnumString) {
        try {
            return restClient
                    .request()
                    .addParam("query-string", inetnumString)
                    .addParams("type-filter", ObjectType.INETNUM.getName())
                    .addParams("flags", QueryFlag.ALL_MORE.getName(), QueryFlag.NO_REFERENCED.getName(), QueryFlag.NO_FILTERING.getName())
                    .search();
        } catch (RestClientException | ProcessingException ex) {
            LOGGER.warn("Unable to retrieve more specific matches for {}\nreason: {}", inetnumString, ex.toString());
            return Collections.EMPTY_LIST;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private RpslObject searchPersonRole(final String key) {
        try {
            final Collection<RpslObject> result = restClient
                    .request()
                    .addParam("query-string", key)
                    .addParams("type-filter", ObjectType.PERSON.getName(), ObjectType.ROLE.getName())
                    .addParams("flags", QueryFlag.NO_REFERENCED.getName(), QueryFlag.NO_FILTERING.getName())
                    .search();
            switch(result.size()) {
                case 0: LOGGER.warn("No results when searching for {}", key);
                    return null;
                case 1: return result.iterator().next();
                default:
                    LOGGER.warn("Unexpected {} results when searching for {}", result.size(), key);
                    return null;
            }
        } catch (RestClientException | ProcessingException ex) {
            LOGGER.warn("exception when searching for {}", key);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private RpslObject lookup(final ObjectType objectType, final String key) {
        try {
            return restClient
                    .request()
                    .lookup(objectType, key);
        } catch (RestClientException | ProcessingException ex) {
            LOGGER.warn("exception when searching for {}", key);
            return null;
        }
    }

    // parse inetnums (copied from SetLegacyStatus script)

    private Ipv4Resource createIpv4Resource(final String line) {
        return Ipv4Resource.parse(line);
    }

    private Ipv4Resource createIpv4ResourceFromCommaSeparatedList(final String line) {
        final Iterator<String> tokens = COMMA_SEPARATED_LIST_SPLITTER.split(line).iterator();
        if (!tokens.hasNext()) {
            LOGGER.error("line: {} does not contain comma separated list", line);
            return null;
        }

        Ipv4Resource nextAddress = Ipv4Resource.parse(tokens.next());
        final List<Ipv4Resource> addresses = Lists.newArrayList(nextAddress);
        long end = nextAddress.end();
        while (tokens.hasNext()) {
            nextAddress = createIpv4Resource(end + 1, Integer.parseInt(tokens.next().substring(1)));
            addresses.add(nextAddress);
            end = nextAddress.end();
        }

        return concatenateIpv4Resources(addresses);
    }

    private Ipv4Resource createIpv4Resource(final long startAddress, final int prefixLength) {
        final int length = (1 << (32 - prefixLength));
        final long endAddress = startAddress + (length - 1l);
        return new Ipv4Resource(startAddress, endAddress);
    }

    private Ipv4Resource concatenateIpv4Resources(final List<Ipv4Resource> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (int index = 1; index < resources.size(); index++) {
            if (resources.get(index).begin() != resources.get(index - 1).end() + 1) {
                throw new IllegalArgumentException("found gap after " + resources.get(index).toString());
            }
        }

        return new Ipv4Resource(resources.get(0).begin(), resources.get(resources.size() - 1).end());
    }

    // options

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_FILENAME).withRequiredArg().required();
        parser.accepts(ARG_REST_API_URL).withRequiredArg().required();
        parser.accepts(ARG_SOURCE).withRequiredArg().required();
        return parser;
    }
}
