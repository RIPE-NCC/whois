package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class MntnerValidationEmailTestSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getFixtures() {
        return ["DEV-MNT": """\
            mntner:   DEV-MNT
            descr:    description
            admin-c:  TP1-TEST
            mnt-by:   DEV-MNT
            upd-to:   name,email@domain.com
            auth:     MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:   TEST
            """.stripIndent()
        ]
    }

    def "create maintainer with broken upd-to attribute"() {
        when:
           def ack = syncUpdateWithResponse("""\
            mntner:   EMAILTEST-MNT
            descr:    description
            admin-c:  TP1-TEST
            mnt-by:   EMAILTEST-MNT
            upd-to:   name,email@domain.com
            auth:     MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:   TEST
            password: update
            """.stripIndent())

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[mntner] EMAILTEST-MNT" }
        ack.errorMessagesFor("Create", "[mntner] EMAILTEST-MNT") == [
                "Syntax error in name,email@domain.com"
        ]
    }

    def "update maintainer with broken upd-to attribute"() {
        when:
        def ack = syncUpdateWithResponse("""\
            mntner:   DEV-MNT
            descr:    description
            admin-c:  TP1-TEST
            mnt-by:   DEV-MNT
            upd-to:   name,email@domain.com
            auth:     MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:   TEST
            password: update
            """.stripIndent())

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[mntner] DEV-MNT" }
        ack.errorMessagesFor("Modify", "[mntner] DEV-MNT") == [
                "Syntax error in name,email@domain.com"
        ]
    }

    def "update maintainer with correct upd-to attribute"() {
        when:
        def ack = syncUpdateWithResponse("""\
            mntner:   DEV-MNT
            descr:    description
            admin-c:  TP1-TEST
            mnt-by:   DEV-MNT
            upd-to:   email@domain.com   #changed
            auth:     MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:   TEST
            password: update
            """.stripIndent())

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] DEV-MNT" }
    }

    def "update maintainer with upd-to attribute having +"() {
        when:
        def ack = syncUpdateWithResponse("""\
            mntner:   DEV-MNT
            descr:    description
            admin-c:  TP1-TEST
            mnt-by:   DEV-MNT
            upd-to:   email+filter@domain.com   #changed
            auth:     MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:   TEST
            password: update
            """.stripIndent())

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] DEV-MNT" }
    }
}
