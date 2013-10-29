package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.TestDateTimeProvider
import net.ripe.db.whois.scheduler.task.update.PendingUpdatesCleanup
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.joda.time.LocalDateTime

class PendingRouteSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getFixtures() {
        [
                "AS-MNT": """\
                mntner:      AS-MNT
                descr:       used for aut-num
                admin-c:     TP1-TEST
                upd-to:      updto_as@ripe.net
                mnt-nfy:     mntnfy_as@ripe.net
                notify:      notify_as@ripe.net
                auth:        MD5-PW \$1\$eUJDS9FF\$M.Rnslf2/Joum8D1e8cLQ/  #as
                mnt-by:      AS-MNT
                referral-by: AS-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "AS2-MNT": """\
                mntner:      AS2-MNT
                descr:       used for aut-num
                admin-c:     TP1-TEST
                upd-to:      updto_as2@ripe.net
                mnt-nfy:     mntnfy_as2@ripe.net
                notify:      notify_as2@ripe.net
                auth:        MD5-PW \$1\$xrdaPju9\$pdea/wDdhZd4nGNaCH5xI1  #as2
                mnt-by:      AS2-MNT
                referral-by: AS2-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "P-INET-MNT": """\
                mntner:      P-INET-MNT
                descr:       used for aut-num
                admin-c:     TP1-TEST
                upd-to:      updto_pinet@ripe.net
                mnt-nfy:     mntnfy_pinet@ripe.net
                notify:      notify_pinet@ripe.net
                auth:        MD5-PW \$1\$oHHeFFDr\$wUBxFsxTb6GQykxSlZN4S.  #pinet
                mnt-by:      P-INET-MNT
                referral-by: P-INET-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
                "AS0 - AS4294967295": """\
                as-block:       AS0 - AS4294967295
                descr:          Full ASN range
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                changed:        dbtest@ripe.net
                source:         TEST
                """,
                "AS100": """\
                aut-num:        AS100
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         AS-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
                "AS200": """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as200@ripe.net
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         AS2-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
                "PARENT-INET": """\
                inetnum:        192.168.0.0 - 192.169.255.255
                netname:        EXACT-INETNUM
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ALLOCATED PA
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-lower:      P-INET-MNT
                changed:        dbtest@ripe.net
                source:         TEST
                """,
        ]
    }

    def "create route, mnt-by, parent inet and ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                password:   as
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, no hierarchical pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
        ]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, both hierarchical pw supplied, no mnt-by pw"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, ASN pw supplied, no mnt-by pw"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT"
        ]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, inet pw supplied, no mnt-by pw"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                "Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT"
        ]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & parent inet pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif2 = notificationFor "updto_as@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth request notification"
        notif2.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif = notificationFor "updto_pinet@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"
        notif.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied, then inet pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif = notificationFor "updto_pinet@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"
        notif.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")


        when:
        def message2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

        then:
        def ack2 = ackFor message2

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 1, 0, 0, 0)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 0, 1)
        ack2.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack2.infoSuccessMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "This update concludes a pending update on route 192.168.0.0/16AS100"]

        def notif2 = notificationFor "mntnfy_owner@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied, then same ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif = notificationFor "updto_pinet@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"
        notif.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")


        when:
        def message2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack2 = ackFor message2

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 0, 0, 0, 1)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 0, 1)

        def notif2 = notificationFor "updto_pinet@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth request notification"

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied, then same ASN pw supplied, then inet pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif = notificationFor "updto_pinet@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"
        notif.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")


        when:
        def message2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack2 = ackFor message2

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 0, 0, 0, 1)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 0, 1)
        ack2.infoSuccessMessagesFor("Noop PENDING", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT"
        ]

        notificationFor("updto_pinet@ripe.net").toString().contains(
                "Please submit the following objects *exactly as shown*\n" +
                        "and add your authorisation.\n" +
                        "---\n" +
                        "CREATE REQUESTED FOR:\n" +
                        "\n" +
                        "route:          192.168.0.0/16")
        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")


        when:
        def message3 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack3 = ackFor message3

        ack3.summary.nrFound == 1
        ack3.summary.assertSuccess(1, 1, 0, 0, 0)
        ack3.summary.assertErrors(0, 0, 0, 0)
        ack3.countErrorWarnInfo(0, 0, 1)
        ack3.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack3.infoSuccessMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "This update concludes a pending update on route 192.168.0.0/16AS100"]

        def notif3 = notificationFor "mntnfy_owner@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"
        notif3.contents =~ /(?ms)OBJECT BELOW CREATED:\n\nroute:\s*192.168.0.0\/16/
        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & parent inet pw supplied, then ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif2 = notificationFor "updto_as@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth request notification"
        notif2.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")


        when:
        def message2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   as
                """.stripIndent()
        )

