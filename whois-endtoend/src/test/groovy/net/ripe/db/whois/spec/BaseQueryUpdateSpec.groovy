package net.ripe.db.whois.spec

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import net.ripe.db.whois.common.Message
import net.ripe.db.whois.common.Messages
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.RpslObject
import java.time.LocalDateTime

import java.util.function.Consumer

import static net.ripe.db.whois.common.domain.CIString.ciString

abstract class BaseQueryUpdateSpec extends BaseEndToEndSpec {
    def setupSpec() {
        DatabaseHelper.setupDatabase()
        whoisFixture.start()
    }

    def setup() {
        databaseHelper.clearAclLimits()
        databaseHelper.insertAclIpLimit("0/0", -1, true)
        databaseHelper.insertAclIpLimit("::0/0", -1, true)

        allFixtures();
        allAuthoritativeResources();
    }

    def oneBasicFixture(String key) {
        def s = BasicFixtures.basicFixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return object(s)
    }

    Map<String, String> getFixtures() {
        Maps.newHashMap()
    }

    Map<String, String> getBasicFixtures() {
        BasicFixtures.basicFixtures
    }

    /**
     * Override this method to return a list of test case specific authoritative resources.
     * @return list of authoritative resources
     */
    List<String> getAuthoritativeResources() {
        Lists.newArrayList();
    }

    private void allFixtures() {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(basicFixtures.values().collect { RpslObject.parse(it.stripIndent(true)) })
        rpslObjects.addAll(fixtures.values().collect { RpslObject.parse(it.stripIndent(true)) })

        getDatabaseHelper().addObjects(rpslObjects)
    }

    private void allAuthoritativeResources() {
        if (!getAuthoritativeResources().isEmpty()) {
            // if authoritative resources for this test case have been specified clear the existing ones and add the test case specific ones
            getAuthoritativeResourceDao().delete("test", "0.0.0.0/0")
            getAuthoritativeResourceDao().delete("test", "::/0")
            getAuthoritativeResources().forEach(new Consumer<String>() {
                @Override
                void accept(String res) {
                    getAuthoritativeResourceDao().create("test", res)
                }
            })
            whoisFixture.refreshAuthoritativeResourceData();
        }
    }

    String getFixture(String key) {
        def s = fixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return s.stripIndent(true)
    }

    Map<String, String> getTransients() {
        Maps.newHashMap()
    }

    String getTransient(String key) {
        def s = transients[key]
        if (s == null) {
            throw new IllegalArgumentException('No transient for ${key}')
        }

        return s.stripIndent(true)
    }

    def dbfixture(String string) {
        getDatabaseHelper().addObject(string)
        string
    }

    def grepQueryLog(String pattern) {
        boolean result = false;
        getTestWhoisLog().messages.each { line ->
            if (line =~ pattern) result = true;
        }
        result
    }

    def dnsStubbedResponse(String domain, String... messages) {
        Message[] messageList = new Message[messages.length];
        for (int i = 0; i < messages.length; i++) {
            messageList[i] = new Message(Messages.Type.ERROR, messages[i]);
        }

        getDnsGatewayStub().addResponse(ciString(domain), messageList)
    }

    def setTime(LocalDateTime localDateTime) {
        whoisFixture.setTime(localDateTime)
    }

    def getTime() {
        return whoisFixture.getTestDateTimeProvider().currentDateTime
    }

    def resetTime() {
        whoisFixture.getTestDateTimeProvider().reset()
    }
}
