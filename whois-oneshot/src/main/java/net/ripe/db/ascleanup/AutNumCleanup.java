package net.ripe.db.ascleanup;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysql.jdbc.Driver;
import net.ripe.db.whois.common.ClockDateTimeProvider;
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
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.mail.MailConfiguration;
import net.ripe.db.whois.update.mail.MailGateway;
import net.ripe.db.whois.update.mail.MailGatewaySmtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutNumCleanup {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutNumCleanup");
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

    private static final Map<RpslObject, Set<String>> autnumPerObjectMap = Maps.newHashMap();
    private static final String MAIL_FROM = "RIPE Database Administration local <unread@ripe.net>";
    private static final String MAIL_HOST = "massmailer.ripe.net";
    private static final int MAIL_PORT = 25;
    private static final String LOG_DIR = "var";
    private final MailGateway mailGateway;


    public static void main(String[] argv) throws Exception {
//        new AutNumCleanup(createMailGateway()).execute(argv[0]);
        new AutNumCleanup(null).execute(argv[0]);
    }

    public AutNumCleanup(final MailGateway mailGateway) {
        this.mailGateway = mailGateway;
    }

    private static MailGateway createMailGateway() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setPort(MAIL_PORT);
        mailSender.setHost(MAIL_HOST);

        final LoggerContext loggerContext = new LoggerContext(new ClockDateTimeProvider());
        loggerContext.setBaseDir(LOG_DIR);

        return new MailGatewaySmtp(loggerContext, new MailConfiguration(MAIL_FROM), mailSender);
    }

    public void execute(final String mysqlConnectionPassword) throws Exception {
        final Path resourceDataFile = Files.createTempFile("autnumCleanup", "");

        final Downloader downloader = new Downloader();

        downloader.downloadToWithMd5Check(LOGGER, new URL("ftp://ftp.ripe.net/ripe/stats/delegated-ripencc-extended-latest"), resourceDataFile);

        final AuthoritativeResourceLoader authoritativeResourceLoader = new AuthoritativeResourceLoader(LOGGER, "ripe", new Scanner(resourceDataFile), Sets.newHashSet("reserved"));
        final AuthoritativeResource authoritativeResource = authoritativeResourceLoader.load();

        final DataSource dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:mysql://dbc-whois5.ripe.net/WHOIS_UPDATE_RIPE", "rdonly", mysqlConnectionPassword);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        final Joiner JOINER = Joiner.on(',');
        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 " +
                        "AND object_type NOT IN (100," +
                        // quick win filter to reduce data size by >80%
                        JOINER.join(
                                ObjectTypeIds.getId(ObjectType.PERSON),
                                ObjectTypeIds.getId(ObjectType.ROLE),
                                ObjectTypeIds.getId(ObjectType.INETNUM),
                                ObjectTypeIds.getId(ObjectType.INET6NUM),
                                ObjectTypeIds.getId(ObjectType.DOMAIN)
                        ) + ")",
                new CleanupRowCallbackHandler(authoritativeResource));


        for (final RpslObject rpslObject : autnumPerObjectMap.keySet()) {

            JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                    "SELECT object_id, object " +
                            "FROM last " +
                            "WHERE sequence_id != 0 " +
                            "AND object_type = " + ObjectTypeIds.getId(ObjectType.MNTNER) +
                            "AND pkey in ('" + JOINER.join(rpslObject.getValuesForAttribute(AttributeType.MNT_BY)) + "')",
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(final ResultSet rs) throws SQLException {
                            final int objectId = rs.getInt(1);
                            RpslObject object = null;
                            try {
                                object = RpslObject.parse(objectId, rs.getBytes(2));
                            } catch (RuntimeException e) {
                                LOGGER.warn("Unable to parse RPSL object with object_id: {}", objectId);
                            }

                            if (object != null) {
                                for (final CIString email : object.getValuesForAttribute(AttributeType.UPD_TO)) {
//                                    mailGateway.sendEmail(email.toString(), "RIPE NCC Aut-num cleanup", createMailContent(autnumPerObjectMap.get(rpslObject)));
                                    System.err.println(createMailContent(autnumPerObjectMap.get(rpslObject)));
                                    System.err.println("---");
                                }
                            }
                        }
                    });
        }
    }

    private String createMailContent(final Set<String> autnums) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Dear Maintainer, ")
                .append("We're inclined to inform you that you maintain referenced stuff that we want to get rid of.")
                .append("Therefore, clean out your fridge yet more urgently your RIPE database objects, infact ")
                .append("the objects containing references to these autnums:\n")
                .append(Joiner.on(',').join(autnums))
                .append("\nand we shan't bother you again.\n\n")
                .append("Kindly, \nThe RIPE database people.");
        return builder.toString();
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
                LOGGER.warn("Unable to parse RPSL object with object_id: {}", objectId);
            }

            if (object != null && authoritativeResource.isMaintainedInRirSpace(object)) {
                final Set<String> foundAutnums = Sets.newHashSet();

                for (final RpslAttribute attribute : object.findAttributes(ATTRIBUTES_TO_CHECK)) {
                    for (final CIString value : attribute.getCleanValues()) {
                        final Matcher matcher = AS_PATTERN.matcher(value.toString());
                        while (matcher.find()) {
                            foundAutnums.add(matcher.group());
                        }
                    }
                }

                if (autnumPerObjectMap.get(object) == null) {
                    autnumPerObjectMap.put(object, foundAutnums);
                } else {
                    autnumPerObjectMap.get(object).addAll(foundAutnums);
                }
            }
        }
    }
}
