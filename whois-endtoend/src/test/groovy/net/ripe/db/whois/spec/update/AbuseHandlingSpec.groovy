package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class AbuseHandlingSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
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
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
                "ABUSEC-PA": """\
                inetnum:      192.170.0.0 - 192.170.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH300-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
                "RS-ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                """,
                "PART-PA": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "SUB-ALLOC-PA": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    SUB-MNT
                source:       TEST
                """,
                "ASS-END-A": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                org:          ORG-OFA10-TEST
                """,
                "ASS-END-HM-A": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                org:          ORG-OFA10-TEST
                """,
                "ASSPI-A": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                org:          ORG-OFA10-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                """,
                "LEGACY": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                org:          ORG-OFA10-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
                "ASSANY-A": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                org:          ORG-OFA10-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "LIR-ASS-PI-A": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED PI
                source:       TEST
                """,
                "ABUSEC-INET6NUM": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH300-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED PI
                source:       TEST
                """,
                "LIR-AGGR-32-48-A": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                """,
                "ABUSEC-AUTNUM"    : """\
                aut-num:        AS101
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                import-via:     AS6777 from AS5580 accept AS-ATRATO
                export-via:     AS6777 to AS5580 announce AS2
                remarks:        remarkable
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                abuse-c:        AH300-TEST
                notify:         noreply@ripe.net
                mnt-lower:      lir-MNT
                mnt-by:         lir-MNT
                source:         TEST
                """,
                "ROLE-AM-NOREF": """\
                role:         Abuse Role
                remarks:      DO NOT REFERENCE this object
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ROLE-AM": """\
                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ROLE-NO-AM-NO-REF": """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      SR99-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ROLE-AH300": """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                abuse-mailbox:abuse@lir.net
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH300-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ORG-OTHER-A": """\
                organisation: auto-1
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
                "ORG-OTHER-NO-ABUSE-C": """\
                organisation: ORG-NA1-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
                "AS352-A": """\
                aut-num:     AS352
                as-name:     TEST-AS
                descr:       With abuse-c link
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                org:         ORG-OFA10-TEST
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                """,
                "AS1309-A": """\
                aut-num:     AS1309
                as-name:     TEST-AS
                descr:       With abuse-c link
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                org:         ORG-OFA10-TEST
                sponsoring-org: ORG-LIRA-TEST
                status:      ASSIGNED
                mnt-by:      RIPE-NCC-END-MNT
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                """,
        ]
    }

    def "create ROLE, no abuse-mailbox"() {
      given:

      expect:
        queryObjectNotFound("-r -T role SR2-TEST", "role", "Standard Role")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      SR2-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[role] SR2-TEST   Standard Role" }

        query_object_not_matches("-rGBT role SR2-TEST", "role", "Standard Role", "abuse-mailbox:")
    }

    def "create ROLE, with abuse-mailbox"() {
      given:

      expect:
        queryObjectNotFound("-r -T role AH2-TEST", "role", "Abuse Handler")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[role] AH2-TEST   Abuse Handler" }
        ack.warningSuccessMessagesFor("Create", "[role] AH2-TEST   Abuse Handler") == [
                "There are no limits on queries for ROLE objects containing \"abuse-mailbox:\""]

        query_object_matches("-rGBT role AH2-TEST", "role", "Abuse Handler", "abuse-mailbox:")
    }

    def "create ROLE, with 2 abuse-mailbox"() {
      given:

      expect:
        queryObjectNotFound("-r -T role AH2-TEST", "role", "Abuse Handler")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                abuse-mailbox:abuse2@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[role] AH2-TEST   Abuse Handler" }
        ack.errorMessagesFor("Create", "[role] AH2-TEST   Abuse Handler") == [
                "Attribute \"abuse-mailbox\" appears more than once"
        ]

        queryObjectNotFound("-rGBT role AH2-TEST", "role", "Abuse Handler")
    }

    def "modify ORGANISATION, add abuse-c ref ROLE, no abuse-mailbox"() {
      given:

      expect:
        query_object_not_matches("-r -T role SR1-TEST", "role", "Standard Role", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
          def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                abuse-c:      SR1-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-LIR2-TEST") ==
                ["The \"abuse-c\" ROLE object 'SR1-TEST' has no \"abuse-mailbox:\" Add \"abuse-mailbox:\" to the ROLE object, then update the ORGANISATION object"]

        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")
    }

    def "modify ORGANISATION, change admin-c ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
          def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      AH1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "modify ORGANISATION, change tech-c ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
          def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "create DOMAIN, then modify, change zone-c ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         AH1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 193.in-addr.arpa") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 193.in-addr.arpa" }

        query_object_matches("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa", "AH1-TEST")
    }

    def "create ROUTE, then modify, change ping-hdl ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                ping-hdl:       TP1-test
                source:         TEST
                override:   denis,override1

                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                ping-hdl:       AH1-test
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.infoSuccessMessagesFor("Create", "[route] 99.13.0.0/16") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 99.13.0.0/16AS2000" }

        query_object_matches("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16", "AH1-test")
        query_object_not_matches("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16", "TP1-test")
    }

    def "create ASSIGNED PA, no org, then modify, change admin-c ref ROLE with abuse-mailbox"() {
      given:
        dbfixture(getTransient("ALLOC-PA"))

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      AH1-TEST
                tech-c:       TP2-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }

        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127", "AH1-test")
        query_object_not_matches("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127", "TP1-test")
    }

    def "create ASSIGNED PA ref org no abuse-c, then modify, change admin-c ref ROLE with abuse-mailbox"() {
      given:
        dbfixture(getTransient("ALLOC-PA"))

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      AH1-TEST
                tech-c:       TP2-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }

        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127", "AH1-test")
        query_object_not_matches("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127", "TP1-test")
    }

    def "create ASSIGNED PA, no org, then modify, add abuse-c ref ROLE with abuse-mailbox"() {
      given:
        dbfixture(getTransient("ALLOC-PA"))

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                abuse-c:      AH1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Authorisation override used"]

        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127", "abuse-c")
    }

    def "create ASSIGNED PA, then modify, duplicate abuse-c"() {
      given:
        dbfixture(getTransient("ALLOC-PA"))

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                abuse-c:      AH1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network updated
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP2-TEST
                abuse-c:      AH1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password:   lir

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Authorisation override used"]
        ack.warningSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Duplicate abuse-c \"AH1-TEST\" also found in referenced organisation \"ORG-LIRA-TEST\"."]

        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Duplicate abuse-c \"AH1-TEST\" also found in referenced organisation \"ORG-LIRA-TEST\"."]
    }

    def "create ROLE, with abuse-mailbox, self ref"() {
      given:

      expect:
        queryObjectNotFound("-r -T role AH2-TEST", "role", "Abuse Handler")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      AH2-TEST
                tech-c:       AH2-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 3, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[role] AH2-TEST   Abuse Handler" }
        ack.errorMessagesFor("Create", "[role] AH2-TEST   Abuse Handler") == [
                "Self reference is not allowed for attribute type \"admin-c\"",
                "Self reference is not allowed for attribute type \"tech-c\""]
    }

    def "create ROLE, with abuse-mailbox, self ref abuse-c"() {
      given:

      expect:
        queryObjectNotFound("-r -T role AH2-TEST", "role", "Abuse Handler")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH2-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[role] AH2-TEST   Abuse Handler" }
        ack.errorMessagesFor("Create", "[role] AH2-TEST   Abuse Handler") ==
                ["\"abuse-c\" is not valid for this object type"]

        queryObjectNotFound("-rGBT role AH2-TEST", "role", "Abuse Handler")
    }

    def "modify ROLE, remove abuse-mailbox, no refs"() {
      given:
        syncUpdate(getTransient("ROLE-AM-NOREF") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Role
                remarks:      DO NOT REFERENCE this object
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_not_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")
    }

    def "modify ROLE, change abuse-mailbox, no refs"() {
      given:
        syncUpdate(getTransient("ROLE-AM-NOREF") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Role
                remarks:      DO NOT REFERENCE this object
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:LIRabuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "LIRabuse@lir.net")
    }

    def "modify ROLE, add abuse-mailbox, no refs"() {
      given:
        syncUpdate(getTransient("ROLE-NO-AM-NO-REF") + "password: lir")

      expect:
        query_object_not_matches("-r -T role SR99-TEST", "role", "Standard Role", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      SR99-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[role] SR99-TEST   Standard Role" }
        ack.warningSuccessMessagesFor("Modify", "[role] SR99-TEST   Standard Role") == [
                "There are no limits on queries for ROLE objects containing \"abuse-mailbox:\""]

        query_object_matches("-rGBT role SR99-TEST", "role", "Standard Role", "abuse@lir.net")
    }

    def "modify ROLE, add abuse-mailbox, ref admin-c"() {
      given:

      expect:
        query_object_not_matches("-r -T role SR1-TEST", "role", "Standard Role", "abuse-mailbox:")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "SR1-TEST")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-mailbox:abuse@lir.net
                nic-hdl:      SR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[role] SR1-TEST   Standard Role" }
        ack.warningSuccessMessagesFor("Modify", "[role] SR1-TEST   Standard Role") == [
                "There are no limits on queries for ROLE objects containing \"abuse-mailbox:\""]

        query_object_matches("-rGBT role SR1-TEST", "role", "Standard Role", "abuse@lir.net")
    }

    def "modify ROLE, with abuse-mailbox, add remarks:"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH1-TEST
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AH1-TEST   Abuse Handler" }

        query_object_matches("-rGBT role AH1-TEST", "role", "Abuse Handler", "just added")
    }

    def "delete ROLE, abuse-mailbox, no refs"() {
      given:
        syncUpdate(getTransient("ROLE-AM-NOREF") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:         Abuse Role
                remarks:      DO NOT REFERENCE this object
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                delete:  unreferenced

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[role] AR1-TEST   Abuse Role" }

        queryObjectNotFound("-rGBT role AR1-TEST", "role", "Abuse Role")
    }

    def "modify ORGANISATION, add abuse-c ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
        def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "modify ORGANISATION, no abuse-c, add remarks:"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
        def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                remarks:      just added
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "just added")
    }

    def "modify ORGANISATION, with abuse-c, add remarks:"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_matches("-r -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c:")

      when:
         def ack = syncUpdateWithResponse("""
                organisation: ORG-LIRA-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                remarks:      just added

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIRA-TEST" }

        query_object_matches("-r -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "just added")
    }

    def "create ORGANISATION, type OTHER, with abuse-c "() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: auto-1
                org-type:     OTHER
                org-name:     Create Local Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-CLR1-TEST" }

        query_object_matches("-r -T organisation ORG-CLR1-TEST", "organisation", "ORG-CLR1-TEST", "AH1-TEST")
    }

    def "create ORGANISATION, type OTHER, with no abuse-c "() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: auto-1
                org-type:     OTHER
                org-name:     Create Local Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-CLR1-TEST" }

        query_object_not_matches("-r -T organisation ORG-CLR1-TEST", "organisation", "ORG-CLR1-TEST", "abuse-c")
    }

    def "modify ORGANISATION, add abuse-c "() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                abuse-c:      AH1-TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }

        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "abuse-c")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "AH1-TEST")
    }

    def "modify ORGANISATION, type OTHER, without abuse-c, add remarks:"() {
      given:

      expect:
        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                remarks:      just added

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }

        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "just added")
    }

    def "modify ORGANISATION, type OTHER, with abuse-c, add remarks:"() {
      given:
        whoisFixture.dumpSchema()
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        whoisFixture.dumpSchema()

      expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                remarks:      just added

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }

        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "just added")
    }

    def "modify ORGANISATION, add 2 abuse-c ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-LIR2-TEST") ==
                ["Attribute \"abuse-c\" appears more than once"]

        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "modify ORGANISATION, add abuse-c, change admin-c & tech-c, all ref ROLE with abuse-mailbox"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")
        query_object_not_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")

      when:
        def ack = syncUpdateWithResponse("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      AH1-TEST
                tech-c:       AH1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "modify ROLE with abuse-mailbox, ref by admin-c & tech-c in org, no ref by abuse-c, remove abuse-mailbox, org has no abuse-c"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      AR1-TEST
                tech-c:       AR1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                password:     hm

                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_not_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")
    }

    def "modify ROLE with abuse-mailbox, ref by admin-c & tech-c, no ref by abuse-c, remove abuse-mailbox, org has abuse-c"() {
        given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

        expect:
          query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

        when:
        def message = syncUpdate("""\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      AR1-TEST
                tech-c:       AR1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                password:     hm""".stripIndent(true))

        def roleMessage = syncUpdate("""\
                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        def ack2 = new AckResponse("", roleMessage)
        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 0, 1, 0, 0)
        ack2.summary.assertErrors(0, 0, 0, 0)

        ack2.countErrorWarnInfo(0, 1, 0)
        ack2.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_not_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")
    }

    def "modify ROLE with abuse-mailbox, ref by admin-c & tech-c, no ref by abuse-c, change abuse-mailbox value"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      AR1-TEST
                tech-c:       AR1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                password:     hm

                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:LIRabuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "LIRabuse@lir.net")
    }

    def "modify ROLE with abuse-mailbox, ref by abuse-c, change abuse-mailbox value"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AR1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                password:     hm

                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:LIRabuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.successes.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "LIRabuse@lir.net")
    }

    def "modify ROLE with abuse-mailbox, ref by abuse-c, remove abuse-mailbox"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AR1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                password:     hm

                role:         Abuse Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.errors.any { it.operation == "Modify" && it.key == "[role] AR1-TEST   Abuse Role" }

        query_object_matches("-rGBT role AR1-TEST", "role", "Abuse Role", "abuse@lir.net")
    }

    def "modify ORGANISATION with abuse-c, change abuse-c to ref ROLE with no abuse-mailbox"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIRA-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      SR1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIRA-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-LIRA-TEST") ==
                ["The \"abuse-c\" ROLE object 'SR1-TEST' has no \"abuse-mailbox:\" Add \"abuse-mailbox:\" to the ROLE object, then update the ORGANISATION object"]

        query_object_matches("-rGBT organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "AH1-TEST")
    }

    def "modify ORGANISATION, type LIR, with abuse-c, remove abuse-c"() {
      given:
        syncUpdate(getTransient("ROLE-AM") + "password: lir")

      expect:
        query_object_matches("-r -T role AR1-TEST", "role", "Abuse Role", "abuse-mailbox:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-LIRA-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIRA-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-LIRA-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, no ref, remove abuse-c"() {
      given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")

      expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")

      when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }

        query_object_not_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref LEGACY, remove abuse-c"() {
      given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        syncUpdate(getTransient("LEGACY") + "override: denis,override1")

      expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OFA10-TEST")

      when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }

        query_object_not_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref ASSIGNED PA, remove abuse-c"() {
      given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        dbfixture(getTransient("ASS-END-A"))

      expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OFA10-TEST")

      when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }

        query_object_not_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref ASSIGNED PA with HM mntner, remove abuse-c"() {
        given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        dbfixture(getTransient("ASS-END-HM-A"))

        expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OFA10-TEST")

        when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OFA10-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref ASSIGNED PI, remove abuse-c"() {
        given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        syncUpdate(getTransient("ASSPI-A") + "override: denis,override1")

        expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OFA10-TEST")

        when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OFA10-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref ASSIGNED ANYCAST, remove abuse-c"() {
        given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        syncUpdate(getTransient("ASSANY-A") + "override: denis,override1")

        expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OFA10-TEST")

        when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OFA10-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref ASN, remove abuse-c"() {
        given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        syncUpdate(getTransient("AS1309-A") + "override: denis,override1")

        expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T aut-num AS1309", "aut-num", "AS1309", "ORG-OFA10-TEST")

        when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OFA10-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref IPv6 ASSIGNED PI, remove abuse-c"() {
        given:
          syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
          syncUpdate(getTransient("LIR-ASS-PI-A") + "override: denis,override1")

        expect:
          query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
          query_object_matches("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "ORG-OFA10-TEST")

        when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OFA10-TEST") ==
                ["\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object"]

        query_object_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
    }

    def "modify ORGANISATION with abuse-c, type OTHER, ref AGGREGATED-BY-LIR, remove abuse-c"() {
      given:
        syncUpdate(getTransient("ORG-OTHER-A") + "password: lir")
        syncUpdate(getTransient("LIR-AGGR-32-48-A") + "override: denis,override1")

      expect:
        query_object_matches("-r -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32", "ORG-OFA10-TEST")

      when:
        def message = syncUpdate("""\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OFA10-TEST" }

        query_object_not_matches("-rGBT organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c:")
        query_object_matches("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32", "ORG-OFA10-TEST")
    }

    def "create ORGANISATION, type RIR, with no abuse-c "() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

      when:
        def ack = syncUpdateWithResponse("""
                organisation: auto-1
                org-type:     RIR
                org-name:     Create Regional Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-CRR1-TEST" }

        query_object_not_matches("-r -T organisation ORG-CRR1-TEST", "organisation", "ORG-CRR1-TEST", "abuse-c")
    }

    def "create ALLOCATED UNSPECIFIED ref ORGANISATION with no abuse-c, type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "override: denis,override1")

      expect:
        query_object_not_matches("-rGBT organisation ORG-RIR1-TEST", "organisation", "ORG-RIR1-TEST", "abuse-c:")

      when:
        def message = syncUpdate("""\
                inetnum:      92.0.0.0 - 92.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 92.0.0.0 - 92.255.255.255" }

        query_object_not_matches("-rGBT organisation ORG-RIR1-TEST", "organisation", "ORG-RIR1-TEST", "abuse-c:")
        queryObject("-r -T inetnum 92.0.0.0 - 92.255.255.255", "inetnum", "92.0.0.0 - 92.255.255.255")
    }

    def "modify ALLOCATED UNSPECIFIED ref ORGANISATION with no abuse-c, type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "override: denis,override1")

      expect:
        query_object_not_matches("-rGBT organisation ORG-RIR1-TEST", "organisation", "ORG-RIR1-TEST", "abuse-c:")

      when:
        def message = syncUpdate("""\
                inetnum:      92.0.0.0 - 92.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      92.0.0.0 - 92.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                remarks:      just added

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 92.0.0.0 - 92.255.255.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 92.0.0.0 - 92.255.255.255" }

        query_object_not_matches("-rGBT organisation ORG-RIR1-TEST", "organisation", "ORG-RIR1-TEST", "abuse-c:")
        query_object_matches("-r -T inetnum 92.0.0.0 - 92.255.255.255", "inetnum", "92.0.0.0 - 92.255.255.255", "just added")
    }

    def "create ALLOCATED PA ref ORGANISATION with no abuse-c, type LIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        query_object_not_matches("-rGBT organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c:")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "modify ORGANISATION, add abuse-c referencing PERSON"() {
      given:

      expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")
        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "abuse-c:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                abuse-c:      TP1-TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OR1-TEST") ==
                ["\"abuse-c:\" references a PERSON object This must reference a ROLE object with an \"abuse-mailbox:\""]

        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "abuse-c")
    }

    def "create ROUTE with abuse-c, verify it has not been created"() {
        given:

        expect:
        query_object_matches("-r -T role AH1-TEST", "role", "Abuse Handler", "abuse-mailbox:")

        when:
        def message = syncUpdate("""\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                abuse-c:        AH1-TEST
                ping-hdl:       TP1-test
                source:         TEST
                override:   denis,override1

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16") == [
                "\"abuse-c\" is not valid for this object type"]

        query_object_not_matches("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16", "TP1-test")
    }

    def "remove abuse-mailbox from ROLE referenced as abuse-c from an INETNUM"() {
        given:
        syncUpdate(getTransient("ROLE-AH300") + "override: denis,override1")
        syncUpdate(getTransient("ABUSEC-PA") + "override: denis,override1")

        when:
        def message = syncUpdate("""\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH300-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Modify" && it.key == "[role] AH300-TEST   Standard Role" }
        ack.errorMessagesFor("Modify", "[role] AH300-TEST   Standard Role") == [
                "There is an inetnum referencing role Standard Role's abuse-mailbox"]
    }

    def "remove abuse-mailbox from ROLE referenced as abuse-c from an INET6NUM"() {
        given:
        syncUpdate(getTransient("ROLE-AH300") + "override: denis,override1")
        syncUpdate(getTransient("ABUSEC-INET6NUM") + "override: denis,override1")

        when:
        def message = syncUpdate("""\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH300-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Modify" && it.key == "[role] AH300-TEST   Standard Role" }
        ack.errorMessagesFor("Modify", "[role] AH300-TEST   Standard Role") == [
                "There is an inet6num referencing role Standard Role's abuse-mailbox"]
    }

    def "remove abuse-mailbox from ROLE referenced as abuse-c from an AUTNUM"() {
        given:
        syncUpdate(getTransient("ROLE-AH300") + "override: denis,override1")
        syncUpdate(getTransient("ABUSEC-AUTNUM") + "override: denis,override1")

        when:
        def message = syncUpdate("""\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH300-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Modify" && it.key == "[role] AH300-TEST   Standard Role" }
        ack.errorMessagesFor("Modify", "[role] AH300-TEST   Standard Role") == [
                "There is an aut-num referencing role Standard Role's abuse-mailbox"]
    }

    def "modify AUTNUM change org ref to ORGANISATION with no abuse-c, type OTHER"() {
        given:
        syncUpdate(getTransient("AS1309-A") + "override: denis,override1")

        when:
        def message = syncUpdate("""\
                aut-num:     AS1309
                as-name:     TEST-AS
                descr:       With abuse-c link
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                org:         ORG-OR1-TEST
                sponsoring-org: ORG-LIRA-TEST
                status:      ASSIGNED
                mnt-by:      RIPE-NCC-END-MNT
                mnt-by:      EXACT-MB-MNT
                source:      TEST

                password: owner3
                password: mb-exact
                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS1309" }
        ack.errorMessagesFor("Modify", "[aut-num] AS1309") ==
                ["ORG-OR1-TEST must include an \"abuse-c:\" attribute"]
    }

    def "create INETNUM with org ref to ORGANISATION with no abuse-c, type OTHER"() {
        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                org:          ORG-OR1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["ORG-OR1-TEST must include an \"abuse-c:\" attribute"]
    }

}
