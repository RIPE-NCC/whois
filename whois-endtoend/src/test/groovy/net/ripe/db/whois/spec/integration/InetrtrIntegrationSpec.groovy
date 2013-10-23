package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class InetrtrIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TEST-MNT": """\
                    mntner: TEST-MNT
                    descr: description
                    admin-c: AP1-TEST
                    mnt-by: TEST-MNT
                    upd-to: noreply@ripe.net
                    auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    changed: dbtest@ripe.net 20120707
                    source: TEST
                    """,
                "REF-MNT": """\
                    mntner: REF-MNT
                    descr: description
                    admin-c: AP1-TEST
                    mnt-by: REF-MNT
                    upd-to: noreply@ripe.net
                    auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
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
                    mnt-by:  TEST-MNT
                    changed: dbtest@ripe.net 20120101
                    source:  TEST
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
                    admin-c:        AP1-TEST
                    tech-c:         AP1-TEST
                    notify:         noreply@ripe.net
                    mnt-lower:      TEST-MNT
                    mnt-routes:     TEST-MNT
                    mnt-by:         TEST-MNT
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
                    mnt-by:       TEST-MNT
                    mbrs-by-ref:  TEST-MNT
                    changed:      noreply@ripe.net 20120101
                    source:       TEST
                    """,
                "RTR_SET": """\
                    rtr-set:         rtrs-ripetest
                    descr:           some description
                    tech-c:          AP1-TEST
                    admin-c:         AP1-TEST
                    mnt-by:          TEST-MNT
                    mbrs-by-ref:     REF-MNT
                    changed:         ripe@ripe.net 20121127
                    source: TEST
                """,
                "RTR_SET_NO_MBRSBYREF": """\
                    rtr-set:         rtrs-no-mbrsbyref
                    descr:           some description
                    tech-c:          AP1-TEST
                    admin-c:         AP1-TEST
                    mnt-by:          TEST-MNT
                    changed:         ripe@ripe.net 20121127
                    source: TEST
                """,
                "ORG1": """\
                    organisation: ORG-TOL1-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     OTHER
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       TEST-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                    """
        ]
    }

    def "create inetrtr"() {
      when:
        def data = """\
            inet-rtr:        test.ripe.net
            descr:           description
            alias:           test-101.ripe.net
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            interface:       2001:FF6::2 masklen 64
            peer:            BGP4 192.168.10.1 asno(AS1)
            mp-peer:         MPBGP 2001:FF6::1 asno(AS1)
            member-of:       rtrs-ripetest
            remarks:         inet-rtr object
            org:             ORG-TOL1-TEST
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            notify:          noreply-test-irt@ripe.net
            mnt-by:          REF-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
            password:        update
        """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))
      then:
        createResponse =~ /Create SUCCEEDED: \[inet-rtr\] test.ripe.net/
    }

    def "create, with member-of"() {
      when:
        def data = """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            mnt-by:          REF-MNT
            member-of:       rtrs-ripetest
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """

        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      then:
        createResponse =~ /SUCCESS/
    }

    def "create, with member-of that does not contain needed mnt-by's"() {
      when:
        def data = """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            mnt-by:          REF-MNT
            member-of:       rtrs-no-mbrsbyref
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """

        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      then:
        createResponse =~ /FAIL/
        createResponse.contains("***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                "            referenced set [rtrs-no-mbrsbyref]")
    }

    def "modify"() {
      given:

        def createResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/

      when:

        def updateResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          REF-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      then:
        updateResponse =~ /SUCCESS/
    }

    def "modify, member-of"() {
      given:
        def createResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/

      when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            mnt-by:          REF-MNT
            member-of:       rtrs-ripetest
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      then:
        updateResponse =~ /SUCCESS/
    }

    def "modify, member-of not in rtr-set's mbrs-by-ref"() {
      given:
        def createResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/

      when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          TEST-MNT
            mnt-by:          REF-MNT
            member-of:       rtrs-no-mbrsbyref
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
        """.stripIndent()))

      then:
        updateResponse =~ /FAIL/
        updateResponse.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                "            referenced set [rtrs-no-mbrsbyref]")
    }

    def "delete inet-rtr object"() {
      given:
        def inetrtr = """\
                inet-rtr: test
                descr: description
                local-as: AS101
                ifaddr: 10.2.3.4 masklen 32
                admin-c: AP1-TEST
                tech-c: AP1-TEST
                mnt-by: TEST-MNT
                changed: noreply@ripe.net 20120101
                source: TEST
            """.stripIndent()
        def insertResponse = syncUpdate(new SyncUpdate(data: inetrtr + "password: emptypassword"))

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def deleteResponse = syncUpdate(new SyncUpdate(data: inetrtr + "password: emptypassword\ndelete: reason"))

      then:
        deleteResponse =~ /Delete SUCCEEDED: \[inet-rtr\] test/
    }

    def "create, asnumber reference does not exist"() {
      when:
        def data = """\
            inet-rtr:        test.ripe.net
            descr:           description
            local-as:        AS302
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          REF-MNT
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        emptypassword
            password:        update
        """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      then:
        createResponse =~ /Create SUCCEEDED: \[inet-rtr\] test.ripe.net/
    }
}
