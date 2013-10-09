package net.ripe.db.whois.spec.query

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.spec.EndToEndSpec

class GrsQuerySpec extends EndToEndSpec {
    def setupSpec() {
        DatabaseHelper.setupDatabase()
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS")
        start()
    }

    def cleanupSpec() {
        stop()
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

        def response = query("--resource AS10")
      then:
        response =~ "aut-num:        AS10"
        response != ~"No entries found"
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
