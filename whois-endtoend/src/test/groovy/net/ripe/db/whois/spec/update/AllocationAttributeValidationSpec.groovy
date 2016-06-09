package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class AllocationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["ALLOC-PA-MANDATORY"      : """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ALLOC-PA-EXTRA"          : """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        some description  # extra
                country:      NL
                geoloc:       0.0 0.0           # extra
                language:     NL                # extra
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                remarks:      a new remark      # extra
                notify:       notify@ripe.net   # extra
                mnt-lower:    LIR-MNT           # extra
                mnt-routes:   OWNER-MNT         # extra
                mnt-domains:  DOMAINS-MNT       # extra
                mnt-irt:      IRT-TEST          # extra
                source:       TEST
                """,
         "ALLOC-PA-EXTRA-RIPE-NCC-MNTNER": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT  # hm-mnt
                mnt-lower:    LIR-MNT          # extra
                mnt-routes:   RIPE-NCC-HM-MNT  # hm-mnt
                mnt-routes:   OWNER-MNT        # extra
                mnt-domains:  RIPE-NCC-HM-MNT  # hm-mnt
                mnt-domains:  DOMAINS-MNT      # extra
                source:       TEST
                """,
         "ASSIGN-PI"               : """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "IRT"                     : """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest@ripe.net
                notify:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """,
         "IRT2"                    : """\
                irt:          IRT-2-TEST
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      dbtest@ripe.net
                notify:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """,
         "DOMAINS2-MNT"            : """\
                mntner:      DOMAINS2-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_domains@ripe.net
                mnt-nfy:     mntnfy_domains@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$anTWxMgQ\$8aBWq5u5ZFHLA5aeZsSxG0  #domains
                mnt-by:      DOMAINS2-MNT
                source:      TEST
                """,
         "NON-TOPLEVEL-ASSIGN-PI"  : """\
                inetnum:      193.168.255.0 - 193.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "V6ALLOC-RIR"             : """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
        ]
    }

    def "modify inet6num, add mnt-lower with lir password should be possible"() {
        given:
        syncUpdate(getTransient("V6ALLOC-RIR") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def ack = syncUpdateWithResponse("""
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT          # added
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LIR *CHANGE* attributes (inetnum and inet6num)
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def "modify inet6num, change lir-unlocked attributes with lir password should be possible"() {      // TODO
        given:
        syncUpdate(getTransient("V6ALLOC-RIR") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "password: owner")

        expect:
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")
        queryObject("-r -T irt irt-test", "irt", "irt-test")

        when:
        def ack = syncUpdateWithResponse("""
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                geoloc:       20 20
                language:     NL
                remarks:      some new remarks
                notify:       notify@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                mnt-routes:   LIR2-MNT
                mnt-irt:      IRT-TEST
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

    }

    def "modify inetnum, change sponsoring-org with lir mntner is not possible"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "password: hm\npassword: lir\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum  192.168.255.0 - 192.168.255.255", "inetnum", " 192.168.255.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.255.0 - 192.168.255.255") == [
                "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
    }

    def "modify inetnum, change netname with lir mnt is possible for non-toplevel allocations"() {
        given:
        syncUpdate(getTransient("NON-TOPLEVEL-ASSIGN-PI") + "password: lir\npassword: owner3\npassword: hm")


        expect:
        queryObject("-GBr -T inetnum 193.168.255.0 - 193.168.255.255", "inetnum", "193.168.255.0 - 193.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      193.168.255.0 - 193.168.255.255
                netname:      DIFFERENT-TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 193.168.255.0 - 193.168.255.255" }
    }

    def "modify inetnum, change netname with lir mnt is not possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-MANDATORY") + "password: hm\npassword: owner3")


        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      DIFFERENT-TEST-NET-NAME
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

                password: owner3
                password: lir
                """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.169.255.255") == [
                "The \"netname\" attribute can only be changed by the RIPE NCC"]
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LIR *CHANGE* attributes (inetnum and inet6num)  WITH OVERRIDE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    def "modify inetnum, change lir-locked attributes with override should be possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-MANDATORY") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME2
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:     denis,override1
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

    }

    def "modify inetnum, change sponsoring-org with override is possible"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "password: hm\npassword: lir\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum  192.168.255.0 - 192.168.255.255", "inetnum", " 192.168.255.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override: denis,override1
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }
    }

    def "modify inetnum, change status with override is possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-MANDATORY") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override: denis,override1
                """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
    }

    def "modify inetnum, change org with RS mntner is possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-MANDATORY") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
    }

    def "modify inetnum, change sponsoring-org with RS mntner is possible"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "password: hm\npassword: lir\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum  192.168.255.0 - 192.168.255.255", "inetnum", " 192.168.255.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: nccend
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }

    }

    def "modify inetnum, change netname with RS mntner is possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-MANDATORY") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      DIFFERENT-TEST-NET-NAME
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

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
