package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class Inet6numIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    changed: ripe@test.net
                    source: TEST
                """,
                "TEST-MNT": """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                """,
                "PWR-MNT": """\
                    mntner:  RIPE-NCC-HM-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-HM-MNT
                    referral-by: RIPE-NCC-HM-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
                "OTHER-MNT": """\
                    mntner: OTHER-MNT
                    admin-c: TEST-PN
                    mnt-by: OTHER-MNT
                    referral-by: OTHER-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$bdQftquX\$S10GZRVq2SNG9SWmMHliI. # otherpassword
                    source: TEST
                """,
                "END-MNT": """\
                    mntner:  RIPE-NCC-END-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-END-MNT
                    referral-by: RIPE-NCC-END-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
                "IRT": """\
                    irt: irt-IRT1
                    address: Street 1
                    e-mail: test@ripe.net
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    mnt-by: TEST-MNT
                    changed: test@ripe.net 20120505
                    source: TEST
                """,
                "INET6ROOT": """\
                    inet6num: 0::/0
                    netname: IANA-BLK
                    descr: The whole IPv6 address space
                    country: EU
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED-BY-RIR
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "ORG1": """\
                    organisation: ORG-TOL1-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     RIR
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "ORGLIR": """\
                    organisation: ORG-TOL2-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     LIR
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "INET6WOEND": """\
                   inet6num: 2221::/64
                   netname: RIPE-NCC
                   descr: some descr
                   country: DK
                   admin-c: TEST-PN
                   tech-c: TEST-PN
                   status: ASSIGNED ANYCAST
                   mnt-by: TEST-MNT
                   changed: ripe@test.net 20120505
                   source: TEST
                """,
                "INVALIDPKEY": """\
                   inet6num: A000:0011:0Fe::12C/64
                   netname: RIPE-NCC
                   descr: some descr
                   country: DK
                   org: ORG-TOL2-TEST
                   admin-c:  TEST-PN
                   tech-c: TEST-PN
                   status: ASSIGNED ANYCAST
                   mnt-by: TEST-MNT
                   changed: ripe@test.net 20120505
                   source: TEST
                """
        ]
    }

    def "delete inet6num"() {
      given:
        def inet6num = """\
                inet6num: 2001::/48
                netname: RIPE-NCC
                descr: some description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: ASSIGNED
                mnt-by: TEST-MNT
                changed: org@ripe.net 20120505
                source: TEST
                password: emptypassword
                password: update
            """

        def object = new SyncUpdate(data: inet6num.stripIndent())
        def insert = syncUpdate object

      expect:
        insert =~ /SUCCESS/

      when:
        def delete = new SyncUpdate(data: inet6num.stripIndent() + "delete:yes\npassword:update")
        def response = syncUpdate delete

      then:
        response =~ /Delete SUCCEEDED: \[inet6num\] 2001::\/48/
    }

    def "delete nonexisting inet6num"() {
      given:
        def inet6num = """\
                inet6num: 2a00:1f78::fffe/48
                netname: RIPE-NCC
                descr: some description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: SUB-ALLOCATED PA
                mnt-by: TEST-MNT
                changed: org@ripe.net 20120505
                source: TEST
            """

        def delete = new SyncUpdate(data: inet6num.stripIndent() + "delete:yes\npassword:update")

      when:
        def response = syncUpdate delete

      then:
        response =~ /Delete FAILED/
        response =~ /Object \[inet6num\] 2a00:1f78::fffe\/48 does not exist in the database/
    }

    def "modify, unrecognised status"() {
      given:
        def inet6num = """\
                    inet6num: 2a00:1f78::fffe/48
                    netname: RIPE-NCC
                    descr: some description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED
                    mnt-by: TEST-MNT
                    changed: org@ripe.net 20120505
                    source: TEST
                    password: update
                    password: emptypassword
                """
        def insert = syncUpdate(new SyncUpdate(data: inet6num.stripIndent()));

      expect:
        insert =~ /SUCCESS/

      when:
        def modify = syncUpdate(new SyncUpdate(data: """\
                    inet6num: 2a00:1f78::fffe/48
                    netname: RIPE-NCC
                    descr: some description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    changed: org@ripe.net 20120505
                    source: TEST
                    password: update
                """.stripIndent()))

      then:
        modify =~ /FAIL/
        modify =~ /Syntax error in SUB-ALLOCATED PA/
    }

    def "modify, new and old inet6num identical"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c:  TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: DK
                                        admin-c:  TEST-PN
                                        tech-c: TEST-PN
                                        status: ASSIGNED
                                        mnt-by: TEST-MNT
                                        changed: ripe@test.net 20120505
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /SUCCESS/
        update =~ /No operation: \[inet6num\] 2001::\/64/
    }

    def "modify, status changed"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: DK
                                        admin-c: TEST-PN
                                        status: ASSIGNED ANYCAST
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ "Error:   status value cannot be changed, you must delete and re-create the\n            object"
    }

    def "modify, status ALLOCATED-BY-RIR requires org attribute"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       org: ORG-TOL1-TEST
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ALLOCATED-BY-RIR
                                       mnt-by: RIPE-NCC-HM-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: DK
                                        admin-c: TEST-PN
                                        status: ALLOCATED-BY-RIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   Missing required "org:" attribute/
    }

    def "modify, status ASSIGNED does not require org attribute"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /SUCCESS/
    }

    def "modify, status ASSIGNED demands referenced org to have org-type LIR or OTHER if org is present"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: DK
                                        org: ORG-TOL1-TEST
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   Referenced organisation has wrong "org-type"/
        update =~ /Allowed values are \[LIR, OTHER\]/
    }

    def "modify, status AGGREGATED-BY-LIR cant change assignment-size"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: AGGREGATED-BY-LIR
                                       assignment-size: 68
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: DK
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        assignment-size: 76
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   "assignment-size:" value cannot be changed/
    }

    def "modify, status ASSIGNED ANYCAST needs endusermntner auth for changing org, 1st descr, and remove mnt-lower"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2221::/64
                                        netname: RIPE-NCC
                                        descr: other descr
                                        country: DK
                                        admin-c: TEST-PN
                                        tech-c: TEST-PN
                                        status: ASSIGNED ANYCAST
                                        changed: ripe@test.net 20120505
                                        org: ORG-TOL2-TEST
                                        mnt-by: TEST-MNT
                                        mnt-lower: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
