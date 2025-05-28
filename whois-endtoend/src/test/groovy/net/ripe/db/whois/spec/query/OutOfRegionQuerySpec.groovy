package net.ripe.db.whois.spec.query

import com.google.common.collect.Sets
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.BaseEndToEndSpec
import net.ripe.db.whois.spec.BasicFixtures
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class OutOfRegionQuerySpec extends BaseEndToEndSpec {

    def setupSpec() {
        DatabaseHelper.setupDatabase()
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        whoisFixture.start();
    }

    def setup () {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(BasicFixtures.basicFixtures.values().collect { RpslObject.parse(it.stripIndent(true)) })
        getDatabaseHelper().addObjects(rpslObjects)
    }

    /*
    *  If no source is defined (the default) both "source: RIPE" and “source: RIPE-NONAUTH” ROUTE(6) objects are returned.
    * */
    // J23
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
    def "query -source TEST,TEST-NONAUTH -mB 193.4.0.0/16 within source TEST"() {
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

    def "query autnum in TEST, not return NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST",
            RpslObject.parse(
                "aut-num:        AS100\n" +
                "status:         OTHER\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST-NONAUTH"
            )
        )

        def response = query("-source TEST AS100")
        then:
        !response.contains("% Information related to 'AS100'")
        !response.contains("% No abuse contact registered for AS100")
        !response.contains("organisation:   ORG-OTO1-TEST")
        !response.contains("person:         Test Person")
    }

    def "query autnum in TEST-NONAUTH, not return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST",
            RpslObject.parse(
                "aut-num:        AS100\n" +
                "status:         OTHER\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST"
            )
        )

        def response = query("-source TEST-NONAUTH AS100")
        then:
        !response.contains("% Information related to 'AS100'")
        !response.contains("% No abuse contact registered for AS100")
        response.contains("%ERROR:101: no entries found")
        response.contains("% No entries found in source TEST-NONAUTH.")
    }

    def "query autnum in TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST",
            RpslObject.parse(
                "aut-num:        AS100\n" +
                "status:         OTHER\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST-NONAUTH"
            )
        )

        def response = query("-source TEST-NONAUTH AS100")
        then:
        response.contains("% Information related to 'AS100'")
        response.contains("% Abuse contact for 'AS100' is 'abuse@lir.net'")
        response.contains("organisation:   ORG-OTO1-TEST")
        response.contains("person:         Test Person")
    }

    def "query autnum in TEST and TEST-NONAUTH, return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST",
            RpslObject.parse(
                "aut-num:        AS100\n" +
                "status:         OTHER\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST"
            )
        )

        def response = query("AS100")
        then:
        response.contains("% Information related to 'AS100'")
        response.contains("% Abuse contact for 'AS100' is 'abuse@lir.net'")
        response.contains("aut-num:        AS100")
        response.contains("organisation:   ORG-OTO1-TEST")
        response.contains("person:         Test Person")
    }

    def "query autnum in TEST and TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST",
            RpslObject.parse(
                "aut-num:        AS100\n" +
                "status:         OTHER\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST-NONAUTH"
            )
        )

        def response = query("AS100")
        then:
        response.contains("% Information related to 'AS100'")
        response.contains("% Abuse contact for 'AS100' is 'abuse@lir.net'")
        response.contains("aut-num:        AS100")
        response.contains("organisation:   ORG-OTO1-TEST")
        response.contains("person:         Test Person")
    }

    // C22
    def "query exact route in TEST, do not return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST -B 193.4.0.0/24")
        then:
        response !=~ "source: TEST-NONAUTH"
    }

    // C23
    def "query more specific route in TEST, do not return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST -mB 193.4.0.0/23")
        then:
        !response.contains("TEST-NONAUTH")
    }

    // E22
    def "query exact route in TEST-NONAUTH, do not return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")

        def response = query("-s TEST-NONAUTH -B 193.4.0.0/24")
        then:
        !response.contains("source:         TEST\n")
    }

    // E23
    def "query more specific route in TEST-NONAUTH, do not return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")

        def response = query("-s TEST-NONAUTH -mB 193.4.0.0/23")
        then:
        !response.contains("source:         TEST\n")
    }

    // F22
    def "query exact route in TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -B 193.4.0.0/24")
        then:
        !response.contains("source:         TEST\n")
    }

    // F23
    def "query more specific route in TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -mB 193.4.0.0/23")
        then:
        !response.contains("source:         TEST\n")
    }

    // G23
    def "query more specific route in TEST-NONAUTH, return only TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS102\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -mB 193.4.0.0/23")
        then:
        !response.contains("source:         TEST\n")
    }

    // H22
    def "query exact route in both sources, return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS102\nsource: TEST")

        def response = query("-B 193.4.0.0/24")
        then:
        response.contains("person:         Test Person\n")
        response.contains("route:          193.4.0.0/24\n")
    }

    // H23
    def "query more specific route in both sources, return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS102\nsource: TEST")

        def response = query("-mB 193.4.0.0/23")
        then:
        response.contains("route:          193.4.0.0/24\n")
    }

    // I22
    def "query exact route in both sources, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS102\nsource: TEST-NONAUTH")

        def response = query("-B 193.4.0.0/24")
        then:
        response.contains("person:         Test Person\n")
        response.contains("route:          193.4.0.0/24\n")
        response.contains("source:         TEST-NONAUTH\n")
    }

    // I23
    def "query more specific route in both sources, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS102\nsource: TEST-NONAUTH")

        def response = query("-mB 193.4.0.0/23")
        then:
        response.contains("route:          193.4.0.0/24\n")
        response.contains("source:         TEST-NONAUTH\n")
    }

    // C34
    def "query exact route6 in TEST, do not return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0200::/40\norigin: AS102\nsource: TEST-NONAUTH")

        def response = query("-s TEST -B 2001:1578:0200::/40")
        then:
        !response.contains("route6:         2001:1578:0200::/40\n")
    }

    // C35
    def "query more specific route6 in TEST, do not return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0200::/40\norigin: AS102\nsource: TEST-NONAUTH")

        def response = query("-s TEST -mB 2001:1578:0200::/39")
        then:
        !response.contains("route6:         2001:1578:0200::/40\n")
    }

    // D35
    def "query more specific route6 in TEST, return only TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0200::/40\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST")

        def response = query("-s TEST -mB 2001:1578:0200::/39")
        then:
        response.contains("route6:         2001:1578:0300::/40\n")
        !response.contains("source:         TEST-NONAUTH\n")
    }

    // E34
    def "query exact route6 in TEST-NONAUTH, do not return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST")

        def response = query("-s TEST-NONAUTH -B 2001:1578:0300::/40")
        then:
        !response.contains("person:         Test Person\n")
        !response.contains("route6:         2001:1578:0300::/40\n")
    }

    // E35
    def "query more specific route6 in TEST-NONAUTH, do not return TEST"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST")

        def response = query("-s TEST-NONAUTH -mB 2001:1578:0200::/39")
        then:
        !response.contains("route6:         2001:1578:0300::/40\n")
    }

    // F34
    def "query exact route6 in TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -B 2001:1578:0300::/40")
        then:
        !response.contains("person:         Test Person\n")
        response.contains("route6:         2001:1578:0300::/40\n")
    }

    // F35
    def "query more specific route6 in TEST-NONAUTH, return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -mB 2001:1578:0200::/39")
        then:
        response.contains("route6:         2001:1578:0300::/40\n")
    }

    // G35
    def "query more specific route6 in TEST-NONAUTH, only return TEST-NONAUTH"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0200::/40\norigin: AS102\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route6: 2001:1578:0300::/40\norigin: AS103\nsource: TEST-NONAUTH")

        def response = query("-s TEST-NONAUTH -mB 2001:1578:0200::/39")
        then:
        !response.contains("route6:         2001:1578:0200::/40\n")
        response.contains("route6:         2001:1578:0300::/40\n")
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
    def "query --resource 193.4.1.0/24 within source 2-GRS"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("--resource 193.4.1.0/24")
        then:
        response.contains(
                "% Information related to '193.4.1.0/24AS102'\n" +
                "\n" +
                "route:          193.4.1.0/24\n" +
                "origin:         AS102\n" +
                "descr:          prove is from 2GRS-source\n" +
                "source:         2GRS\n"
        )
    }

    /*
    * For queries with the "resources" flag, don't return out of region objects from RIPE-NONAUTH' but from the GRS database
    * */
    def "query --resource 193.4.0.0/23 within source 2-GRS"() {
        when:
        databaseHelper.addObjectToSource("TEST", "route: 193.4.0.0/24\norigin: AS103\nsource: TEST")
        databaseHelper.addObjectToSource("TEST", "route: 193.4.1.0/24\norigin: AS102\nsource: TEST-NONAUTH")
        databaseHelper.addObjectToSource("2-GRS", "route: 193.4.1.0/24\norigin: AS102\ndescr: prove is from 2GRS-source\nsource: 2GRS")

        def response = query("--resource -mB 193.4.0.0/23")
        then:
        response =~ "Information related to '193.4.0.0/24AS103"
        response =~ "source:         TEST"
        response !=~ "source:         TEST-NONAUTH"
        response =~ "Information related to '193.4.1.0/24AS102"
        response =~ "route:          193.4.1.0/24\n" +
                    "origin:         AS102\n" +
                    "descr:          prove is from 2GRS-source\n" +
                    "source:         2GRS"
    }

}
