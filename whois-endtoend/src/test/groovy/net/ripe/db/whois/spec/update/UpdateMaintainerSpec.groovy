package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.experimental.categories.Category(EndToEndTest.class)
class UpdateMaintainerSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "CRE-MNT": """\
            mntner: CRE-MNT
            descr: description
            admin-c: TP1-TEST
            mnt-by: OWNER-MNT
            referral-by: CRE-MNT
            upd-to: updto_cre@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
            "SELF-MNT": """\
            mntner: SELF-MNT
            descr: description
            admin-c: TP1-TEST
            mnt-by: SELF-MNT
            referral-by: SELF-MNT
            upd-to: updto_cre@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
            "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TP1-TEST
            mnt-by: OWNER-MNT
            referral-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
            "UPD2-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TP1-TEST
            remarks: added comment
            mnt-by: OWNER-MNT
            referral-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
            "UPD3-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TP1-TEST
            remarks: added comment
            mnt-by: OWNER-MNT
            referral-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120901
            source: TEST
            """,
            "UPD4-MNT": """\
            mntner: UPD-MNT
            descr: description
            adminc: TP1-TEST
            remarks: added comment
            mnt-by: OWNER-MNT
            referral-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120901
            source: TEST
            """
    ]}

    def "create new maintainer already exists"() {
      given:
        def toUpdate = dbfixture(getTransient("UPD-MNT"))

      expect:
        def qryBefore = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "NeW",
                body: toUpdate + "\npassword: owner"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[mntner] UPD-MNT") ==
                ["Enforced new keyword specified, but the object already exists in the database"]

        def qryAfter = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")
        qryBefore == qryAfter
    }

    def "create self maintained maintainer object check pw hash filtering"() {
      expect:
        queryNothing("-rGBT mntner SELF-MNT")

      when:
        def message = send new Message(
                subject: "create SELF-MNT",
                body: """\
                mntner: SELF-MNT
                descr: description
                admin-c: TP1-TEST
                mnt-by: SELF-MNT
                referral-by: SELF-MNT
                upd-to: updto_cre@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Create" && it.key == "[mntner] SELF-MNT"}
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT mntner SELF-MNT", "mntner", "SELF-MNT")
        query_object_not_matches("-rGBT mntner SELF-MNT", "mntner", "SELF-MNT", "\\\$1\\\$fU9ZMQN9\\\$QQtm3kRqZXWAuLpeOiLN7.")
        query_object_matches("-rGBT mntner SELF-MNT", "mntner", "SELF-MNT", "MD5-PW # Filtered")
    }

    def "create maintainer object, no password value"() {
      given:
        def mnt = object(getTransient("SELF-MNT"))

      expect:
        queryNothing("-rGBT mntner SELF-MNT")

      when:
        def message = send new Message(
                subject: "create mntner",
                body: mnt + "password: "
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errorMessagesFor("Create", "[mntner] SELF-MNT") ==
                ["Authorisation for [mntner] SELF-MNT failed using \"mnt-by:\" not authenticated by: SELF-MNT"]

        queryNothing("-rGBT mntner SELF-MNT")
    }

    def "create maintainer object, no password attr"() {
      given:
        def mnt = object(getTransient("SELF-MNT"))

      expect:
        queryNothing("-rGBT mntner SELF-MNT")

      when:
        def message = send new Message(
                subject: "create mntner",
                body: mnt
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errorMessagesFor("Create", "[mntner] SELF-MNT") ==
                ["Authorisation for [mntner] SELF-MNT failed using \"mnt-by:\" not authenticated by: SELF-MNT"]

        queryNothing("-rGBT mntner SELF-MNT")
    }

    def "create maintainer object, wrong password value"() {
      given:
        def mnt = object(getTransient("SELF-MNT"))

      expect:
        queryNothing("-rGBT mntner SELF-MNT")

      when:
        def message = send new Message(
                subject: "create mntner",
                body: mnt + "password: fred"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errorMessagesFor("Create", "[mntner] SELF-MNT") ==
                ["Authorisation for [mntner] SELF-MNT failed using \"mnt-by:\" not authenticated by: SELF-MNT"]

        queryNothing("-rGBT mntner SELF-MNT")
    }

    def "create maintainer object maintained by other mntner"() {
      expect:
        queryNothing("-rGBT mntner CRE-MNT")

      when:
        def message = send new Message(
                subject: "create CRE-MNT",
                body: """\
                mntner: CRE-MNT
                descr: description
                admin-c: TP1-TEST
                mnt-by: OWNER-MNT
                referral-by: CRE-MNT
                upd-to: updto_cre@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Create" && it.key == "[mntner] CRE-MNT"}
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT mntner CRE-MNT", "mntner", "CRE-MNT")
    }

    def "create maintainer object maintained by other mntner using own pw"() {
      expect:
        queryNothing("-rGBT mntner CRE-MNT")

      when:
        def message = send new Message(
                subject: "create CRE-MNT",
                body: """\
                mntner: CRE-MNT
                descr: description
                admin-c: TP1-TEST
                mnt-by: OWNER-MNT
                referral-by: CRE-MNT
                upd-to: updto_cre@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errors.any {it.operation == "Create" && it.key == "[mntner] CRE-MNT"}
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errorMessagesFor("Create", "[mntner] CRE-MNT") ==
                ["Authorisation for [mntner] CRE-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rGBT mntner CRE-MNT", "mntner", "CRE-MNT")
    }

    def "modify maintainer no changes"() {
      given:
        def toUpdate = dbfixture(getTransient("UPD-MNT"))

      expect:
        def qryBefore = queryObject("-r -T mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "update UPD-MNT",
                body: toUpdate + "\npassword: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.find { it.operation == "No operation" && it.key == "[mntner] UPD-MNT"}.warnings == ["Submitted object identical to database object"]

        def qryAfter = queryObject("-r -T mntner UPD-MNT", "mntner", "UPD-MNT")
        qryBefore == qryAfter
    }

    def "update maintainer no changes syncupdate"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        def toUpdate = object(getTransient("UPD-MNT"))

      expect:
        queryObject("-r -T mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def response = syncUpdate(toUpdate + "password: owner")

      then:
        response =~ "No operation: \\[mntner\\] UPD-MNT"
        response =~ "Warning: Submitted object identical to database object"
    }

    def "modify maintainer add remarks"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        def toUpdate = object(getTransient("UPD2-MNT"))

      expect:
        query_object_not_matches("-r -T mntner UPD-MNT", "mntner", "UPD-MNT", "remarks:\\s*added comment")
        queryObject("-r -T mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "update UPD-MNT",
                body: toUpdate + "password: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] UPD-MNT"}
        query_object_matches("-r -T mntner UPD-MNT", "mntner", "UPD-MNT", "remarks:\\s*added comment")

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.added("mntner", "UPD-MNT", "remarks:        added comment")
    }

    def "update maintainer new keyword"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        def toUpdate = object(getTransient("UPD-MNT"))

      expect:
        def qryBefore = queryObject("-rT mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "NEW",
                body: toUpdate + "password: owner"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)

        ack.errors.any {it.key == "[mntner] UPD-MNT" }
        ack.errorMessagesFor("Create", "[mntner] UPD-MNT") ==
                ["Enforced new keyword specified, but the object already exists in the database"]

        def qryAfter = queryObject("-rT mntner UPD-MNT", "mntner", "UPD-MNT")
        qryBefore == qryAfter
    }

    def "modify maintainer object no password value"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        def toUpdate = object(getTransient("UPD3-MNT"))

      expect:
        def qryBefore = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: toUpdate + "password: "
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Modify", "[mntner] UPD-MNT") ==
                ["Authorisation for [mntner] UPD-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        def qryAfter = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")
        qryBefore == qryAfter
    }

    def "modify maintainer object syntax error"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        def toUpdate = object(getTransient("UPD4-MNT"))

      expect:
        def qryBefore = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: toUpdate + "password: owner"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errorMessagesFor("Modify", "[mntner] UPD-MNT")[0] == "\"adminc\" is not a known RPSL attribute"
        ack.errorMessagesFor("Modify", "[mntner] UPD-MNT")[1] == "Mandatory attribute \"admin-c\" is missing"

        def qryAfter = queryObject("-rGBT mntner UPD-MNT", "mntner", "UPD-MNT")
        qryBefore == qryAfter
    }

    def "create maintainer object with all optional and multiple and duplicate attrs"() {
      expect:
        queryNothing("-rGBT mntner CRE-MNT")

      when:
        def message = send new Message(
                subject: "create CRE-MNT",
                body: """\
                mntner: CRE-MNT
                tech-c: TP2-TEST
                descr: description
                +
                org:     ORG-OTO1-TEST
                admin-c: TP2-TEST
                admin-c: TP2-TEST
                org:     ORG-OTO1-TEST
                descr: second description
                remarks:  first line
                          of remarks
                +third line
                +
                mnt-nfy: mntnfy_cre2@ripe.net
                org:     ORG-OTO1-TEST
                org:     ORG-OTO1-TEST
                notify: nfy_cre2@ripe.net
                TECH-c: TP1-TEST
                TECH-c: TP1-TEST
                mnt-nfy: mntnfy_cre@ripe.net
                notify: nfy_cre@ripe.net
                mnt-nfy: mntnfy_cre@ripe.net
                admin-c: TP1-TEST
                mnt-by: CRE-MNT, owner-mnt
                abuse-mailbox: nfy_cre@ripe.net
                referral-by: owner-MNT
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                upd-to: updto_cre@ripe.net
                upd-to: updto_cre2@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                changed: dbtest@ripe.net
                source: TEST
                auth:PGPKEY-D83C3FBD

                password: owner3
                password: update
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Create" && it.key == "[mntner] CRE-MNT"}
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT mntner CRE-MNT", "mntner", "CRE-MNT")
        query_object_not_matches("-rGBT mntner CRE-MNT", "mntner", "CRE-MNT", "\\\$1\\\$fU9ZMQN9\\\$QQtm3kRqZXWAuLpeOiLN7.")
        query_object_matches("-rGBT mntner CRE-MNT", "mntner", "CRE-MNT", "MD5-PW # Filtered")
    }

    // modify maintainer add pgp auth
    @Ignore("TODO: Need to discuss with Denis, it is failing because of the Unknown Object Reference")
    def "modify maintainer add pgp auth"() {
      given:
        dbfixture(getTransient("UPD-MNT"))
        object(getTransient("UPD2-MNT"))

      expect:
        query_object_not_matches("-r -T mntner UPD-MNT", "mntner", "UPD-MNT", "PGPKEY-1290F9D2")

      when:
        def message = send new Message(
                subject: "update UPD-MNT",
                body: """\
                mntner: UPD-MNT
                descr: description
                admin-c: TP1-TEST
                mnt-by: owner-MNT
                referral-by: UPD-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                auth:   PGPKEY-1290F9D2
                changed:     dbtest@ripe.net 20121109
                source:      TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] UPD-MNT"}
        query_object_matches("-r -T mntner UPD-MNT", "mntner", "UPD-MNT", "PGPKEY-1290F9D2")
    }

    def "create maintainer object with disallowed name"() {
      expect:
        queryNothing("-rGBT mntner NEW-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner: NEW-MNT
                descr: description
                admin-c: TP1-TEST
                mnt-by: NEW-MNT
                referral-by: NEW-MNT
                upd-to: updto_cre@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errors.any {it.operation == "Create" && it.key == "[mntner] NEW-MNT"}
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[mntner] NEW-MNT") ==
                ["Reserved name used"]

        queryObjectNotFound("-rGBT mntner NEW-MNT", "mntner", "NEW-MNT")
    }
}
