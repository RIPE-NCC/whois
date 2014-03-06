package net.ripe.db.whois;

import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.api.syncupdate.SyncUpdateBuilder;
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
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.NettyWhoisClientFactory;
import net.ripe.db.whois.common.support.WhoisClientHandler;
import net.ripe.db.whois.db.WhoisServer;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.TestWhoisLog;
import net.ripe.db.whois.scheduler.task.unref.UnrefCleanup;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.dns.DnsGatewayStub;
import net.ripe.db.whois.update.mail.MailGateway;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.joda.time.LocalDateTime;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class WhoisFixture {
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
    protected DnsGatewayStub dnsGatewayStub;

    protected IpRanges ipRanges;
    protected TestDateTimeProvider testDateTimeProvider;
    protected JettyBootstrap jettyBootstrap;
    protected Map<String, Stub> stubs;
    protected DatabaseHelper databaseHelper;
    protected IpTreeUpdater ipTreeUpdater;
    protected SourceContext sourceContext;
    protected UnrefCleanup unrefCleanup;
    protected IndexDao indexDao;
    protected WhoisServer whoisServer;
    protected RestClient restClient;
    protected TestWhoisLog testWhoisLog;

    static {
        Slf4JLogConfiguration.init();

        System.setProperty("application.version", "0.1-ENDTOEND");
        System.setProperty("mail.update.threads", "2");
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
        tagsDao = applicationContext.getBean(TagsDao.class);
        pendingUpdateDao = applicationContext.getBean(PendingUpdateDao.class);
        mailGateway = applicationContext.getBean(MailGateway.class);
        whoisDataSource = applicationContext.getBean(SourceAwareDataSource.class);
        sourceContext = applicationContext.getBean(SourceContext.class);
        unrefCleanup = applicationContext.getBean(UnrefCleanup.class);
        indexDao = applicationContext.getBean(IndexDao.class);
        restClient = applicationContext.getBean(RestClient.class);
        testWhoisLog = applicationContext.getBean(TestWhoisLog.class);

        databaseHelper.setup();
        whoisServer.start();

        restClient.setRestApiUrl(String.format("http://localhost:%s/whois", jettyBootstrap.getPort()));

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

    public String syncupdate(final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect) throws IOException {
        return syncupdate(jettyBootstrap, data, isHelp, isDiff, isNew, isRedirect);
    }

    public static String syncupdate(final JettyBootstrap jettyBootstrap, final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect) throws IOException {
        return new SyncUpdateBuilder()
                .setHost("localhost")
                .setPort(jettyBootstrap.getPort())
                .setSource("TEST")
                .setData(data)
                .setHelp(isHelp)
                .setDiff(isDiff)
                .setNew(isNew)
                .setRedirect(isRedirect)
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
        return 1 == new JdbcTemplate(whoisDataSource).queryForInt("" +
                "select count(*) " +
                "from last " +
                "where object_type = ? " +
                "and pkey = ? " +
                "and sequence_id != 0 ",
                ObjectTypeIds.getId(objectType),
                pkey);
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

    public RpslObject restLookup(ObjectType objectType, String pkey, String... passwords) {
        return restClient.request()
                .addParams("password", passwords)
                .lookup(objectType, pkey);
    }

    public List<String> queryPersistent(final List<String> queries) throws Exception {
        final String END_OF_HEADER = "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n\n";
        final WhoisClientHandler client = NettyWhoisClientFactory.newLocalClient(QueryServer.port);

        List<String> responses = new ArrayList<>();

        client.connectAndWait();

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

    public SourceContext getSourceContext() {
        return sourceContext;
    }

    public void unrefCleanup() {
        unrefCleanup.run();
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
