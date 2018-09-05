package net.ripe.db.whois.spec.update
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.scheduler.task.update.PendingUpdatesCleanup
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import net.ripe.db.whois.update.dao.PendingUpdateDao

@org.junit.experimental.categories.Category(IntegrationTest.class)
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
                source:         TEST
                """,
                "ROUTE": """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
        ]
    }

    def "create route, mnt-by, parent inet and ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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

        def asnotify = notificationFor "notify_as100@ripe.net"
        asnotify.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "Mails are sent when confirmation by other mntner times out"() {

        given:
        setTime(getTime().minusWeeks(2))
        syncUpdate(new SyncUpdate(data: """
                inetnum:        192.168.0.0 - 192.169.255.255
                netname:        EXACT-INETNUM
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ALLOCATED PA
                mnt-by:         OWNER-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                aut-num:        AS100
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         AS-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        expect:
        noPendingUpdates()

        when:
        // route create attempt by AS holder (pending)
        syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route AS-MNT
                origin:         AS100
                mnt-by:         AS-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
                password:   as
                """.stripIndent(), redirect: false))
        then:
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
        countPendingUpdates() == 0

        when:
        clearAllMails()
        resetTime()
        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()

        then:
        noMoreMessages()
    }

    def "Create route, pending request is removed on creation: both parties required"() {
        given:
        setTime(getTime().minusWeeks(2))
        syncUpdate(new SyncUpdate(data: """
                inetnum:        192.168.0.0 - 192.169.255.255
                netname:        EXACT-INETNUM
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ALLOCATED PA
                mnt-by:         OWNER-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                aut-num:        AS100
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         AS-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        expect:
        noPendingUpdates()

        when:
        // route create attempt by AS holder (pending)
          syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route AS-MNT
                origin:         AS100
                mnt-by:         AS2-MNT
                source:         TEST
                password:   as
                password:   as2
                """.stripIndent(), redirect: false))
        then:
          queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
          // route create attempt by owner (both AS and IP) (success)
          syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route AS-MNT
                origin:         AS100
                mnt-by:         AS2-MNT
                source:         TEST
                password:   owner
                password:   as2
                """.stripIndent(), redirect: false))
        then:
        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        clearAllMails()
        resetTime()
        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()

        then:
        noMoreMessages()
        noPendingUpdates()
    }

    def "Create route, with 3 involved maintainers"() {
        given:
        setTime(getTime().minusWeeks(2))
        syncUpdate(new SyncUpdate(data: """
                mntner:      AS200-MNT
                descr:       used for aut-num 200
                admin-c:     TP1-TEST
                upd-to:      updto_as@ripe.net
                mnt-nfy:     mntnfy_as@ripe.net
                notify:      notify_as@ripe.net
                auth:        MD5-PW \$1\$oHHeFFDr\$wUBxFsxTb6GQykxSlZN4S.  #pinet
                mnt-by:      AS200-MNT
                source:      TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                inetnum:        37.221.216.0 - 37.221.223.255
                netname:        EXACT-INETNUM-1
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ALLOCATED PA
                mnt-by:         AS-MNT
                mnt-routes:     AS200-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                aut-num:        AS200
                as-name:        ASTEST200
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         AS200-MNT
                mnt-routes:     AS200-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                inetnum:        37.221.220.0 - 37.221.221.255
                netname:        EXACT-INETNUM-0
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED PA
                mnt-by:         AS-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                mntner:      AS100-MNT
                descr:       used for aut-num 100
                admin-c:     TP1-TEST
                upd-to:      updto_as@ripe.net
                mnt-nfy:     mntnfy_as@ripe.net
                notify:      notify_as@ripe.net
                auth:        MD5-PW \$1\$oHHeFFDr\$wUBxFsxTb6GQykxSlZN4S.  #pinet
                mnt-by:      AS100-MNT
                source:      TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                aut-num:        AS100
                as-name:        ASTEST100
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         AS100-MNT
                mnt-routes:     AS100-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                route:          37.221.216.0/21
                descr:          Route AS200-MNT
                origin:         AS200
                mnt-by:         AS200-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        expect:
        queryObject("-rGBT inetnum 37.221.216.0 - 37.221.223.255", "inetnum", "37.221.216.0 - 37.221.223.255")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")
        queryObject("-rGBT route 37.221.216.0/21", "route", "37.221.216.0/21")
        queryObject("-rGBT inetnum 37.221.220.0 - 37.221.221.255", "inetnum", "37.221.220.0 - 37.221.221.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        noPendingUpdates()

        when:
        def message = send new Message(
                    subject: "",
                    body: """\
                    route:          37.221.220.0/24
                    descr:          Route AS-MNT
                    origin:         AS100
                    mnt-by:         AS-MNT
                    source:         TEST

                    password: as
                    """.stripIndent()
        )
        then:
        queryObjectNotFound("-rGBT route 37.221.220.0/24", "route", "37.221.220.0/24")
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 37.221.220.0/24AS100" }
        ack.errorMessagesFor("Create", "[route] 37.221.220.0/24AS100") == [
                "Authorisation for [route] 37.221.216.0/21AS200 failed using \"mnt-by:\" not authenticated by: AS200-MNT"
        ]

        when:
        clearAllMails()
        resetTime()
        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()

        then:
        noMoreMessages()
        noPendingUpdates()
    }

    def "create route, pending request is removed on creation, second party could do it all"() {
        given:
        setTime(getTime().minusWeeks(2))
        syncUpdate(new SyncUpdate(data: """
                inetnum:        192.168.0.0 - 192.169.255.255
                netname:        EXACT-INETNUM
                descr:          Exact match inetnum object
                country:        EU
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ALLOCATED PA
                mnt-by:         OWNER-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))

        syncUpdate(new SyncUpdate(data: """
                aut-num:        AS100
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify_as100@ripe.net
                mnt-by:         OWNER-MNT
                mnt-by:         AS-MNT
                source:         TEST
                override: denis,override1
                """.stripIndent(), redirect: false))
        expect:
        noPendingUpdates()

        when:
        // route create attempt by AS holder (pending)
        syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route AS-MNT
                origin:         AS100
                mnt-by:         AS-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
                password:   as
                """.stripIndent(), redirect: false))
        then:
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        // route create attempt by owner (both AS and IP) (success)
        syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route AS-MNT
                origin:         AS100
                mnt-by:         AS-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
                password:   owner
                """.stripIndent(), redirect: false))
        then:
        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        clearAllMails()
        resetTime()
        ((PendingUpdatesCleanup)whoisFixture.getApplicationContext().getBean("pendingUpdatesCleanup")).run()

        then:
        noMoreMessages()
        noPendingUpdates()
    }

    def "create route, no hierarchical pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
                source:         TEST

                password:   owner
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT",
        ]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, both hierarchical pw supplied, no mnt-by pw"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
                source:         TEST

                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") == [
                "Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"
        ]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & parent inet pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.pendingUpdates.any { it.operation == "Create SUCC" && it.key == "[route] 192.168.0.0/16AS100" }
        ack.warningPendingMessagesFor("Create SUCC", "[route] 192.168.0.0/16AS100") == []
        ack.infoPendingMessagesFor("Create SUCC", "[route] 192.168.0.0/16AS100") == []

        def notif2 = notificationFor "mntnfy_owner@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"

        def asnotify = notificationFor "notify_as100@ripe.net"
        asnotify.subject =~ "Notification of RIPE Database changes"

        noMoreMessages()

        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, mnt-by & ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

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
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.pendingUpdates == []
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT"]

        def notif = notificationFor "updto_owner@ripe.net"
        notif.subject =~ "RIPE Database updates, auth error notification"
        notif.authFailed("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "second noop pending update is deleted after route is successfully created"() {
      given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")

      expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

      when:
        syncUpdate(new SyncUpdate(data: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()))

      then:
        syncUpdate(new SyncUpdate(data: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                source:         TEST

                password:   owner
                password:   as
                """.stripIndent()))

      then:
        def response = syncUpdate(new SyncUpdate(data: """
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                source:         TEST

                password:   owner
                password:   hm
                password:   pinet
                """.stripIndent()))
      then:
        response =~ /SUCCESS/

      then:
        ((PendingUpdateDao)applicationContext.getBean("pendingUpdateDao")).findByTypeAndKey(ObjectType.ROUTE, "192.168.0.0/16AS100").isEmpty()
    }

    def "create route, mnt-by & parent inet pw supplied, route already exists"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: denis,override1")
        syncUpdate(getTransient("AS100") + "override: denis,override1")
        syncUpdate(getTransient("ROUTE") + "override: denis,override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObject("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                remarks:        just added
                source:         TEST

                password:   owner
                password:   pinet
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 192.168.0.0/16AS100" }

        query_object_matches("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16", "just added")
    }
}
