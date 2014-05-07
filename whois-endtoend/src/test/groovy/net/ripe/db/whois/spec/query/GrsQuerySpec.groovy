package net.ripe.db.whois.spec.query
import com.google.common.collect.Sets
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.BaseEndToEndSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class GrsQuerySpec extends BaseEndToEndSpec {

    static def grsFixtures = [
            "TST-MNT": """\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "TST-MNT2": """\
                mntner:      TST-MNT2
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$bnGNJ2PC\$4r38DENnw07.9ktKP//Kf1  #test2
                mnt-by:      TST-MNT2
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
            "TEST-PN2": """\
                person:  Test Person2
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: TP2-TEST
                mnt-by:  TST-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
            "OWNER-MNT": """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner@ripe.net
                mnt-nfy:     mntnfy_owner@ripe.net
                notify:      notify_owner@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                referral-by: OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
    ]

    def setupSpec() {
        DatabaseHelper.setupDatabase()
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        whoisFixture.start();
    }

    def setup () {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(grsFixtures.values().collect { RpslObject.parse(it.stripIndent()) })
        getDatabaseHelper().addObjects(rpslObjects)
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

    def "query --resource 2001:2002::/64 matches inet6num and route"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "inet6num: 2001:2002::/64\nnetname: test")
        databaseHelper.addObjectToSource("1-GRS", "route6: 2001:2002::/64\norigin: AS10")

        def response = query("--resource 2001:2002::/64")
      then:
        response =~ "" +
                "% Information related to '2001:2002::/64'\n" +
                "\n" +
                "inet6num:       2001:2002::/64\n" +
                "netname:        test\n" +
                "\n" +
                "% Information related to '2001:2002::/64AS10'\n" +
                "\n" +
                "route6:         2001:2002::/64\n" +
                "origin:         AS10"
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

    def "query -s 1-GRS -r -T inetnum 10.0.0.0 limits type to inetnum"() {
      when:
        databaseHelper.addObjectToSource("1-GRS", "inetnum: 10.0.0.0\nnetname: test")
        databaseHelper.addObjectToSource("1-GRS", "route: 10.0.0.0\norigin: AS10")

        def response = query("-s 1-GRS -r -T inetnum 10.0.0.0")
      then:
        response =~ "inetnum"
        response !=~ "route"
    }


    def "query -r -T mntner --resource DEV-MNT is not valid"() {
        when:
        databaseHelper.addObjectToSource("1-GRS", "mntner: DEV-MNT")

        def response = query("-r -T mntner --resource DEV-MNT")
        then:
        response =~ "%ERROR:115: invalid search key"
        response !=~ "mntner"
    }

    def "--all-sources found in both"() {
      given:
        databaseHelper.addObject("" +
                "role:    Abuse Me\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "e-mail:  dbtest@ripe.net\n" +
                "admin-c: AB-TEST\n" +
                "tech-c:  AB-TEST\n" +
                "nic-hdl: AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:  TST-MNT2\n" +
                "changed: dbtest@ripe.net 20121016\n" +
                "source:  TEST")
        databaseHelper.addObjectToSource("1-GRS", "role: Abuse Me\nnic-hdl: AB-TEST\nsource: 1-GRS")

      expect:
        query("-q sources") =~ "TEST:3:N:0-0"
        query("-q sources") =~ "1-GRS:3:N:0-0"
        queryObject("--all-sources AB-TEST", "source", "TEST")
        queryObject("--all-sources AB-TEST", "source", "1-GRS")

    }

    def "--all-sources not found"() {
      given:
        databaseHelper.addObject("" +
                "role:    Abuse Me\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "e-mail:  dbtest@ripe.net\n" +
                "admin-c: AB-TEST\n" +
                "tech-c:  AB-TEST\n" +
                "nic-hdl: AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:  TST-MNT2\n" +
                "changed: dbtest@ripe.net 20121016\n" +
                "source:  TEST")
        databaseHelper.addObjectToSource("1-GRS", "role: Abuse Me\nnic-hdl: AB-TEST")

        expect:
        queryObjectNotFound("--all-sources TEST,1- AB-TEST", "role", "Abuse Me")
    }

    def "--sources search only in test"() {
      given:
        databaseHelper.addObjectToSource("1-GRS", "role: Abuse Me\nnic-hdl: AB-TEST")
        databaseHelper.addObject("" +
                "role:    Abuse Me\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "e-mail:  dbtest@ripe.net\n" +
                "admin-c: AB-TEST\n" +
                "tech-c:  AB-TEST\n" +
                "nic-hdl: AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:  TST-MNT2\n" +
                "changed: dbtest@ripe.net 20121016\n" +
                "source:  TEST")

        expect:
          queryObject("--sources TEST AB-TEST", "role", "Abuse Me")
          queryCountObjects("--sources TEST AB-TEST") == 1
    }

    def "--sources search in test and grs"() {
      given:
        databaseHelper.addObjectToSource("1-GRS", "role: Grs Role\nnic-hdl: AB-TEST")
        databaseHelper.addObject("" +
                "role:    Abuse Me\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "e-mail:  dbtest@ripe.net\n" +
                "admin-c: AB-TEST\n" +
                "tech-c:  AB-TEST\n" +
                "nic-hdl: AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:  TST-MNT2\n" +
                "changed: dbtest@ripe.net 20121016\n" +
                "source:  TEST")

      expect:
        queryObject("--sources TEST,1-GRS AB-TEST", "role", "Abuse Me")
        queryObject("--sources TEST,1-GRS AB-TEST", "role", "Grs Role")
        queryCountObjects("--sources TEST,1-GRS AB-TEST") == 2
    }

    def "--resource found in grs-source"() {
      given:
        databaseHelper.addObjectToSource("1-GRS", "inet6num: 2001:2002::/64\nnetname: TEST-NET")

      expect:
        queryObject("--resource 2001:2002::/64", "inet6num", "2001:2002::/64")
    }

    def "--resource aut-num "() {
      given:
        databaseHelper.addObjectToSource("1-GRS", "aut-num: AS123")

      expect:
        queryObject("--resource AS123", "aut-num", "AS123")
    }
}
