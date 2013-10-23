package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class AutNumIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
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
                "OTHER-MNT": """\
            mntner: OTHER-MNT
            descr: description
            admin-c: AP1-TEST
            mnt-by: OTHER-MNT
            upd-to: noreply@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "AP1-PN": """\
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
                "AUTNUM101": """\
            aut-num:        AS101
            as-name:        End-User-1
            descr:          description
            import:         from AS1 accept ANY
            export:         to AS1 announce AS2
            mp-import:      afi ipv6.unicast from AS1 accept ANY
            mp-export:      afi ipv6.unicast to AS1 announce AS2
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
                "AS-SET1": """\
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
                "AS-BLOCK1": """\
            as-block:       AS100 - AS300
            descr:          RIPE NCC ASN block
            org:            ORG-NCC1-RIPE
            admin-c:        AP1-TEST
            tech-c:         AP1-TEST
            mnt-by:         UPD-MNT
            mbrs-by-ref:    UPD-MNT
            source:         TEST
            """,
                "AS-BLOCK2": """\
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
        println response
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
    }
}
