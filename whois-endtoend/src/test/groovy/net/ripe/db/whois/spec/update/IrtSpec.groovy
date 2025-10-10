package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class IrtSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
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
            "IRT-INV-SIG": """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                signature:    PGPKEY-ABCDEFAB
                encryption:   PGPKEY-ABCDEFAB
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """,
            "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       END-USER-MNT
                source:       TEST
            """,
            "INETNUM1": """\
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
            "INETNUM2": """\
                inetnum:      192.168.202.0 - 192.168.202.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST
            """
    ]}

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, no irt pw"() {
      given:
            dbfixture(getTransient("INETNUM1"))
            queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
            syncUpdate(getTransient("IRT1") + "password: owner\npassword: irt")
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")

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
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-irt:\" not authenticated by: irt-test"]
        ack.warningMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
              ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:      irt-test")
    }

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, irt pw supplied"() {
        given:
            dbfixture(getTransient("INETNUM1"))
            syncUpdate(getTransient("IRT1") + "password: owner")

        expect:
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")
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
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password:     end
                password:     irt
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:        irt-test")
    }

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, no irt exist"() {
        given:
            dbfixture(getTransient("INETNUM1"))

        expect:
            queryObjectNotFound("-r -T irt irt-tesT", "irt", "irt-test")
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
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: end
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 3, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Unknown object referenced irt-test",
                        "Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-irt:\" no valid maintainer found"]
        ack.warningMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:      irt-test")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            dbfixture(getTransient("ALLOC-PA"))
            syncUpdate(getTransient("IRT1") + "password: owner")
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
            def ack = syncUpdateWithResponse("""
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: end
                password: hm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)

            ack.countErrorWarnInfo(1, 1, 0)
            ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
            ack.errorMessagesFor("Create", "[inetnum] 192.168.201.0 - 192.168.201.255") ==
                    ["Authorisation for [inetnum] 192.168.201.0 - 192.168.201.255 failed using \"mnt-irt:\" not authenticated by: irt-test"]
            queryObjectNotFound("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, no irt exist"() {
        given:
            dbfixture(getTransient("ALLOC-PA"))
            queryObjectNotFound("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
            def ack = syncUpdateWithResponse("""
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: end
                password: hm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)
            ack.countErrorWarnInfo(2, 1, 0)
            ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
            ack.errorMessagesFor("Create", "[inetnum] 192.168.201.0 - 192.168.201.255") ==
                    ["Unknown object referenced irt-test",
                            "Authorisation for [inetnum] 192.168.201.0 - 192.168.201.255 failed using \"mnt-irt:\" no valid maintainer found"]

            queryObjectNotFound("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, irt pw supplied"() {
        given:
            dbfixture(getTransient("ALLOC-PA"))
            syncUpdate(getTransient("IRT1") + "password: owner")
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
            def ack = syncUpdateWithResponse("""
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: end
                password: irt
                password: hm
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }

            query_object_matches("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "mnt-irt:\\s*irt-test")
    }

    def "delete inetnum, with mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            dbfixture(getTransient("INETNUM2"))

        expect:
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")
            queryObject("-r -T inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255")

        when:
            def message = send new Message(
                    subject: "delete 192.168.202.0 - 192.168.202.255",
                    body: """\
                    inetnum:      192.168.202.0 - 192.168.202.255
                    netname:      RIPE-NET1
                    descr:        /24 assigned
                    country:      NL
                    admin-c:      TP1-TEST
                    tech-c:       TP1-TEST
                    status:       ASSIGNED PA
                    mnt-by:       END-USER-MNT
                    mnt-irt:      irt-test
                    source:       TEST
                    delete:       test deletion

                    password: end
                    """.stripIndent(true)
            )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }

        queryObjectNotFound("-r -T inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255")
    }

    def "modify INETNUM, remove mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            dbfixture(getTransient("INETNUM2"))

        expect:
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")
            queryObject("-r -T inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.202.0 - 192.168.202.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST

                password: end
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.202.0 - 192.168.202.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_not_matches("-rGBT inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255", "mnt-irt:\\s*irt-test")
    }

    def "modify INETNUM, add mnt-irt, no mnt-by pw, no irt pw, using override"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            dbfixture(getTransient("INETNUM2"))

        expect:
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")
            queryObject("-r -T inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255")

        when:
        def response = syncUpdate("""\
                inetnum:      192.168.202.0 - 192.168.202.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-irt:      irt-test
                mnt-by:       END-USER-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.202.0 - 192.168.202.255") ==
                ["Authorisation override used"]
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.202.0 - 192.168.202.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_matches("-rGBT inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255","mnt-irt:        irt-test")
    }

    def "create IRT, with abuse-mailbox"() {
        when:
        def response = syncUpdate("""\
                irt:           irt-test
                address:       RIPE NCC
                e-mail:        irt-dbtest@ripe.net
                signature:     PGPKEY-D83C3FBD
                encryption:    PGPKEY-D83C3FBD
                auth:          PGPKEY-D83C3FBD
                auth:          MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:       irt_nfy1_dbtest@ripe.net
                notify:        nfy_dbtest@ripe.net
                abuse-mailbox: abuse@ripe.net
                admin-c:       TP1-TEST
                tech-c:        TP1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST

                password: owner
                password: irt
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[irt] irt-test" }
        ack.errorMessagesFor("Create", "[irt] irt-test") ==
                ["\"abuse-mailbox\" is not valid for this object type"]

        queryObjectNotFound("-r -T irt irt-tesT", "irt", "irt-test")
    }

    def "modify IRT, add abuse-mailbox"() {
        given:
        dbfixture("""\
                irt:           irt-test
                address:       RIPE NCC
                e-mail:        irt-dbtest@ripe.net
                signature:     PGPKEY-D83C3FBD
                encryption:    PGPKEY-D83C3FBD
                auth:          PGPKEY-D83C3FBD
                auth:          MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:       irt_nfy1_dbtest@ripe.net
                notify:        nfy_dbtest@ripe.net
                admin-c:       TP1-TEST
                tech-c:        TP1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST
                """.stripIndent(true));
        expect:
        queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        when:
        def response = syncUpdate("""\
                irt:           irt-test
                address:       RIPE NCC
                e-mail:        irt-dbtest@ripe.net
                signature:     PGPKEY-D83C3FBD
                encryption:    PGPKEY-D83C3FBD
                auth:          PGPKEY-D83C3FBD
                auth:          MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:       irt_nfy1_dbtest@ripe.net
                notify:        nfy_dbtest@ripe.net
                abuse-mailbox: abuse2@ripe.net
                admin-c:       TP1-TEST
                tech-c:        TP1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST

                password: owner
                password: irt
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[irt] irt-test" }
        ack.errorMessagesFor("Modify", "[irt] irt-test") ==
                ["\"abuse-mailbox\" is not valid for this object type"]

        ! queryMatches("-r -T irt irt-tesT", "abuse2@ripe.net")
    }

    def "modify IRT, remove abuse-mailbox"() {
        given:
        dbfixture("""\
                irt:           irt-test
                address:       RIPE NCC
                e-mail:        irt-dbtest@ripe.net
                signature:     PGPKEY-D83C3FBD
                encryption:    PGPKEY-D83C3FBD
                auth:          PGPKEY-D83C3FBD
                auth:          MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:       irt_nfy1_dbtest@ripe.net
                notify:        nfy_dbtest@ripe.net
                abuse-mailbox: abuse@ripe.net
                admin-c:       TP1-TEST
                tech-c:        TP1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST
                """.stripIndent(true));
        expect:
        queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        when:
        def response = syncUpdate("""\
                irt:           irt-test
                address:       RIPE NCC
                e-mail:        irt-dbtest@ripe.net
                signature:     PGPKEY-D83C3FBD
                encryption:    PGPKEY-D83C3FBD
                auth:          PGPKEY-D83C3FBD
                auth:          MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:       irt_nfy1_dbtest@ripe.net
                notify:        nfy_dbtest@ripe.net
                admin-c:       TP1-TEST
                tech-c:        TP1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST

                password: owner
                password: irt
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[irt] irt-test" }

        !queryMatches("-r -T irt irt-tesT", "abuse-mailbox")
    }

}
