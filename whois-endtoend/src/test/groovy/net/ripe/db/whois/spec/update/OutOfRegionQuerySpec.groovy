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
    def "query -mB 193.4.0.0/16 without specified source"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("-mB 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/24AS103'"
        response =~ "route:          193.4.0.0/24\n" +
                    "origin:         AS103\n" +
                    "source:         TEST"
        response =~ "Information related to '193.4.1.0/24AS102'"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
        response !=~ "descr: prove is from 2GRS-source"
    }

    /*
    *  If both sources are defined both "source: RIPE" and “source: RIPE-NONAUTH” ROUTE(6) objects are returned.
    * */
    def "query -source TEST TEST-NONAUTH -mB 193.4.0.0/16 within source TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("-source TEST,TEST-NONAUTH -mB 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/24AS103'"
        response =~ "route:          193.4.0.0/24\n" +
                    "origin:         AS103\n" +
                    "source:         TEST"
        response =~ "Information related to '193.4.1.0/24AS102'"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
        response !=~ "descr: prove is from 2GRS-source"
    }

    /*
     *  If "sources" is used in queries out-of-region resources will be shown only if ‘RIPE-NONAUTH’ is included explicitly.
     *  */
    def "query -s TEST -mB 193.4.0.0/16 within source TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("-s TEST -mB 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/24AS103'"
        response =~ "route:          193.4.0.0/24\n" +
                    "origin:         AS103\n" +
                    "source:         TEST"
        response !=~ "Information related to '193.4.1.0/24AS102'"
        response !=~ "descr: prove is from 2GRS-source"
    }

    def "query -s TEST-NONAUTH -mB 193.4.0.0/16 within source TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("--sources TEST-NONAUTH -mB 193.4.0.0/16")
        then:
        response !=~ "Information related to '193.4.0.0/24AS103'"
        response =~ "Information related to '193.4.1.0/24AS102'"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
        response !=~ "descr: prove is from 2GRS-source"
    }

    /*
    * For queries with the "resources" flag, don't return out of region objects from RIPE-NONAUTH' but from the GRS database
    * */
    def "query --resource -mB 193.4.0.0/16 within source 2-GRS"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("--resource -mB 193.4.0.0/16")
        then:
        response =~ "Information related to '193.4.0.0/24AS103'"
        response =~ "route:          193.4.0.0/24\n" +
                    "origin:         AS103\n" +
                    "source:         TEST"
        response =~ "Information related to '193.4.1.0/24AS102'"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "source:         TEST-NONAUTH"
        response =~ "Information related to '193.4.1.0/24AS102"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "descr:          prove is from 2GRS-source\n" +
                    "source:         2GRS"
    }

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
        response != ~"descr: prove is from TEST-source"
    }

}