//        update =~ /Error:   Changing first "DESCR:" value requires administrative authorisation/
        update =~ /Error:   Changing "mnt-lower:" value requires administrative authorisation/
    }

    def "modify, mnt-routes with value outside given address prefix"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        mnt-routes: TEST-MNT {2002::/64,2001::/24}
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   2002::\/64 is outside the range of this object/
        update =~ /Error:   2001::\/24 is outside the range of this object/
        !(update =~ /Unknown object referenced TEST-MNT \{2002::\/64,2001::\/24\}/)
    }


    def "create, status requires org attribute"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ALLOCATED-BY-RIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   Missing required "org:" attribute/
    }

    def "create, status requires rs auth"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num:  2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        org: ORG-TOL1-TEST
                                        status: ALLOCATED-BY-RIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ "Error:   Status ALLOCATED-BY-RIR can only be created by the database\n" +
                "            administrator"
    }

    def "create, status ASSIGNED needs parent status AGGREGATED-BY-LIR"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2221::/128
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   inet6num parent has incorrect status: ASSIGNED ANYCAST/
    }

    def "create, correct parent-child hierarchy "() {
      given:
        def grandparent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        grandparent =~ /SUCCESS/

      when:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/32
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-RIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))

      and:
        parent =~ /SUCCESS/

      then:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        assignment-size: 68
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      then:
        update =~ /SUCCESS/
    }


    def "create, incorrect parent-child hierarchy "() {
      given:
        def grandparent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        grandparent =~ /SUCCESS/

      when:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        assignment-size: 68
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))

      then:
        parent =~ /SUCCESS/

      when:

        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/68
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        assignment-size: 72
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      then:
        update =~ /FAIL/
        update =~ /Error:   Only two levels of hierarchy allowed with status AGGREGATED-BY-LIR/
    }

    def "create, parent auth by mnt-lower only"() {
      given:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-lower: RIPE-NCC-HM-MNT
                                        mnt-by: TEST-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        parent =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: OTHER-MNT
                                        source: TEST
                                        password: emptypassword
                                        password: otherpassword
                                    """.stripIndent()))

      then:
        update =~ /SUCCESS/
    }

    def "create incorrect assignment size"() {
      given:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-lower: RIPE-NCC-HM-MNT
                                        mnt-by: TEST-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        parent =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/32
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: OTHER-MNT
                                        source: TEST
                                        password: emptypassword
                                        password: otherpassword
                                    """.stripIndent()))

      then:
        update.contains("***Error:   Prefix length for 2001::/32 must be 64")
    }

    def "create assignment size missing"() {
      given:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-lower: RIPE-NCC-HM-MNT
                                        mnt-by: TEST-MNT
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        parent.contains("Missing required \"assignment-size\" attribute")
    }

    def "create status ASSIGNED with assignment size"() {
      given:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-lower: RIPE-NCC-HM-MNT
                                        mnt-by: TEST-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: update
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        parent =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/64
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        assignment-size: 32
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: OTHER-MNT
                                        source: TEST
                                        password: emptypassword
                                        password: otherpassword
                                    """.stripIndent()))

      then:
        update.contains("" +
                "assignment-size: 32\n" +
                "***Error:   \"assignment-size:\" attribute only allowed with status\n" +
                "            AGGREGATED-BY-LIR")
    }

    def "create, parent auth by mnt-lower fail"() {
      given:
        def parent = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/8
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: AGGREGATED-BY-LIR
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-lower: RIPE-NCC-HM-MNT
                                        mnt-by: RIPE-NCC-HM-MNT
                                        assignment-size: 64
                                        source: TEST
                                        password: emptypassword
                                    """.stripIndent()))
      expect:
        parent =~ /SUCCESS/

      when:
        def update = syncUpdate(new SyncUpdate(data: """\
                                        inet6num: 2001::/32
                                        netname: RIPE-NCC
                                        descr: some descr
                                        country: ES
                                        admin-c: TEST-PN
                                        status: ASSIGNED
                                        tech-c: TEST-PN
                                        changed: ripe@test.net 20120505
                                        mnt-by: OTHER-MNT
                                        source: TEST
                                        password: otherpassword
                                    """.stripIndent()))

      then:
        update =~ /FAIL/
        update =~ /Authorisation for parent \[inet6num\] 2000::\/8 failed/
        update =~ /using "mnt-lower:/
        update =~ /not authenticated by: RIPE-NCC-HM-MNT/
    }

    def "create, auth by mnt-irt succeeds"() {
      when:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ALLOCATED-BY-RIR
                                       mnt-by: RIPE-NCC-HM-MNT
                                       mnt-irt: irt-IRT1
                                       org: ORG-TOL1-TEST
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      then:
        insert =~ /SUCCESS/
    }

    def "create, auth by mnt-irt fails"() {
      when:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ALLOCATED-BY-RIR
                                       mnt-by: TEST-MNT
                                       mnt-irt: irt-IRT1
                                       org: ORG-TOL1-TEST
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                    """.stripIndent()))
      then:
        insert =~ /FAIL/
        insert =~ /Authorisation for \[inet6num\] 2001::\/64 failed/
        insert =~ /using "mnt-irt:"/
        insert =~ /not authenticated by: irt-IRT1/
    }

    def "create, auth by mnt-irt fails with override"() {
      when:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2001::/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c: TEST-PN
                                       tech-c: TEST-PN
                                       status: ALLOCATED-BY-RIR
                                       mnt-by: TEST-MNT
                                       mnt-irt: irt-IRT1
                                       org: ORG-TOL1-TEST
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: wrong_password
                                       override: denis,override1
                                    """.stripIndent()))
      then:
        insert.contains("Create SUCCEEDED: [inet6num] 2001::/64")
    }

    def "primary key is normalised during update"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: 2005:0011:0Fe::12C/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c:  TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED
                                       mnt-by: TEST-MNT
                                       changed: ripe@test.net 20120505
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/
        insert =~ /Create SUCCEEDED: \[inet6num\] 2005:11:fe::\/64/
        insert =~ /Value 2005:0011:0Fe::12C\/64 converted to 2005:11:fe::\/64/
    }

    def "primary key of existing object is normalised during update"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                                       inet6num: A000:0011:fE:00::012c/64
                                       netname: RIPE-NCC
                                       descr: some descr
                                       country: DK
                                       admin-c:  TEST-PN
                                       tech-c: TEST-PN
                                       status: ASSIGNED ANYCAST
                                       mnt-by: TEST-MNT
                                       org: ORG-TOL2-TEST
                                       changed: ripe@test.net 20120506
                                       source: TEST
                                       password: update
                                       password: emptypassword
                                    """.stripIndent()))
      expect:
        insert =~ /SUCCESS/
        insert =~ /Modify SUCCEEDED: \[inet6num\] a000:11:fe::\/64/
        insert =~ /Value A000:0011:fE:00::012c\/64 converted to a000:11:fe::\/64/
    }
}
