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
        def response = query("--list-sourceNames")

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
}
