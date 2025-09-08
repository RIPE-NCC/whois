package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class NotificationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "MOD-MNT": """\
                mntner:      MOD-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$Bsso7xK2\$u1I7XvRIJyMQlF2rYWbYx.  #modify
                mnt-by:      MOD-MNT
                source:      TEST
                """,
            "MOD2-MNT": """\
                mntner:      MOD-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                notify:      notify_test@ripe.net
                auth:        MD5-PW \$1\$Bsso7xK2\$u1I7XvRIJyMQlF2rYWbYx.  #modify
                mnt-by:      MOD-MNT
                source:      TEST
                """,
            "MOD3-MNT": """\
                mntner:      MOD-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                notify:      new_notify_test@ripe.net
                auth:        MD5-PW \$1\$Bsso7xK2\$u1I7XvRIJyMQlF2rYWbYx.  #modify
                mnt-by:      MOD-MNT
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
            "TST2-MNT": """\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER2-MNT
                source:      TEST
                """,
            "TST3-MNT": """\
                mntner:      TST-MNT3
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$p4syt8vq\$AOwjgBvR4MA3o4ccMSMvh0  #test3
                mnt-by:      OWNER2-MNT
                source:      TEST
                """,
            "TST5-MNT": """\
                mntner:      TST-MNT5
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst5@ripe.net
                notify:      notify_tst5@ripe.net
                notify:      notify2_tst5@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "TST6-MNT": """\
                mntner:      TST-MNT6
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst6@ripe.net
                notify:      notify_tst6@ripe.net
                notify:      notify2_tst6@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER3-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "CREATE-MNT": """\
                mntner:      CREATE-MNT
                descr:       to be created
                admin-c:     TP1-TEST
                upd-to:      updto_create@ripe.net
                mnt-nfy:     mntnfy_create@ripe.net
                notify:      notify_create@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST
                """,
            "PN-ORG": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-OTO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST
                """,
            "INETNUM": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       TST-MNT3
                mnt-irt:      irt-test-notify
                source:       TEST
            """,
            "IRT_TEST_NOTIFY": """\
                irt:          irt-test-notify
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest-irt@ripe.net
                notify:       dbtest-notify@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       TST-MNT3
                source:       TEST
                """,
            "IRT_TEST_NOTIFY2": """\
                irt:          irt-test-notify2
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest-irt2@ripe.net
                notify:       dbtest-notify@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       TST-MNT3
                source:       TEST
                """,
            "ASSPI": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
        ]
    }

    def "add notify to object"() {
      given:
        dbfixture(getTransient("MOD-MNT"))
        def mnt = object(getTransient("MOD2-MNT"))

      expect:
        queryObject("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT")
        query_object_not_matches("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")

      when:
        def message = send new Message(
                subject: "add notify",
                body: mnt + "password: modify"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] MOD-MNT"}
        ack.countErrorWarnInfo(0, 4, 0)

        noMoreMessages()

        query_object_matches("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")
    }

    def "change notify in object"() {
      given:
        dbfixture(getTransient("MOD2-MNT"))
        def mnt = object(getTransient("MOD3-MNT"))

      expect:
        query_object_matches("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")

      when:
        def message = send new Message(
                subject: "add notify",
                body: mnt + "password: modify"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] MOD-MNT"}
        ack.countErrorWarnInfo(0, 4, 0)

        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.changed("mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net", "notify:\\s*new_notify_test@ripe.net")

        noMoreMessages()

        query_object_matches("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*new_notify_test@ripe.net")
        query_object_not_matches("-r -BG -T mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")
    }

    def "create MNTNER with notify"() {
      given:
        def mnt = object(getTransient("MOD2-MNT"))

      expect:
        queryNothing("-rBGT mntner MOD-MNT")

      when:
        def message = send new Message(
                subject: "create with notify",
                body: mnt + "password: modify"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[mntner] MOD-MNT" }
        query_object_matches("-rGBT mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")

        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "MOD-MNT" }

        noMoreMessages()
    }

    def "delete MNTNER with notify"() {
      given:
        dbfixture(getTransient("MOD2-MNT"))
        def mnt = object(getTransient("MOD2-MNT"))

      expect:
        query_object_matches("-rBGT mntner MOD-MNT", "mntner", "MOD-MNT", "notify:\\s*notify_test@ripe.net")

      when:
        def message = send new Message(
                subject: "delete with notify",
                body: mnt + "delete: del reason\npassword: modify"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)

        ack.successes.any {it.operation == "Delete" && it.key == "[mntner] MOD-MNT"}
        queryNothing("-rGBT mntner MOD-MNT")

        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.deleted.any { it.type == "mntner" && it.key == "MOD-MNT" }
        notif.contents =~ "\\*\\*\\*Info:\\s*del reason"

        noMoreMessages()
    }

    def "change mnt-by of object"() {
      given:
        def mnt = object(getTransient("TST2-MNT"))

      expect:
        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "mnt-by:\\s*OWNER-MNT")

      when:
        def message = send new Message(
                subject: "change mnt-by",
                body: mnt + "password: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] TST-MNT"}
        ack.countErrorWarnInfo(0, 4, 0)

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.changed("mntner", "TST-MNT", "mnt-by:\\s*OWNER-MNT", "mnt-by:\\s*OWNER2-MNT")

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "mnt-by:\\s*OWNER2-MNT")
    }

    def "change mnt-by of object no mnt-nfy"() {
      given:
        def mnt = object(getTransient("TST3-MNT"))

      expect:
        query_object_matches("-rBGT mntner TST-MNT3", "mntner", "TST-MNT3", "mnt-by:\\s*OWNER3-MNT")

      when:
        def message = send new Message(
                subject: "change mnt-by",
                body: mnt + "password: owner3"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] TST-MNT3"}
        ack.countErrorWarnInfo(0, 4, 0)

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT3", "mntner", "TST-MNT3", "mnt-by:\\s*OWNER2-MNT")
    }

    def "auth fail modify object"() {
      given:
        def mnt = object(getTransient("TST2-MNT"))

      expect:
        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "mnt-by:\\s*OWNER-MNT")

      when:
        def message = send new Message(
                subject: "change mnt-by",
                body: mnt + "password: null"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] TST-MNT"}
        ack.countErrorWarnInfo(1, 3, 0)
        ack.errorMessagesFor("Modify", "[mntner] TST-MNT") ==
                ["Authorisation for [mntner] TST-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.authFailed("MODIFY", "mntner", "TST-MNT")

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "mnt-by:\\s*OWNER-MNT")
    }

    def "create object mnt-by has mnt-nfy"() {
      given:
        def mnt = object(getTransient("CREATE-MNT"))

      expect:
        queryNothing("-rBGT mntner CREATE-MNT")

      when:
        def message = send new Message(
                subject: "create mntner",
                body: mnt + "password: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Create" && it.key == "[mntner] CREATE-MNT"}
        ack.countErrorWarnInfo(0, 4, 0)

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "CREATE-MNT" }

        def notif2 = notificationFor "notify_create@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.created.any { it.type == "mntner" && it.key == "CREATE-MNT" }

        noMoreMessages()

        query_object_matches("-rBGT mntner CREATE-MNT", "mntner", "CREATE-MNT", "mnt-by:\\s*OWNER-MNT")
    }

    def "create object fail auth error"() {
      given:
        def mnt = object(getTransient("CREATE-MNT"))

      expect:
        queryNothing("-rBGT mntner CREATE-MNT")

      when:
        def message = send new Message(
                subject: "create mntner",
                body: mnt + "password: null"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errors.any {it.operation == "Create" && it.key == "[mntner] CREATE-MNT"}
        ack.countErrorWarnInfo(1, 3, 0)
        ack.errorMessagesFor("Create", "[mntner] CREATE-MNT") ==
                ["Authorisation for [mntner] CREATE-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.authFailed("CREATE", "mntner", "CREATE-MNT")

        noMoreMessages()

        queryNothing("-rBGT mntner CREATE-MNT")
    }

    def "delete object mnt-by has mnt-nfy updto obj has notify"() {
      given:
        def mnt = oneBasicFixture("TST-MNT4")

      expect:
        query_object_matches("-rBGT mntner TST-MNT4", "mntner", "TST-MNT4", "mnt-by:\\s*OWNER-MNT")

      when:
        def message = send new Message(
                subject: "delete mntner",
                body: mnt + "password: owner\ndelete: not required\n"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Delete" && it.key == "[mntner] TST-MNT4"}
        ack.countErrorWarnInfo(0, 4, 0)

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.deleted.any { it.type == "mntner" && it.key == "TST-MNT4" }
        notif.contents =~ "\\*\\*\\*Info:\\s*not required"

        def notif2 = notificationFor "notify_tst4@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.deleted.any { it.type == "mntner" && it.key == "TST-MNT4" }
        notif2.contents =~ "\\*\\*\\*Info:\\s*not required"

        noMoreMessages()

        queryNothing("-rBGT mntner TST-MNT4")
    }

    def "fail to delete object with auth error mnt-by has mnt-nfy updto obj has notify"() {
      given:
        def mnt = oneBasicFixture("TST-MNT4")

      expect:
        query_object_matches("-rBGT mntner TST-MNT4", "mntner", "TST-MNT4", "mnt-by:\\s*OWNER-MNT")

      when:
        def message = send new Message(
                subject: "delete mntner",
                body: mnt + "password: null\ndelete: not required\n"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] TST-MNT4"}
        ack.countErrorWarnInfo(1, 3, 0)
        ack.errorMessagesFor("Delete", "[mntner] TST-MNT4") ==
                ["Authorisation for [mntner] TST-MNT4 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.failedDeleted.any { it.type == "mntner" && it.key == "TST-MNT4" }

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT4", "mntner", "TST-MNT4", "mnt-by:\\s*OWNER-MNT")
    }

    def "modify object 2xmnt-by one has 2xmnt-nfy updto obj has 2xnotify"() {
      expect:
        query_object_matches("-rBGT mntner TST-MNT5", "mntner", "TST-MNT5", "notify:\\s*notify_tst5@ripe.net")
        query_object_matches("-rBGT mntner TST-MNT5", "mntner", "TST-MNT5", "notify:\\s*notify2_tst5@ripe.net")

      when:
        def message = send new Message(
                subject: "modify mntner",
                body: """\
                mntner:      TST-MNT5
                descr:       MNTNER for test
                admin-c:     TP2-TEST # was TP1-TEST
                upd-to:      updto_tst5@ripe.net
                notify:      notify_tst5@ripe.net
                notify:      notify2_tst5@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST

                password:     owner4
            """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] TST-MNT5"}
        ack.countErrorWarnInfo(0, 4, 0)

        def notif = notificationFor "mntnfy_owner4@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "mntner" && it.key == "TST-MNT5" }
        notif.changed("mntner", "TST-MNT5", "admin-c:\\s+TP1-TEST", "admin-c:\\s+TP2-TEST")

        def notif2 = notificationFor "mntnfy2_owner4@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.modified.any { it.type == "mntner" && it.key == "TST-MNT5" }
        notif2.changed("mntner", "TST-MNT5", "admin-c:\\s+TP1-TEST", "admin-c:\\s+TP2-TEST")

        def notif3 = notificationFor "notify_tst5@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"
        notif3.modified.any { it.type == "mntner" && it.key == "TST-MNT5" }
        notif3.changed("mntner", "TST-MNT5", "admin-c:\\s+TP1-TEST", "admin-c:\\s+TP2-TEST")

        def notif4 = notificationFor "notify2_tst5@ripe.net"
        notif4.subject =~ "Notification of RIPE Database changes"
        notif4.modified.any { it.type == "mntner" && it.key == "TST-MNT5" }
        notif4.changed("mntner", "TST-MNT5", "admin-c:\\s+TP1-TEST", "admin-c:\\s+TP2-TEST")

        def notif5 = notificationFor "mntnfy_owner@ripe.net"
        notif5.subject =~ "Notification of RIPE Database changes"
        notif5.modified.any { it.type == "mntner" && it.key == "TST-MNT5" }
        notif5.changed("mntner", "TST-MNT5", "admin-c:\\s+TP1-TEST", "admin-c:\\s+TP2-TEST")
        notif5.contents =~ "The old object can be seen in the history using the query options --list-versions and --show-version 2 TST-MNT5"

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT5", "mntner", "TST-MNT5", "notify:\\s*notify_tst5@ripe.net")
        query_object_matches("-rBGT mntner TST-MNT5", "mntner", "TST-MNT5", "notify:\\s*notify2_tst5@ripe.net")
    }

    def "fail modify object 2xmnt-by has mnt-nfy 2xupdto obj has 2xnotify"() {
      expect:
        query_object_matches("-rBGT mntner TST-MNT6", "mntner", "TST-MNT6", "mnt-by:\\s*owner3")
        query_object_matches("-rBGT mntner TST-MNT6", "mntner", "TST-MNT6", "mnt-by:\\s*owner4")

      when:
        def message = send new Message(
                subject: "modify mntner",
                body: """\
                mntner:      TST-MNT6
                descr:       MNTNER for test
                admin-c:     TP2-TEST  # was TP1-TEST
                upd-to:      updto_tst6@ripe.net
                notify:      notify_tst6@ripe.net
                notify:      notify2_tst6@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER3-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST

                password:     null
            """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] TST-MNT6"}
        ack.countErrorWarnInfo(1, 3, 0)
        ack.errorMessagesFor("Modify", "[mntner] TST-MNT6") ==
                ["Authorisation for [mntner] TST-MNT6 failed using \"mnt-by:\" not authenticated by: OWNER3-MNT, OWNER4-MNT"]

        def notif = notificationFor "updto_owner3@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.failedModified.any { it.type == "mntner" && it.key == "TST-MNT6" }
        !notif.added("mntner", "TST-MNT6", "admin-c:\\s+TP1-TEST")

        def notif2 = notificationFor "updto2_owner3@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth error notification"
        notif2.failedModified.any { it.type == "mntner" && it.key == "TST-MNT6" }
        !notif2.added("mntner", "TST-MNT6", "admin-c:\\s+TP1-TEST")

        def notif3 = notificationFor "updto_owner4@ripe.net"
        notif3.subject =~ "RIPE Database updates, auth error notification"
        notif3.failedModified.any { it.type == "mntner" && it.key == "TST-MNT6" }
        !notif3.added("mntner", "TST-MNT6", "admin-c:\\s+TP1-TEST")

        def notif4 = notificationFor "updto2_owner4@ripe.net"
        notif4.subject =~ "RIPE Database updates, auth error notification"
        notif4.failedModified.any { it.type == "mntner" && it.key == "TST-MNT6" }
        !notif4.added("mntner", "TST-MNT6", "admin-c:\\s+TP1-TEST")

        noMoreMessages()

        query_object_matches("-rBGT mntner TST-MNT6", "mntner", "TST-MNT6", "mnt-by:\\s*owner3")
        query_object_matches("-rBGT mntner TST-MNT6", "mntner", "TST-MNT6", "mnt-by:\\s*owner4")
    }

    def "create person with reference to ORGANISATION with ref-nfy"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-OTO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}

        def notif = notificationFor "dbtest-org@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "person" && it.key == "First Person" }

        def notif2 = notificationFor "mntnfy_owner@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.created.any { it.type == "person" && it.key == "First Person" }

        noMoreMessages()

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "modify person add reference to ORGANISATION with ref-nfy"() {
      given:
        syncUpdate(getTransient("PN") + "password: owner")

      expect:
        query_object_not_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-OTO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        def notif = notificationFor "dbtest-org@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "person" && it.key == "First Person" }

        def notif2 = notificationFor "mntnfy_owner@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.modified.any { it.type == "person" && it.key == "First Person" }

        noMoreMessages()

        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")
    }

    def "modify person remove reference to ORGANISATION with ref-nfy"() {
      given:
        dbfixture(getTransient("PN-ORG"))

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "person" && it.key == "First Person" }

        def notif2 = notificationFor "dbtest-org@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.modified.any { it.type == "person" && it.key == "First Person" }

        noMoreMessages()

        query_object_not_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")
    }

    def "modify person no change to reference to ORGANISATION with ref-nfy"() {
      given:
        dbfixture(getTransient("PN-ORG"))

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-OTO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                remarks: updated
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "person" && it.key == "First Person" }

        noMoreMessages()

        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")
    }

    def "delete person referencing an ORGANISATION with ref-nfy"() {
      given:
        dbfixture(getTransient("PN-ORG"))

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-OTO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-OTO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST
                delete: get rid

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person"}

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.deleted.any { it.type == "person" && it.key == "First Person" }

        def notif2 = notificationFor "dbtest-org@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.deleted.any { it.type == "person" && it.key == "First Person" }

        noMoreMessages()

        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete an inetnum referencing an IRT with irt-nfy"() {
        given:
            syncUpdate(getTransient("IRT_TEST_NOTIFY") + "password: test3")
            queryObject("-r -T irt irt-test-notify", "irt", "irt-test-notify")
            dbfixture(getTransient("INETNUM"))
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
            def message = send new Message(
                    subject: "",
                    body: """\
                    inetnum:      192.168.200.0 - 192.168.200.255
                    netname:      RIPE-NET
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ASSIGNED PA
                    mnt-by:       TST-MNT3
                    mnt-irt:      irt-test-notify
                    source:       TEST
                    delete: get rid

                    password: test3
                    """.stripIndent(true)
            )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}

        def notif = notificationFor "dbtest-irt@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.deleted.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }

        noMoreMessages()

        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify an inetnum referencing an IRT with irt-nfy, changing the mnt-irt"() {
        given:
            syncUpdate(getTransient("IRT_TEST_NOTIFY") + "password: test3\npassword: irt")
            queryObject("-r -T irt irt-test-notify", "irt", "irt-test-notify")

            dbfixture(getTransient("INETNUM"))
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

            syncUpdate(getTransient("IRT_TEST_NOTIFY2") + "password: test3")
            queryObject("-r -T irt irt-test-notify2", "irt", "irt-test-notify2")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                    inetnum:      192.168.200.0 - 192.168.200.255
                    netname:      RIPE-NET
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ASSIGNED PA
                    mnt-by:       TST-MNT3
                    mnt-irt:      irt-test-notify2
                    source:       TEST


                    password: test3
                    password: irt
                    """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        def notif2 = notificationFor "dbtest-irt2@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }

        def notif = notificationFor "dbtest-irt@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }

        noMoreMessages()

        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255","mnt-irt:\\s*irt-test-notify2")
    }

    def "modify an inetnum referencing an IRT with irt-nfy, removing the mnt-irt"() {
        given:
            syncUpdate(getTransient("IRT_TEST_NOTIFY") + "password: test3\npassword: irt")
            queryObject("-r -T irt irt-test-notify", "irt", "irt-test-notify")

            dbfixture(getTransient("INETNUM"))
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                    inetnum:      192.168.200.0 - 192.168.200.255
                    netname:      RIPE-NET
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ASSIGNED PA
                    mnt-by:       TST-MNT3
                    source:       TEST

                    password: test3
                    """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        def notif = notificationFor "dbtest-irt@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }

        noMoreMessages()

        query_object_not_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255","mnt-irt:        irt-test-notify")
    }

    def "modify an inetnum referencing an IRT with irt-nfy, modifying admin-c, should not notify"() {
        given:
            syncUpdate(getTransient("IRT_TEST_NOTIFY") + "password: test3\npassword: irt")
            queryObject("-r -T irt irt-test-notify", "irt", "irt-test-notify")

            dbfixture(getTransient("INETNUM"))
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                    inetnum:      192.168.200.0 - 192.168.200.255
                    netname:      RIPE-NET
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP2-TEST
                    tech-c:       TP2-TEST
                    status:       ASSIGNED PA
                    mnt-by:       TST-MNT3
                    mnt-irt:      irt-test-notify
                    source:       TEST

                    password: test3
                    """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        noMoreMessages()

        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255","mnt-irt:        irt-test-notify")
    }

    def "create an inetnum referencing an IRT with irt-nfy"() {
        given:
            dbfixture("""\
                    inetnum:      192.168.0.0 - 192.168.255.255
                    netname:      RIPE-NET
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ALLOCATED PA
                    mnt-by:       RIPE-NCC-HM-MNT
                    mnt-by:       TST-MNT3
                    source:       TEST
            """.stripIndent(true))
            syncUpdate(getTransient("IRT_TEST_NOTIFY") + "password: test3\npassword: irt")
            queryObject("-r -T irt irt-test-notify", "irt", "irt-test-notify")

            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
            def ack = syncUpdateWithResponseWithNotifications("""
                    inetnum:      192.168.201.0 - 192.168.201.255
                    netname:      RIPE-NET
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ASSIGNED PA
                    mnt-by:       TST-MNT3
                    mnt-irt:      irt-test-notify
                    source:       TEST

                    password: test3
                    password: irt
                    password: hm
                    """.stripIndent(true)
        )

        then:
            ack.success
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}

            def notif = notificationFor "dbtest-irt@ripe.net"
            notif.subject =~ "Notification of RIPE Database changes"
            notif.created.any { it.type == "inetnum" && it.key == "192.168.201.0 - 192.168.201.255" }

            noMoreMessages()

            query_object_matches("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255","mnt-irt:        irt-test-notify")
    }

    def "modify inetnum, add remarks:"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        def notif = notificationFor "dbtest@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
        notif.contents =~ /(?ms)OBJECT BELOW MODIFIED:\n\n@@.+@@.*?-mnt-by:\s*LIR-MNT\n\+mnt-by:\s*lir-MNT.+?THIS IS THE NEW VERSION OF THE OBJECT:\n*inetnum:\s*192.168.200.0 - 192.168.200.255.+?The old object can be seen in the history using the query options/

        def notif2 = notificationFor "mntnfy_lir@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
        notif.contents =~ /(?ms)OBJECT BELOW MODIFIED:\n\n@@.+@@.*?-mnt-by:\s*LIR-MNT\n\+mnt-by:\s*lir-MNT.+?THIS IS THE NEW VERSION OF THE OBJECT:\n*inetnum:\s*192.168.200.0 - 192.168.200.255.+?The old object can be seen in the history using the query options/

        def notif3 = notificationFor "mntnfy_hm@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"
        notif3.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
        notif.contents =~ /(?ms)OBJECT BELOW MODIFIED:\n\n@@.+@@.*?-mnt-by:\s*LIR-MNT\n\+mnt-by:\s*lir-MNT.+?THIS IS THE NEW VERSION OF THE OBJECT:\n*inetnum:\s*192.168.200.0 - 192.168.200.255.+?The old object can be seen in the history using the query options/

        noMoreMessages()

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "modify inetnum, add remarks: with syntax error, no notifs sent"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.garbageContains("inetnum:      192.168.200.0 - 192.168.200.255");
        ack.garbageContains("remarks      just added")

        noMoreMessages()

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "create inetnum"() {
        given:

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
            def ack = syncUpdateWithResponseWithNotifications("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                source:       TEST
                password: hm

                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

            def notif = notificationFor "dbtest@ripe.net"
            notif.subject =~ "Notification of RIPE Database changes"
            notif.created.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW CREATED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            def notif2 = notificationFor "mntnfy_lir@ripe.net"
            notif2.subject =~ "Notification of RIPE Database changes"
            notif2.created.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW CREATED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            def notif3 = notificationFor "mntnfy_hm@ripe.net"
            notif3.subject =~ "Notification of RIPE Database changes"
            notif3.created.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW CREATED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            noMoreMessages()

            queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete inetnum"() {
        given:
            syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
            def ack = syncUpdateWithResponseWithNotifications("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                delete:   testing notifications
                password: hm

                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 0, 1, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

            def notif = notificationFor "dbtest@ripe.net"
            notif.subject =~ "Notification of RIPE Database changes"
            notif.deleted.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW DELETED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            def notif2 = notificationFor "mntnfy_lir@ripe.net"
            notif2.subject =~ "Notification of RIPE Database changes"
            notif2.deleted.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW DELETED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            def notif3 = notificationFor "mntnfy_hm@ripe.net"
            notif3.subject =~ "Notification of RIPE Database changes"
            notif3.deleted.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW DELETED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/

            noMoreMessages()

            queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum notif message structure"() {
        given:

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
            def ack = syncUpdateWithResponseWithNotifications("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                source:       TEST
                password: hm

                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

            def notif = notificationFor "dbtest@ripe.net"
            notif.subject =~ "Notification of RIPE Database changes"
            notif.created.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            def regExp = "(?ms)This is to notify you of changes in RIPE Database or" +
                         ".+?object authorisation failures" +
                         ".+?Change requested from:" +
                         ".+?~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                         "Some object\\(s\\)" +
                         ".+?---\n" +
                         "OBJECT BELOW CREATED:\n\n" +
                         "inetnum:\\s*192.168.200.0 - 192.168.200.255" +
                         ".+?The RIPE Database is subject to Terms and Conditions:" +
                         ".+?For assistance or clarification please visit https://www.ripe.net/s/notify"
            notif.contents =~ regExp

            queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify inetnum notif message structure"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        def notif = notificationFor "dbtest@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
        def regExp = "(?ms)This is to notify you of changes in RIPE Database or" +
                ".+?object authorisation failures" +
                ".+?Change requested from:" +
                ".+?~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "Some object\\(s\\)" +
                ".+?---\n" +
                "OBJECT BELOW MODIFIED:\n\n" +
                "@@.+@@.*?-mnt-by:\\s*LIR-MNT\n\\+mnt-by:\\s*lir-MNT" +
                ".+?THIS IS THE NEW VERSION OF THE OBJECT:\n*" +
                "inetnum:\\s*192.168.200.0 - 192.168.200.255" +
                ".+?The old object can be seen in the history using the query options " +
                "--list-versions and --show-version 1 192.168.200.0 - 192.168.200.255" +
                ".+?The RIPE Database is subject to Terms and Conditions:" +
                ".+?For assistance or clarification please visit https://www.ripe.net/s/notify"
        notif.contents =~ regExp

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "delete inetnum notif message structure"() {
        given:
            syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
            def ack = syncUpdateWithResponseWithNotifications("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                delete:   testing notifications
                password: hm

                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 0, 1, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

            def notif = notificationFor "dbtest@ripe.net"
            notif.subject =~ "Notification of RIPE Database changes"
            notif.deleted.any { it.type == "inetnum" && it.key == "192.168.200.0 - 192.168.200.255" }
            !(notif.contents =~ /(?ms)@@.+@@/)
            notif.contents =~ /(?ms)OBJECT BELOW DELETED:\n\ninetnum:\s*192.168.200.0 - 192.168.200.255/
            def regExp = "(?ms)This is to notify you of changes in RIPE Database or" +
                         ".+?object authorisation failures" +
                         ".+?Change requested from:" +
                         ".+?~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                         "Some object\\(s\\)" +
                         ".+?---\n" +
                         "OBJECT BELOW DELETED:\n\n" +
                         "inetnum:\\s*192.168.200.0 - 192.168.200.255" +
                         ".+?The RIPE Database is subject to Terms and Conditions:" +
                         ".+?For assistance or clarification please visit https://www.ripe.net/s/notify"
            notif.contents =~ regExp

            queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify inetnum with auth error notif message structure"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       dbtest@ripe.net
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                source:       TEST

                password: false
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT, LIR-MNT"]

        def notif = notificationFor "updto_lir@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.authFailed("MODIFY", "inetnum", "192.168.200.0 - 192.168.200.255")
        def regExp = "(?ms)This is to notify you of changes in RIPE Database or" +
                ".+?object authorisation failures" +
                ".+?Change requested from:" +
                ".+?~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "Some objects" +
                ".+?but \\*failed\\*\n" +
                "the proper authorisation for any of the referenced\n" +
                "maintainers" +
                ".+?---\n" +
                "MODIFY REQUESTED FOR:\n\n" +
                "inetnum:\\s*192.168.200.0 - 192.168.200.255" +
                ".+?The RIPE Database is subject to Terms and Conditions:" +
                ".+?For assistance or clarification please visit https://www.ripe.net/s/notify"
        notif.contents =~ regExp
        !(notif.contents =~ /(?ms)OBJECT BELOW MODIFIED:/)
        (notif.contents =~ /(?ms)@@.+@@/)
        !(notif.contents =~ /(?ms)The old object can be seen in the history using the query options --list-versions and --show-version/)

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "create object with syncupdate, fail auth error"() {
        given:
        def mnt = object(getTransient("CREATE-MNT"))

        expect:
        queryNothing("-rBGT mntner CREATE-MNT")

        when:
        def message = syncUpdate(new SyncUpdate(data: """
                mntner:      CREATE-MNT
                descr:       to be created
                admin-c:     TP1-TEST
                upd-to:      updto_create@ripe.net
                mnt-nfy:     mntnfy_create@ripe.net
                notify:      notify_create@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                source:      TEST
                password: null

                """.stripIndent(true), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errors.any {it.operation == "Create" && it.key == "[mntner] CREATE-MNT"}
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[mntner] CREATE-MNT") ==
                ["Authorisation for [mntner] CREATE-MNT failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.authFailed("CREATE", "mntner", "CREATE-MNT")

        noMoreMessages()

        queryNothing("-rBGT mntner CREATE-MNT")
    }

}
