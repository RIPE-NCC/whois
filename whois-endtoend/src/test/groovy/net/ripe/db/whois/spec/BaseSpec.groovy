package net.ripe.db.whois.spec

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper

abstract class BaseSpec extends EndToEndSpec {
    def setupSpec() {
        DatabaseHelper.setupDatabase()
        start()
    }

    def cleanupSpec() {
        stop()
    }
}
