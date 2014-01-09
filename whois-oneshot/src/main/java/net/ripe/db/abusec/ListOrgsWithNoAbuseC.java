package net.ripe.db.abusec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
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
import java.util.concurrent.atomic.AtomicInteger;

public class ListOrgsWithNoAbuseC {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListOrgsWithNoAbuseC.class);

    private static final RestClient restClient = new RestClient("https://rest.db.ripe.net", "RIPE");
    private static final Downloader downloader = new Downloader();

    private static final List<String> splitFiles = ImmutableList.of(
            "/ncc/ftp/ripe/dbase/split/ripe.db.inetnum.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.inet6num.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz");


    static void checkOrg(RpslObject rpslObject) {
        final String orgId = rpslObject.getValueForAttribute(AttributeType.ORG).toString();
        final RpslObject orgObject = restClient.lookup(ObjectType.ORGANISATION, orgId.toString());
        final String abuseC = orgObject.getValueForAttribute(AttributeType.ABUSE_C).toString();
        final RpslObject roleObject = restClient.lookup(ObjectType.ROLE, abuseC);
        roleObject.getValueForAttribute(AttributeType.ABUSE_MAILBOX);
    }

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        final AtomicInteger count = new AtomicInteger();

        for (String splitFile : splitFiles) {
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

                try {
                    switch (rpslObject.getType()) {
                        case INET6NUM:
                            if (Inet6numStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS)) == Inet6numStatus.ASSIGNED_PI) {
                                checkOrg(rpslObject);
                            }
                            break;
                        case INETNUM:
                            final InetnumStatus status = InetnumStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS));
                            if (status == InetnumStatus.ASSIGNED_PI || status == InetnumStatus.ALLOCATED_PI || status == InetnumStatus.LIR_PARTITIONED_PI) {
                                checkOrg(rpslObject);
                            }
                            break;
                        case AUT_NUM:
                            checkOrg(rpslObject);
                            break;
                        default:
                            LOGGER.error("Ignoring object " + rpslObject.getFormattedKey());
                            continue;
                    }
                } catch (RuntimeException e) {
                    System.out.println(rpslObject.getFormattedKey() + ": " + e.getMessage());
                }
            }
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
                        " union\t\n" +
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
