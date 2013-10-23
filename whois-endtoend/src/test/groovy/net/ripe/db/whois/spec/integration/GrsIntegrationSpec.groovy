package net.ripe.db.whois.spec.integration
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper

@org.junit.experimental.categories.Category(IntegrationTest.class)
class GrsIntegrationSpec extends BaseSpec {

    def setupSpec() {
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        whoisFixture.start()
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

    def "query --list-sources"() {
      when:
        def response = query("--list-sources")

      then:
        response =~ "TEST"
        response =~ "1-GRS"
        response =~ "2-GRS"
        response =~ "3-GRS"
    }

    def "query --resource AS10 match in GRS"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "aut-num: AS1")
        databaseHelper.addObjectToSource("2-GRS", "aut-num: AS10")
        databaseHelper.addObjectToSource("3-GRS", "aut-num: AS100")
        databaseHelper.addObjectToSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS10")
      then:
        response =~ "aut-num:        AS10"
        response != ~"No entries found"
    }

    def "query --resource AS10 match in 2 GRS sources"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "aut-num: AS10")
        databaseHelper.addObjectToSource("2-GRS", "aut-num: AS10")
        databaseHelper.addObjectToSource("3-GRS", "aut-num: AS100")
        databaseHelper.addObjectToSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS10")
      then:
        response =~ "aut-num:        AS10"
        response != ~"No entries found"
    }

    def "query --resource AS1000 match in alias, not in resource data"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "aut-num: AS1")
        databaseHelper.addObjectToSource("2-GRS", "aut-num: AS10")
        databaseHelper.addObjectToSource("3-GRS", "aut-num: AS100")
        databaseHelper.addObjectToSource("TEST", "aut-num: AS1000")

        def response = query("--resource AS1000")
      then:
        response != ~"aut-num:        AS1000"
        response =~ "No entries found"
    }

    def "query --resource 10.0.0.0 matches inetnum and route"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "inetnum: 10.0.0.0\nnetname: test")
        databaseHelper.addObjectToSource("2-GRS", "route: 10.0.0.0\norigin: AS10")
        databaseHelper.addObjectToSource("3-GRS", "mntner: 10.0.0.0")

        def response = query("--resource 10.0.0.0")
      then:
        response =~ "" +
                "% Information related to '10.0.0.0'\n" +
                "\n" +
                "inetnum:        10.0.0.0\n" +
                "netname:        test\n" +
                "\n" +
                "% Information related to '10.0.0.0AS10'\n" +
                "\n" +
                "route:          10.0.0.0\n" +
                "origin:         AS10"

        response !=~ "mntner"
    }

    def "query -r -T inetnum --resource 10.0.0.0 limits type to inetnum"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "inetnum: 10.0.0.0\nnetname: test")
        databaseHelper.addObjectToSource("2-GRS", "route: 10.0.0.0\norigin: AS10")
        databaseHelper.addObjectToSource("3-GRS", "mntner: 10.0.0.0")

        def response = query("-r -T inetnum --resource 10.0.0.0")
      then:
        response =~ "inetnum"
        response !=~ "route"
        response !=~ "mntner"
    }

    def "query -r -T mntner --resource DEV-MNT is not valid"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "mntner: DEV-MNT")

        def response = query("-r -T mntner --resource DEV-MNT")
      then:
        response =~ "%ERROR:115: invalid search key"
        response !=~ "mntner"
    }
}
