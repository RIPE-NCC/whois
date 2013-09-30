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
    private static final Pattern AS_PATTERN = Pattern.compile("(?i)(?<![\\w:])(AS\\d+)(?![\\w:])");
    private static final List<AttributeType> ATTRIBUTES_TO_CHECK = Lists.newArrayList(
            AttributeType.IMPORT,
            AttributeType.EXPORT,
            AttributeType.MP_IMPORT,
            AttributeType.MP_EXPORT,
            AttributeType.DEFAULT,
            AttributeType.MP_DEFAULT,
            AttributeType.AGGR_BNDRY,
            AttributeType.AGGR_MTD,
            AttributeType.COMPONENTS,
            AttributeType.EXPORT_COMPS,
            AttributeType.FILTER,
            AttributeType.MP_FILTER,
            AttributeType.IFADDR,
            AttributeType.INTERFACE,
            AttributeType.INJECT,
            AttributeType.LOCAL_AS,
            AttributeType.MP_MEMBERS,
            AttributeType.MEMBERS,
            AttributeType.PEER,
            AttributeType.PEERING,
            AttributeType.MP_PEER,
            AttributeType.MP_PEERING,
            AttributeType.ORIGIN
    );

    private static final String MAIL_FROM = "RIPE Database Administration local <unread@ripe.net>";
    private static final String MAIL_HOST = "massmailer.ripe.net";
    private static final int MAIL_PORT = 25;
    private static final String LOG_DIR = "var";
    private static final Joiner JOINER = Joiner.on(',');
    private final MailGateway mailGateway;
    private final Map<RpslObject, Set<String>> objectWithReferencedAutnums = Maps.newHashMap();


    public static void main(String[] argv) throws Exception {
//        new AutNumCleanup(createMailGateway()).execute(argv[0]);
        new AutNumCleanup(null).execute(argv[0]);
    }

    private static MailGateway createMailGateway() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setPort(MAIL_PORT);
        mailSender.setHost(MAIL_HOST);

        final LoggerContext loggerContext = new LoggerContext(new ClockDateTimeProvider());
        loggerContext.setBaseDir(LOG_DIR);

        return new MailGatewaySmtp(loggerContext, new MailConfiguration(MAIL_FROM), mailSender);
    }

    public AutNumCleanup(final MailGateway mailGateway) {
        this.mailGateway = mailGateway;
    }

    public void execute(final String mysqlConnectionPassword) throws Exception {
        final Path resourceDataFile = Files.createTempFile("autnumCleanup", "");

        final Downloader downloader = new Downloader();

        downloader.downloadToWithMd5Check(LOGGER, new URL("ftp://ftp.ripe.net/ripe/stats/delegated-ripencc-extended-latest"), resourceDataFile);

        final AuthoritativeResourceLoader authoritativeResourceLoader = new AuthoritativeResourceLoader(LOGGER, "ripe", new Scanner(resourceDataFile), Sets.newHashSet("reserved"));
        final AuthoritativeResource authoritativeResource = authoritativeResourceLoader.load();

        final DataSource dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:mysql://dbc-whois5.ripe.net/WHOIS_UPDATE_RIPE", "rdonly", mysqlConnectionPassword);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        System.out.println("There are " + authoritativeResource.getNrAutNums() + " reserved autnums");

        findAutnumReferencesPerObject(jdbcTemplate, authoritativeResource);
        sendMails(jdbcTemplate);
    }

    private void findAutnumReferencesPerObject(final JdbcTemplate jdbcTemplate, final AuthoritativeResource authoritativeResource) {
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

                        if (object == null) {
                            return;
                        }

                        final Set<String> foundAutnums = Sets.newHashSet();

                        for (final RpslAttribute attribute : object.findAttributes(ATTRIBUTES_TO_CHECK)) {
                            for (final CIString value : attribute.getCleanValues()) {
                                final Matcher matcher = AS_PATTERN.matcher(value.toString());
                                while (matcher.find()) {
                                    final String match = matcher.group();
                                    if (authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, CIString.ciString(match))) {
                                        foundAutnums.add(match);
                                    }
                                }
                            }
                        }

                        if (!foundAutnums.isEmpty()) {
                            if (objectWithReferencedAutnums.get(object) == null) {
                                objectWithReferencedAutnums.put(object, foundAutnums);
                            } else {
                                objectWithReferencedAutnums.get(object).addAll(foundAutnums);
                            }
                        }
                    }
                });
        //debug
        System.out.println(" Found " + objectWithReferencedAutnums.values().size() + " non-unique autnums");
    }

    private void sendMails(final JdbcTemplate jdbcTemplate) {
        final Map<CIString, List<Container>> mntnerPerContainers = Maps.newHashMap();

        for (final RpslObject rpslObject : objectWithReferencedAutnums.keySet()) {
            if (!objectWithReferencedAutnums.get(rpslObject).isEmpty()) {
                final Set<CIString> mntners = Sets.newHashSet();
                JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                        "SELECT object_id, object" +
                                " FROM last" +
                                " WHERE sequence_id != 0" +
                                " AND object_type = " + ObjectTypeIds.getId(ObjectType.MNTNER) +
                                " AND pkey in ('" + JOINER.join(rpslObject.getValuesForAttribute(AttributeType.MNT_BY)) + "')",
                        new RowCallbackHandler() {
                            @Override
                            public void processRow(final ResultSet rs) throws SQLException {
                                final int objectId = rs.getInt(1);
                                try {
                                    mntners.addAll(RpslObject.parse(objectId, rs.getBytes(2)).getValuesForAttribute(AttributeType.UPD_TO));
                                } catch (RuntimeException e) {
                                    LOGGER.warn("Unable to parse RPSL object with object_id: {}", objectId);
                                }
                            }
                        });

                for (final CIString email : mntners) {
                    if (mntnerPerContainers.get(email) == null || mntnerPerContainers.get(email).isEmpty()) {
                        mntnerPerContainers.put(email, Lists.newArrayList(new Container(rpslObject, objectWithReferencedAutnums.get(rpslObject))));
                    } else {
                        mntnerPerContainers.get(email).add(new Container(rpslObject, objectWithReferencedAutnums.get(rpslObject)));
                    }
                }
            }
        }

        System.out.println("About to send " + mntnerPerContainers.size() + " emails");

        int count = 0;
        for (final CIString email : mntnerPerContainers.keySet()) {
            count++;

//            mailGateway.sendEmail(email.toString(), "RIPE NCC Aut-num cleanup", createMailContent(mntnerPerContainers.get(email)));
            if (count == 2) {
                System.out.println(createMailContent(mntnerPerContainers.get(email), email.toString()));
            }
        }
    }

    private String createMailContent(final List<Container> containers, final String email) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Dear Maintainer ").append(email)
                .append("We're inclined to inform you that you maintain referenced stuff that we want to get rid of.")
                .append("Therefore, clean out your fridge yet more urgently your RIPE database objects\n")
                .append("In these object there are the following references to aut-nums that we'd like you to remove:\n")
                .append("RpslObject\tReferenced aut-num\n");
        for (final Container container : containers) {
            builder.append(container.getObjectWithAutnumReferences().getKey())
                    .append("\t ")
                    .append(JOINER.join(container.getAutnumsFoundInObject()))
                    .append("\n");
        }

        builder.append("\n\nKindly, \nThe RIPE database people.");
        return builder.toString();
    }

    private class Container {
        private RpslObject objectWithAutnumReferences;
        private Set<String> autnumsFoundInObject;

        public Container(final RpslObject object, final Set<String> autnums) {
            this.objectWithAutnumReferences = object;
            this.autnumsFoundInObject = autnums;
            System.out.println("new container: " + object.getKey() + ", and autnums " + JOINER.join(autnums));
        }

        private RpslObject getObjectWithAutnumReferences() {
            return objectWithAutnumReferences;
        }

        private Set<String> getAutnumsFoundInObject() {
            return autnumsFoundInObject;
        }
    }
}
