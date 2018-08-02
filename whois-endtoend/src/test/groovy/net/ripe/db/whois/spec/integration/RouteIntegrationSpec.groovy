package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class RouteIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    source: TEST
                """,
                "TEST-MNT": """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                """,
                "TEST-MNT2": """\
                    mntner: TEST-MNT2
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT2
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$5aMDZg3w\$zL59TnpAszf6Ft.zs148X0 # update2
                    source: TEST
                """,
                "TEST-MNT3": """\
                    mntner: TEST-MNT3
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT3
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$dNvmHMUm\$5A3Q0AlFopJ662JB2FY/w. # update3
                    source: TEST
                """,
                "ROUTES-MNT": """\
                    mntner: ROUTES-MNT
                    admin-c: TEST-PN
                    mnt-by: ROUTES-MNT
                    upd-to: dbtest@ripe.net
                    auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    source: TEST
                """,
                "LOWER-MNT": """\
                    mntner: LOWER-MNT
                    admin-c: TEST-PN
                    mnt-by: LOWER-MNT
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
                    source:       TEST
                """,
                "AS103": """\
                    aut-num:         AS103
                    as-name:         TEST-AS
                    org:             ORG-NCC1-RIPE
                    notify:          notify@as103.net
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "AS123": """\
                    aut-num:         AS123
                    as-name:         SNS-AS
                    descr:           "SATELIT SERVIS" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "AS456": """\
                    aut-num:         AS456
                    as-name:         BRB-AS
                    descr:           "bogus" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "AS12726": """\
                    aut-num:         AS12726
                    as-name:         BRB-AS
                    descr:           "bogus" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "TEST_ROUTE": """\
                    route:           193.254.30.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "OTHER_ROUTE": """\
                    route:           180.0/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    source:          TEST
                """,
                "LESS_SPECIFIC_ROUTE": """\
                    route:           180.0/8
                    descr:           Less specific other route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
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
                    source: TEST
                """,
                "ROUTE_SET": """\
                    route-set: RS-BLA123
                    descr: route set description
                    tech-c: TEST-PN
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    mbrs-by-ref: LOWER-MNT
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
                            source: TEST
                            delete: reason
                            password: update
                    """.stripIndent())

      then:
        def responseAutnum = syncUpdate deleteAutnum

      then:
        responseAutnum =~ /SUCCESS/

      when:
        def deleteRoute = new SyncUpdate(data: """\
                    route: 195.0.0.0/24
                    descr: Test route
                    origin: AS12726
                    mnt-by: TEST-MNT
                    source: TEST
                    delete: reason
                    password: update
                    """.stripIndent())

      then:
        def responseRoute = syncUpdate deleteRoute

      then:
        responseRoute =~ /SUCCESS/
    }

    def "create route, delete referenced autnum object, modify route object"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 195.0/24
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
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
                            source: TEST
                            delete: reason
                            password: update
                    """.stripIndent())

      then:
        def responseAutnum = syncUpdate deleteAutnum

      then:
        responseAutnum =~ /SUCCESS/

      when:
        def modifyRoute = new SyncUpdate(data: """\
                    route: 195.0/24
                    descr: Test route Modified
                    origin: AS12726
                    mnt-by: TEST-MNT
                    source: TEST
                    password:update
                    """.stripIndent())

      then:
        def responseRoute = syncUpdate modifyRoute

      then:
        responseRoute =~ /SUCCESS/
    }

    def "modify route noop"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 193.254.30.0/24
                descr: Test route
                origin: AS12726
                mnt-by: TEST-MNT
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
                source: TEST
                password: update
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /Create FAILED: \[route\] 212.166.64.0\/19AS99999/
        response =~ /Authorisation for \[route\] 212.166.64.0\/19AS99999 failed/
        response =~ /using "route:"/
    }

    def "create route aut-num with mnt-by authentication"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 195.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
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
    }

    def "create route ipaddress exact match mnt-routes authentication inetnum"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 194.0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
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

        response =~ /Authorisation for \[inetnum\] 194.0.0.0 - 194.0.0.255 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route valid address prefix range"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/32
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
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
                route: 195.0.0.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                pingable: 195.0.0.1
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
                route: 195.0.0.0/24
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                pingable: 196.0.0.1
                source: TEST
                password: update
                password: otherpassword
                password: emptypassword
                """.stripIndent())

      then:
        def response = syncUpdate create

      then:
        response =~ /FAIL/
        response =~ /Error:   196.0.0.1 is outside the range of this object/
    }

    def "create route member-of exists in route-set"() {
      when:
        def create = new SyncUpdate(data: """\
                route: 198.0/32
                descr: other route
                origin: AS456
                mnt-by: LOWER-MNT
                member-of: RS-BLA123
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
                        route: 180.0.0.0/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: TEST-MNT
                        pingable: 181.0.0.0
                        source: TEST
                        password: update
                        """.stripIndent())
      then:
        response =~ /FAIL/
        response =~ /Error:   181.0.0.0 is outside the range of this object/
    }

    def "modify route fail on holes"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route: 180.0/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: TEST-MNT
                        holes: 181.0.0.0/32
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
                        source: TEST
                        password: update
                        """.stripIndent())
      then:
        response =~ /Modify SUCCEEDED: \[route\] 194.0.0.0\/24AS123/
    }

    def "create route prefix not allowed"() {
      given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route: 194.0/7
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
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
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute != ~/Automatic creation of route objects of this size in not allowed,
            please contact lir-help@ripe.net for further information./
    }

    def "route reclaim"() {
      given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "mntner: INC-MNT\n" +
                "mnt-by: INC-MNT\n" +
                "auth: MD5-PW \$1\$8o2h6J5S\$FU4b5YVbdGN8/xZoUIZis/\n" + //test
                "source: TEST")
        databaseHelper.addObject("" +
                "mntner: T-MNT\n" +
                "mnt-by: T-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "inetnum: 182.120.0.0 - 182.255.0.0\n" +
                "netname: netname c\n" +
                "mnt-by:  INC-MNT\n" +
                "mnt-by:  RIPE-NCC-END-MNT\n" +
                "source: TEST")
        databaseHelper.addObject("" +
                "route: 182.125.0.0/32\n" +
                "origin: AS132\n" +
                "mnt-by: T-MNT\n" +
                "source: TEST")

      when:
        def response = syncUpdate(new SyncUpdate(data: "" +
                "route: 182.125.0.0/32\n" +
                "origin: AS132\n" +
                "mnt-by: T-MNT\n" +
                "source: TEST\n" +
                "delete: reason\n" +
                "password: test"))

      then:
        response =~ "Delete SUCCEEDED: \\[route\\] 182.125.0.0/32AS132"
    }

    def "create route with partially missing primary key should return proper error"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route:
                            descr: Test route
                            origin: AS12726
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
        expect:
        insertRoute =~ /\nNumber of objects found:\s+0\n/
    }

    def "create route with non-existant origin aut-num"() {
        given:
        queryObjectNotFound("-r -T aut-num AS76", "aut-num", "AS76")
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route:  195.0.0.0/24
                            descr:  Test route
                            origin: AS76
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /SUCCESS/
    }

    def "create route and notify origin aut-num"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route:  195.0.0.0/24
                            descr:  Test route
                            origin: AS103
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /SUCCESS/

        def notif = notificationFor "notify@as103.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "route" && it.key == "195.0.0.0/24" }

        noMoreMessages()

      when:
        def updateRoute = syncUpdate(new SyncUpdate(data: """\
                            route:  195.0.0.0/24
                            descr:  Test route
                            origin: AS103
                            remarks: updated
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
        then:
          updateRoute =~ /SUCCESS/

          noMoreMessages()
    }

    def "create route without notify origin aut-num"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route:  195.0.0.0/24
                            descr:  Test route
                            origin: AS123
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent()))
      expect:
        insertRoute =~ /SUCCESS/

        noMoreMessages()
    }

}
