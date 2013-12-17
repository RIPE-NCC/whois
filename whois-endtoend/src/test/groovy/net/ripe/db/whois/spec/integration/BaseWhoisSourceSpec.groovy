package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.BaseEndToEndSpec
import org.joda.time.LocalDateTime

// TODO [AK] Add query/restapi e2e tests
abstract class BaseWhoisSourceSpec extends BaseEndToEndSpec {

    def setupSpec(){
        DatabaseHelper.setupDatabase()
        whoisFixture.start()
    }

    def setup() {
        whoisFixture.reset()
        setupObjects(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })
    }

    abstract Map<String, String> getFixtures()

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

    def getTags(int objectId) {
        return whoisFixture.getTagsDao().getTags(objectId)
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

    def addObject(String string) {
        getDatabaseHelper().addObjects([RpslObject.parse(string)])
        string
    }

    def removeObject(String string) {
        getDatabaseHelper().removeObject(RpslObject.parse(string))
    }

    def pendingUpdates(ObjectType objectType, String pkey) {
        getPendingUpdateDao().findByTypeAndKey(objectType, pkey)
    }

}
