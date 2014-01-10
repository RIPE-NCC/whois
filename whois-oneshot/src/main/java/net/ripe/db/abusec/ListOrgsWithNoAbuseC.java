package net.ripe.db.abusec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import org.apache.commons.lang.StringUtils;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class ListOrgsWithNoAbuseC {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListOrgsWithNoAbuseC.class);

    private static final CIString RIPE_NCC_END_MNT = ciString("RIPE-NCC-END-MNT");

    private static final RestClient restClient = new RestClient("http://rest.db.ripe.net", "RIPE");
    private static final Downloader downloader = new Downloader();
    private static final String ARG_PASSWD = "passwd";

    private static final List<String> splitFiles = ImmutableList.of(
            "/ncc/ftp/ripe/dbase/split/ripe.db.inetnum.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.inet6num.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz");

    private final Map<String, String> emailByOrgIdMap;
    private final JdbcTemplate jdbcTemplate;


    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(argv);
        final ListOrgsWithNoAbuseC listOrgsWithNoAbuseC = new ListOrgsWithNoAbuseC(options.valueOf(ARG_PASSWD).toString());
        listOrgsWithNoAbuseC.printContactDetails(listOrgsWithNoAbuseC.getOrgsWithoutAbuseC());
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_PASSWD).withRequiredArg();
        return parser;
    }

    public ListOrgsWithNoAbuseC(final String password) throws SQLException {
        jdbcTemplate = setupRSNGTemplate(password);
        emailByOrgIdMap = getSponsoringLirEmailPerOrganisationId(jdbcTemplate);
    }

    public void printContactDetails(final Set<RpslObject> organisations) {
        for (final RpslObject organisation : organisations) {
            // if not souting, what do we do with this?
            System.out.println(String.format("%s|%s|%s\n",
                    organisation.getKey(),
                    organisation.findAttributes(AttributeType.E_MAIL).get(0).getCleanValues().iterator().next(),
                    emailByOrgIdMap.get(organisation.getKey()) == null ? "" : emailByOrgIdMap.get(organisation.getKey())));
        }
    }

    public Set<RpslObject> getOrgsWithoutAbuseC() throws SQLException {
        final AtomicInteger count = new AtomicInteger();
        final Map<RpslObject, Set<CIString>> orgsWithoutAbuseC = Maps.newHashMap();

        for (final String splitFile : splitFiles) {
            for (String nextObject : new RpslObjectFileReader(splitFile)) {
                RpslObject rpslObject;
                try {
                    rpslObject = RpslObject.parse(nextObject);
                } catch (Exception e) {
                    continue;
                }

                if ((count.incrementAndGet() & 0xffff) == 0) {
                    LOGGER.info("Processed: " + count.get());
                }

                if (!rpslObject.getValuesForAttribute(AttributeType.MNT_BY).contains(RIPE_NCC_END_MNT)) {
                    continue;
                }

                switch (rpslObject.getType()) {
                    case INET6NUM:
                        if (Inet6numStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS)) == Inet6numStatus.ASSIGNED_PI) {
                            collectOrgIfNoAbuseC(rpslObject, orgsWithoutAbuseC);
                        }
                        break;
                    case INETNUM:
                        final InetnumStatus status = InetnumStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS));
                        if (status == InetnumStatus.ASSIGNED_PI) {
                            collectOrgIfNoAbuseC(rpslObject, orgsWithoutAbuseC);
                        }
                        break;
                    case AUT_NUM:
                        collectOrgIfNoAbuseC(rpslObject, orgsWithoutAbuseC);
                        break;
                    default:
                        LOGGER.error("Ignoring object " + rpslObject.getFormattedKey());
                }
            }
        }

        final Set<RpslObject> mailableOrgsWithoutAbuseC = Sets.newHashSet();
        final Set<RpslObject> keys = orgsWithoutAbuseC.keySet();
        for (final RpslObject organisation : keys) {
            // look this up among the isMailable resources in RSNG to see if it's a keeper
            if (isMailable(organisation, orgsWithoutAbuseC.get(organisation))) {
                mailableOrgsWithoutAbuseC.add(organisation);
            }
        }

        return mailableOrgsWithoutAbuseC;
    }

    private boolean isMailable(final RpslObject organisation, final Set<CIString> resources) {
        for (final CIString resource : resources) {
            int found;
            if (resource.startsWith(CIString.ciString("AS"))) {
                found = jdbcTemplate.queryForInt("SELECT count(*) FROM (resourcedb.asnresource WHERE as_number = ? OR resourcedb.organisation_id = ?) AND resource_status='ASSIGNED' AND ir_status='ENDUSER_APPROVEDDOCS' AND legacy = 'f'",
                        resource.subSequence(2, resource.length()), organisation.getKey());
            } else if (resource.contains(CIString.ciString(":"))) {
                found = jdbcTemplate.queryForInt("SELECT count(*) FROM resourcedb.ipv6assignmentresource WHERE (ip6_to_slash(resource_start, resource_end) = ? OR resourcedb.organisation_id = ?) AND resource_status='ASSIGNED' AND ir_status='ENDUSER_APPROVEDDOCS'",
                        resource.toString(), organisation.getKey());
            } else {
                found = jdbcTemplate.queryForInt("SELECT count(*) FROM resourcedb.ipv4assignmentresource WHERE (ip4_to_slash(resource_start, resource_end) = ? OR resourcedb.organisation_id = ?) AND resource_status='ASSIGNED' AND ir_status='ENDUSER_APPROVEDDOCS' AND legacy = 'f'",
                        Ipv4Resource.parse(resource).toString(), organisation.getKey());
            }

            if (found > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAbuseContact(final RpslObject orgObject) {
        try {
            final String abuseC = orgObject.getValueForAttribute(AttributeType.ABUSE_C).toString();
            return StringUtils.isNotBlank(abuseC);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void collectOrgIfNoAbuseC(final RpslObject rpslObject, final Map<RpslObject, Set<CIString>> withoutAbuseC) {
        try {
            final String orgId = rpslObject.getValueForAttribute(AttributeType.ORG).toString();
            final RpslObject orgObject = restClient.lookup(ObjectType.ORGANISATION, orgId);
            if (!hasAbuseContact(orgObject)) {
                final Set<CIString> resources = withoutAbuseC.get(orgObject);
                if (resources == null) {
                    withoutAbuseC.put(orgObject, Sets.newHashSet(rpslObject.getKey()));
                } else {
                    resources.add(rpslObject.getKey());
                }
            }
        } catch (IllegalArgumentException e) {
//            System.out.println(String.format("%s has no org attribute, it's probably legacy, or not yet handled", rpslObject.getKey()));
        }
    }

    private JdbcTemplate setupRSNGTemplate(final String custdbConnectionPassword) throws SQLException {
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:postgresql://duck.ripe.net/custdb", "dbase", custdbConnectionPassword);
        return new JdbcTemplate(dataSource);
    }

    private Map<String, String> getSponsoringLirEmailPerOrganisationId(final JdbcTemplate jdbcTemplate) {
        final Map<String, String> emailPerSponsoredOrganisation = Maps.newHashMap();

        jdbcTemplate.query(
                "SELECT min(cm.contact_medium_value), a.oid\n" +
                        "FROM contactmedium cm INNER JOIN contactdetails cd ON cd.id = cm.contactdetails_id\n" +
                        "INNER JOIN organisation o ON cd.contact_organisation_id = o.id\n" +
                        "INNER JOIN membership m ON m.id = o.membership_id\n" +
                        "INNER JOIN\n" +
                        "(\n" +
                        " SELECT asn.organisation_id AS oid, asn.membership_id AS mid\n" +
                        " FROM resourcedb.asnresource asn\n" +
                        " UNION\n" +
                        " SELECT ipv4.organisation_id AS oid, ipv4.membership_id AS mid\n" +
                        " FROM resourcedb.ipv4assignmentresource ipv4\n" +
                        " UNION\n" +
                        " SELECT ipv6.organisation_id AS oid, ipv6.membership_id AS mid\n" +
                        " FROM resourcedb.ipv6assignmentresource ipv6\n" +
                        ") a ON m.id = a.mid\n" +
                        "\n" +
                        "WHERE cm.contact_medium_type = 'EMAIL'\n" +
                        "AND a.oid IS NOT null\n" +
                        "AND a.oid <> ''\n" +
                        "GROUP BY a.oid\n" +
                        "ORDER BY 2;",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        emailPerSponsoredOrganisation.put(rs.getString(1), rs.getString(2));
                    }
                });
        return emailPerSponsoredOrganisation;
    }
}
