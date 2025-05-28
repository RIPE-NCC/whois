package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class AutNumIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT"      : """\
            mntner: UPD-MNT
            descr: description
            admin-c: AP1-TEST
            mnt-by: UPD-MNT
            upd-to: noreply@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            """,
                "OTHER-MNT"    : """\
            mntner: OTHER-MNT
            descr: description
            admin-c: AP1-TEST
            mnt-by: OTHER-MNT
            upd-to: noreply@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            """,
                "PWR-MNT"      : """\
            mntner:  RIPE-NCC-HM-MNT
            descr:   description
            admin-c: AP1-TEST
            mnt-by:  RIPE-NCC-HM-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$tnG/zrDw\$nps8tg76q4jgg5zg5o6os. # hm
            source:  TEST
            """,
                "LEGACY-MNT"  : """\
            mntner:  RIPE-NCC-LEGACY-MNT
            descr:   description
            admin-c: AP1-TEST
            mnt-by:  RIPE-NCC-LEGACY-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$gTs46J2Z\$.iohp.IUDhNAMj7evxnFS1   # legacy
            source:  TEST
            """,
                "AP1-PN"       : """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: AP1-TEST
            mnt-by:  UPD-MNT
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
            mnt-by:       UPD-MNT
            source:       TEST
            """,
                "ORG-NCC1-RIPE": """\
            organisation: ORG-NCC1-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-ref:      UPD-MNT
            mnt-by:       UPD-MNT
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
            mnt-lower:      UPD-MNT
            mnt-by:         UPD-MNT
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
            mnt-by:       UPD-MNT
            mbrs-by-ref:  UPD-MNT
            source:       TEST
            """,
                "AS-BLOCK1"    : """\
            as-block:       AS100 - AS300
            descr:          RIPE NCC ASN block
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            mnt-by:         UPD-MNT
            mbrs-by-ref:    UPD-MNT
            source:         TEST
            """,
                "AS-BLOCK2"    : """\
            as-block:       AS300 - AS500
            descr:          RIPE NCC ASN block
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            mnt-by:         UPD-MNT
            mnt-lower:      OTHER-MNT
            source:         TEST
            """
        ]
    }

    def "delete aut-num object"() {
        def update = new SyncUpdate(data: """\
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
                        mnt-lower:      UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password:       update
                        delete:         reason
                        """.stripIndent(true))
        when:
        def response = syncUpdate update

        then:
        response =~ /SUCCESS/
    }

    def "create aut-num object"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        mnt-lower:      UPD-MNT
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /SUCCESS/
    }

    def "create aut-num object with no parent AS-BLOCK object"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        mnt-lower:      UPD-MNT
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password:       update
                        """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response.contains("***Error:   No parent as-block found for AS1000")
    }

    def "create, member-of reference not found"() {
        given:
        def update = new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        member-of:      AS-NONEXISTING
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))
        when:
        def response = syncUpdate(update);

        then:
        response =~ /FAIL/
        response =~ /Error:   Unknown object referenced AS-NONEXISTING/
    }

    def "create, authentication against asblock's mnt-lower fail"() {
        given:
        def update = new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))
        when:
        def response = syncUpdate(update);

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[as-block\] AS300 - AS500 failed/
        response =~ /using "mnt-lower:"/
        response =~ /not authenticated by: OTHER-MNT/
    }

    def "create, authentication against asblock's mnt-lower succeed"() {
        given:
        def update = new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        password: emptypassword
                        """.stripIndent(true))
        when:
        def response = syncUpdate(update);

        then:
        response =~ /SUCCESS/
    }

    def "create, add comment in managed attribute fails"() {
        when:
        def message = send new Message(
                subject: "",
                body: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        org:            ORG-NCC1-RIPE #test comment
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true));

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
        given:
        def update = new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: alban
                        """.stripIndent(true))
        when:
        def response = syncUpdate(update);

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[as-block\] AS300 - AS500 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: OTHER-MNT/

        response =~ /Error:   Authorisation for \[aut-num\] AS400 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: UPD-MNT/
    }

    def "create, authentication against asblock's mnt-by succeeds, fails on local mnt-by"() {
        given:
        def update = new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        """.stripIndent(true))
        when:
        def response = syncUpdate(update);

        then:
        response =~ /FAIL/
        response =~ /Error:   Authorisation for \[aut-num\] AS400 failed/
        response =~ /using "mnt-by:"/
        response =~ /not authenticated by: UPD-MNT/
    }

    def "modify, only description changed"() {
        given:
        def insertResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));

        then:
        updateResponse =~ /SUCCESS/
    }

    def "modify, add comment in managed attribute fails"() {
        given:
        def insertResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def message = send new Message(
                        subject: "",
                        body: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        org:            ORG-NCC1-RIPE #test comment
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        notify:         noreply@ripe.net
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
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
        given:
        def insertResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OTHER-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));

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
            mnt-by:         OTHER-MNT
            mbrs-by-ref:    UPD-MNT    # matches AS101 mntner
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
            mnt-by:         UPD-MNT
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
            mnt-by:         OTHER-MNT
            mbrs-by-ref:    OTHER-MNT  # replaced UPD-MNT, doing this will cause aut-num update to FAIL
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
            mnt-by:         OTHER-MNT
            mbrs-by-ref:    UPD-MNT    # matches AS101 mntner
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
            mnt-by:         UPD-MNT
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
            mnt-by:         OTHER-MNT
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
            mnt-by:         OTHER-MNT
            mbrs-by-ref: UPD-MNT    # matches AS1 mntner
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
            mnt-by:         UPD-MNT
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
            mnt-by:         OTHER-MNT
            mbrs-by-ref:    UPD-MNT
            mbrs-by-ref:    OTHER-MNT
            source:         TEST
            override: denis,override1
            """.stripIndent(true))
        then:
        replacedMntner =~ /Modify SUCCEEDED: \[as-set] AS101:AS-ANOTHERSET/
        !replacedMntner.contains("***Warning: Changing mbrs-by-ref:  may cause updates to [AS101] to fail, because\n" +
                "            the member-of: reference in [AS101] is no longer protected")
    }

    def "modify, added member-of value does not exist"() {
        given:
        def insertResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        member-of:      AS-BLAGUE
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         OTHER-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));

        then:
        updateResponse =~ /FAIL/
        updateResponse =~ /Unknown object referenced AS-BLAGUE/
    }

    def "modify, added member-of validation succeeds"() {
        given:
        def insertResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));
        expect:
        insertResponse =~ /SUCCESS/

        when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
                        aut-num:        AS400
                        as-name:        End-User-2
                        status:         OTHER
                        member-of:      AS-TESTSET
                        descr:          other description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent(true)));

        then:
        updateResponse =~ /SUCCESS/
    }

    def "create, syntax errors"() {
        given:
        def update = new SyncUpdate(data: """\
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
                        mnt-routes:     UPD-MNT
                        mnt-by:         _UPD-MNT-MNT-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /FAIL/
        response =~ /Syntax error in from AS1 accept/
        response =~ /Syntax error in ato AS1 announce 192.0.0.1/
        response =~ /Syntax error in _UPD-MNT-MNT-MNT/
        response =~ /Syntax error in to AS5580 announce AS2/
    }

    // autnum status tests

    def "create aut-num object, generate OTHER status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        override:       denis,override1
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        autnum =~ /status:         OTHER/
    }

    def "create aut-num object, generate ASSIGNED status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum =~ /status:         ASSIGNED/
    }

    def "create aut-num object, generate LEGACY status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS103
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS103")
        autnum =~ /status:         LEGACY/
    }

    def "create aut-num object, user maintainer, replace incorrect status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        status:         LEGACY
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        override:       denis,override1
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        autnum =~ /status:         OTHER/
    }

    def "create aut-num object, rs maintainer, replace incorrect status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        status:         LEGACY
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /SUCCESS/
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum =~ /status:         ASSIGNED/
    }

    def "update aut-num object, rs maintainer, status cannot be removed, remark can be removed"() {
        given:
        syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        status:         ASSIGNED
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent(true))
        when:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent(true))
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
        def create = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS104
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        override:       denis,override1
                        password: update
                        """.stripIndent(true))
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
                        "mnt-by:         UPD-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

        when:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS104
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        remarks
                        mnt-by:         UPD-MNT
                        source:         TEST
                        password: update
                        """.stripIndent(true))
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
                "mnt-by:         UPD-MNT\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))
    }

    def "create aut-num object, user maintainer, replace invalid status"() {
        when:
        def currentDateTime = getTimeUtcString()
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS104
                        as-name:        End-User
                        status:         INVALID
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        override:       denis,override1
                        password: update
                        """.stripIndent(true))
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
                        "mnt-by:         UPD-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

    }

    def "create aut-num object, user maintainer, duplicate status"() {
        when:
        def currentDateTime = getTimeUtcString()
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS104
                        as-name:        End-User
                        status:         OTHER
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        source:         TEST
                        override:       denis,override1
                        password: update
                        """.stripIndent(true))

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
                        "mnt-by:         UPD-MNT\n" +
                        "created:        %s\n" +
                        "last-modified:  %s\n" +
                        "source:         TEST-NONAUTH", currentDateTime, currentDateTime)))

    }

    // sponsoring org

    def "create autnum without sponsoring-org, with referenced ORG orgtype OTHER, end-mnt"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$UfJlEnmZ\$2.e732Z780Y9Y1GB2rOtg/ # ende\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "organisation:    ORG-OTO1-TEST\n" +
                "org-type:        other\n" +
                "org-name:        Other Test org\n" +
                "address:         RIPE NCC\n" +
                "e-mail:          dbtest@ripe.net\n" +
                "ref-nfy:         dbtest-org@ripe.net\n" +
                "mnt-by:          upd-mnt\n" +
                "mnt-ref:          upd-mnt\n" +
                "source:  TEST")
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent(true)))

        then:
        create =~ /Create FAILED: \[aut-num\] AS400/
        create =~ /Error:   This resource object must be created with a sponsoring-org attribute/
    }

    def "create autnum with sponsoring-org, with referenced ORG orgtype OTHER, end-mnt"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$UfJlEnmZ\$2.e732Z780Y9Y1GB2rOtg/ # ende\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "organisation:    ORG-OTO1-TEST\n" +
                "org-type:        other\n" +
                "org-name:        Other Test org\n" +
                "address:         RIPE NCC\n" +
                "e-mail:          dbtest@ripe.net\n" +
                "ref-nfy:         dbtest-org@ripe.net\n" +
                "abuse-c:         AH1-TEST\n" +
                "mnt-by:          upd-mnt\n" +
                "mnt-ref:          upd-mnt\n" +
                "source:  TEST")
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent(true)))
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum without sponsoring-org, with referenced ORG orgtype LIR, end-mnt"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$UfJlEnmZ\$2.e732Z780Y9Y1GB2rOtg/ # ende\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent(true)))
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum without sponsoring-org, with referenced ORG orgtype OTHER, not end-mnt"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$UfJlEnmZ\$2.e732Z780Y9Y1GB2rOtg/ # ende\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "organisation:    ORG-OTO1-TEST\n" +
                "org-type:        other\n" +
                "org-name:        Other Test org\n" +
                "address:         RIPE NCC\n" +
                "abuse-c:         AH1-TEST\n" +
                "e-mail:          dbtest@ripe.net\n" +
                "ref-nfy:         dbtest-org@ripe.net\n" +
                "mnt-by:          upd-mnt\n" +
                "mnt-ref:          upd-mnt\n" +
                "source:  TEST")
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                descr:          other description
                org:            ORG-OTO1-TEST
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                password: emptypassword
                password: hm
                password: update
                """.stripIndent(true)))
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "create autnum with sponsoring-org, no RS mntner"() {
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent(true)))

        then:
        create =~ /Error:   The "sponsoring-org" attribute can only be added by the RIPE NCC/
    }

    def "create autnum with sponsoring-org succeeds"() {
        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         UPD-MNT
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent(true)))

        then:
        update =~ /Create SUCCEEDED: \[aut-num\] AS400/
    }

    def "modify autnum add sponsoring-org, no RS mntner"() {
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent(true)))
        then:
        create =~ /Create SUCCEEDED/

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent(true)))

        then:
        update =~ /Error:   The "sponsoring-org" attribute can only be added by the RIPE NCC/
    }

    def "modify autnum add sponsoring-org succeeds"() {
        when:
        def create = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                status:         ASSIGNED
                member-of:      AS-TESTSET
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent(true)))
        then:
        create =~ /Create SUCCEEDED/

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                status:         ASSIGNED
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent(true)))

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
                mnt-by:         UPD-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          changed description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: update
                """.stripIndent(true)))

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
                mnt-by:         UPD-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                descr:          changed description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: update
                """.stripIndent(true)))

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
                mnt-by:         UPD-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true))

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS401
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: update
                """.stripIndent(true)))

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS401/
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS401")
        autnum =~ "status:         OTHER"
    }

    def "delete autnum with sponsoring-org"() {
        when:
        databaseHelper.addObject("" +
                "aut-num:        AS400\n" +
                "as-name:        End-User-2\n" +
                "status:         OTHER\n" +
                "member-of:      AS-TESTSET\n" +
                "sponsoring-org: ORG-NCC1-RIPE\n" +
                "descr:          other description\n" +
                "admin-c:        AP1-TEST\n" +
                "tech-c:         AP1-TEST\n" +
                "mnt-by:         UPD-MNT\n" +
                "source:         TEST")
        then:
        queryObject("AS400", "aut-num", "AS400")

        when:
        def delete = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                status:         OTHER
                member-of:      AS-TESTSET
                sponsoring-org: ORG-NCC1-RIPE
                descr:          other description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                delete:         no reason
                password: emptypassword
                password: update
                """.stripIndent(true)))

        then:
        delete =~ /Delete SUCCEEDED: \[aut-num\] AS400/
    }

    def "create aut-num, legacy maintainer reference cannot be added by enduser maintainer"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num:        AS103
                as-name:        End-User
                status:         LEGACY
                descr:          description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                mnt-by:         RIPE-NCC-LEGACY-MNT
                source:         TEST
                password: update
                """.stripIndent(true))
      then:
        response =~ /Create FAILED: \[aut-num\] AS103/
        response =~ /\*\*\*Error:   You cannot add or remove a RIPE NCC maintainer/
    }

    def "modify aut-num, legacy maintainer reference cannot be added by enduser maintainer"() {
      given:
        syncUpdate new SyncUpdate(data: """\
                aut-num:        AS103
                as-name:        End-User
                status:         LEGACY
                descr:          description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                source:         TEST
                password: update
                """.stripIndent(true))
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num:        AS103
                as-name:        End-User
                status:         LEGACY
                descr:          description
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                mnt-by:         RIPE-NCC-LEGACY-MNT
                source:         TEST
                password: update
                """.stripIndent(true))
      then:
        response =~ /Modify FAILED: \[aut-num\] AS103/
        response =~ /\*\*\*Error:   You cannot add or remove a RIPE NCC maintainer/
    }

    def "warn mnt-lower deprecated for aut-num create"() {
        when:
            def response = syncUpdate new SyncUpdate(data: """\
                aut-num:        AS102
                as-name:        End-User-1
                descr:          description
                org:            ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                notify:         noreply@ripe.net
                mnt-lower:      UPD-MNT
                mnt-by:         UPD-MNT
                source:         TEST
                password: update
                """.stripIndent(true))

        then:
            response =~ /Create SUCCEEDED: \[aut-num] AS102/
            response =~ /Deprecated attribute "mnt-lower". This attribute has been removed./
            def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
            !autnum.containsAttribute(AttributeType.MNT_LOWER)
    }

    def "warn mnt-lower deprecated for aut-num update"() {
        when:
            def response = syncUpdate new SyncUpdate(data: """\
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
                            mnt-lower:      UPD-MNT
                            mnt-by:         UPD-MNT
                            source:         TEST
                            password:       update
                            """.stripIndent(true))

        then:
            response =~ /Modify SUCCEEDED: \[aut-num] AS101/
            response =~ /Deprecated attribute "mnt-lower". This attribute has been removed./
            def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS101")
            !autnum.containsAttribute(AttributeType.MNT_LOWER)
    }

    def "delete aut-num with mnt-lower"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
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
            mnt-lower:      UPD-MNT
            mnt-by:         UPD-MNT
            source:         TEST
            password:       update
            delete:         reason
            """.stripIndent(true))

        then:
        response =~ /Delete SUCCEEDED: \[aut-num] AS101/
        !response.contains("Deprecated attribute \"mnt-lower\". This attribute has been removed.")
    }

}
