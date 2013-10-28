package net.ripe.db.whois.spec

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import net.ripe.db.whois.common.Messages
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.RpslObject
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

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

    private void allFixtures() {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(basicFixtures.values().collect { RpslObject.parse(it.stripIndent()) })
        rpslObjects.addAll(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })

        getDatabaseHelper().addObjects(rpslObjects)
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

    def dbfixture(String string) {
        databaseHelper.addObject(string)
        string
    }

    def addTag(String pkey, String tag, String data) {
        getDatabaseHelper().getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) SELECT object_id, \"${tag}\", \"${data}\" from last where pkey='${pkey}'");
    }

    def grepQueryLog(String pattern) {
        DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
        File queryLogFile = new File("var/log/qry/qrylog." + DATE_FORMATTER.print(whoisFixture.getTestDateTimeProvider().currentDate));

        boolean result = false;
        queryLogFile.eachLine { line ->
            if (line =~ pattern) result = true;
        }
        result
    }

    def dnsStubbedResponse(String domain, String... messages) {
        List<net.ripe.db.whois.common.Message> messageList = Lists.newArrayList()
        for (final String message : messages) {
            messageList.add(new net.ripe.db.whois.common.Message(Messages.Type.ERROR, message))
        }

        dnsGatewayStub.addResponse(ciString(domain), messageList.toArray(new net.ripe.db.whois.common.Message[messageList.size()]))
    }
}
