package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message

class IrtSpec extends BaseSpec {

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
                changed:      dbtest@ripe.net 20020101
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
                changed:      dbtest@ripe.net 20020101
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
                changed:      dbtest@ripe.net 20020101
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST
            """
    ]}

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, no irt pw"() {
      given:
            syncUpdate(getTransient("INETNUM1")+"password: end\npassword: hm")
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-irt:\" not authenticated by: irt-test"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:      irt-test")
    }

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, irt pw supplied"() {
        given:
            syncUpdate(getTransient("INETNUM1") + "password: end\npassword: hm")
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     end
                password:     irt
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:        irt-test")
    }

    def "modify INETNUM, add mnt-irt, mnt-by pw supplied, no irt exist"() {
        given:
            syncUpdate(getTransient("INETNUM1") + "password: end\npassword: hm")

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
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Unknown object referenced irt-test",
                        "Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-irt:\" no valid maintainer found"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-irt:      irt-test")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.201.0 - 192.168.201.255") ==
                ["Authorisation for [inetnum] 192.168.201.0 - 192.168.201.255 failed using \"mnt-irt:\" not authenticated by: irt-test"]
        queryObjectNotFound("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, no irt exist"() {
        given:
            queryObjectNotFound("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.201.0 - 192.168.201.255") ==
                ["Unknown object referenced irt-test",
                        "Authorisation for [inetnum] 192.168.201.0 - 192.168.201.255 failed using \"mnt-irt:\" no valid maintainer found"]

        queryObjectNotFound("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
    }

    def "create INETNUM, with mnt-irt, mnt-by pw supplied, irt pw supplied"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            queryObject("-r -T irt irt-tesT", "irt", "irt-test")

        expect:
            queryObjectNotFound("-r -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                password: irt
                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }

        query_object_matches("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "mnt-irt:\\s*irt-test")
    }

    def "delete inetnum, with mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            syncUpdate(getTransient("INETNUM2") + "password: end\npassword: hm\npassword:irt")

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
                    changed:      dbtest@ripe.net 20020101
                    source:       TEST
                    delete:       test deletion

                    password: end
                    """.stripIndent()
            )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }

        queryObjectNotFound("-r -T inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255")
    }

    def "modify INETNUM, remove mnt-irt, mnt-by pw supplied, no irt pw"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            syncUpdate(getTransient("INETNUM2") + "password: end\npassword: hm\npassword: irt")

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
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }

        query_object_not_matches("-rGBT inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255", "mnt-irt:\\s*irt-test")
    }

    def "modify INETNUM, add mnt-irt, no mnt-by pw, no irt pw, using override"() {
        given:
            syncUpdate(getTransient("IRT1") + "password: owner")
            syncUpdate(getTransient("INETNUM2") + "password: end\npassword: hm\npassword: irt")

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
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:     override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.202.0 - 192.168.202.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.202.0 - 192.168.202.255") ==
                ["Authorisation override used"]

        query_object_matches("-rGBT inetnum 192.168.202.0 - 192.168.202.255", "inetnum", "192.168.202.0 - 192.168.202.255","mnt-irt:        irt-test")
    }
}
