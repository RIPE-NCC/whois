package net.ripe.db.whois.spec.update


import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class MemberReclaimSpec extends BaseQueryUpdateSpec {
    @Override
    Map<String, String> getTransients() {
        [
            "PING-PN": """\
                person:  Ping Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: PP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   PARENT-MR-MNT
                mnt-domains:  LIR2-MNT
                source:       TEST
                """,
            "LIR-PART-PA": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
            "SUB-ALLOC": """\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       SUB-MNT
                mnt-lower:    SUB2-MNT
                source:       TEST
                """,
            "ASS-END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                """,
            "ASS-DOM": """\
                domain:         200.168.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                """,
            "ASS-ROUTE": """\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                """,
            "RIR-ALLOC-25-LOW-R-D": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
            "LIR-ALLOC-30": """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                """,
            "LIR-AGGR-32-48": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                """,
            "LIR-AGGR-48-64": """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                """,
            "ASS-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                """,
            "DOMAIN6": """\
                domain:         0.0.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                """,
            "ROUTE6-PARENT30": """\
                route6:         2001:600::/30
                descr:          Route
                origin:         AS10000
                mnt-by:         PARENT-MB-MNT
                source:         TEST
                """,
            "ROUTE6-CHILD32-1": """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                """,
            "ROUTE6-CHILD32-2": """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS20000
                ping-hdl:       PP1-TEST
                mnt-by:         ORIGIN-MB-MNT
                source:         TEST
                """,
            "AS10000": """\
                aut-num:     AS10000
                as-name:     TEST-AS
                status:      OTHER
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      PARENT-MB-MNT
                source:      TEST
                """,
            "AS20000": """\
                aut-num:     AS20000
                as-name:     TEST-AS
                status:      OTHER
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      PARENT-MB-MNT
                source:      TEST-NONAUTH
                """,
            "SUB2-MNT": """\
                mntner:      SUB2-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_domains@ripe.net
                mnt-nfy:     mntnfy_domains@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$PSgV42pA\$SQsl2cHDMeQx3IsMzdqNH/  #sub2
                mnt-by:      SUB2-MNT
                source:      TEST
                """,
    ]}

    def "delete ASSIGNED PA using mnt-lower of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-by of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-routes of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: mr-parent
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-domains of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-ref of ALLOCATED PA org"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify ASSIGNED PA using mnt-lower of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT"]

        query_object_not_matches("-GBrx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "modify ASSIGNED PA using mnt-by of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                remarks:      just added
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT"]

        query_object_not_matches("-GBrx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "delete ASSIGNED PA using mnt-lower of ALLOCATED PA with SUB-ALLOCATED PA in between"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-by of ALLOCATED PA with SUB-ALLOCATED PA in between"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete SUB-ALLOCATED PA using mnt-lower of ALLOCATED PA with more specific ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       SUB-MNT
                mnt-lower:    SUB2-MNT
                source:       TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete SUB-ALLOCATED PA using mnt-by of ALLOCATED PA with more specific ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       SUB-MNT
                mnt-lower:    SUB2-MNT
                source:       TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }

        queryObjectNotFound("-rx -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-lower of SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: sub2
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA using mnt-by of SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       end-user@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete: member reclaim

                password: sub
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-rx -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete ASSIGNED PA reverse delegation using mnt-lower of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        syncUpdate(getTransient("ASS-DOM") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // ASS-DOM
        queryObject("-GBr -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         200.168.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 200.168.192.in-addr.arpa" }

        queryObjectNotFound("-r -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")
    }

    def "delete ASSIGNED PA reverse delegation using mnt-by of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        syncUpdate(getTransient("ASS-DOM") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // ASS-DOM
        queryObject("-GBr -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         200.168.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 200.168.192.in-addr.arpa" }

        queryObjectNotFound("-r -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")
    }

    def "delete ROUTE for ASSIGNED PA using mnt-lower of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("AS10000") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        syncUpdate(getTransient("ASS-ROUTE") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // As10000
        queryObject("-GBr -T aut-num AS10000", "aut-num", "AS10000")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // ASS-ROUTE
        queryObject("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24")

      when:
        def message = syncUpdate("""\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS10000" }

        queryObjectNotFound("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
    }


    def "delete ROUTE for ASSIGNED PA using mnt-by of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("AS10000") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        syncUpdate(getTransient("ASS-ROUTE") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // As10000
        queryObject("-GBr -T aut-num AS10000", "aut-num", "AS10000")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // ASS-ROUTE
        queryObject("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24")

      when:
        def message = syncUpdate("""\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS10000" }

        queryObjectNotFound("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
    }

    def "delete ROUTE for ASSIGNED PA and reverse delegation using mnt-by of ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("AS10000") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("SUB-ALLOC") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        syncUpdate(getTransient("ASS-ROUTE") + "override: denis,override1")
        syncUpdate(getTransient("ASS-DOM") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // As10000
        queryObject("-GBr -T aut-num AS10000", "aut-num", "AS10000")
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // SUB-ALLOC
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // ASS-ROUTE
        queryObject("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // ASS-DOM
        queryObject("-GBr -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete: member reclaim

                domain:         200.168.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                delete: member reclaim

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 0, 0, 2)

        ack.countErrorWarnInfo(6, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS10000" }
        ack.errorMessagesFor("Delete", "[route] 192.168.200.0/24AS10000") ==
                ["Authorisation for [route] 192.168.200.0/24AS10000 failed using \"mnt-by:\" not authenticated by: CHILD-MB-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]
        ack.errors.any { it.operation == "Delete" && it.key == "[domain] 200.168.192.in-addr.arpa" }
        ack.errorMessagesFor("Delete", "[domain] 200.168.192.in-addr.arpa") ==
                ["Authorisation for [domain] 200.168.192.in-addr.arpa failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObject("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        queryObject("-r -T domain 200.168.192.in-addr.arpa", "domain", "200.168.192.in-addr.arpa")
    }

    def "delete ALLOCATED PA using mnt-lower of ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")

      expect:
        // ALLOC-PA
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // ASS-END
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   PARENT-MR-MNT
                mnt-domains:  LIR2-MNT
                source:       TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Deleting this object requires administrative authorisation"]
        ack.warningMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Status ALLOCATED UNSPECIFIED not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED PA"]

        queryObject("-rx -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete full hierarchy using mnt-lower of ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "override: denis,override1")
        syncUpdate(getTransient("LIR-ALLOC-30") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-48-64") + "override: denis,override1")
        syncUpdate(getTransient("ASS-64") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // RIR-ALLOC-25-LOW-R-D
        queryObject("-GBr -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        // LIR-ALLOC-30
        queryObject("-GBr -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        // LIR-AGGR-32-48
        queryObject("-GBr -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        // LIR-AGGR-48-64
        queryObject("-GBr -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        // ASS-64
        queryObject("-GBr -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(4, 0, 0, 4, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/30" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }
        ack.errors.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Delete", "[inet6num] 2001:600::/25") ==
                ["Deleting this object requires administrative authorisation"]

        queryObjectNotFound("-rx -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        queryObjectNotFound("-rx -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObjectNotFound("-rx -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObjectNotFound("-rx -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObject("-rx -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "delete full hierarchy using mnt-by of ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "override: denis,override1")
        syncUpdate(getTransient("LIR-ALLOC-30") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-48-64") + "override: denis,override1")
        syncUpdate(getTransient("ASS-64") + "override: denis,override1")

      expect:
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // RIR-ALLOC-25-LOW-R-D
        queryObject("-GBr -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        // LIR-ALLOC-30
        queryObject("-GBr -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        // LIR-AGGR-32-48
        queryObject("-GBr -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        // LIR-AGGR-48-64
        queryObject("-GBr -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        // ASS-64
        queryObject("-GBr -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                delete: member reclaim

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 0, 0, 5, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/30" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }

        queryObjectNotFound("-rx -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        queryObjectNotFound("-rx -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObjectNotFound("-rx -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObjectNotFound("-rx -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObjectNotFound("-rx -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "delete full network using mnt-lower of ALLOCATED-BY-RIR"() {
      given:

        syncUpdate(getTransient("PING-PN") + "override: denis,override1")
        syncUpdate(getTransient("SUB2-MNT") + "override: denis,override1")
        syncUpdate(getTransient("AS10000") + "override: denis,override1")
        syncUpdate(getTransient("AS20000") + "override: denis,override1")
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "override: denis,override1")
        syncUpdate(getTransient("LIR-ALLOC-30") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "override: denis,override1")
        syncUpdate(getTransient("LIR-AGGR-48-64") + "override: denis,override1")
        syncUpdate(getTransient("ASS-64") + "override: denis,override1")
        syncUpdate(getTransient("DOMAIN6") + "override: denis,override1")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "override: denis,override1")
        syncUpdate(getTransient("ROUTE6-CHILD32-1") + "override: denis,override1")
        syncUpdate(getTransient("ROUTE6-CHILD32-2") + "override: denis,override1")

        DatabaseHelper.dumpSchema(databaseHelper.whoisTemplate.dataSource)
      expect:
        // PING-PN
        queryObject("-GBr -T person PP1-TEST", "person", "Ping Person")
        // SUB2-MNT
        queryObject("-GBr -T mntner SUB2-MNT", "mntner", "SUB2-MNT")
        // AS10000
        queryObject("-GBr -T aut-num AS10000", "aut-num", "AS10000")
        // AS20000
        queryObject("-GBr -T aut-num AS20000", "aut-num", "AS20000")
        // RIR-ALLOC-25-LOW-R-D
        queryObject("-GBr -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        // LIR-ALLOC-30
        queryObject("-GBr -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        // LIR-AGGR-32-48
        queryObject("-GBr -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        // LIR-AGGR-48-64
        queryObject("-GBr -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        // ASS-64
        queryObject("-GBr -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        // DOMAIN6
        queryObject("-GBr -T domain 0.0.6.0.1.0.0.2.ip6.arpa", "domain", "0.0.6.0.1.0.0.2.ip6.arpa")
        // ROUTE6-PARENT30
        queryObject("-GBr -T route6 2001:600::/30", "route6", "2001:600::/30")
        // ROUTE6-CHILD32-1
        query_object_matches("-GBr -T route6 2001:600::/32", "route6", "2001:600::/32", "AS10000")
        // ROUTE6-CHILD32-2
        query_object_matches("-GBr -T route6 2001:600::/32", "route6", "2001:600::/32", "AS20000")

      when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                delete: member reclaim

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                delete: member reclaim

                domain:         0.0.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                delete: member reclaim

                route6:         2001:600::/30
                descr:          Route
                origin:         AS10000
                mnt-by:         PARENT-MB-MNT
                source:         TEST
                delete: member reclaim

                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete: member reclaim

                route6:         2001:600::/32
                descr:          Route
                origin:         AS20000
                ping-hdl:       PP1-TEST
                mnt-by:         ORIGIN-MB-MNT
                source:         TEST
                delete: member reclaim

                aut-num:     AS20000
                as-name:     TEST-AS
                status:      OTHER
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      PARENT-MB-MNT
                source:      TEST-NONAUTH
                delete: member reclaim

                person:  Ping Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: PP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                delete: member reclaim

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 11
        ack.summary.assertSuccess(8, 0, 0, 8, 0)
        ack.summary.assertErrors(3, 0, 0, 3)

        ack.countErrorWarnInfo(3, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/30" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }
        ack.errors.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Delete", "[inet6num] 2001:600::/25") ==
                ["Deleting this object requires administrative authorisation"]
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 0.0.6.0.1.0.0.2.ip6.arpa" }
        ack.successes.any { it.operation == "Delete" && it.key == "[route6] 2001:600::/30AS10000" }
        ack.successes.any { it.operation == "Delete" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.successes.any { it.operation == "Delete" && it.key == "[route6] 2001:600::/32AS20000" }
        ack.errors.any { it.operation == "Delete" && it.key == "[aut-num] AS20000" }
        ack.errorMessagesFor("Delete", "[aut-num] AS20000") ==
                ["Authorisation for [aut-num] AS20000 failed using \"mnt-by:\" not authenticated by: PARENT-MB-MNT"]
        ack.errors.any { it.operation == "Delete" && it.key == "[person] PP1-TEST   Ping Person" }
        ack.errorMessagesFor("Delete", "[person] PP1-TEST   Ping Person") ==
                ["Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rx -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        queryObjectNotFound("-rx -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObjectNotFound("-rx -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObjectNotFound("-rx -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObject("-rx -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        queryObjectNotFound("-GBr -T domain 0.0.6.0.1.0.0.2.ip6.arpa", "domain", "0.0.6.0.1.0.0.2.ip6.arpa")
        queryObjectNotFound("-GBr -T route6 2001:600::/30", "route6", "2001:600::/30")
        queryObjectNotFound("-GBr -T route6 2001:600::/32", "route6", "2001:600::/32")
        queryObject("-rx -T aut-num AS20000", "aut-num", "AS20000")
        queryObject("-rx -T person PP1-TEST", "person", "Ping Person")
    }

}
