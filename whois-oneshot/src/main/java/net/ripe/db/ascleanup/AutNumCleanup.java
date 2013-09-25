package net.ripe.db.ascleanup;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mysql.jdbc.Driver;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceLoader;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutNumCleanup {
    private static final Logger logger = LoggerFactory.getLogger("AutNumCleanup");
    private static final Pattern AS_PATTERN = Pattern.compile("(i)(?<!\\w)AS\\d+(?!\\w)");
    private static final List<AttributeType> ATTRIBUTES_TO_CHECK = Lists.newArrayList(
            AttributeType.IMPORT,
            AttributeType.EXPORT,
            AttributeType.MP_IMPORT,
            AttributeType.MP_EXPORT,
            AttributeType.DEFAULT,
            AttributeType.MP_DEFAULT,
            AttributeType.AGGR_BNDRY,
            AttributeType.AGGR_MTD,
            AttributeType.AS_SET,
            AttributeType.COMPONENTS,
            AttributeType.EXPORT_COMPS,
            AttributeType.FILTER,
            AttributeType.MP_FILTER,
            AttributeType.FILTER_SET,
            AttributeType.IFADDR,
            AttributeType.INTERFACE,
            AttributeType.INJECT,
            AttributeType.LOCAL_AS,
            AttributeType.MP_MEMBERS,
            AttributeType.MEMBERS,
            AttributeType.MEMBER_OF,
            AttributeType.PEER,
            AttributeType.PEERING,
            AttributeType.PEERING_SET,
            AttributeType.MP_PEER,
            AttributeType.MP_PEERING,
            AttributeType.ORIGIN,
            AttributeType.ROUTE_SET,
            AttributeType.RTR_SET);

    public static void main(String[] argv) throws Exception {
        final Path resourceDataFile = Files.createTempFile("AutNumCleanup", "");
        final Downloader downloader = new Downloader();

        downloader.downloadGrsData(logger, new URL("ftp://ftp.ripe.net/ripe/stats/delegated-ripencc-extended-latest"), resourceDataFile);

        final AuthoritativeResourceLoader authoritativeResourceLoader = new AuthoritativeResourceLoader(logger, "ripe", new Scanner(resourceDataFile), Sets.newHashSet("available", "reserved"));
        final AuthoritativeResource authoritativeResource = authoritativeResourceLoader.load();

        DataSource dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:mysql://dbc-whois5.ripe.net/WHOIS_UPDATE_RIPE", "rdonly", argv[1]);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 " +
                        "AND object_type NOT IN (100," +
                        // quick win filter to reduce data size by >80%
                        Joiner.on(',').join(
                                ObjectTypeIds.getId(ObjectType.PERSON),
                                ObjectTypeIds.getId(ObjectType.ROLE),
                                ObjectTypeIds.getId(ObjectType.INETNUM),
                                ObjectTypeIds.getId(ObjectType.INET6NUM),
                                ObjectTypeIds.getId(ObjectType.DOMAIN)
                        ) + ")",
                new CleanupRowCallbackHandler(authoritativeResource));
    }

    private static class CleanupRowCallbackHandler implements RowCallbackHandler {
        private final AuthoritativeResource authoritativeResource;

        public CleanupRowCallbackHandler(AuthoritativeResource authoritativeResource) {
            this.authoritativeResource = authoritativeResource;
        }

        @Override
        public void processRow(final ResultSet rs) throws SQLException {

            final int objectId = rs.getInt(1);
            RpslObject object = null;
            try {
                object = RpslObject.parse(objectId, rs.getBytes(2));
            } catch (RuntimeException e) {
                logger.warn("Unable to parse RPSL object with object_id: {}", objectId);
            }

            for (RpslAttribute attribute : object.findAttributes(ATTRIBUTES_TO_CHECK)) {
                for (CIString value : attribute.getCleanValues()) {
                    Matcher matcher = AS_PATTERN.matcher(value.toString());
                    while (matcher.find()) {
                        // TODO: email template
                        System.out.println(object.getKey() + " has " + matcher.group());
                    }
                }
            }
        }
    }
}
