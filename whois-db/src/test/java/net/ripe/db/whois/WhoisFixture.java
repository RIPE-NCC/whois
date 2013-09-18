package net.ripe.db.whois;

import com.google.common.collect.Maps;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.JettyConfig;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.dao.jdbc.IndexDao;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.scheduler.task.unref.UnrefCleanup;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.dns.DnsGateway;
import net.ripe.db.whois.update.dns.DnsGatewayStub;
import net.ripe.db.whois.update.mail.MailGateway;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.joda.time.LocalDateTime;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

// TODO [AK] Integrate in BaseSpec
public class WhoisFixture {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".*;charset=(.*)");

    private ClassPathXmlApplicationContext applicationContext;

    protected MailSenderStub mailSender;
    protected MailUpdatesTestSupport mailUpdatesTestSupport;
    protected RpslObjectDao rpslObjectDao;
    protected RpslObjectUpdateDao rpslObjectUpdateDao;
    protected TagsDao tagsDao;
    protected PendingUpdateDao pendingUpdateDao;
    protected MailGateway mailGateway;
    protected MessageDequeue messageDequeue;
    protected DataSource whoisDataSource;
    protected DnsGateway dnsGateway;
    protected IpRanges ipRanges;
    protected TestDateTimeProvider testDateTimeProvider;
    protected JettyConfig jettyConfig;
    protected Map<String, Stub> stubs;
    protected DatabaseHelper databaseHelper;
    protected IpTreeUpdater ipTreeUpdater;
    protected SourceContext sourceContext;
    protected UnrefCleanup unrefCleanup;
    protected IndexDao indexDao;

    private static final String SYNCUPDATES_INSTANCE = "TEST";

    private static final String CHARSET = "ISO-8859-1";

    static {
        Slf4JLogConfiguration.init();

        System.setProperty("application.version", "0.1-TEST");
        System.setProperty("mail.dequeue.threads", "2");
        System.setProperty("mail.dequeue.interval", "10");
        System.setProperty("whois.maintainers.power", "RIPE-NCC-HM-MNT");
        System.setProperty("whois.maintainers.enduser", "RIPE-NCC-END-MNT");
        System.setProperty("whois.maintainers.alloc", "RIPE-NCC-HM-MNT,RIPE-NCC-HM2-MNT");
        System.setProperty("whois.maintainers.enum", "RIPE-GII-MNT,RIPE-NCC-MNT");
        System.setProperty("whois.maintainers.dbm", "RIPE-NCC-LOCKED-MNT,RIPE-DBM-MNT");
        System.setProperty("unrefcleanup.enabled", "true");
        System.setProperty("unrefcleanup.deletes", "true");
        System.setProperty("nrtm.enabled", "false");
    }


    public void start() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext-whois-test.xml");

        mailSender = applicationContext.getBean(MailSenderStub.class);
        mailUpdatesTestSupport = applicationContext.getBean(MailUpdatesTestSupport.class);
        rpslObjectDao = applicationContext.getBean(RpslObjectDao.class);
        rpslObjectUpdateDao = applicationContext.getBean(RpslObjectUpdateDao.class);
        tagsDao = applicationContext.getBean(TagsDao.class);
        pendingUpdateDao = applicationContext.getBean(PendingUpdateDao.class);
        mailGateway = applicationContext.getBean(MailGateway.class);
        dnsGateway = applicationContext.getBean(DnsGateway.class);
        messageDequeue = applicationContext.getBean(MessageDequeue.class);
        whoisDataSource = applicationContext.getBean(SourceAwareDataSource.class);
        ipRanges = applicationContext.getBean(IpRanges.class);
        testDateTimeProvider = applicationContext.getBean(TestDateTimeProvider.class);
        jettyConfig = applicationContext.getBean(JettyConfig.class);
        stubs = applicationContext.getBeansOfType(Stub.class);
        databaseHelper = applicationContext.getBean(DatabaseHelper.class);
        ipTreeUpdater = applicationContext.getBean(IpTreeUpdater.class);
        sourceContext = applicationContext.getBean(SourceContext.class);
        unrefCleanup = applicationContext.getBean(UnrefCleanup.class);
        indexDao = applicationContext.getBean(IndexDao.class);

        databaseHelper.setup();
        applicationContext.getBean(WhoisServer.class).start();

        initData();
    }

    private void initData() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("dbase1", "override1", ObjectType.values()));
        databaseHelper.insertUser(User.createWithPlainTextPassword("dbase2", "override2", ObjectType.values()));
    }

    public void reset() {
        databaseHelper.setup();
        initData();
        ipTreeUpdater.rebuild();

        ipRanges.setTrusted("127.0.0.1", "0:0:0:0:0:0:0:1");
        for (final Stub stub : stubs.values()) {
            stub.reset();
        }
    }

    public void stop() {
        applicationContext.getBean(WhoisServer.class).stop();
    }

    public void dumpSchema() throws Exception {
        DatabaseHelper.dumpSchema(whoisDataSource);
    }

    public String send(final String content) {
        return mailUpdatesTestSupport.insert(content);
    }

    public String send(final String subject, final String body) {
        return mailUpdatesTestSupport.insert(subject, body);
    }

    public MimeMessage getMessage(final String to) throws MessagingException {
        return mailSender.getMessage(to);
    }

    public boolean anyMoreMessages() {
        return mailSender.anyMoreMessages();
    }

    public void createRpslObjects(final Collection<RpslObject> rpslObjects) {
        databaseHelper.addObjects(rpslObjects);
    }

    public void deleteRpslObject(final RpslObject rpslObject) {
        final RpslObjectInfo byKey = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());
        rpslObjectUpdateDao.deleteObject(byKey.getObjectId(), byKey.getKey());
    }

    public String syncupdate(final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect, final boolean doPost, final int responseCode) throws IOException {
        return syncupdate(jettyConfig, data, isHelp, isDiff, isNew, isRedirect, doPost, responseCode);
    }

    public static String syncupdate(final JettyConfig jettyConfig, final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect, final boolean doPost, final int responseCode) throws IOException {
        if (doPost) {
            return doPostRequest(getSyncupdatesUrl(jettyConfig, null), getQuery(data, isHelp, isDiff, isNew, isRedirect), responseCode);
        } else {
            return doGetRequest(getSyncupdatesUrl(jettyConfig, getQuery(data, isHelp, isDiff, isNew, isRedirect)), responseCode);
        }
    }

    public String aclPost(final String path, final String apiKey, final String data, final int responseCode) throws IOException {
        Map<String, String> properties = Maps.newLinkedHashMap();
        properties.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        properties.put(HttpHeaders.CONTENT_LENGTH, Integer.toString(data.length()));
        return doPostRequest(getAclUrl(path, apiKey), data, properties, responseCode);
    }

    public boolean dnsCheckedFor(final String key) {
        return ((DnsGatewayStub) dnsGateway).getCheckedUpdates().remove(ciString(key));
    }

    public void setRipeRanges(final String... ripeRanges) {
        ipRanges.setTrusted(ripeRanges);
    }

    public void setTime(LocalDateTime dateTime) {
        testDateTimeProvider.setTime(dateTime);
    }

    public boolean objectExists(final ObjectType objectType, final String pkey) {
        return 1 == new JdbcTemplate(whoisDataSource).queryForInt("" +
                "select count(*) " +
                "from last " +
                "where object_type = ? " +
                "and pkey = ? " +
                "and sequence_id != 0 ",
                ObjectTypeIds.getId(objectType),
                pkey);
    }

    private static String getSyncupdatesUrl(final JettyConfig jettyConfig, final String query) {
        final StringBuilder builder = new StringBuilder();
        builder.append("http://localhost:");
        builder.append(jettyConfig.getPort(Audience.PUBLIC));
        builder.append("/whois/syncupdates/");
        builder.append(SYNCUPDATES_INSTANCE);
        if (query != null && query.length() > 0) {
            builder.append("?");
            builder.append(query);
        }
        return builder.toString();
    }

    private String getAclUrl(final String path, final String apiKey) {
        final StringBuilder builder = new StringBuilder();
        builder.append("http://localhost:");
        builder.append(jettyConfig.getPort(Audience.INTERNAL));
        builder.append("/api/acl/");
        builder.append(path);
        builder.append("?apiKey=");
        builder.append(apiKey);
        return builder.toString();
    }

    private static String getQuery(final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect) {
        final StringBuilder builder = new StringBuilder();
        int params = 0;

        if ((data != null) && (data.length() > 0)) {
            builder.append("DATA=");
            builder.append(encode(data));
            params++;
        }
        if (isHelp) {
            builder.append(params > 0 ? "&" : "");
            builder.append("HELP=yes");
            params++;
        }
        if (isDiff) {
            builder.append(params > 0 ? "&" : "");
            builder.append("DIFF=yes");
            params++;
        }
        if (isNew) {
            builder.append(params > 0 ? "&" : "");
            builder.append("NEW=yes");
            params++;
        }
        if (isRedirect) {
            builder.append(params > 0 ? "&" : "");
            builder.append("REDIRECT=yes");
        }

        return builder.toString();
    }

    private static String doGetRequest(final String url, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        assertThat(connection.getResponseCode(), is(responseCode));

        return readResponse(connection);
    }

    private static String doPostRequest(final String url, final String data, final int responseCode) throws IOException {
        Map<String, String> properties = Maps.newLinkedHashMap();
        properties.put(HttpHeaders.CONTENT_LENGTH, Integer.toString(data.length()));
        properties.put(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        return doPostRequest(url, data, properties, responseCode);
    }

    private static String doPostRequest(final String url, final String data, final Map<String, String> properties, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setDoInput(true);
        connection.setDoOutput(true);

        Writer writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data);
        writer.close();

        assertThat(connection.getResponseCode(), is(responseCode));

        return readResponse(connection);
    }

    private static String readResponse(final HttpURLConnection connection) throws IOException {
        final InputStream inputStream;

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        final String contentType = connection.getContentType();
        final Matcher matcher = CHARSET_PATTERN.matcher(contentType);
        final String charsetName = matcher.matches() ? matcher.group(1) : Charset.defaultCharset().name();

        final byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        return new String(bytes, charsetName);
    }

    private static String encode(final String data) {
        try {
            return URLEncoder.encode(data, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public TagsDao getTagsDao() {
        return tagsDao;
    }

    public PendingUpdateDao getPendingUpdateDao() {
        return pendingUpdateDao;
    }

    public RpslObjectDao getRpslObjectDao() {
        return rpslObjectDao;
    }

    public String query(final String query) {
        return DummyWhoisClient.query(QueryServer.port, query);
    }

    public void reloadTrees() {
        ipTreeUpdater.update();
    }

    public SourceContext getSourceContext() {
        return sourceContext;
    }

    public void unrefCleanup() {
        unrefCleanup.run();
    }

    public void setIpRanges(String... ranges) {
        ipRanges.setTrusted(ranges);
    }

    public void rebuildIndexes() {
        indexDao.rebuild();
    }
}
