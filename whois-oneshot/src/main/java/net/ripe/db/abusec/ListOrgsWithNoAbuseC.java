package net.ripe.db.abusec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mysql.jdbc.Driver;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
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

public class ListOrgsWithNoAbuseC {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListOrgsWithNoAbuseC.class);

    private static final RestClient restClient = new RestClient("https://rest.db.ripe.net", "RIPE");
    private static final Downloader downloader = new Downloader();

    private static final List<String> splitFiles = ImmutableList.of(
            "/ncc/ftp/ripe/dbase/split/ripe.db.inetnum.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.inet6num.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz");


    static void checkOrg(String orgId) {

    }

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        for (String splitFile : splitFiles) {
            for (String nextObject : new RpslObjectFileReader(splitFile)) {
                RpslObject rpslObject;
                try {
                    rpslObject = RpslObject.parse(nextObject);
                } catch (Exception e) {
                    continue;
                }

                List<RpslAttribute> org = rpslObject.findAttributes(AttributeType.ORG);

                switch (rpslObject.getType()) {
                    case INET6NUM:
                        try {
                            if (Inet6numStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS)) == Inet6numStatus.ASSIGNED_PI) {

                            }
                        } catch (RuntimeException e) {

                        }
                        break;
                    case INETNUM:
                        break;
                    case AUT_NUM:
                        break;
                    default:
                        LOGGER.error("Ignoring object " + rpslObject.getFormattedKey());
                        continue;
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

        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                "SELECT min(cm.contact_medium_value), a.oid\n" +
                        "FROM contactmedium cm INNER JOIN contactdetails cd ON cd.id = cm.contactdetails_id\n" +
                        "INNER JOIN organisation o ON cd.contact_organisation_id = o.id\n" +
                        "INNER JOIN membership m ON m.id = o.membership_id\n" +
                        "inner join\n" +
                        "(\n" +
                        " select asn.organisation_id as oid, asn.membership_id as mid\n" +
                        " from resourcedb.asnresource asn\n" +
                        " union\t\n" +
                        " select ipv4.organisation_id as oid, ipv4.membership_id as mid\n" +
                        " from resourcedb.ipv4assignmentresource ipv4\n" +
                        " union\n" +
                        " select ipv6.organisation_id as oid, ipv6.membership_id as mid\n" +
                        " from resourcedb.ipv6assignmentresource ipv6\n" +
                        ") a on m.id = a.mid\n" +
                        "\n" +
                        "where cm.contact_medium_type = 'EMAIL'\n" +
                        "and a.oid is not null\n" +
                        "and a.oid <> ''\n" +
                        "group by a.oid\n" +
                        "order by 2;",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        emailPerSponsoredOrganisation.put(rs.getString(1), rs.getString(2));
                    }
                });
        return emailPerSponsoredOrganisation;
    }
}
