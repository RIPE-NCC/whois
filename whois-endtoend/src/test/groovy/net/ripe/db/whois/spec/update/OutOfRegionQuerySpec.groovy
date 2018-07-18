package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.spec.BaseEndToEndSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class OutOfRegionQuerySpec extends BaseEndToEndSpec {

    def setupSpec() {
        DatabaseHelper.setupDatabase()
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        whoisFixture.start();
    }

    /*
    *  If no source is defined (the default) both "source: RIPE" and “source: RIPE-NONAUTH” ROUTE(6) objects are returned.
    * */
    def "query -B 193.4.0.0/16 without specified source"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/16\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("TEST", "inetnum: 193.4.0.0/16\nsource: TEST")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.0.0/16\norigin: AS102\nsource: 2GRS")

        def response = query("-B 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/16'"
        response =~ "inetnum:        193.4.0.0/16\n" +
                    "source:         TEST"
        response =~ "route:          193.4.0.0/16\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
    }

    /*
     *  If "sources" is used in queries out-of-region resources will be shown only if ‘RIPE-NONAUTH’ is included explicitly.
     *  */
    def "query -s TEST -B 193.4.0.0/16 within source TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/16\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("TEST", "inetnum: 193.4.0.0/16\nsource: TEST")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.0.0/16\norigin: AS102\nsource: 2GRS")

        def response = query("--sources TEST -B 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/16'"
        response =~ "inetnum:        193.4.0.0/16\n" +
                    "source:         TEST"
        response !=~ "route:          193.4.0.0/16"
    }

    def "query -s TEST-NONAUTH -B 193.4.0.0/16 within source TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/16\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("TEST", "inetnum: 193.4.0.0/16\nsource: TEST")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.0.0/16\norigin: AS102\nsource: 2GRS")

        def response = query("--sources TEST-NONAUTH -B 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/16'"
        response =~ "inetnum:        193.4.0.0/16\n"+
                    "source:         TEST"
        response =~ "route:          193.4.0.0/16\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
    }

    /*
    * For queries with the "resources" flag, don't return out of region objects from RIPE-NONAUTH' but from the GRS database
    * */
    def "query --resource AS1000 match in GRS"() {
        when:
        databaseHelper.addObjectToSource("1-GRS", "aut-num: AS1000\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "aut-num: AS10")
        databaseHelper.addObjectToSource("3-GRS", "aut-num: AS100")
        databaseHelper.addObjectToSource("TEST", "aut-num: AS1000\ndescr: prove is from TEST-source\nsource: TEST-NONAUTH")

        def response = query("--resource AS1000")
        then:
        response =~ "aut-num:        AS1000"
        response != ~"No entries found"
    }

}
