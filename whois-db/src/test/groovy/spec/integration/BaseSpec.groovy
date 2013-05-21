package spec.integration

import net.ripe.db.whois.WhoisFixture
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.profiles.WhoisProfile
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import org.joda.time.LocalDateTime
import spec.domain.AckResponse
import spec.domain.Message
import spec.domain.NotificationResponse
import spec.domain.SyncUpdate
import spock.lang.Specification

// TODO [AK] Now that we also have access to query here, we can expand our tests
abstract class BaseSpec extends Specification {
    static WhoisFixture whoisFixture

    def setupSpec() {
        DatabaseHelper.setupDatabase()

        WhoisProfile.setEndtoend();
        whoisFixture = new WhoisFixture()
        whoisFixture.start()

    }

    def setup() {
        whoisFixture.reset()

        setupObjects(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })
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


    abstract Map<String, String> getFixtures()

    def send(String message) {
        whoisFixture.send(message)
    }

    def send(Message message) {
        message.from = whoisFixture.send(message.subject, message.body.stripIndent())
        message
    }

    AckResponse ackFor(String from) {
        def ack = new AckResponse(whoisFixture.getMessage(from))
        ack
    }

    AckResponse ackFor(Message message) {
        def ack = new AckResponse(whoisFixture.getMessage(message.from))
        ack
    }

    NotificationResponse notificationFor(String to) {
        new NotificationResponse(whoisFixture.getMessage(to))
    }

    String syncUpdate(SyncUpdate syncUpdate) {
        String response = whoisFixture.syncupdate(syncUpdate.data, syncUpdate.help, syncUpdate.diff, syncUpdate.forceNew, syncUpdate.redirect, syncUpdate.post, syncUpdate.responseCode)
        response
    }

    String saveAcl(String path, String data, final int responseCode) {
        whoisFixture.aclPost(path, data, responseCode)
    }

    def noMoreMessages() {
        !whoisFixture.anyMoreMessages()
    }

    def setupObjects(Collection<RpslObject> objects) {
        whoisFixture.createRpslObjects(objects)
    }

    def deleteObject(String key) {
        whoisFixture.deleteRpslObject(RpslObject.parse(fixtures.get(key).stripIndent()));
    }

    def dumpSchema() {
        whoisFixture.dumpSchema()
    }

    def dnsCheckedFor(String key) {
        whoisFixture.dnsCheckedFor(key)
    }

    def setRipeRanges(String... ripeRanges) {
        whoisFixture.setRipeRanges(ripeRanges)
    }

    def setTime(LocalDateTime dateTime) {
        whoisFixture.setTime(dateTime)
    }


    def objectExists(ObjectType objectType, String pkey) {
        whoisFixture.objectExists(objectType, pkey)
    }

    def getDatabaseHelper() {
        return whoisFixture.getDatabaseHelper()
    }

    def getTags(int objectId) {
        return whoisFixture.getTagsDao().getTags(objectId)
    }

    def getRpslObjectDao() {
        return whoisFixture.getRpslObjectDao()
    }

    def getTagsDao() {
        return whoisFixture.getTagsDao()
    }

    def unrefCleanup() {
        whoisFixture.unrefCleanup()
    }

    def oneBasicFixture(String key) {
        def s = fixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return object(s)
    }

    def object(String string) {
        return RpslObject.parse(string.stripIndent()).toString()
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

    def queryObject(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        assert qry =~ "${type}:\\s*${pkey}"

        return qry
    }

    def queryObjectNotFound(String qryStr, String type, String pkey) {
        def qry = query(qryStr)
        !(qry =~ "${type}:\\s*${pkey}")
    }

    def queryNothing(String qryStr) {
        def qry = query(qryStr)
        qry =~ "%ERROR:101: no entries found"
    }

    def query_object_matches(String qry_str, String type, String pkey, String regex) {
        def qry = query(qry_str)
        (qry =~ /(?i)${type}:\s*${pkey}/) && (qry =~ /(?i)${regex}/)
    }

    def query_object_not_matches(String qry_str, String type, String pkey, String regex) {
        def qry = query(qry_str)
        (qry =~ type + ":\\s*" + pkey) && !(qry =~ regex)
    }

    def addObject(String string) {
        whoisFixture.getDatabaseHelper().addObjects([RpslObject.parse(string)])
        string
    }

    def updateObject(String string) {
        whoisFixture.getDatabaseHelper().updateObject(RpslObject.parse(string))
    }

    def removeObject(String string) {
        whoisFixture.getDatabaseHelper().removeObject(RpslObject.parse(string))
    }

}
