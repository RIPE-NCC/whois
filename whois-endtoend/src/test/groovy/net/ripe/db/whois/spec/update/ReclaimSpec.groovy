package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse

@org.junit.jupiter.api.Tag("IntegrationTest")
class ReclaimSpec extends BaseQueryUpdateSpec {

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
            "ALLOC-UNS2": """\
                inetnum:      192.168.0.0 - 192.169.255.255
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
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
            "JOINT-ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
            "ERX-ALLOC-PA": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """,
            "ALLOC-PA-ROUTES": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   LIR2-MNT
                source:       TEST
                """,
            "ALLOC-PA-DOMAINS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
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
            "ASS-END": """\
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
            "LEGACY-ALLOC": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      RIPE-NET1
                descr:        /8 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "LEGACY-ALLOC-JOINT": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      RIPE-NET1
                descr:        /8 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-by:       RIPE-NCC-LEGACY-MNT
                source:       TEST
                """,
            "LEGACY_END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       END-USER-MNT
                source:       TEST
                """,
            "EXACT-ROUTE": """\
                route:       192.168.200.0/24
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      OWNER2-MNT
                source:      TEST
                """,
    ]}

    def "modify end user using allocation mnt-lower"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
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

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "20130101")
    }

    def "delete end user assignment using allocation mnt-lower"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

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

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete route for end user assignment using allocation mnt-lower"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        syncUpdate(getTransient("EXACT-ROUTE") + "override:  denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")

        when:
        def message = syncUpdate("""\
                route:       192.168.200.0/24
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      OWNER2-MNT
                source:      TEST
                delete:  lir reclaim

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS3000" }

        queryObjectNotFound("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
    }

    def "delete route for end user assignment using allocation mnt-routes"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-ROUTES") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        syncUpdate(getTransient("EXACT-ROUTE") + "override:  denis,override1")

        expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")

        when:
        def message = syncUpdate("""\
                route:       192.168.200.0/24
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      OWNER2-MNT
                source:      TEST
                delete:  lir reclaim

                password: lir2
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS3000" }
        ack.errorMessagesFor("Delete", "[route] 192.168.200.0/24AS3000") == [
                "Authorisation for [route] 192.168.200.0/24AS3000 failed using \"mnt-by:\" not authenticated by: OWNER2-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT, LIR-MNT"]

        queryObject("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
    }

    def "delete end user assignment using allocation 2nd mnt-lower"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete end user assignment using allocation user's joint mnt-by"() {
      given:
        syncUpdate(getTransient("JOINT-ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

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

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete end user assignment using allocation RS's joint mnt-by"() {
      given:
        syncUpdate(getTransient("JOINT-ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

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

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete allocation using user's joint mnt-by"() {
      given:
        syncUpdate(getTransient("JOINT-ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:  lir

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
                ["Status ALLOCATED UNSPECIFIED not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LIR-PARTITIONED PA"]

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete allocation using RS's joint mnt-by"() {
      given:
        syncUpdate(getTransient("JOINT-ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:  RS

                password: hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.warningMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Status ALLOCATED UNSPECIFIED not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete end user assignment using allocation mnt-routes"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-ROUTES") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override
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
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT, LIR-MNT"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete end user assignment using allocation mnt-domains"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-DOMAINS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

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
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                "Authorisation for [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT, LIR-MNT"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete end user assignment using legacy allocation mnt-by, no RS mntner"() {
      given:
        syncUpdate(getTransient("LEGACY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete legacy allocation without override"() {
      given:
        syncUpdate(getTransient("LEGACY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      RIPE-NET1
                descr:        /8 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                source:       TEST
                delete:  lir

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.0.0.0 - 192.255.255.255") ==
                ["Only RIPE NCC can create/delete a top level object with status 'LEGACY' Contact legacy@ripe.net for more info"]

        queryObject("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "delete legacy allocation using override"() {
      given:
        syncUpdate(getTransient("LEGACY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      RIPE-NET1
                descr:        /8 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                source:       TEST
                delete:  lir
                override: denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.0.0.0 - 192.255.255.255") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "delete end user legacy assignment using legacy allocation mnt-lower, no RS mntner"() {
        given:
        syncUpdate(getTransient("LEGACY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

        syncUpdate(getTransient("LEGACY_END") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  lir override

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }


    def "delete end user legacy assignment using mnt-by, RS mntner"() {
        given:
        syncUpdate(getTransient("LEGACY-ALLOC-JOINT") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

        syncUpdate(getTransient("LEGACY_END") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:  asdf

                password: lir
                """.stripIndent(true)
        )

        then:
        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete end user exact route object for legacy assignment using mnt-by"() {
        given:
        syncUpdate(getTransient("LEGACY-ALLOC-JOINT") +
                "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

        syncUpdate(getTransient("LEGACY_END") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        syncUpdate(getTransient("EXACT-ROUTE") + "override:  denis,override1")
        queryObject("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")

        when:
        def message = syncUpdate("""\
                route:       192.168.200.0/24
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      OWNER2-MNT
                source:      TEST
                delete:  lir reclaim

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 192.168.200.0/24AS3000" }

        queryObjectNotFound("-r -T route 192.168.200.0/24", "route", "192.168.200.0/24")
    }

}