        then:
        def ack2 = ackFor message2

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 1, 0, 0, 0)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 0, 1)
        ack2.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack2.infoSuccessMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "This update concludes a pending update on route 192.168.0.0/16AS100"]

        def notif3 = notificationFor "mntnfy_owner@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"
        notif3.contents =~ /(?ms)OBJECT BELOW CREATED:\n\nroute:\s*192.168.0.0\/16/
        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

    }

    def "create route, mnt-by & parent inet pw supplied, ASN pw supplied 2 weeks later"() {
      given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

      expect:
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT inetnum 192.168.0.0", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        ((TestDateTimeProvider) whoisFixture.getTestDateTimeProvider()).setTime(new LocalDateTime().minusWeeks(2))
        syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent())

        whoisFixture.getTestDateTimeProvider().reset()

        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()

        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

      then:
        def ack = ackFor pending

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.successes.any { it.operation == "Create PENDING" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def updtoPinet= notificationFor "updto_pinet@ripe.net"
        updtoPinet.subject =~ "RIPE Database updates, auth request notification"

        def updtoOwner = notificationFor "updto_owner@ripe.net"
        updtoOwner.subject =~ "Notification of RIPE Database pending update timeout on \\[route\\] 192.168.0.0/16AS100"

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

    }

    def "create route, mnt-by & ASN pw supplied, p inet pw supplied 2 weeks later"() {
      given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

      expect:
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT inetnum 192.168.0.0", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        ((TestDateTimeProvider) dateTimeProvider).setTime(new LocalDateTime().minusWeeks(2))
        syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent())

        whoisFixture.getTestDateTimeProvider().reset()

        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()


      then:
        def notif2 = notificationFor "updto_owner@ripe.net"
        notif2.subject.contains("Notification of RIPE Database pending update timeout on [route] 192.168.0.0/16AS100")
        notif2.contents.toString().contains("NO FINAL CREATE REQUESTED FOR:")

      when:
        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

      then:
        def ack = ackFor pending

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied, p inet pw supplied for same-object-with-extra-remark"() {
      given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

      expect:
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT inetnum 192.168.0.0", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent())

        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                remarks:         same same but different
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

      then:
        def ack = ackFor pending

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"

        noMoreMessages()
    }

    def "create route, mnt-by & p inet pw supplied, then ASN pw supplied for same-object-with-extra-endofline-comment"() {
      given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

      expect:
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT inetnum 192.168.0.0", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent())

        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT  #endoflinecomment
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   as
                """.stripIndent()
        )

      then:
        def ack = ackFor pending

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif = notificationFor "mntnfy_owner@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "mnt-by", "OWNER-MNT  #endoflinecomment")
    }

    def "create route, mnt-by & ASN pw supplied, then p inet pw supplied for same-but-different, then ASN pw supplied for same-but-different"() {
      given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

      expect:
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT inetnum 192.168.0.0", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent())

        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                remarks:         same same but different
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

      then:
        def ack = ackFor pending

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.successes.any { it.operation == "Create PENDING" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif = notificationFor "updto_as@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"

        noMoreMessages()

      when:
        def pending2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                remarks:         same same but different
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

      then:
        def ack2 = ackFor pending2

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 1, 0, 0, 0)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 0, 1)
        ack2.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

        def notif2 = notificationFor "mntnfy_owner@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()
    }

    def "create route, mnt-by & ASN pw supplied (1), then pinet pw supplied with different mb (2), then ASN pw supplied for (2), then pinet supplied for (1)"() {
        given:
        databaseHelper.addObject(getTransient("PARENT-INET"));
        databaseHelper.addObject(getTransient("AS100"));

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
//        ack.successes.any { it.operation == "Create PENDING" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif = notificationFor "updto_pinet@ripe.net"
        notif.subject =~ "RIPE Database updates, auth request notification"
        notif.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        when:
        def pending = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   lir
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack2 = ackFor pending

        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 0, 0, 0, 1)
        ack2.summary.assertErrors(0, 0, 0, 0)
        ack2.countErrorWarnInfo(0, 1, 2)
        ack2.successes.any { it.operation == "Create PENDING" && it.key == "[route] 192.168.0.0/16AS100" }
        ack2.warningPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack2.infoPendingMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT",
                        "The route object 192.168.0.0/16AS100 will be saved for one week pending the second authorisation"]

        def notif2 = notificationFor "updto_as@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth request notification"
        notif2.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        when:
        def pending2 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   as
                """.stripIndent()
        )

        then:
        def ack3 = ackFor pending2

        ack3.summary.nrFound == 1
        ack3.summary.assertSuccess(1, 1, 0, 0, 0)
        ack3.summary.assertErrors(0, 0, 0, 0)
        ack3.countErrorWarnInfo(0, 0, 1)
        ack3.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack3.infoSuccessMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "This update concludes a pending update on route 192.168.0.0/16AS100"]

        def notif3 = notificationFor "mntnfy_lir@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        when:
        def pending3 = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

        then:
        def ack4 = ackFor pending3

        ack4.summary.nrFound == 1
        ack4.summary.assertSuccess(0, 0, 0, 0, 0)
        ack4.summary.assertErrors(1, 0, 1, 0)
        ack4.countErrorWarnInfo(1, 0, 0)
        ack4.errors.any { it.operation == "Modify" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Modify", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: LIR-MNT"]

        def notif4 = notificationFor "updto_lir@ripe.net"
        notif4.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()
    }
}
