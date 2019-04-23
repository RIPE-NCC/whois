package net.ripe.db.whois.spec.query

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class AbuseQuerySpec extends BaseQueryUpdateSpec {
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
                "ALLOC-PA-A": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
                "SUB-ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
                "SUB-ALLOC-PA-A": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      SR1-TEST
                tech-c:       AH1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                org:          ORG-END1-TEST
                """,
                "LIR-FIRST-PART-PA": """\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                "LIR-PART-PA-A": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                org:          ORG-END1-TEST
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
                org:          ORG-END1-TEST
                """,
                "ASS-END-NO-A": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                """,
                "ASS-END2-NO-A": """\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                """,
                "ASS-END-NO-A-ORG": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                org:          ORG-OR1-TEST
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                """,
                "ROUTE": """\
                route:          192.168.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                ping-hdl:       TP1-test
                source:         TEST
                """,
                "TEST-MNT": """\
                mntner:      TEST-MNT
                descr:       MNTNER for test
                admin-c:     AH1-TEST
                upd-to:      updto_tst4@ripe.net
                notify:      notify_tst4@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      TEST-MNT
                source:      TEST
                org:          ORG-OFA10-TEST
                """,
                "IRT1": """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """,
                "ABUSE-ROLE":"""\
                role:    Abuse Me
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: AB-TEST
                tech-c:  AB-TEST
                nic-hdl: AB-TEST
                abuse-mailbox: abuse@test.net
                mnt-by:  TST-MNT2
                source:  TEST
                """,
                "ANOTHER-ABUSE-ROLE": """\
                role:           Another Abuse Contact
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                nic-hdl:        AAC1-TEST
                abuse-mailbox:  more_abuse@test.net
                mnt-by:         TST-MNT2
                source:         TEST
                """,
                "YET-ANOTHER-ABUSE-ROLE": """\
                role:           Yet Another Abuse Contact
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                nic-hdl:        YAHC1-TEST
                abuse-mailbox:  yet_more_abuse@test.net
                mnt-by:         TST-MNT2
                source:         TEST
                """,
                "ORG-W-ABUSE_C": """\
                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                abuse-c:         AB-TEST
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:          TEST
                """,
                "AUTNUM": """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                org:            ORG-FO1-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """
        ]
    }

    def "query ALLOCATED PA, no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")

      expect:
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        query_object_not_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c")
    }

    def "query ALLOCATED PA, no abuse-c, admin-c ref ROLE with abuse-mailbox"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")

      expect:
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        query_object_not_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c")

      when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      AH1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST

                password: hm
                """.stripIndent()
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "AH1-TEST")
    }

    def "query ALLOCATED PA, no abuse-c, ORGANISATION admin-c ref ROLE with abuse-mailbox"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")

      expect:
        // "ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        query_object_not_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c")

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
                """.stripIndent()
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }

        query_object_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "AH1-TEST")
    }

    def "query ALLOCATED PA, no abuse-c, MNTNER admin-c ref ROLE with abuse-mailbox, MNTNER ref org with abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("TEST-MNT") + "password: owner3\npassword: test")

      expect:
        // "ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        // "TEST-MNT"
        query_object_matches("-rBG -T mntner TEST-MNT", "mntner", "TEST-MNT", "ORG-OFA10-TEST")
        query_object_matches("-rBG -T mntner TEST-MNT", "mntner", "TEST-MNT", "AH1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c")

      when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       TEST-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST

                password: hm
                """.stripIndent()
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "TEST-MNT")
    }

    def "query ALLOCATED PA, no abuse-c, IRT with abuse-mailbox"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("IRT1") + "password: owner")

      expect:
        // "ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        // "IRT1"
        queryObject("-r -T irt irt-tesT", "irt", "irt-test")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c")

      when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-irt:      irt-test
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST

                password: hm
                password: irt
                """.stripIndent()
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "irt-test")
    }

    def "query ALLOCATED PA, no abuse-c, more specific ASSIGNED PA has abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END-A") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        // "ASS-END-A"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-END1-TEST")
        // ORGANISATION without abuse-c
        query_object_not_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "abuse-c")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-OFA10-TEST", "organisation", "ORG-OFA10-TEST", "abuse-c")
    }

    def "query ASSIGNED PA, no abuse-c for ASSIGNED PA or ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir\npassword: end\n")

      expect:
        // "ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION without abuse-c
        query_object_not_matches("-rBG -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org:")
    }

    def "query ALLOCATED PA, with abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
    }

    def "query ASSIGNED PA with org, no abuse-c, parent ALLOCATED PA with abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END-NO-A-ORG") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "ASS-END-NO-A-ORG"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OR1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'abuse@lir.net'")
    }

    def "query ASSIGNED PA with org, no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A-ORG") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A-ORG"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OR1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
    }

    def "query LIR-PARTITIONED PA, ASSIGNED PA with org, no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A-ORG") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A-ORG"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OR1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "% Abuse contact for '192.168.128.0 - 192.168.255.255' is 'abuse@lir.net'")
    }

    def "query SUB-ALLOCATED, ASSIGNED PA with org, no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A-ORG") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A-ORG"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-OR1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "% Abuse contact for '192.168.0.0 - 192.168.255.255' is 'abuse@lir.net'")
    }

    def "query ASSIGNED PA with org, with abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-A"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-END1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'my_abuse@lir.net'")
    }

    def "query LIR-PARTITIONED PA, ASSIGNED PA with org, with abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-A"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-END1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "% Abuse contact for '192.168.128.0 - 192.168.255.255' is 'abuse@lir.net'")
    }

    def "query SUB-ALLOCATED, ASSIGNED PA with org, with abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED & LIR-PARTITIONED in between with no abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-A"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ORG-END1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "% Abuse contact for '192.168.0.0 - 192.168.255.255' is 'abuse@lir.net'")
    }

    def "query ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with abuse-c & LIR-PARTITIONED with no abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA-A") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-END1-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'my_abuse@lir.net'")
    }

    def "query LIR-PARTITIONED PA, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with abuse-c & LIR-PARTITIONED with no abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA-A") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-END1-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "% Abuse contact for '192.168.128.0 - 192.168.255.255' is 'my_abuse@lir.net'")
    }

    def "query SUB-ALLOCATED, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with abuse-c & LIR-PARTITIONED with no abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA-A") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-END1-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "% Abuse contact for '192.168.0.0 - 192.168.255.255' is 'my_abuse@lir.net'")
    }

    def "query ALLOCATED, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with abuse-c & LIR-PARTITIONED with no abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA-A") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-END1-TEST")
        // "LIR-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
    }

    def "query ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with no abuse-c & LIR-PARTITIONED with abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA-A") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-END1-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'my_abuse@lir.net'")
    }

    def "query LIR-PARTITIONED PA, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with no abuse-c & LIR-PARTITIONED with abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA-A") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-END1-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "% Abuse contact for '192.168.128.0 - 192.168.255.255' is 'my_abuse@lir.net'")
    }

    def "query SUB-ALLOCATED, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with no abuse-c & LIR-PARTITIONED with abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA-A") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-END1-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "% Abuse contact for '192.168.0.0 - 192.168.255.255' is 'abuse@lir.net'")
    }

    def "query ALLOCATED, ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with no abuse-c & LIR-PARTITIONED with abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA-A") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-END1-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
    }

    def "query 2 ASSIGNED PA with no abuse-c, parent ALLOCATED PA with abuse-c, SUB-ALLOCATED with no abuse-c & 2 LIR-PARTITIONED 1 with 1 without abuse-c in between"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        syncUpdate(getTransient("LIR-PART-PA-A") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("LIR-FIRST-PART-PA") + "password: owner3\npassword: lir\npassword: lir2")
        syncUpdate(getTransient("ASS-END-NO-A") + "password: lir2\npassword: end\npassword: owner3")
        syncUpdate(getTransient("ASS-END2-NO-A") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // "SUB-ALLOC-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-LIR2-TEST")
        // "LIR-PART-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "ORG-END1-TEST")
        // "LIR-FIRST-PART-PA"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255", "ORG-LIR2-TEST")
        // "ASS-END-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "org:")
        // "ASS-END2-NO-A"
        query_object_not_matches("-rBG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "org:")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")

      and:
        queryLineMatches("-rBG -T inetnum 192.168.100.0 - 192.168.100.255", "% Abuse contact for '192.168.100.0 - 192.168.100.255' is 'abuse@lir.net'")
        queryLineMatches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'my_abuse@lir.net'")
    }

    def "query -b ALLOCATED PA, with abuse-c"() {
      given:
        syncUpdate(getTransient("SUB-ALLOC-PA-A") + "override: denis,override1")
        syncUpdate(getTransient("ROUTE") + "override: denis,override1")

      expect:
        // "SUB-ALLOC-PA-A"
        query_object_matches("-rBG 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ORG-END1-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-END1-TEST", "organisation", "ORG-END1-TEST", "abuse-c")
        // "ROUTE"
        query_object_matches("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16", "AS2000")

      and:
        query_object_matches("-b 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "abuse-mailbox:\\s*my_abuse@lir.net")
        ! queryLineMatches("-b 192.168.0.0 - 192.168.255.255", "abuse-mailbox:\\s*abuse@lir.net")
        ! queryLineMatches("-b 192.168.0.0 - 192.168.255.255", "% Information related to '192.168.0.0/16AS2000'")
    }

    def "query -b aut-num with abuse-c"() {
        given:
            databaseHelper.addObject(getTransient("ABUSE-ROLE"))
            databaseHelper.addObject(getTransient("ORG-W-ABUSE_C"))
            databaseHelper.addObject(getTransient("AUTNUM"))

        expect:
            queryObject("--abuse-contact AS200", "aut-num", "AS200")
            queryObject("--abuse-contact AS200", "abuse-mailbox", "abuse@test.net")
            !(query("--abuse-contact AS200") =~ "%WARNING:902: useless IP flag passed")
    }

    def "query -b aut-num without abuse-c"() {
        given:
            databaseHelper.addObject("" +
                    "aut-num:        AS200\n" +
                    "as-name:        ASTEST\n" +
                    "descr:          description\n" +
                    "import:         from AS1 accept ANY\n" +
                    "export:         to AS1 announce AS2\n" +
                    "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                    "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                    "admin-c:        TP1-TEST\n" +
                    "tech-c:         TP1-TEST\n" +
                    "mnt-by:         OWNER-MNT\n" +
                    "source:         TEST")

        expect:
            queryObject("--abuse-contact AS200", "aut-num", "AS200")
            queryObjectNotFound("--abuse-contact AS200", "abuse-mailbox", "abuse@test.net")
    }

    def "query -b aut-num with abuse-c on resource"() {
        given:
        databaseHelper.addObject("" +
                "role:           Another Abuse Contact\n" +
                "nic-hdl:        AH2-TEST\n" +
                "abuse-mailbox:  more_abuse@test.net\n" +
                "mnt-by:         TST-MNT2\n" +
                "source:         TEST")
        databaseHelper.addObject("" +
                "aut-num:        AS200\n" +
                "as-name:        ASTEST\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "abuse-c:        AH2-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST")

        expect:
        queryObject("--abuse-contact AS200", "aut-num", "AS200")
        queryObject("--abuse-contact AS200", "abuse-mailbox", "more_abuse@test.net")
    }

    def "inverse query for organisation using person"() {
        given:
        databaseHelper.addObject(getTransient("ABUSE-ROLE"))
        databaseHelper.addObject(getTransient("ORG-W-ABUSE_C"))

        expect:
        queryObject("-i pn AB-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "inverse query for aut-num using person"() {
        given:
        databaseHelper.addObject("" +
                "role:           Another Abuse Contact\n" +
                "nic-hdl:        AH2-TEST\n" +
                "abuse-mailbox:  more_abuse@test.net\n" +
                "mnt-by:         TST-MNT2\n" +
                "source:         TEST")
        databaseHelper.addObject("" +
                "aut-num:        AS200\n" +
                "as-name:        ASTEST\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "abuse-c:        AH2-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST")

        expect:
        queryObject("-i pn AH2-TEST", "aut-num", "AS200")
    }

    def "assignments with different abuse-c overrides org reference"() {
      given:
            databaseHelper.addObject(getTransient("ANOTHER-ABUSE-ROLE"))
            databaseHelper.addObject(getTransient("YET-ANOTHER-ABUSE-ROLE"))
            databaseHelper.addObject(getTransient("ALLOC-PA-A"))
            databaseHelper.addObject("" +
                "inetnum:      192.168.100.0 - 192.168.100.255\n" +
                "netname:      RIPE-NET1\n" +
                "descr:        /24 assigned\n" +
                "country:      NL\n" +
                "org:          ORG-LIRA-TEST\n" +
                "abuse-c:      AAC1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ASSIGNED PA\n" +
                "mnt-by:       LIR2-MNT\n" +
                "source:       TEST\n")
            databaseHelper.addObject("" +
                "inetnum:      192.168.200.0 - 192.168.200.255\n" +
                "netname:      RIPE-NET2\n" +
                "descr:        /24 assigned\n" +
                "country:      NL\n" +
                "org:          ORG-LIRA-TEST\n" +
                "abuse-c:      YAHC1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ASSIGNED PA\n" +
                "mnt-by:       LIR2-MNT\n" +
                "source:       TEST\n")
      expect:
        // Allocation
        queryLineMatches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
        // All more specific - Allocation
        queryLineMatches("-r -M -T inetnum 192.168.0.0 - 192.171.255.255", "% Abuse contact for '192.168.0.0 - 192.169.255.255' is 'abuse@lir.net'")
        // All more specific - First Assignment
        queryLineMatches("-r -M -T inetnum 192.168.0.0 - 192.171.255.255", "% Abuse contact for '192.168.100.0 - 192.168.100.255' is 'more_abuse@test.net'")
        // All more specific - Second Assignment
        queryLineMatches("-r -M -T inetnum 192.168.0.0 - 192.171.255.255", "% Abuse contact for '192.168.200.0 - 192.168.200.255' is 'yet_more_abuse@test.net'")
    }

    def "query aut-num with suspect abuse-c without responsible org"() {
        given:
        databaseHelper.getInternalsTemplate().update("insert into abuse_email (address, status, created_at) values ('more_abuse@test.net', 'SUSPECT', now())")
        databaseHelper.addObject("" +
                "role:           Another Abuse Contact\n" +
                "nic-hdl:        AH2-TEST\n" +
                "abuse-mailbox:  more_abuse@test.net\n" +
                "mnt-by:         TST-MNT2\n" +
                "source:         TEST")
        databaseHelper.addObject("" +
                "aut-num:        AS200\n" +
                "as-name:        ASTEST\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "abuse-c:        AH2-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST")

        expect:
        !queryLineMatches("AS200", "% Abuse-mailbox validation failed.")

        cleanup:
        databaseHelper.getInternalsTemplate().update("delete from abuse_email")
    }

    def "query aut-num with suspect abuse-c with responsible org"() {
        given:
        databaseHelper.getInternalsTemplate().update("insert into abuse_email (address, status, created_at) values ('more_abuse@test.net', 'SUSPECT', now())")
        databaseHelper.addObject("" +
                "role:           Another Abuse Contact\n" +
                "nic-hdl:        AH2-TEST\n" +
                "abuse-mailbox:  more_abuse@test.net\n" +
                "mnt-by:         TST-MNT2\n" +
                "source:         TEST")
        databaseHelper.addObject("" +
                "aut-num:        AS200\n" +
                "as-name:        ASTEST\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "abuse-c:        AH2-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "org:            ORG-LIR2-TEST\n" +
                "source:         TEST")

        expect:
        queryLineMatches("AS200", "% Abuse-mailbox validation failed. Please refer to ORG-LIR2-TEST for further information.")

        cleanup:
        databaseHelper.getInternalsTemplate().update("delete from abuse_email")
    }

    def "query aut-num with suspect abuse-c with sponsoring org"() {
        given:
        databaseHelper.getInternalsTemplate().update("insert into abuse_email (address, status, created_at) values ('more_abuse@test.net', 'SUSPECT', now())")
        databaseHelper.addObject("" +
                "role:           Another Abuse Contact\n" +
                "nic-hdl:        AH2-TEST\n" +
                "abuse-mailbox:  more_abuse@test.net\n" +
                "mnt-by:         TST-MNT2\n" +
                "source:         TEST")
        databaseHelper.addObject("" +
                "aut-num:        AS200\n" +
                "as-name:        ASTEST\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "abuse-c:        AH2-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "sponsoring-org: ORG-LIR2-TEST\n" +
                "source:         TEST")

        expect:
        queryLineMatches("AS200", "% Abuse-mailbox validation failed. Please refer to ORG-LIR2-TEST for further information.")

        cleanup:
        databaseHelper.getInternalsTemplate().update("delete from abuse_email")
    }

}
