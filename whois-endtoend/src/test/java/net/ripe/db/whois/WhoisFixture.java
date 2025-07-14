package net.ripe.db.whois;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.core.MultivaluedMap;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.httpserver.CertificatePrivateKeyPair;
import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import net.ripe.db.whois.api.rest.WhoisRestService;
import net.ripe.db.whois.api.rest.client.NotifierCallback;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.syncupdate.SyncUpdateBuilder;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.AuthoritativeResourceDao;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.dao.jdbc.IndexDao;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpki.DummyRpkiDataProvider;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.db.WhoisServer;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.TestWhoisLog;
import net.ripe.db.whois.update.dns.DnsGatewayStub;
import net.ripe.db.whois.update.mail.MailSenderStub;
import net.ripe.db.whois.update.mail.WhoisMailGatewaySmtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class WhoisFixture {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisFixture.class);

    private ClassPathXmlApplicationContext applicationContext;

    protected MailSenderStub mailSender;
    protected MailUpdatesTestSupport mailUpdatesTestSupport;
    protected RpslObjectDao rpslObjectDao;
    protected RpslObjectUpdateDao rpslObjectUpdateDao;
    protected AuthoritativeResourceDao authoritativeResourceDao;
    protected AuthoritativeResourceData authoritativeResourceData;
    protected WhoisMailGatewaySmtp mailGateway;
    protected MessageDequeue messageDequeue;
    protected DataSource whoisDataSource;
    protected DataSource internalsDataSource;
    protected DnsGatewayStub dnsGatewayStub;

    protected IpRanges ipRanges;
    protected TestDateTimeProvider testDateTimeProvider;
    protected JettyBootstrap jettyBootstrap;
    protected Map<String, Stub> stubs;
    protected DatabaseHelper databaseHelper;
    protected IpTreeUpdater ipTreeUpdater;
    protected SourceContext sourceContext;
    protected IndexDao indexDao;
    protected WhoisServer whoisServer;
    protected QueryServer queryServer;
    protected RestClient restClient;
    protected WhoisRestService whoisRestService;

    protected DummyRpkiDataProvider rpkiDataProvider;
    protected TestWhoisLog testWhoisLog;

    static {
        Slf4JLogConfiguration.init();
        System.setProperty("application.version", "0.1-ENDTOEND");
        System.setProperty("mail.update.threads", "2");
        System.setProperty("mail.dequeue.interval", "10");
        System.setProperty("grs.sources", "TEST-GRS");
        System.setProperty("feature.toggle.changed.attr.available", "true");
        System.setProperty("ipranges.bogons", "192.0.2.0/24,2001:2::/48");
        System.setProperty("git.commit.id.abbrev", "0");
        // enable https
        final CertificatePrivateKeyPair certificatePrivateKeyPair = new CertificatePrivateKeyPair();
        System.setProperty("port.api.secure", "0");
        System.setProperty("http.sni.host.check", "false");
        System.setProperty("whois.certificates", certificatePrivateKeyPair.getCertificateFilename());
        System.setProperty("whois.private.keys", certificatePrivateKeyPair.getPrivateKeyFilename());
    }

    public void start() throws Exception {
        applicationContext = WhoisProfile.initContextWithProfile("applicationContext-whois-test.xml", WhoisProfile.TEST);

        databaseHelper = applicationContext.getBean(DatabaseHelper.class);
        whoisServer = applicationContext.getBean(WhoisServer.class);
        jettyBootstrap = applicationContext.getBean(JettyBootstrap.class);
        mailUpdatesTestSupport = applicationContext.getBean(MailUpdatesTestSupport.class);
        mailSender = applicationContext.getBean(MailSenderStub.class);
        dnsGatewayStub = applicationContext.getBean(DnsGatewayStub.class);
        ipTreeUpdater = applicationContext.getBean(IpTreeUpdater.class);
        ipRanges = applicationContext.getBean(IpRanges.class);
        stubs = applicationContext.getBeansOfType(Stub.class);
        messageDequeue = applicationContext.getBean(MessageDequeue.class);
        testDateTimeProvider = applicationContext.getBean(TestDateTimeProvider.class);
        rpslObjectDao = applicationContext.getBean(RpslObjectDao.class);
        rpslObjectUpdateDao = applicationContext.getBean(RpslObjectUpdateDao.class);
        authoritativeResourceDao = applicationContext.getBean(AuthoritativeResourceDao.class);
        authoritativeResourceData = applicationContext.getBean(AuthoritativeResourceData.class);
        mailGateway = applicationContext.getBean(WhoisMailGatewaySmtp.class);
        whoisDataSource = applicationContext.getBean(SourceAwareDataSource.class);
        internalsDataSource = applicationContext.getBean("internalsDataSource", DataSource.class);
        sourceContext = applicationContext.getBean(SourceContext.class);
        indexDao = applicationContext.getBean(IndexDao.class);
        restClient = applicationContext.getBean(RestClient.class);
        whoisRestService = applicationContext.getBean(WhoisRestService.class);
        testWhoisLog = applicationContext.getBean(TestWhoisLog.class);
        rpkiDataProvider = applicationContext.getBean(DummyRpkiDataProvider.class);

        databaseHelper.setup();
        whoisServer.start();

        queryServer = applicationContext.getBean(QueryServer.class);

        ReflectionTestUtils.setField(restClient, "restApiUrl", String.format("http://localhost:%s/whois", jettyBootstrap.getPort()));
        ReflectionTestUtils.setField(whoisRestService, "baseUrl", String.format("http://localhost:%d/whois", jettyBootstrap.getPort()));

        initData();
    }

    private void initData() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("denis", "override1", ObjectType.values()));
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
        whoisServer.stop();
    }

    public void dumpSchema() throws Exception {
        DatabaseHelper.dumpSchema(whoisDataSource);
    }

    public void dumpInternalsSchema() throws Exception {
        DatabaseHelper.dumpSchema(internalsDataSource);
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

    public void clearAllMails()  {
        mailSender.reset();
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

    public String syncupdate(final String data, final String charset, final boolean isHelp, final boolean isDiff,
                             final boolean isNew, final boolean isRedirect, final MultivaluedMap<String, String> headers) {
        return syncupdate(jettyBootstrap, data, charset, isHelp, isDiff, isNew, isRedirect, headers);
    }

    public static String syncupdate(final JettyBootstrap jettyBootstrap, final String data, final String charset,
                                    final boolean isHelp, final boolean isDiff, final boolean isNew,
                                    final boolean isRedirect, final MultivaluedMap<String, String> headers) {
        return new SyncUpdateBuilder()
                .setProtocol("https")
                .setHost("localhost")
                .setPort(jettyBootstrap.getSecurePort())
                .setSource("TEST")
                .setData(data)
                .setCharset(charset)
                .setHelp(isHelp)
                .setDiff(isDiff)
                .setNew(isNew)
                .setRedirect(isRedirect)
                .setHeaders(headers)
                .build()
                .post();
    }

    public boolean dnsCheckedFor(final String key) {
        return dnsGatewayStub.getCheckedUpdates().remove(ciString(key));
    }

    public void setRipeRanges(final String... ripeRanges) {
        ipRanges.setTrusted(ripeRanges);
    }

    public void setTime(LocalDateTime dateTime) {
        testDateTimeProvider.setTime(dateTime);
    }

    public boolean objectExists(final ObjectType objectType, final String pkey) {
        return 1 == new JdbcTemplate(whoisDataSource).queryForObject(
                "select count(*) " +
                "from last " +
                "where object_type = ? " +
                "and pkey = ? " +
                "and sequence_id != 0 ",
                Integer.class,
                ObjectTypeIds.getId(objectType), pkey);
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public DummyRpkiDataProvider getRpkiDataProvider(){
        return rpkiDataProvider;
    }
    public AuthoritativeResourceDao getAuthoritativeResourceDao() {
        return authoritativeResourceDao;
    }

    public RpslObjectDao getRpslObjectDao() {
        return rpslObjectDao;
    }

    public String query(final String query) {
        return TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);
    }

    public RpslObject restLookup(ObjectType objectType, String pkey, String... passwords) {
        return restClient.request()
                .addParams("password", passwords)
                .lookup(objectType, pkey);
    }


    public RpslObject restPost(RpslObject rpslObject, final List<ErrorMessage> errors, String... passwords) {
        return restClient.request()
                .addParams("password", passwords)
                .setNotifier(new NotifierCallback() {
                    @Override
                    public void notify(List<ErrorMessage> messages) {
                        errors.addAll(messages);
                    }
                })
                .create(rpslObject);
    }

    public RpslObject restPut(RpslObject rpslObject,  final List<ErrorMessage> errors, String... passwords) {
        return restClient.request()
                .addParams("password", passwords)
                .setNotifier(new NotifierCallback() {
                    @Override
                    public void notify(List<ErrorMessage> messages) {
                        errors.addAll(messages);
                    }
                })
                .update(rpslObject);
    }

    public RpslObject restDelete(RpslObject rpslObject,  final List<ErrorMessage> errors, String... passwords) {
        return restClient.request()
                .addParams("password", passwords)
                .setNotifier(new NotifierCallback() {
                    @Override
                    public void notify(List<ErrorMessage> messages) {
                        errors.addAll(messages);
                    }
                })
                .delete(rpslObject);
    }

    public List<String> queryPersistent(final List<String> queries) throws Exception {
        final String END_OF_HEADER = "% See https://docs.db.ripe.net/terms-conditions.html\n\n";
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(queryServer.getPort());

        List<String> responses = new ArrayList<>();

        client.connectAndWait();
        client.waitForResponseEndsWith(END_OF_HEADER);

        for (Iterator<String> it = queries.iterator(); it.hasNext(); ) {
            client.sendLine(it.next());
            if (it.hasNext()) {
                client.waitForResponseEndsWith(END_OF_HEADER);
            } else {
                client.waitForClose();
            }
            responses.add(client.getResponse());
            client.clearBuffer();
        }
        return responses;
    }

    public void reloadTrees() {
        ipTreeUpdater.update();
    }

    public void refreshAuthoritativeResourceData() {
        authoritativeResourceData.refreshActiveSource();
    }

    public SourceContext getSourceContext() {
        return sourceContext;
    }

    public void rebuildIndexes() {
        indexDao.rebuild();
    }

    public IpRanges getIpRanges() {
        return ipRanges;
    }

    public IpTreeUpdater getIpTreeUpdater() {
        return ipTreeUpdater;
    }

    public TestDateTimeProvider getTestDateTimeProvider() {
        return testDateTimeProvider;
    }

    public ClassPathXmlApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public DnsGatewayStub getDnsGatewayStub() {
        return dnsGatewayStub;
    }

    public TestWhoisLog getTestWhoisLog() {
        return testWhoisLog;
    }

    public MailSenderStub getMailSender() {
        return mailSender;
    }
}
