package net.ripe.db.whois.spec

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import net.ripe.db.whois.WhoisFixture
import net.ripe.db.whois.WhoisServer
import net.ripe.db.whois.api.MailUpdatesTestSupport
import net.ripe.db.whois.api.httpserver.JettyConfig
import net.ripe.db.whois.api.mail.dequeue.MessageDequeue
import net.ripe.db.whois.common.Messages
import net.ripe.db.whois.common.Slf4JLogConfiguration
import net.ripe.db.whois.common.Stub
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.domain.IpRanges
import net.ripe.db.whois.common.domain.User
import net.ripe.db.whois.common.iptree.IpTreeUpdater
import net.ripe.db.whois.common.profiles.WhoisProfile
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.common.support.DummyWhoisClient
import net.ripe.db.whois.query.QueryServer
import net.ripe.db.whois.update.dns.DnsGatewayStub
import net.ripe.db.whois.update.mail.MailSenderStub
import org.springframework.context.support.ClassPathXmlApplicationContext
import spec.domain.AckResponse
import spec.domain.Message
import spec.domain.NotificationResponse
import spec.domain.SyncUpdate
import spock.lang.Specification

import javax.mail.Address

import static net.ripe.db.whois.common.domain.CIString.ciString

class EndToEndSpec extends Specification {
    protected static ClassPathXmlApplicationContext applicationContext
    protected static DatabaseHelper databaseHelper

    protected static WhoisServer whoisServer
    protected static JettyConfig jettyConfig
    protected static MailUpdatesTestSupport mailUpdatesTestSupport
    protected static MailSenderStub mailSenderStub
    protected static DnsGatewayStub dnsGatewayStub
    protected static IpTreeUpdater ipTreeUpdater
    protected static IpRanges ipRanges
    protected static Collection<Stub> stubs
    protected static MessageDequeue messageDequeue

    def setupSpec() {
        Slf4JLogConfiguration.init();

        System.setProperty("application.version", "0.1-ENDTOEND")
        System.setProperty("mail.dequeue.threads", "2");
        System.setProperty("mail.dequeue.interval", "10");
        System.setProperty("whois.maintainers.power", "RIPE-NCC-HM-MNT");
        System.setProperty("whois.maintainers.enduser", "RIPE-NCC-END-MNT");
        System.setProperty("whois.maintainers.alloc", "RIPE-NCC-HM-MNT,RIPE-NCC-HM2-MNT");
        System.setProperty("whois.maintainers.enum", "RIPE-GII-MNT,RIPE-NCC-MNT");
        System.setProperty("whois.maintainers.dbm", "RIPE-NCC-LOCKED-MNT,RIPE-DBM-MNT");
        System.setProperty("unrefcleanup.enabled", "true");
        System.setProperty("unrefcleanup.deletes", "true");
        System.setProperty("nrtm.enabled", "false")
        WhoisProfile.setEndtoend()
    }

    protected def start() {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext-whois-test.xml")
        databaseHelper = applicationContext.getBean(DatabaseHelper.class)
        whoisServer = applicationContext.getBean(WhoisServer.class)
        jettyConfig = applicationContext.getBean(JettyConfig.class)
        mailUpdatesTestSupport = applicationContext.getBean(MailUpdatesTestSupport.class)
        mailSenderStub = applicationContext.getBean(MailSenderStub.class)
        dnsGatewayStub = applicationContext.getBean(DnsGatewayStub.class)
        ipTreeUpdater = applicationContext.getBean(IpTreeUpdater.class)
        ipRanges = applicationContext.getBean(IpRanges.class)
        stubs = applicationContext.getBeansOfType(Stub.class).values()
        messageDequeue = applicationContext.getBean(MessageDequeue.class)
        whoisServer.start()
    }

    protected def stop() {
        messageDequeue.forceStopNow()
        ipTreeUpdater.stop()
        applicationContext.close()
        whoisServer.stop()
    }

    def setup() {
        databaseHelper.setup()

        databaseHelper.insertUser(User.createWithPlainTextPassword("dbase1", "override1", ObjectType.values()))
        databaseHelper.insertUser(User.createWithPlainTextPassword("dbase2", "override2", ObjectType.values()))

        databaseHelper.clearAclLimits()
        databaseHelper.insertAclIpLimit("0/0", -1, true)
        databaseHelper.insertAclIpLimit("::0/0", -1, true)

        allFixtures()

        stubs.each { it.reset() }

        ipRanges.setTrusted("127.0.0.1", "0:0:0:0:0:0:0:1")
        ipTreeUpdater.rebuild()
    }

    def cleanup() {
    }

    Map<String, String> getBasicFixtures() {
        BasicFixtures.basicFixtures
    }


    Map<String, String> getFixtures() {
        Maps.newHashMap()
    }

    String getFixture(String key) {
        def s = fixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return s.stripIndent()
    }

    Map<String, String> getTransients() {
        Maps.newHashMap()
    }

    String getTransient(String key) {
        def s = transients[key]
        if (s == null) {
            throw new IllegalArgumentException('No transient for ${key}')
        }

        return s.stripIndent()
    }

    private void allFixtures() {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(basicFixtures.values().collect { RpslObject.parse(it.stripIndent()) })
        rpslObjects.addAll(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })

