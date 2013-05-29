package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.source.Source

@org.junit.experimental.categories.Category(IntegrationTest.class)
class GrsIntegrationSpec extends BaseSpec {

    def setupSpec() {
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        whoisFixture.start()
    }

    def cleanupSpec() {
        DatabaseHelper.resetGrsSources()
        if (whoisFixture != null) {
            try {
                whoisFixture.stop()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    def "query --list-sources"() {
      when:
        def response = query("--list-sources")

      then:
        response =~ "TEST"
        response =~ "TEST-GRS"
        response =~ "1-GRS"
        response =~ "2-GRS"
        response =~ "3-GRS"
    }

    def "query --resource AS10 match in GRS"() {
      when:
        addObjectForSource("1-GRS", "aut-num: AS1")
        addObjectForSource("2-GRS", "aut-num: AS10")
        addObjectForSource("3-GRS", "aut-num: AS100")
        addObjectForSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS10")
      then:
        response =~ "aut-num:        AS10"
        response != ~"No entries found"
    }

    def "query --resource AS10 match in 2 GRS sources"() {
      when:
        addObjectForSource("1-GRS", "aut-num: AS10")
        addObjectForSource("2-GRS", "aut-num: AS10")
        addObjectForSource("3-GRS", "aut-num: AS100")
        addObjectForSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS10")
      then:
        response =~ "aut-num:        AS10"
        response != ~"No entries found"
    }

    def "query --resource AS1000 match in alias, not in resource data"() {
      when:
        addObjectForSource("1-GRS", "aut-num: AS1")
        addObjectForSource("2-GRS", "aut-num: AS10")
        addObjectForSource("3-GRS", "aut-num: AS100")
        addObjectForSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS1000")
      then:
        response != ~"aut-num:        AS1000"
        response =~ "No entries found"
    }

    private def addObjectForSource(String sourceName, String rpslObject) {
        try {
            whoisFixture.sourceContext.current = Source.master(sourceName)
            databaseHelper.addObject(rpslObject)
        } finally {
            whoisFixture.sourceContext.removeCurrentSource()
        }
    }
}
