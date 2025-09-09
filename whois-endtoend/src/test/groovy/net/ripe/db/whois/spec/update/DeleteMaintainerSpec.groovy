package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class DeleteMaintainerSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "DEL-MNT": """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-nfy:     mntnfy_test@ripe.net
                notify:      notif_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                source:      TEST
                """,
            "DEL-DIFF-MNT": """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                descr:       object not identical to one above
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                source:      TEST
                """,
            "DEL2-MNT": """\
                mntner:      DEL2-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "LOWER": """\
                inetnum:     10.0.0.0 - 10.0.0.255
                netname:     TestInetnum
                descr:       Inetnum for testing
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED PA
                mnt-by:      OWNER-MNT
                mnt-lower:   DEL-MNT
                source:      TEST
                """,
            "DOM": """\
                inetnum:     10.0.0.0 - 10.0.0.255
                netname:     TestInetnum
                descr:       Inetnum for testing
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED PA
                mnt-by:      OWNER-MNT
                mnt-domains: DEL-MNT
                source:      TEST
                """,
            "ROUTE": """\
                inetnum:     10.0.0.0 - 10.0.0.255
                netname:     TestInetnum
                descr:       Inetnum for testing
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED PA
                mnt-by:      OWNER-MNT
                mnt-routes:  DEL-MNT {10.0.0.0/32}
                source:      TEST
                """
    ]}

    def "delete non-existent maintainer"() {
      given:
        def non_exist = object(getTransient("DEL-MNT"))

      expect:
        queryObjectNotFound("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete non existent MNTNER DEL-MNT",
                body: non_exist + "delete: reason\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 3, 0)

        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") == ["Object [mntner] DEL-MNT does not exist in the database"]

        queryNothing("-r -T mntner DEL-MNT")

        noMoreMessages()
    }

    def "delete maintainer with override password"() {
      given:
        def non_exist = object(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete non existent MNTNER DEL-MNT",
                body: non_exist + "delete: reason\noverride: delete"
        )

      then:
        ackFor message
        noMoreMessages()
    }

    def "delete self-maintained maintainer"() {
      given:
        def toDelete = dbfixture(getTransient("DEL-MNT"))

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: delete"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[mntner] DEL-MNT" }

        queryNothing("-r -T mntner DEL-MNT")

        def notif = notificationFor "notif_test@ripe.net"
        notif.deleted.any { it.type == "mntner" && it.key == "DEL-MNT" }

        noMoreMessages()
    }

    def "delete maintainer with referral-by"() {
        given:
        def toDelete = dbfixture( """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-nfy:     mntnfy_test@ripe.net
                notify:      notif_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                referral-by: DEL-MNT
                mnt-by:      DEL-MNT
                source:      TEST
                """.stripIndent(true))

        expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

        when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: delete"
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[mntner] DEL-MNT" }

        queryNothing("-r -T mntner DEL-MNT")

        def notif = notificationFor "notif_test@ripe.net"
        notif.deleted.any { it.type == "mntner" && it.key == "DEL-MNT" }

        noMoreMessages()
    }

    def "delete non self-maintained maintainer"() {
      given:
        def toDelete = dbfixture(getTransient("DEL2-MNT"))

      expect:
        queryObject("-r -T mntner DEL2-MNT", "mntner", "DEL2-MNT")
        queryObject("-r -T mntner OWNER-MNT", "mntner", "OWNER-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL2-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: owner"
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[mntner] DEL2-MNT" }
        queryNothing("-r -T mntner DEL2-MNT")

        def notification = notificationFor "mntnfy_owner@ripe.net"
        notification.subject == "Notification of RIPE Database changes"
        notification.deleted.any { it.type == "mntner" && it.key == "DEL2-MNT" }

        noMoreMessages()
    }

    def "delete non self-maintained maintainer with own password"() {
      given:
        def toDelete = dbfixture(getTransient("DEL2-MNT"))

      expect:
        queryObject("-r -T mntner DEL2-MNT", "mntner", "DEL2-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL2-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: delete"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 3, 0)

        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL2-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL2-MNT") ==
                ["Authorisation for [mntner] DEL2-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObject("-r -T mntner DEL2-MNT", "mntner", "DEL2-MNT")

        def notif = notificationFor "updto_owner@ripe.net"
        notif.authFailed("DELETE", "mntner", "DEL2-MNT")

        noMoreMessages()
    }

    def "delete maintainer authentication failed"() {
      given:
        def toDelete = dbfixture(getTransient("DEL-MNT"))

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: WRONG"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") ==
                ["Authorisation for [mntner] DEL-MNT failed using \"mnt-by:\" not authenticated by: DEL-MNT, DEL-MNT"]

        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

        def updto = notificationFor "updto_test@ripe.net"
        updto.authFailed("DELETE", "mntner", "DEL-MNT")

        noMoreMessages()
    }

    def "delete maintainer not identical"() {
      given:
        dbfixture(getTransient("DEL-MNT"))
        def toDelete = getTransient("DEL-DIFF-MNT")

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete + "delete: testing mntner delete\npassword: delete"
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 4, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") == ["Object [mntner] DEL-MNT doesn't match version in database"]
    }

    def "delete maintainer not case sensitive"() {
      given:
        dbfixture(getTransient("DEL-MNT"))
        def toDelete = getTransient("DEL-MNT")

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete.replace("MNTNER for test", "MNTNER FOR TEST") +
                        "delete: testing mntner delete\npassword: delete"
        )

      then:
        def ack = ackFor message

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[mntner] DEL-MNT" }

        queryNothing("-r -T mntner DEL-MNT")

        def notif = notificationFor "notif_test@ripe.net"
        notif.deleted.any { it.type == "mntner" && it.key == "DEL-MNT" }

        noMoreMessages()
    }

    def "delete maintainer referenced in mnt-by"() {
      expect:
        queryObject("-B -r -T mntner OWNER-MNT", "mntner", "OWNER-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner@ripe.net
                mnt-nfy:     mntnfy_owner@ripe.net
                notify:      notify_owner@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                source:      TEST
                delete: testing

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] OWNER-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] OWNER-MNT") ==
                ["Object [mntner] OWNER-MNT is referenced from other objects"]
    }

    def "delete maintainer referenced in mnt-lower"() {
      given:
        syncUpdate(getTransient("DEL-MNT") + "password: delete")
        syncUpdate(getTransient("LOWER") + "password: owner\npassword: owner2")

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")
        queryObject("-rx -T inetnum 10.0.0.0 - 10.0.0.255", "inetnum", "10.0.0.0 - 10.0.0.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-nfy:     mntnfy_test@ripe.net
                notify:      notif_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                source:      TEST
                delete: testing

                password: delete
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") ==
                ["Object [mntner] DEL-MNT is referenced from other objects"]
    }

    def "delete mntner referenced in mnt-routes"() {
      given:
        syncUpdate(getTransient("DEL-MNT") + "password: delete")
        syncUpdate(getTransient("ROUTE") + "password: owner\npassword: owner2")

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")
        queryObject("-rx -T inetnum 10.0.0.0 - 10.0.0.255", "inetnum", "10.0.0.0 - 10.0.0.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-nfy:     mntnfy_test@ripe.net
                notify:      notif_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                source:      TEST
                delete: testing

                password: delete
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") ==
                ["Object [mntner] DEL-MNT is referenced from other objects"]
    }

    def "delete mntner referenced in mnt-domain"() {
      given:
        syncUpdate(getTransient("DEL-MNT") + "password: delete")
        syncUpdate(getTransient("DOM") + "password: owner\npassword: owner2")

      expect:
        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")
        queryObject("-rx -T inetnum 10.0.0.0 - 10.0.0.255", "inetnum", "10.0.0.0 - 10.0.0.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-nfy:     mntnfy_test@ripe.net
                notify:      notif_test@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                source:      TEST
                delete: testing

                password: delete
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] DEL-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] DEL-MNT") ==
                ["Object [mntner] DEL-MNT is referenced from other objects"]

        queryObject("-r -T mntner DEL-MNT", "mntner", "DEL-MNT")
    }

}
