package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.ObjectType
import spec.domain.SyncUpdate

// TODO: [AH] We check successful errors with response =~ /SUCCESS/; this is very error-prone and misleading, should be fixed everywhere
// TODO: [AH] Use $ in regexp to increase efficiency, e.g. matching for /not authenticated by: TEST-MNT/ happily matches for 'not authenticated by: TEST-MNT2' !!!

@org.junit.experimental.categories.Category(IntegrationTest.class)
class RouteIntegrationSpec extends BaseWhoisSourceSpec {

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
                "TEST-MNT2": """\
                    mntner: TEST-MNT2
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT2
                    referral-by: TEST-MNT2
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$5aMDZg3w\$zL59TnpAszf6Ft.zs148X0 # update2
                    changed: dbtest@ripe.net 20120707
                    source: TEST
                """,
                "TEST-MNT3": """\
                    mntner: TEST-MNT3
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT3
                    referral-by: TEST-MNT3
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$dNvmHMUm\$5A3Q0AlFopJ662JB2FY/w. # update3
                    changed: dbtest@ripe.net 20120707
                    source: TEST
                """,
                "ROUTES-MNT": """\
                    mntner: ROUTES-MNT
                    admin-c: TEST-PN
                    mnt-by: ROUTES-MNT
                    referral-by: ROUTES-MNT
                    upd-to: dbtest@ripe.net
                    auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    source: TEST
                """,
                "LOWER-MNT": """\
                    mntner: LOWER-MNT
                    admin-c: TEST-PN
                    mnt-by: LOWER-MNT
                    referral-by: LOWER-MNT
                    upd-to: dbtest@ripe.net
                    auth: MD5-PW \$1\$bdQftquX\$S10GZRVq2SNG9SWmMHliI. # otherpassword
                    source: TEST
                """,
                "ORG-NCC1-RIPE": """\
                    organisation: ORG-NCC1-RIPE
                    org-name:     Ripe NCC organisation
                    org-type:     LIR
                    address:      Singel 258
                    e-mail:       bitbucket@ripe.net
                    mnt-ref:      TEST-MNT
                    mnt-by:       TEST-MNT
                    changed:      admin@test.com 20120505
                    source:       TEST
                """,
                "AS123": """\
                    aut-num:         AS123
                    as-name:         SNS-AS
                    descr:           "SATELIT SERVIS" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "AS456": """\
                    aut-num:         AS456
                    as-name:         BRB-AS
                    descr:           "bogus" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "AS12726": """\
                    aut-num:         AS12726
                    as-name:         BRB-AS
                    descr:           "bogus" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "TEST_ROUTE": """\
                    route:           193.254.30.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "OTHER_ROUTE": """\
                    route:           180.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "LESS_SPECIFIC_ROUTE": """\
                    route:           180.0/8
                    descr:           Less specific other route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                """,
                "INETNUM_MNT_ROUTES": """\
                    inetnum: 194.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    mnt-routes: ROUTES-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "INETNUM_MNT_BY": """\
                    inetnum: 195.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "INETNUM_MNT_LOWER": """\
                    inetnum: 196.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    mnt-lower: LOWER-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "INETNUM_MNT_BY_MNT3": """\
                    inetnum: 197.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: NL
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT3
                    changed: ripe@test.net 20120601
                    source: TEST
                """,
                "INETNUM_MNT_ROUTES_RANGES_VALID": """\
                    inetnum: 198.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    mnt-routes: ROUTES-MNT {198.0.0.0/32}
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "ROUTE_SET": """\
                    route-set: RS-BLA123
                    descr: route set description
                    tech-c: TEST-PN
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    mbrs-by-ref: LOWER-MNT
                    changed: ripe@test.net 20120202
                    source: TEST
                """
        ]
    }

    def "delete route"() {
      when:
        def delete = new SyncUpdate(data: """\
                route: 193.254.30.0/24
                descr: Test route
                origin: AS12726
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                delete: reason
                """.stripIndent())

      then:
        def response = syncUpdate delete

      then:
        response =~ /SUCCESS/
        response =~ /Delete SUCCEEDED: \[route\] 193.254.30.0\/24AS12726/
    }

    def "create route, delete referenced autnum object, delete route object"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0/24
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /SUCCESS/

      when:
        def deleteAutnum = new SyncUpdate(data: """\
                            aut-num: AS12726
                            as-name: BRB-AS
                            descr: "bogus" Ltd
                            org: ORG-NCC1-RIPE
                            mnt-by: TEST-MNT
                            mnt-routes: ROUTES-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            delete: reason
                            password: update
                    """.stripIndent())

      then:
        def responseAutnum = syncUpdate deleteAutnum

      then:
        responseAutnum =~ /SUCCESS/
        println "Delete Autnum response: ${responseAutnum}"

      when:
        def deleteRoute = new SyncUpdate(data: """\
                    route: 195.0/24
                    descr: Test route
                    origin: AS12726
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20091015
                    source: TEST
                    delete: reason
                    password: update
                    """.stripIndent())

      then:
        def responseRoute = syncUpdate deleteRoute

      then:
        responseRoute =~ /SUCCESS/
        println "Delete Route response: ${responseRoute}"

    }

    def "create route, delete referenced autnum object, modify route object"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0/24
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /SUCCESS/

      when:
        def deleteAutnum = new SyncUpdate(data: """\
                            aut-num: AS12726
                            as-name: BRB-AS
                            descr: "bogus" Ltd
                            org: ORG-NCC1-RIPE
                            mnt-by: TEST-MNT
                            mnt-routes: ROUTES-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            delete: reason
                            password: update
                    """.stripIndent())

      then:
        def responseAutnum = syncUpdate deleteAutnum

      then:
        responseAutnum =~ /SUCCESS/
        println "Delete Autnum response: ${responseAutnum}"

      when:
        def modifyRoute = new SyncUpdate(data: """\
                    route: 195.0/24
                    descr: Test route Modified
                    origin: AS12726
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20091015
                    source: TEST
                    password:update
                    """.stripIndent())

      then:
        def responseRoute = syncUpdate modifyRoute

      then:
        responseRoute =~ /ERROR/
        responseRoute =~ /Unknown object referenced AS12726/
        println "Modify Route response: ${responseRoute}"

    }

    def "modify route noop"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 193.254.30.0/24
                descr: Test route
                origin: AS12726
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /No operation: \[route\] 193.254.30.0\/24AS12726/
    }

    def "create route aut-num does not exist"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 212.166.64.0/19
                descr: Test route
                origin: AS99999
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /Create FAILED: \[route\] 212.166.64.0\/19AS99999/
        response =~ /Authorisation for \[route\] 212.166.64.0\/19AS99999 failed/
        response =~ /using "origin:"/
    }

    def "create route aut-num with mnt-by authentication"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 195.0.0.0\/24AS123/
    }

    def "create route aut-num with mnt-routes authentication"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 195.0.0.0\/24AS456/
    }

    def "create route ipaddress exact match mnt-routes authentication"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/24
                descr: Test route
                origin: AS123
                mnt-by: ROUTES-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress exact match mnt-by authentication"() {
      when:
        def create = new SyncUpdate(data: """\
                route:  193.254.30.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/

    }

    def "create route ipaddress exact match autnum auth passes, ip authentication fail"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/24
                descr: Test route
                origin: AS123
                mnt-by: ROUTES-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route\] 180.0.0.0\/24AS12726 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route ipaddress first less specific mnt-routes authentication route"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                            route:           180.0/16
                            descr:           Less specific other route
                            origin:          AS123
                            mnt-by:          TEST-MNT
                            mnt-routes:      ROUTES-MNT
                            changed:         ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/20
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific mnt-lower authentication route"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                            route:           180.0/16
                            descr:           Less specific other route
                            origin:          AS123
                            mnt-by:          TEST-MNT
                            mnt-lower:       LOWER-MNT
                            changed:         ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/20
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: otherpassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific mnt-by authentication route"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                            route:           180.0/16
                            descr:           Less specific other route
                            origin:          AS123
                            mnt-by:          TEST-MNT
                            changed:         ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insert =~ /SUCCESS/

      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/20
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific authentication fail route"() {
      given:
        def insert = syncUpdate(new SyncUpdate(data: """\
                            route:           180.0/16
                            descr:           Less specific other route
                            origin:          AS123
                            mnt-by:          TEST-MNT
                            changed:         ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insert =~ /SUCCESSFUL/

      when:
        def create = new SyncUpdate(data: """\
                route: 180.0/20
                descr: less specific
                origin: AS123
                mnt-by: ROUTES-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAILED:/
        response =~ /Authorisation for \[route\] 180.0.0.0\/16AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/
        response =~ /Authorisation for \[aut-num\] AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/
    }

    def "create route ipaddress exact match mnt-routes authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 194.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress exact match mnt-by authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress exact match authentication fail inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: otherpassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route\] 195.0.0.0\/24AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[aut-num\] AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[inetnum\] 195.0.0.0 - 195.0.0.255 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

    }

    def "create route ipaddress first less specific mnt-routes authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 194.0/28
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific mnt-lower authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 196.0/28
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: otherpassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific mnt-by authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/28
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
    }

    def "create route ipaddress first less specific authentication fail inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 194.0/28
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: wrong
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route\] 194.0.0.0\/28AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[aut-num\] AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[inetnum\] 194.0.0.0 - 194.0.0.255 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route address prefix range outside route - no authenticating maintainer"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/24
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /Create PENDING:/

        response =~ /Authorisation for \[inetnum\] 198.0.0.0 - 198.0.0.255 failed
            using "mnt-routes:"
            no valid maintainer found/
    }

    def "create route valid address prefix range"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/32
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 198.0.0.0\/32AS456/
    }

    def "create route holes contained withing prefix value"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                holes: 195.0.0.0/32
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 195.0.0.0\/24AS456/
    }

    def "create route holes not contained withing prefix value"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                holes: 196.0.0.0/32
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Error:   196.0.0.0\/32 is outside the range of this object/
    }

    def "create route pingable contained withing prefix value"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                pingable: 195.0/32
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 195.0.0.0\/24AS456/
    }

    def "create route pingable not contained withing prefix value"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                pingable: 196.0/32
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Error:   196.0\/32 is outside the range of this object/
    }

    def "create route member-of exists in route-set"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/32
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                member-of: RS-BLA123
                changed: ripe@test.net 20091015
                source: TEST
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route\] 198.0.0.0\/32AS456/
    }

    def "create route member-of does not exist in route-set"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/32
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                member-of: RS-BLA123
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }

    def "modify route succeeds"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                route: 193.254.30.0/24
                descr: other route
                origin: AS12726
                mnt-by: TEST-MNT
                export-comps: {194.55.167.0/24}
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[route\] 193.254.30.0\/24AS12726/
    }

    def "modify route exact same object"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route:           193.254.30.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    changed:         ripe@test.net 20091015
                    source:          TEST
                    password:        update
                """.stripIndent()))

      then:
        response =~ /No operation: \[route\] 193.254.30.0\/24AS12726/
    }

    def "modify route fail on route-set reference"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route:           193.254.30.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    member-of:       RS-BLA123
                    changed:         ripe@test.net 20091015
                    source:          TEST
                    password:        update
                """.stripIndent()))

      then:
        response =~ /Modify FAILED: \[route\] 193.254.30.0\/24AS12726/
        response.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }

    def "modify route change maintainers"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route: 180.0/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: ROUTES-MNT
                        changed: ripe@test.net 20091015
                        source: TEST
                        password: update
                        password: emptypassword
                        """.stripIndent())
      then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[route\] 180.0.0.0\/24AS12726/
    }

    def "modify route fail on pingables "() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route: 180.0/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: TEST-MNT
                        pingable: 181.0.0.0/32
                        changed: ripe@test.net 20091015
                        source: TEST
                        password: update
                        """.stripIndent())
      then:
        response =~ /FAIL/
        response =~ /Error:   181.0.0.0\/32 is outside the range of this object/
    }

    def "modify route fail on holes"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route: 180.0/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: TEST-MNT
                        holes: 181.0.0.0/32
                        changed: ripe@test.net 20091015
                        source: TEST
                        password: update
                        """.stripIndent())
      then:
        response =~ /FAIL/
        response =~ /Error:   181.0.0.0\/32 is outside the range of this object/
    }

    def "modify route "() {
      given:
        def create = syncUpdate new SyncUpdate(data: """\
                        route: 194.0/24
                        descr: Test route
                        origin: AS123
                        mnt-by: TEST-MNT
                        changed: ripe@test.net 20091015
                        source: TEST
                        password: update
                        password: emptypassword
                        """.stripIndent())
      expect:
        create =~ /SUCCESS/

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route: 194.0/24
                        descr: Test route
                        origin: AS123
                        mnt-by: LOWER-MNT
                        changed: ripe@test.net 20091015
                        source: TEST
                        password: update
                        """.stripIndent())
      then:
        response =~ /Modify SUCCEEDED: \[route\] 194.0.0.0\/24AS123/
    }

    def "create route prefix not allowed"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0/7
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /FAIL/
        insertRoute =~ /Automatic creation of route objects of this size in not allowed,
            please contact lir-help@ripe.net for further information./
    }

    def "create route prefix allowed"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0/8
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute != ~/Automatic creation of route objects of this size in not allowed,
            please contact lir-help@ripe.net for further information./
    }

    def "create route, without pending authentication"() {
      given:
        def response = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            password: update3
                            """.stripIndent()))
      expect:
        response =~ /Create SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
    }

    def "create route, pending inetnum, pending autnum, with mnt-by authentication"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                route: 212.166.64.0/19
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                changed: ripe@test.net 20091015
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /Create FAILED: \[route\] 212.166.64.0\/19AS456\n/
        response =~ /Authorisation for \[aut-num\] AS456 failed\n\s+using "mnt-routes:"\n\s+not authenticated by: ROUTES-MNT\n/

        System.err.println(response)

        notificationFor("dbtest@ripe.net").authFailed("CREATE", "route", "212.166.64.0/19")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").isEmpty()
    }

    def "create route, with inetnum, with autnum, missing mnt-by authentication"() {
      given:
        def pendInetnum = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update3
                            """.stripIndent()))
      expect:
        pendInetnum =~ /Create FAILED: \[route\] 197.0.0.0\/24AS123\n/
        pendInetnum =~ /not authenticated by: TEST-MNT2\n/

        notificationFor("dbtest@ripe.net").authFailed("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").isEmpty()
    }

    def "create route, pending inetnum, with autnum, with mnt-by authentication"() {
      given:
        def pendInetnum = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      expect:
        pendInetnum =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        pendInetnum =~ /\*\*\*Info:\s+Authorisation for \[inetnum\] 197.0.0.0 - 197.0.0.255 failed\n/
        pendInetnum =~ /not authenticated by: TEST-MNT3\n/
        pendInetnum =~ /\*\*\*Warning:\s+This update has only passed one of the two required hierarchical/
        pendInetnum =~ /\*\*\*Info:\s+The route object 197.0.0.0\/24AS123 will be saved for one week/

        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1
    }

    def "create route, with inetnum, pending autnum, with mnt-by authentication"() {
      given:
        def pendInetnum = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      expect:
        pendInetnum =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        pendInetnum =~ /\*\*\*Info:\s+Authorisation for \[aut-num\] AS123 failed\n/
        pendInetnum =~ /not authenticated by: TEST-MNT\n/
        pendInetnum =~ /\*\*\*Warning:\s+This update has only passed one of the two required hierarchical/
        pendInetnum =~ /\*\*\*Info:\s+The route object 197.0.0.0\/24AS123 will be saved for one week/

        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1
    }

    def "create route pending auth, 2nd update identical to first update"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def identical = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        identical =~ /Noop PENDING:\s+\[route\] 197.0.0.0\/24AS123\n/
        identical =~ /\*\*\*Info:\s+Authorisation for \[inetnum\] 197.0.0.0 - 197.0.0.255 failed\n/
        identical != ~/\*\*\*Warning:\s+This update has only passed one of the two/
        identical != ~/\*\*\*Info:\s+The route object 197.0.0.0\/24AS123 will be saved/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1
    }

    def "create route pending auth, 2nd update fails mnt-by auth"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithIpAuth =~ /\*\*\*Info:    This update concludes a pending update on route 197.0.0.0\/24AS123/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 0
    }

    def "create route pending auth, 2nd update passes only mnt-by auth"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create FAILED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithIpAuth =~ /\*\*\*Error:   Authorisation for \[aut-num\] AS123 failed\n/
        inetnumWithIpAuth =~ /\*\*\*Error:   Authorisation for \[inetnum\] 197.0.0.0 - 197.0.0.255 failed\n/
        notificationFor("dbtest@ripe.net").authFailed("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1
    }

    def "create route pending auth, 2nd update is not identical to first update"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Other description
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 2
    }

    def "create route pending auth, aut-num deleted after authenticated"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def deleteAutNum = syncUpdate(new SyncUpdate(data: """\
                            aut-num:         AS123
                            as-name:         SNS-AS
                            descr:           "SATELIT SERVIS" Ltd
                            org:             ORG-NCC1-RIPE
                            mnt-by:          TEST-MNT
                            changed:         ripe@test.net 20091015
                            source:          TEST
                            password:        update
                            delete:          test
                            """.stripIndent()))
      then:
        deleteAutNum =~ /Delete SUCCEEDED: \[aut-num\] AS123\n/
        noMoreMessages()
        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create FAILED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithIpAuth =~ /\*\*\*Error:   Unknown object referenced AS123\n/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1
    }

    def "create route pending auth, aut-num updated after authenticated"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def modifyAutNum = syncUpdate(new SyncUpdate(data: """\
                            aut-num:         AS123
                            as-name:         SNS-AS
                            admin-c:         TEST-PN
                            tech-c:          TEST-PN
                            descr:           "SATELIT SERVIS" Ltd
                            org:             ORG-NCC1-RIPE
                            mnt-by:          ROUTES-MNT
                            changed:         ripe@test.net 20091015
                            source:          TEST
                            password:        update
                            """.stripIndent()))
      then:
        modifyAutNum =~ /Modify SUCCEEDED: \[aut-num\] AS123\n/
        noMoreMessages()
        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithIpAuth =~ /\*\*\*Info:    This update concludes a pending update on route 197.0.0.0\/24AS123\n/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 0
    }

    def "create route pending auth, inetnum deleted after authenticated"() {
      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def deleteInetnum = syncUpdate(new SyncUpdate(data: """\
                            inetnum: 197.0/24
                            netname: RIPE-NCC
                            descr: description
                            country: NL
                            admin-c: TEST-PN
                            tech-c: TEST-PN
                            status: SUB-ALLOCATED PA
                            mnt-by: TEST-MNT3
                            changed: ripe@test.net 20120601
                            source: TEST
                            password: update3
                            delete: test
                            """.stripIndent()))
      then:
        deleteInetnum =~ /Delete SUCCEEDED: \[inetnum\] 197.0.0.0 - 197.0.0.255\n/
        noMoreMessages()
        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithAutnumAuth =~ /\*\*\*Info:    This update concludes a pending update on route 197.0.0.0\/24AS123\n/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 0
    }

    def "create route pending auth, 1st and 2nd update passes successfully"() {
      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithIpAuth =~ /\*\*\*Info:    This update concludes a pending update on route 197.0.0.0\/24AS123\n/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 0
    }

    def "create route pending auth, 1st and 2nd update passes successfully in different order"() {
      when:
        def inetnumWithIpAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update2
                            password: update3
                            """.stripIndent()))
      then:
        inetnumWithIpAuth =~ /Create PENDING: \[route\] 197.0.0.0\/24AS123\n/
        notificationFor("dbtest@ripe.net").pendingAuth("CREATE", "route", "197.0.0.0/24")
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 1

      when:
        def inetnumWithAutnumAuth = syncUpdate(new SyncUpdate(data: """\
                            route: 197.0.0.0/24
                            descr: Test route
                            origin: AS123
                            mnt-by: TEST-MNT2
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update
                            password: update2
                            """.stripIndent()))
      then:
        inetnumWithAutnumAuth =~ /SUCCEEDED: \[route\] 197.0.0.0\/24AS123\n/
        inetnumWithAutnumAuth =~ /\*\*\*Info:    This update concludes a pending update on route 197.0.0.0\/24AS123\n/
        noMoreMessages()

        pendingUpdates(ObjectType.ROUTE, "197.0.0.0/24AS123").size() == 0
    }

    def "update with multiple route objects pending auth"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0.0.0/24
                            descr: Test route
                            origin: AS456
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST

                            route: 196.0.0.0/24
                            descr: Test route
                            origin: AS456
                            mnt-by: TEST-MNT
                            changed: ripe@test.net 20091015
                            source: TEST

                            password: update
                            password: otherpassword

                            """.stripIndent()))
      then:
        response =~ /Create PENDING: \[route\] 195.0.0.0\/24AS456\n/
        response =~ /Create PENDING: \[route\] 196.0.0.0\/24AS456\n/

        def notification = notificationFor("dbtest@ripe.net")
        notification.pendingAuth("CREATE", "route", "195.0.0.0/24")
        notification.pendingAuth("CREATE", "route", "196.0.0.0/24")

        noMoreMessages()
    }

}
