package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class AutNumIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT"      : """\
            mntner: UPD-MNT
            descr: description
            admin-c: AP1-TEST
            mnt-by: UPD-MNT
            referral-by: UPD-MNT
            upd-to: noreply@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "OTHER-MNT"    : """\
            mntner: OTHER-MNT
            descr: description
            admin-c: AP1-TEST
            mnt-by: OTHER-MNT
            upd-to: noreply@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "PWR-MNT"      : """\
            mntner:  RIPE-NCC-HM-MNT
            descr:   description
            admin-c: AP1-TEST
            mnt-by:  RIPE-NCC-HM-MNT
            referral-by: RIPE-NCC-HM-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$tnG/zrDw\$nps8tg76q4jgg5zg5o6os. # hm
            changed: dbtest@ripe.net 20120707
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
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """,
                "ORG-NCC1-RIPE": """\
            organisation: ORG-NCC1-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-ref:      UPD-MNT
            mnt-by:       UPD-MNT
            changed:      noreply@ripe.net 20120505
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
            mnt-routes:     UPD-MNT
            mnt-by:         UPD-MNT
            changed:        noreply@ripe.net 20120101
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
            changed:      noreply@ripe.net 20120101
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
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password:       update
                        delete:         reason
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())

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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password:       update
                        """.stripIndent())

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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        password: emptypassword
                        """.stripIndent())
        when:
        def response = syncUpdate(update);

        then:
        response =~ /SUCCESS/
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: alban
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));

        then:
        updateResponse =~ /SUCCESS/
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));

        then:
        updateResponse =~ /FAIL/
        updateResponse.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [AS-TESTSET]")
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));

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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: emptypassword
                        password: update
                        """.stripIndent()));

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
                        mnt-lower:      _UPD-MNT-MNT-MNT
                        mnt-routes:     UPD-MNT
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())

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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
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
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
        then:
        response =~ /SUCCESS/
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum =~ /status:         ASSIGNED/
    }

    def "update aut-num object, rs maintainer, status cannot be removed"() {
        given:
        syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         ASSIGNED
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
        when:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS102/
        update =~ /Warning: "status:" attribute cannot be removed/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum.equals(RpslObject.parse("""\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         ASSIGNED
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }

    def "update autnum object, user maintainer, status cannot be removed"() {
        when:
        def create = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS100/
        then:
        def createdAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        createdAutnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
        when:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        remarks
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS100/
        update =~ /\*\*\*Warning: "status:" attribute cannot be removed/
        then:
        def updatedAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        updatedAutnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        remarks
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }


    def "update autnum object, user maintainer, moving remark is noop"() {
        when:
        def create = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS100/
        then:
        def createdAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        createdAutnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
        when:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        update =~ /No operation: \[aut-num\] AS100/
        update =~ /\*\*\*Warning: Submitted object identical to database object/
    }

    def "update autnum object, removing status and remarks is noop"() {
        when:
        def create = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        create =~ /Create SUCCEEDED: \[aut-num\] AS100/
        then:
        def update = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        update =~ /No operation: \[aut-num\] AS100/
        update =~ /\*\*\*Warning: Submitted object identical to database object/
        then:
        def updatedAutnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        updatedAutnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }

    def "create aut-num object, rs maintainer, generate ASSIGNED status, generate remark"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum.equals(RpslObject.parse("""\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         ASSIGNED
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }

    def "create aut-num object, rs maintainer, generate ASSIGNED status, user-specified remark is moved beside status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        remarks:        user remark
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: hm
                        password: update
                        """.stripIndent())
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS102")
        autnum.equals(RpslObject.parse("""\
                        aut-num:        AS102
                        as-name:        RS-2
                        descr:          description
                        remarks:        user remark
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         ASSIGNED
                        mnt-by:         RIPE-NCC-HM-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }

    def "create aut-num object, user maintainer, replace invalid status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        status:         INVALID
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        response =~ /\*\*\*Warning: Supplied attribute 'status' has been replaced with a generated value/
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        autnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
    }

    def "create aut-num object, user maintainer, duplicate status"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        aut-num:        AS100
                        as-name:        End-User
                        status:         OTHER
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password: update
                        """.stripIndent())
        then:
        response =~ /SUCCESS/
        then:
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS100")
        println autnum
        autnum.equals(RpslObject.parse("""\
                        aut-num:        AS100
                        as-name:        End-User
                        remarks:        For information on "status:" attribute read https://www.ripe.net/data-tools/db/faq/faq-status-values-legacy-resources
                        status:         OTHER
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        """.stripIndent()))
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
                "changed: denis@ripe.net 20121016\n" +
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent()))

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
                "mnt-by:          upd-mnt\n" +
                "mnt-ref:          upd-mnt\n" +
                "changed: denis@ripe.net 20121016\n" +
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent()))
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: ende
                password: emptypassword
                password: update
                """.stripIndent()))
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
                "e-mail:          dbtest@ripe.net\n" +
                "ref-nfy:         dbtest-org@ripe.net\n" +
                "mnt-by:          upd-mnt\n" +
                "mnt-ref:          upd-mnt\n" +
                "changed: denis@ripe.net 20121016\n" +
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: hm
                password: update
                """.stripIndent()))
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent()))

        then:
        create =~ /Error:   The sponsoring-org can only be added by the RIPE NCC/
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent()))

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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent()))
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                """.stripIndent()))

        then:
        update =~ /Error:   The sponsoring-org can only be added by the RIPE NCC/
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent()))
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: emptypassword
                password: update
                password: hm
                """.stripIndent()))

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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:       denis,override1
                """.stripIndent())

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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: update
                """.stripIndent()))

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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:       denis,override1
                """.stripIndent())

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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: update
                """.stripIndent()))

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS400/
        update =~ /Warning: The attribute 'sponsoring-org' can only be removed by RIPE NCC/
        queryObject("-rBG AS400", "sponsoring-org", "ORG-NCC1-RIPE")
    }

    def "modify autnum without status in db with same object adds status"() {
        given:
        databaseHelper.addObject("""\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:       denis,override1
                """.stripIndent())

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                aut-num:        AS400
                as-name:        End-User-2
                member-of:      AS-TESTSET
                descr:          description
                sponsoring-org: ORG-NCC1-RIPE
                admin-c:        AP1-TEST
                tech-c:         AP1-TEST
                mnt-by:         UPD-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                password: update
                """.stripIndent()))

        then:
        update =~ /Modify SUCCEEDED: \[aut-num\] AS400/
        def autnum = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS400")
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
                "changed:        noreply@ripe.net 20120101\n" +
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
                changed:        noreply@ripe.net 20120101
                source:         TEST
                delete:         no reason
                password: emptypassword
                password: update
                """.stripIndent()))

        then:
        delete =~ /Delete SUCCEEDED: \[aut-num\] AS400/
    }
}
