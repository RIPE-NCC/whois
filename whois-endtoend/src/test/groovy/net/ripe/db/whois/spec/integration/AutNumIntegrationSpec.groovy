package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.junit.jupiter.api.Tag

import java.time.LocalDateTime

@Tag("IntegrationTest")
class AutNumIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
            "PGPKEY-5763950D": """\
                key-cert:       PGPKEY-5763950D
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
            """,
            "OWNER-MNT"      : """\
                mntner: OWNER-MNT
                descr: description
                admin-c: AP1-TEST
                mnt-by: OWNER-MNT
                upd-to: noreply@ripe.net
                auth:   PGPKEY-5763950D
                auth:   SSO person@net.net
                source: TEST
            """,
            "TEST-MNT"    : """\
                mntner: TEST-MNT
                descr: description
                admin-c: AP1-TEST
                mnt-by: TEST-MNT
                upd-to: noreply@ripe.net
                auth:   PGPKEY-5763950D
                auth:   SSO test@ripe.net
                auth:   SSO person@net.net
                source: TEST
            """,
            "PWR-MNT"      : """\
                mntner:  RIPE-NCC-HM-MNT
                descr:   description
                admin-c: AP1-TEST
                mnt-by:  RIPE-NCC-HM-MNT
                upd-to:  dbtest@ripe.net
                auth:    SSO person@net.net
                source:  TEST
            """,
            "LEGACY-MNT"  : """\
                mntner:  RIPE-NCC-LEGACY-MNT
                descr:   description
                admin-c: AP1-TEST
                mnt-by:  RIPE-NCC-LEGACY-MNT
                upd-to:  dbtest@ripe.net
                source:  TEST
            """,
            "AP1-PN"       : """\
                person:  Admin Person
                address: Admin Road
                address: Town
                address: UK
                phone:   +44 282 411141
                nic-hdl: AP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
            """,
            "ROLE-A": """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      AP1-TEST
                tech-c:       AP1-TEST
                nic-hdl:      AH1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
            """,
            "ORG-NCC1-RIPE": """\
                organisation: ORG-NCC1-RIPE
                org-name:     Ripe NCC organisation
                org-type:     LIR
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                mnt-ref:      OWNER-MNT
                mnt-by:       OWNER-MNT
                source:       TEST
            """,
            "AUTNUM101"    : """\
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
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                notify:         noreply@ripe.net
                mnt-lower:      OWNER-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
            """,
            "AS-SET1"      : """\
                as-set:       AS-TESTSET
                descr:        Test Set
                members:      AS1
                tech-c:       AP1-TEST
                tech-c:       AP1-TEST
                admin-c:      AP1-TEST
                notify:       noreply@ripe.net
                mnt-by:       OWNER-MNT
                mbrs-by-ref:  OWNER-MNT
                source:       TEST
            """,
            "AS-BLOCK1"    : """\
                as-block:       AS100 - AS300
                descr:          RIPE NCC ASN block
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                mbrs-by-ref:    OWNER-MNT
                source:         TEST
            """,
            "AS-BLOCK2"    : """\
                as-block:       AS300 - AS500
                descr:          RIPE NCC ASN block
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                mnt-lower:      TEST-MNT
                source:         TEST
            """
        ]
    }

    def "delete aut-num object"() {
        when:
        def response = syncUpdate("""
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
                        org:            ORG-NCC1-RIPE
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-lower:      OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        delete:         reason
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/
    }

    def "create aut-num object"() {

        when:
        def response = syncUpdate("""
                        aut-num:        AS102
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        import:         from AS1 accept ANY
                        export:         to AS1 announce AS2
                        default:        to AS1
                        mp-import:      afi ipv6.unicast from AS1 accept ANY
                        mp-export:      afi ipv6.unicast to AS1 announce AS2
                        import-via:     AS6777 from AS5580 accept AS-ATRATO
                        export-via:     AS6777 to AS5580 announce AS2
                        mp-default:     to AS1
                        remarks:        remarkable
                        org:            ORG-NCC1-RIPE
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-lower:      OWNER-MNT
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)


        then:
        response =~ /SUCCESS/
    }

    def "create aut-num object with no parent AS-BLOCK object"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS1000
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        import:         from AS1 accept ANY
                        export:         to AS1 announce AS2
                        default:        to AS1
                        mp-import:      afi ipv6.unicast from AS1 accept ANY
                        mp-export:      afi ipv6.unicast to AS1 announce AS2
                        import-via:     AS6777 from AS5580 accept AS-ATRATO
                        export-via:     AS6777 to AS5580 announce AS2
                        mp-default:     to AS1
                        remarks:        remarkable
                        org:            ORG-NCC1-RIPE
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-lower:      OWNER-MNT
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)


        then:
        response.contains("***Error:   No parent as-block found for AS1000")
    }

    def "create, member-of reference not found"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS102
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        member-of:      AS-NONEXISTING
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /FAIL/
        response =~ /Error:   Unknown object referenced AS-NONEXISTING/
    }

    def "create, authentication against asblock's mnt-lower fail"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_OWNER_MNT)

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[as-block\] AS300 - AS500 failed/
        response =~ /using "mnt-lower:"/
        response =~ /not authenticated by: TEST-MNT/
    }

    def "create, authentication against asblock's mnt-lower succeed"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/
    }

    def "create, add comment in managed attribute fails"() {
        given:
        setTime(LocalDateTime.parse("2026-02-05T15:00:00")) // current time must be within 1 hour of signing time

        when:
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256
                
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          other description
                org:            ORG-NCC1-RIPE #test comment
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                notify:         noreply@ripe.net
                mnt-routes:     OWNER-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
                -----BEGIN PGP SIGNATURE-----
                
                iQEzBAEBCAAdFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAmmErFUACgkQu8y7LVdj
                lQ3cPAf+Ln4+gwGfGEEJlhAyx8FXuzgAoPv1iI5Z1R4QmF0BINqDANimaYsmiZ9Y
                ZE6SPWMVhRajdu/2uKg7RUog1MzOQeimbf+apNQHpvsoeN821gIGGIO9U4Wbgjeu
                Q/gH3bW/lLqmMoFx+6jKimULecFv7aqDIbGYKbaqfDo2hpboA0vgtg4U5vdOSsSC
                PP3bY/YsPxs4/9JyvrWRvbbF1D/LpR6O/CplC1qmzEBK3SFzEUiK+glsUZpkrSY6
                qeU/HNnCN4tXc+eHvCOjf1vJgigugL4mvhaxoK7q0w1NTLO5lzUJ3kxUfvir+AZL
                h6L7UTX4PU40i5q8qNnMVwuMmIWEmA==
                =QAtU
                -----END PGP SIGNATURE-----
            """.stripIndent(true).replaceAll("\n\n", "\n  \t  \n"))

        then:
        def ack = ackFor message
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS400" }
        ack.errorMessagesFor("Create", "[aut-num] AS400") == [
                "Comments are not allowed on RIPE NCC managed Attribute \"org:\""]
    }

    def "create, authentication against asblock's mnt-by and local mnt-by fail"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_INVALID_SIGNATURE_API_KEY)

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[as-block\] AS300 - AS500 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: TEST-MNT/

        response =~ /Error:   Authorisation for \[aut-num\] AS400 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: OWNER-MNT/
    }

    def "create, authentication against asblock's mnt-by succeeds, fails on local mnt-by"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_TEST_NO_MNT)

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[aut-num\] AS400 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: OWNER-MNT/
    }

    def "modify, only description changed"() {
        def insertResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true), null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);

        then:
        updateResponse =~ /SUCCESS/
    }

    def "modify, add comment in managed attribute fails"() {
        setTime(LocalDateTime.parse("2026-02-05T15:00:00"))

        def insertResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def message = send new Message(
                        subject: "",
                        body: """\
                        -----BEGIN PGP SIGNED MESSAGE-----
                        Hash: SHA256
                        
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        org:            ORG-NCC1-RIPE #test comment
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        -----BEGIN PGP SIGNATURE-----
                        
                        iQEzBAEBCAAdFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAmmErYQACgkQu8y7LVdj
                        lQ3Yzgf/STkQrRoXpGvBTSzutCFLA2RS6xcCLuNgTMcwlTohF0xi1zLkaz1/E0DD
                        ZI+PaQeEy2euM/XhstarzhyAAbyS5nNr31xCygNrjnfbHh5RKClzNrp6qH64xw7t
                        P496FXpDOMJr2LqesgGUORhRvbW4wE7k4OnF5foHwtp/S7Aw90V00u55T+FUTOA5
                        mjeu8u0KQhthM4xHVV9lm9+5WN+Vx6bT1hctQ2gtaQKOODjwWMOaXGhLoOcsslHz
                        1GwV43pwns6HRkn56njH1vc86iow6XnbpFuC+n74/Mq7TRZdxV4YApBd/XfcdP1h
                        680WFcmI+m7rhGhFW1Djsb0xbSgSqw==
                        =grA+
                        -----END PGP SIGNATURE-----
                        """.stripIndent(true));

        then:
        def ack = ackFor message
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS400" }
        ack.errorMessagesFor("Modify", "[aut-num] AS400") == [
                "Comments are not allowed on RIPE NCC managed Attribute \"org:\""]
    }

    def "modify, added member-of validation fail"() {
        def insertResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         TEST-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);

        then:
        updateResponse =~ /FAIL/
        updateResponse.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [AS-TESTSET]")
    }

    def "change mbrs-by-ref from as-set add warning if change causes aut-num member-of to fail"() {

        when:
        syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref:    OWNER-MNT    # matches AS101 mntner
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        syncUpdate new SyncUpdate(data: """\
            aut-num:        AS101
            as-name:        End-User-1
            member-of:      AS101:AS-ANOTHERSET             # added member-of set
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
            import-via:     AS6777 from AS5580 accept AS-ATRATO
            export-via:     AS6777 to AS5580 announce AS2
            remarks:        remarkable
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         OWNER-MNT
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        def replacedMntner = syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref:    TEST-MNT  # replaced OWNER-MNT, doing this will cause aut-num update to FAIL
            source:         TEST
            override: denis,override1
            """.stripIndent(true))
        then:
        replacedMntner =~ /Modify SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        replacedMntner.contains("***Warning: Changing mbrs-by-ref:  may cause updates to [AS101] to fail, because\n" +
                "            the member-of: reference in [AS101] is no longer protected")
    }

    def "change mbrs-by-ref from as-set do not add warning if mbrs-by-ref is ANY"() {

        when:
        syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref:    OWNER-MNT    # matches AS101 mntner
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        syncUpdate new SyncUpdate(data: """\
            aut-num:        AS101
            as-name:        End-User-1
            member-of:      AS101:AS-ANOTHERSET             # added member-of set
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
            import-via:     AS6777 from AS5580 accept AS-ATRATO
            export-via:     AS6777 to AS5580 announce AS2
            remarks:        remarkable
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         OWNER-MNT
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        def replacedMntner = syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref:    ANY
            source:         TEST
            override: denis,override1
            """.stripIndent(true))
        then:
        replacedMntner =~ /Modify SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        !replacedMntner.contains("***Warning: Changing mbrs-by-ref:  may cause updates to [AS101] to fail, because\n" +
                "            the member-of: reference in [AS101] is no longer protected")
    }

    def "change mbrs-by-ref from as-set do not add warning if member-of valid"() {

        when:
        syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref: OWNER-MNT    # matches AS1 mntner
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        syncUpdate new SyncUpdate(data: """\
            aut-num:        AS101
            as-name:        End-User-1
            member-of:      AS101:AS-ANOTHERSET             # added member-of set
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
            import-via:     AS6777 from AS5580 accept AS-ATRATO
            export-via:     AS6777 to AS5580 announce AS2
            remarks:        remarkable
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         OWNER-MNT
            source:         TEST
            override: denis,override1
            """.stripIndent(true))

        def replacedMntner = syncUpdate new SyncUpdate(data: """\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref:    OWNER-MNT
            mbrs-by-ref:    TEST-MNT
            source:         TEST
            override: denis,override1
            """.stripIndent(true))
        then:
        replacedMntner =~ /Modify SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        !replacedMntner.contains("***Warning: Changing mbrs-by-ref:  may cause updates to [AS101] to fail, because\n" +
                "            the member-of: reference in [AS101] is no longer protected")
    }

    def "modify, added member-of value does not exist"() {
        def insertResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-BLAGUE
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         TEST-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);

        then:
        updateResponse =~ /FAIL/
        updateResponse =~ /Unknown object referenced AS-BLAGUE/
    }

    def "modify, added member-of validation succeeds"() {
        def insertResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate("""
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT);

        then:
        updateResponse =~ /SUCCESS/
    }

    def "create, syntax errors"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS102
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        import:         from AS1 accept
                        export:         ato AS1 announce 192.0.0.1
                        default:        to AS1
                        mp-import:      afi ipv6.unicast from AS1 accept ANY
                        mp-export:      afi ipv6.unicast to AS1 announce AS2
                        import-via:     AS6777 from AS5580 accept AS-ATRATO
                        export-via:     to AS5580 announce AS2
                        mp-default:     to AS1
                        remarks:        remarkable
                        org:            ORG-NCC1-RIPE
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     OWNER-MNT
                        mnt-by:         _OWNER-MNT-MNT-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /FAIL/
        response =~ /Syntax error in from AS1 accept/
        response =~ /Syntax error in ato AS1 announce 192.0.0.1/
        response =~ /Syntax error in _OWNER-MNT-MNT-MNT/
        response =~ /Syntax error in to AS5580 announce AS2/
    }

    // autnum status tests

    def "create aut-num object, generate OTHER status"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS100
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        override:       denis,override1
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /SUCCESS/

        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        autnum =~ /status:         OTHER/
    }

    def "create aut-num object, generate ASSIGNED status"() {
        when:
        def response =  syncUpdate("""
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum =~ /status:         ASSIGNED/
    }

    def "create aut-num object, generate LEGACY status"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS103
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS103")
        autnum =~ /status:         LEGACY/
    }

    def "create aut-num object, user maintainer, replace incorrect status"() {
        when:
        def response = syncUpdate("""
                        aut-num:        AS100
                        as-name:        End-User
                        status:         LEGACY
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        override:       denis,override1
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        autnum =~ /status:         OTHER/
    }

    def "create aut-num object, rs maintainer, replace incorrect status"() {
        when:
        def response =  syncUpdate("""
                        aut-num:        AS102
                        as-name:        RS-2
                        status:         LEGACY
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /SUCCESS/
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum =~ /status:         ASSIGNED/
    }

    def "update aut-num object, rs maintainer, status cannot be removed, remark can be removed"() {

        syncUpdate("""
                aut-num:        AS102
                as-name:        RS-2
                descr:          description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                status:         ASSIGNED
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        when:
        def update = syncUpdate("""
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS102/
        update =~ /Warning: "status:" attribute cannot be removed/

        when:
        def currentDateTime = getTimeUtcString()
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")

        then:
        autnum.equals(RpslObject.parse(
                String.format(  "aut-num:        AS102\n" +
                                "as-name:        RS-2\n" +
                                "descr:          description\n" +
                                "admin-c:        AP1-TEST\n" +
                                "tech-c:         AP1-TEST\n" +
                                "status:         ASSIGNED\n" +
                                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                                "created:        %s\n" +
                                "last-modified:  %s\n" +
                                "source:         TEST", currentDateTime, currentDateTime)))
    }

    def "update autnum object, user maintainer, status cannot be removed"() {
        when:
        def currentDateTime = getTimeUtcString()
        def create = syncUpdate("""
                        aut-num:        AS104
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        override:       denis,override1
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS104/

        then:
        def createdAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104")
        createdAutnum.equals(RpslObject.parse(String.format(
                        "aut-num:        AS104\n" +
                        "as-name:        End-User\n" +
                        "descr:          description\n" +
                        "admin-c:        AP1-TEST\n" +
                        "tech-c:         AP1-TEST\n" +
                        "status:         OTHER\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

        when:
        def update = syncUpdate("""
                        aut-num:        AS104
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        remarks
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS104/
        update =~ /\*\*\*Warning: "status:" attribute cannot be removed/

        then:
        def updatedAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104")
        updatedAutnum.equals(RpslObject.parse(String.format(
                "aut-num:        AS104\n" +
                "as-name:        End-User\n" +
                "descr:          description\n" +
                "admin-c:        AP1-TEST\n" +
                "tech-c:         AP1-TEST\n" +
                "remarks:        remarks\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))
    }

    def "create aut-num object, user maintainer, replace invalid status"() {
        when:
        def currentDateTime = getTimeUtcString()
        def response = syncUpdate("""
                        aut-num:        AS104
                        as-name:        End-User
                        status:         INVALID
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        override:       denis,override1
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        response =~ /SUCCESS/

        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104")
        autnum.equals(RpslObject.parse(String.format(
                        "aut-num:        AS104\n" +
                        "as-name:        End-User\n" +
                        "descr:          description\n" +
                        "admin-c:        AP1-TEST\n" +
                        "tech-c:         AP1-TEST\n" +
                        "status:         OTHER\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

    }

    def "create aut-num object, user maintainer, duplicate status"() {

        when:
        def currentDateTime = getTimeUtcString()
        def response = syncUpdate("""
                        aut-num:        AS104
                        as-name:        End-User
                        status:         OTHER
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OWNER-MNT
                        source:         TEST
                        override:       denis,override1
                        """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /SUCCESS/

        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104")
        autnum.equals(RpslObject.parse(String.format(
                        "aut-num:        AS104\n" +
                        "as-name:        End-User\n" +
                        "status:         OTHER\n" +
                        "descr:          description\n" +
                        "admin-c:        AP1-TEST\n" +
                        "tech-c:         AP1-TEST\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

    }

    // sponsoring org

    def "create autnum without sponsoring-org, with referenced ORG orgtype OTHER, end-mnt"() {
        given:
        databaseHelper.addObject("""\
            mntner: RIPE-NCC-END-MNT
            mnt-by: RIPE-NCC-END-MNT
            source: TEST
            """.stripIndent(true))


        databaseHelper.addObject("""\
            organisation:    ORG-OTO1-TEST
            org-type:        other
            org-name:        Other Test org
            address:         RIPE NCC
            e-mail:          dbtest@ripe.net
            ref-nfy:         dbtest-org@ripe.net
            mnt-by:          OWNER-MNT
            mnt-ref:          OWNER-MNT
            source:  TEST
            """.stripIndent(true))

        when:
        def create = syncUpdate("""
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        create =~ /Create FAILED: \[aut-num\] AS400/
        create =~ /Error:   This resource object must be created with a sponsoring-org attribute/
    }

    def "create autnum with sponsoring-org, with referenced ORG orgtype OTHER, end-mnt"() {
        given:
        databaseHelper.addObject("""\
            mntner: RIPE-NCC-END-MNT
            mnt-by: RIPE-NCC-END-MNT
            auth: SSO person@net.net
            source: TEST
            """.stripIndent(true))

        databaseHelper.addObject("""\
            organisation:    ORG-OTO1-TEST
            org-type:        other
            org-name:        Other Test org
            address:         RIPE NCC
            e-mail:          dbtest@ripe.net
            ref-nfy:         dbtest-org@ripe.net
            abuse-c:         AH1-TEST
            mnt-by:          OWNER-MNT
            mnt-ref:          OWNER-MNT
            source:  TEST
            """.stripIndent(true))

        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum without sponsoring-org, with referenced ORG orgtype LIR, end-mnt"() {
        given:
        databaseHelper.addObject("""\
            mntner: RIPE-NCC-END-MNT
            mnt-by: RIPE-NCC-END-MNT
            auth: SSO person@net.net
            source: TEST
            """.stripIndent(true))

        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum without sponsoring-org, with referenced ORG orgtype OTHER, not end-mnt"() {
        given:
        databaseHelper.addObject("""\
            mntner: RIPE-NCC-END-MNT
            mnt-by: RIPE-NCC-END-MNT
            source: TEST
            """.stripIndent(true))

        databaseHelper.addObject("""\
            organisation:    ORG-OTO1-TEST
            org-type:        other
            org-name:        Other Test org
            address:         RIPE NCC
            abuse-c:         AH1-TEST
            e-mail:          dbtest@ripe.net
            ref-nfy:         dbtest-org@ripe.net
            mnt-by:          OWNER-MNT
            mnt-ref:          OWNER-MNT
            source:  TEST
            """.stripIndent(true))

        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum with sponsoring-org, no RS mntner"() {
        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        create =~ /Error:   The "sponsoring-org" attribute can only be added by the RIPE NCC/
    }

    def "create autnum with sponsoring-org succeeds"() {
        when:
        def update = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "modify autnum add sponsoring-org, no RS mntner"() {
        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED/

        when:
        def update = syncUpdate("""
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Error:   The "sponsoring-org" attribute can only be added by the RIPE NCC/
    }

    def "modify autnum add sponsoring-org succeeds"() {
        when:
        def create = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         ASSIGNED
                member-of:      AS-TESTSET
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        create =~ /Create SUCCEEDED/

        when:
        def update = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         ASSIGNED
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS400/
    }

    def "modify autnum change other attribute than sponsoring-org, no RS mntner"() {
        given:
        databaseHelper.addObject("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          other description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          changed description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS400/
    }

    def "modify autnum remove sponsoring-org, no RS mntner"() {
        given:
        databaseHelper.addObject("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          other description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          changed description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Modify FAILED: \[aut-num\] AS400/
        update =~ /Error:   The "sponsoring-org" attribute can only be removed by the RIPE NCC/
        queryObject("-rBG AS400", "sponsoring-org", "ORG-NCC1-RIPE")
    }

    def "modify autnum without status in db with same object adds status"() {
        given:
        databaseHelper.addObject("""\
                aut-num:        AS401
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate("""\
                aut-num:        AS401
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS401/
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS401")
        autnum =~ "status:         OTHER"
    }

    def "delete autnum with sponsoring-org"() {
        when:
        databaseHelper.addObject("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                """.stripIndent(true))
        then:
        queryObject("AS400", "aut-num", "AS400")

        when:
        def delete = syncUpdate("""\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                source:         TEST
                delete:         no reason
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        delete =~ /Delete SUCCEEDED: \[aut-num\] AS400/
    }

    def "create aut-num, legacy maintainer reference cannot be added by enduser maintainer"() {
        when:
        def response = syncUpdate("""\
                aut-num:        AS103
                as-name:        End-User
                status:         LEGACY
                descr:          description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         OWNER-MNT
                mnt-by:         RIPE-NCC-LEGACY-MNT
                source:         TEST
                """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
      then:
        response =~ /Create FAILED: \[aut-num\] AS103/
        response =~ /\*\*\*Error:   You cannot add or remove a RIPE NCC maintainer/
    }

    def "modify aut-num, legacy maintainer reference cannot be added by enduser maintainer"() {
        syncUpdate("""\
            aut-num:        AS103
            as-name:        End-User
            status:         LEGACY
            descr:          description
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        when:
        def response = syncUpdate("""\
            aut-num:        AS103
            as-name:        End-User
            status:         LEGACY
            descr:          description
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            mnt-by:         OWNER-MNT
            mnt-by:         RIPE-NCC-LEGACY-MNT
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /Modify FAILED: \[aut-num\] AS103/
        response =~ /\*\*\*Error:   You cannot add or remove a RIPE NCC maintainer/
    }

    def "warn mnt-lower deprecated for aut-num create"() {
        when:
        def response = syncUpdate("""\
            aut-num:        AS102
            as-name:        End-User-1
            descr:          description
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-lower:      OWNER-MNT
            mnt-by:         OWNER-MNT
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /Create SUCCEEDED: \[aut-num] AS102/
        response =~ /Deprecated attribute "mnt-lower". This attribute has been removed./
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        !autnum.containsAttribute(AttributeType.MNT_LOWER)
    }

    def "warn mnt-lower deprecated for aut-num update"() {
        when:
        def response = syncUpdate("""\
                    aut-num:        AS101
                    as-name:        End-User-1
                    descr:          description
                    import:         from AS1 accept ANY
                    export:         to AS1 announce AS2
                    mp-import:      afi ipv6.unicast from AS1 accept ANY
                    import-via:     AS6777 from AS5580 accept AS-ATRATO
                    export-via:     AS6777 to AS5580 announce AS2
                    remarks:        remarkable
                    org:            ORG-NCC1-RIPE
                    admin-c:        AP1-TEST
                    tech-c:         AP1-TEST
                    notify:         noreply@ripe.net
                    mnt-lower:      OWNER-MNT
                    mnt-by:         OWNER-MNT
                    source:         TEST
                    """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /Modify SUCCEEDED: \[aut-num] AS101/
        response =~ /Deprecated attribute "mnt-lower". This attribute has been removed./
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS101")
        !autnum.containsAttribute(AttributeType.MNT_LOWER)
    }

    def "delete aut-num with mnt-lower"() {
        when:
        def response = syncUpdate("""\
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
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-lower:      OWNER-MNT
            mnt-by:         OWNER-MNT
            source:         TEST
            delete:         reason
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)

        then:
        response =~ /Delete SUCCEEDED: \[aut-num] AS101/
        !response.contains("Deprecated attribute \"mnt-lower\". This attribute has been removed.")
    }

    //@Ignore
    def "replace mbrs-by-ref from as-set causes aut-num member-of to fail"() {
        when:
        def createSet = syncUpdate("""\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref: OWNER-MNT    # matches AS1 mntner
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        createSet =~ /Create SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        when:
        def memberOfSet = syncUpdate("""\
            aut-num:        AS101
            as-name:        End-User-1
            member-of:      AS101:AS-ANOTHERSET             # added member-of set
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
            import-via:     AS6777 from AS5580 accept AS-ATRATO
            export-via:     AS6777 to AS5580 announce AS2
            remarks:        remarkable
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         OWNER-MNT
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
            memberOfSet =~ /Modify SUCCEEDED: \[aut-num] AS101/
        when:
        def replacedMntner = syncUpdate("""\
            as-set:         AS101:AS-ANOTHERSET
            descr:          Test Set
            members:        AS101
            tech-c:         AP1-TEST
            tech-c:         AP1-TEST
            admin-c:        AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         TEST-MNT
            mbrs-by-ref: TEST-MNT  # replaced OWNER-MNT, doing this will cause aut-num update to FAIL
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
        replacedMntner =~ /Modify SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        when:
        def modifyAutnum = syncUpdate("""\
            aut-num:        AS101
            as-name:        End-User-1
            member-of:      AS101:AS-ANOTHERSET              # no longer authenticated
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
            import-via:     AS6777 from AS5580 accept AS-ATRATO
            export-via:     AS6777 to AS5580 announce AS2
            remarks:        updated                         # was remarkable
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            notify:         noreply@ripe.net
            mnt-by:         OWNER-MNT
            source:         TEST
            """.stripIndent(true),null, false, getApiKeyDummy().BASIC_AUTH_PERSON_ANY_MNT)
        then:
            modifyAutnum =~ /FAIL/
            modifyAutnum.contains(
                    "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                            "            referenced set [AS101:AS-ANOTHERSET]")
    }


}
