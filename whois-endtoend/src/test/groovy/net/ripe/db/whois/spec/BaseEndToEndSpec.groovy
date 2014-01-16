package net.ripe.db.whois.spec

import net.ripe.db.whois.WhoisFixture
import net.ripe.db.whois.common.TestDateTimeProvider
import net.ripe.db.whois.common.profiles.WhoisProfile
import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslAttribute
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.NotificationResponse
import net.ripe.db.whois.spec.domain.SyncUpdate
import net.ripe.db.whois.update.dns.DnsGatewayStub
import spock.lang.Specification

import javax.mail.Address

class BaseEndToEndSpec extends Specification {
    static WhoisFixture whoisFixture

    def setupSpec() {
        WhoisProfile.setEndtoend();
        whoisFixture = new WhoisFixture()
    }


    def setup() {
        whoisFixture.reset()
    }

    def cleanupSpec() {
        if (whoisFixture != null) {
            try {
                whoisFixture.stop()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    def query(String query) {
        whoisFixture.reloadTrees()
        def result = whoisFixture.query(query)

        print """\
>>>>> QUERY

query : ${query}

----

${result}

<<<<<
"""

        result
    }

    RpslObject restLookup(ObjectType objectType, String pkey, String... passwords) {
        whoisFixture.reloadTrees();
        return whoisFixture.restLookup(objectType, pkey, passwords);
    }

    boolean hasAttribute(RpslObject object, String attribute, String value, String comment) {
        println " >>> " + object.getKey().toString() + " hasAttribute [" + attribute +": " + value + " # " + comment +"]";

        List<RpslAttribute> attributes = object.findAttributes(AttributeType.getByName(attribute));
        for (RpslAttribute attr : attributes) {
            if (Objects.equals(attr.getValue(), value) && Objects.equals(attr.getCleanComment(), comment)){
                return true;
            }
        }
        return false;
    }

    public List<String> queryPersistent(final List<String> queries){
        whoisFixture.reloadTrees()
        return whoisFixture.queryPersistent(queries)
    }

    def queryObject(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        return objectMatches(qry, type, pkey)
    }

    def objectMatches(String input, String type, String pkey){
        assert input =~ "${type}:\\s*${pkey}"
        return input
    }

    def queryObjectNotFound(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        !(qry =~ "${type}:\\s*${pkey}")
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

    def responseMatches(String input, String matchStr){
        input =~ /(?i)(?m)${matchStr}/
    }

    def query_object_matches(String qry_str, String type, String pkey, String pattern) {
        def qry = query(qry_str)
        qry.split("\n\n").any {
            it =~ /(?i)(?ms)^${type}:\s*${pkey}.*?${pattern}.*?/
        }
        // query_object_matches from integration tests:
        // def query_object_matches(String qry_str, String type, String pkey, String regex) {
        //   def qry = query(qry_str)
        //   (qry =~ /(?i)${type}:\s*${pkey}/) && (qry =~ /(?i)${regex}/)

    }

    def query_object_not_matches(String qry_str, String type, String pkey, String pattern) {
        !query_object_matches(qry_str, type, pkey, pattern)
        // query_object_not_matches from integration tests:
        // def query_object_not_matches(String qry_str, String type, String pkey, String regex) {
        //     def qry = query(qry_str)
        //     (qry =~ type + ":\\s*" + pkey) && !(qry =~ regex)
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
        whoisFixture.send(message)
    }

    def send(Message message) {
        message.from = whoisFixture.send(message.subject, message.body.stripIndent())
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


    AckResponse ackFor(String from) {
        def ack = new AckResponse(whoisFixture.getMessage(from))
        println "\n\n>>>>> RECEIVE ACK\n\nto: " + from + "\nsubject: " + ack.subject + "\n\n" + ack.contents + "\n\n<<<<<\n\n"
        ack
    }

    AckResponse ackFor(Message message) {
        def ack = new AckResponse(whoisFixture.getMessage(message.from))

        print """\
>>>>> RECEIVE ACK

subject: ${ack.subject}

----

${ack.contents}

<<<<<
"""
        ack
    }

    void printAllRecipients() {
        def recipients = getMailSender().getAllRecipients()
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
        def notification = new NotificationResponse(whoisFixture.getMessage(to))

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

---

${syncUpdate.getData()}

<<<<<
"""

        def response = whoisFixture.syncupdate(syncUpdate.getData(), syncUpdate.isHelp(), syncUpdate.isDiff(), syncUpdate.isForceNew(), syncUpdate.isRedirect())

        print """\
>>>>> RECEIVE SYNCUPDATE RESPONSE

${response}

<<<<<
"""
        response
    }

    def noMoreMessages() {
        !whoisFixture.anyMoreMessages()
    }

    def object(String string) {
        return RpslObject.parse(string.stripIndent()).toString()
    }


    def getDatabaseHelper() {
        return whoisFixture.getDatabaseHelper()
    }

    def getIpTreeUpdater() {
        return whoisFixture.getIpTreeUpdater()
    }

    def getIpRanges() {
        return whoisFixture.getIpRanges()
    }

    def getRpslObjectDao() {
        return whoisFixture.getRpslObjectDao()
    }

    def getTagsDao() {
        return whoisFixture.getTagsDao()
    }

    def getPendingUpdateDao() {
        return whoisFixture.getPendingUpdateDao()
    }

    def getApplicationContext() {
        return whoisFixture.getApplicationContext()
    }

    def getMailSender(){
        return whoisFixture.getMailSender()
    }

    public DnsGatewayStub getDnsGatewayStub() {
        return whoisFixture.getDnsGatewayStub();
    }

    public TestDateTimeProvider getTestDateTimeProvider() {
        return whoisFixture.getTestDateTimeProvider();
    }
}