        databaseHelper.addObjects(rpslObjects)
    }

    def oneBasicFixture(String key) {
        def s = BasicFixtures.basicFixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return object(s)
    }

    def object(String string) {
        return RpslObject.parse(string.stripIndent()).toString()
    }

    def dbfixture(String string) {
        databaseHelper.addObject(string)
        string
    }

    def query(String query) {
        ipTreeUpdater.update()
        def result = DummyWhoisClient.query(QueryServer.port, query)

        print """\
>>>>> QUERY

query : ${query}

----

${result}

<<<<<
"""

        result
    }

    def queryObject(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        assert qry =~ "(?i)${type}:\\s*${pkey}"

        return qry
    }

    def queryObjectNotFound(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        !(qry =~ "(?i)${type}:\\s*${pkey}")
    }

    def queryNothing(String qryStr) {
        def qry = query(qryStr)
        qry =~ "%ERROR:101: no entries found"
    }

    def queryError(String qryStr, String errorStr) {
        def qry = query(qryStr)
        qry =~ /(?i)${errorStr}/
    }

    def queryMatches(String qryStr, String matchStr) {
        def qry = query(qryStr)
        qry =~ /(?i)(?s)${matchStr}/
    }

    def queryLineMatches(String qryStr, String matchStr) {
        def qry = query(qryStr)
        qry =~ /(?i)(?m)${matchStr}/
    }

    def query_object_matches(String qry_str, String type, String pkey, String pattern) {
        def qry = query(qry_str)
        qry.split("\n\n").any {
            it =~ /(?i)(?ms)^${type}:\s*${pkey}.*?${pattern}.*?/
        }
    }

    def query_object_not_matches(String qry_str, String type, String pkey, String pattern) {
        !query_object_matches(qry_str, type, pkey, pattern)
    }

    def queryCommentMatches(String qry_str, String prePattern, String pkey, String postPattern) {
        def qry = query(qry_str)
        qry.split("\n\n").any {
            it.trim().startsWith("%") && it =~ /(?i)(?ms)${prePattern}.*?${pkey}.*?${postPattern}/
        }
    }

    def queryCommentNotMatches(String qry_str, String prePattern, String pkey, String postPattern) {
        ! queryCommentMatches(qry_str, prePattern, pkey, postPattern)
    }

    def queryCountObjects(String qry_str) {
        def qry = query(qry_str)
        qry.split("\n\n").count {
            !it.trim().isEmpty() && !it.trim().startsWith("%")
        }
    }

    def queryCountErrors(String qry_str) {
        def qry = query(qry_str)
        qry.split("\n\n").count { it.trim().startsWith("%ERROR") }
    }

    def send(String message) {
        mailUpdatesTestSupport.insert(message)
        println "\n\n>>>>> SEND MESSAGE\n\n"
    }

    def send(Message message) {
        message.from = mailUpdatesTestSupport.insert(message.subject, message.body.stripIndent())

        print """\
>>>>> SEND MESSAGE

from   : ${message.from}
subject: ${message.subject}

----

${message.body.stripIndent()}

<<<<<
"""

        message
    }

    AckResponse ackFor(Message message) {
        def ack = new AckResponse(mailSenderStub.getMessage(message.from))

        print """\
>>>>> RECEIVE ACK

subject: ${ack.subject}

----

${ack.contents}

<<<<<
"""

        ack
    }

    AckResponse ackFor(String from) {
        AckResponse ackResponse = new AckResponse(mailSenderStub.getMessage(from))
        println "\n\n>>>>> RECEIVE ACK\n\nto: " + from + "\nsubject: " + ackResponse.subject + "\n\n" + ackResponse.contents + "\n\n<<<<<\n\n"
        ackResponse
    }

    void printAllRecipients() {
        def recipients = mailSenderStub.getAllRecipients()
        println ">>>>> (TO)"
        if (recipients.isEmpty()) {
            print "no mails left"
        } else {
            for (Address recipient : recipients) {
                println recipient
            }
        }
        println "<<<<<\n"
    }

    NotificationResponse notificationFor(String to) {
        def notification = new NotificationResponse(mailSenderStub.getMessage(to))

        print """\
>>>>> RECEIVE NOTIFICATION

to: ${to}

subject: ${notification.subject}

----

${notification.contents}

<<<<<
"""

        notification
    }

    String syncUpdate(String content) {
        syncUpdate(new SyncUpdate(data: content, redirect: true))
    }

    String syncUpdate(SyncUpdate syncUpdate) {
        print """\
>>>>> SEND SYNCUPDATE

help     : ${syncUpdate.isHelp()}
diff     : ${syncUpdate.isDiff()}
forceNew : ${syncUpdate.isForceNew()}
redirect : ${syncUpdate.isRedirect()}
post     : ${syncUpdate.isPost()}

---

${syncUpdate.getData()}

<<<<<
"""

        def response = WhoisFixture.syncupdate(jettyConfig, syncUpdate.getData(), syncUpdate.isHelp(), syncUpdate.isDiff(), syncUpdate.isForceNew(), syncUpdate.isRedirect(), syncUpdate.getPost(), syncUpdate.getResponseCode())

        print """\
>>>>> RECEIVE SYNCUPDATE RESPONSE

${response}

<<<<<
"""

        response
    }

    def noMoreMessages() {
        !mailSenderStub.anyMoreMessages()
    }

    def dnsStubbedResponse(String domain, String... messages) {
        List<net.ripe.db.whois.common.Message> messageList = Lists.newArrayList()
        for (final String message : messages) {
            messageList.add(new net.ripe.db.whois.common.Message(Messages.Type.ERROR, message))
        }

        dnsGatewayStub.addResponse(ciString(domain), messageList.toArray(new net.ripe.db.whois.common.Message[messageList.size()]))
    }

    def addTag(String pkey, String tag, String data) {
        databaseHelper.getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) SELECT object_id, \"${tag}\", \"${data}\" from last where pkey='${pkey}'");
    }
}
