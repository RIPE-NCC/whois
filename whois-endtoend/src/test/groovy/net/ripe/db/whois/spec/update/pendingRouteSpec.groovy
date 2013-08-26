package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message
import spock.lang.Ignore

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 26/08/2013
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */
class pendingRouteSpec extends BaseSpec {

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

    def "create route, parent inet and ASN pw supplied"() {
        given:
        syncUpdate(getTransient("PARENT-INET") + "override: override1")
        syncUpdate(getTransient("AS100") + "override: override1")

        expect:
        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGBT aut-num AS100", "aut-num", "AS100")
        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")

        when:
        def message = syncUpdate("""\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS100
                mnt-by:         OWNER-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   owner
                password:   pinet
                password:   as
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 192.168.0.0/16AS100" }

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
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT",
                        "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT"]

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
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                        "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: P-INET-MNT"]

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
        ack.errorMessagesFor("Create", "[route] 192.168.0.0/16AS100") ==
                ["Authorisation for [route] 192.168.0.0/16AS100 failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                        "Authorisation for [aut-num] AS100 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT, AS-MNT"]

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, parent inet pw supplied"() {
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

//        def notif = notificationFor "updto_hm@ripe.net"
//        notif.subject =~ "RIPE Database updates, auth request notification"

        def notif2 = notificationFor "updto_as@ripe.net"
        notif2.subject =~ "RIPE Database updates, auth request notification"
        notif2.pendingAuth("CREATE", "route", "192.168.0.0/16")

        noMoreMessages()

        queryObjectNotFound("-rGBT route 192.168.0.0/16", "route", "192.168.0.0/16")
    }

    def "create route, ASN pw supplied"() {
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

}
