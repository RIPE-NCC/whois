package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper

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

    def "query sources"() {
      when:
        def response = query("--list-sources")

      then:
        response =~ "TEST"
        response =~ "TEST-GRS"
        response =~ "1-GRS"
        response =~ "2-GRS"
        response =~ "3-GRS"
    }
}
