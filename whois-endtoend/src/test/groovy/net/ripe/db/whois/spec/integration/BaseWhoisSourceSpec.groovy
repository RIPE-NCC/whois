package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.FormatHelper
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.BaseEndToEndSpec
import java.time.LocalDateTime

abstract class BaseWhoisSourceSpec extends BaseEndToEndSpec {

    def setupSpec(){
        DatabaseHelper.setupDatabase()
        whoisFixture.start()
    }

    def setup() {
        whoisFixture.reset()
        setupObjects(fixtures.values().collect { RpslObject.parse(it.stripIndent(true)) })
    }

    abstract Map<String, String> getFixtures()

    def setupObjects(Collection<RpslObject> objects) {
        whoisFixture.createRpslObjects(objects)
    }

    def deleteObject(String key) {
        whoisFixture.deleteRpslObject(RpslObject.parse(fixtures.get(key).stripIndent(true)));
    }

    def dumpSchema() {
        whoisFixture.dumpSchema()
    }

    def dumpInternalsSchema() {
        whoisFixture.dumpInternalsSchema()
    }

    def dnsCheckedFor(String key) {
        whoisFixture.dnsCheckedFor(key)
    }

    def setRipeRanges(String... ripeRanges) {
        whoisFixture.setRipeRanges(ripeRanges)
    }

    def setTime(LocalDateTime localDateTime) {
        whoisFixture.setTime(localDateTime)
    }

    def getTime() {
        return whoisFixture.getTestDateTimeProvider().currentDateTime;
    }

    def getTimeUtcString() {
        return FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentZonedDateTime)
    }

    def resetTime() {
        whoisFixture.getTestDateTimeProvider().reset()
    }

    def objectExists(ObjectType objectType, String pkey) {
        whoisFixture.objectExists(objectType, pkey)
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
        getDatabaseHelper().deleteObject(RpslObject.parse(string))
    }

}
