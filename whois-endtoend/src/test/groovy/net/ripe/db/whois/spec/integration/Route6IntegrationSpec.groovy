package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class Route6IntegrationSpec extends BaseWhoisSourceSpec {

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
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT2
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$5aMDZg3w\$zL59TnpAszf6Ft.zs148X0 # update2
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
                    descr:           "foobar" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "AS789": """\
                    aut-num:         AS789
                    as-name:         ACK-AS
                    descr:           "Ack" Ltd
                    org:             ORG-NCC1-RIPE
                    mnt-by:          ROUTES-MNT
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
                "MNTBY_ROUTE6": """\
                    route6:          2001:1578:0200::/40
                    descr:           TEST-ROUTE6
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    source:          TEST
                """,
                "MNTROUTES_ROUTE6": """\
                    route6:           9999::/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    source:          TEST
                """,
                "LESS_SPECIFIC_MNTROUTES_ROUTE": """\
                    route6:           9999::/16
                    descr:           Less specific other route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    mnt-routes:      ROUTES-MNT
                    source:          TEST
                """,
                "INET6NUM_MNT_ROUTES": """\
                    inet6num: bbbb::/24
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
                "INET6NUM_MNT_LOWER": """\
                    inet6num: dddd::0/24
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
                "INET6NUM_MNT_BY": """\
                    inet6num: 5353::0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    source: TEST
                """,
                "INET6NUM_MNT_ROUTES_RANGE": """\
                    inet6num: bbdd::/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: ROUTES-MNT
                    mnt-routes: ROUTES-MNT {bbdd::/48}
                    source: TEST
                """,
                "INET6NUM_SLASH_12": """\
                    inet6num: 2000::/12
                    netname: RIPE-NCC
                    descr: description
                    country: NL
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: ROUTES-MNT
                    mnt-routes: ROUTES-MNT {2000::/12}
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

    def "delete route6"() {
        when:
        def delete = new SyncUpdate(data: """\
                route6: 2001:1578:200::/40
                descr: TEST-ROUTE6
                origin: AS12726
                mnt-by: TEST-MNT
                source: TEST
                password: update
                delete: reason
                """.stripIndent(true))

        then:
        def response = syncUpdate delete

        then:
        response =~ /SUCCESS/
        response =~ /Delete SUCCEEDED: \[route6\] 2001:1578:200::\/40AS12726/
    }

    def "modify route6 noop"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 2001:1578:0200::/40
                descr: TEST-ROUTE6
                origin: AS12726
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /No operation: \[route6\] 2001:1578:200::\/40AS12726/
    }

    def "create route6 aut-num with mnt-by authentication"() {
        when:
        def create = new SyncUpdate(data: """\
                route6:          2001:1578:0200::/40
                descr:           ROUTE6 TEST
                origin:          AS123
                mnt-by:          TEST-MNT
                source:          TEST
                password:        update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] 2001:1578:200::\/40AS123/
    }

    def "create route6 aut-num with mnt-routes authentication"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 2001:1578:0200::/40
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] 2001:1578:200::\/40AS456/
    }

    def "create route6 aut-num not authenticated"() {
        when:
        def create = new SyncUpdate(data: """\
                route: 212.166.64.0/19
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response.contains("***Error:   Authorisation for [route] 212.166.64.0/19AS456 failed\n" +
                "            using \"route:\"\n" +
                "            no valid maintainer found")
    }

    def "create route6 ipaddress exact match mnt-routes authentication"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbbb::/24
                descr: Test route
                origin: AS123
                mnt-by: ROUTES-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress exact match mnt-by authentication"() {
        when:
        def create = new SyncUpdate(data: """\
                route6:  5353::0/24
                descr: Test route
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/

    }

    def "create route6 ipaddress exact match authentication fail"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbbb::/24
                descr: Test route
                origin: AS123
                mnt-by: ROUTES-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route6\] bbbb::\/24AS123 failed
            using "mnt-by:"
            not authenticated by: ROUTES-MNT/

        response =~ /Authorisation for \[inet6num\] bbbb::\/24 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route6 ipaddress first less specific mnt-routes authentication route6"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 9999::/32
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific mnt-lower authentication route6"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: dddd::0/48
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: otherpassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific mnt-by authentication route6"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 2001:1578:0200::/80
                descr: less specific
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific authentication fail route6"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 9999::/32
                descr: less specific
                origin: AS123
                mnt-by: ROUTES-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route6\] 9999::\/32AS123 failed
            using "mnt-by:"
            not authenticated by: ROUTES-MNT/

        response =~ /Authorisation for \[route6\] 9999::\/24AS12726 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route6 ipaddress exact match mnt-routes authentication inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbbb::/24
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress exact match mnt-by authentication inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress exact match authentication fail inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: wrong
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route6\] 5353::\/24AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[inet6num\] 5353::\/24 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/
    }

    def "create route6 ipaddress first less specific mnt-routes authentication inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbbb::/32
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                password: emptypassword
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific mnt-lower authentication inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: dddd::0/28
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                password: otherpassword
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific mnt-by authentication inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/28
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
    }

    def "create route6 ipaddress first less specific authentication fail inet6num"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbbb::/32
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                source: TEST
                password: wrong
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Authorisation for \[route6\] bbbb::\/32AS123 failed
            using "mnt-by:"
            not authenticated by: TEST-MNT/

        response =~ /Authorisation for \[inet6num\] bbbb::\/24 failed
            using "mnt-routes:"
            not authenticated by: ROUTES-MNT/
    }

    def "create route6 address prefix range outside route - no authenticating maintainer"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbdd::/24
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /Authorisation for \[inet6num\] bbdd::\/24 failed
            using "mnt-routes:"
            no valid maintainer found/
    }

    def "create route6 valid address prefix range"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: bbdd::/48
                descr: other route
                origin: AS456
                mnt-by: TEST-MNT
                source: TEST
                password: emptypassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] bbdd::\/48AS456/
    }

    def "create route6 holes contained withing prefix value"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                holes: 5353::0/32
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] 5353::\/24AS123/
    }

    def "create route6 holes not contained withing prefix value"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                holes: 5354::0/32
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Error:   5354::0\/32 is outside the range of this object/
    }

    def "create route6 pingable contained withing prefix value"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                pingable: 5353::
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] 5353::\/24AS123/
    }

    def "create route6 pingable not contained withing prefix value"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                pingable: 5354::0
                mnt-by: TEST-MNT
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response =~ /Error:   5354::0 is outside the range of this object/
    }

    def "create route6 member-of exists in route-set"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                mnt-by: LOWER-MNT
                member-of: RS-BLA123
                source: TEST
                password: otherpassword
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[route6\] 5353::\/24AS123/
    }

    def "create route6 member-of does not exist in route-set"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                member-of: RS-BLA123
                source: TEST
                password: update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }

    def "create route6 member-of does not exist in route-set, override"() {
        when:
        def create = new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: Test route6
                origin: AS123
                mnt-by: TEST-MNT
                member-of: RS-BLA123
                source: TEST
                override:     denis,override1
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /SUCCEEDED/
        response.contains(
                "***Warning:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }

    def "modify route6 succeeds"() {
        when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route6:          2001:1578:0200::/40
                    export-comps: {3323::/24}
                    descr:           TEST-ROUTE6
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    source:          TEST
                    password:       update
                """.stripIndent(true)))

        then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[route6\] 2001:1578:200::\/40AS12726/
    }

    def "modify route6 exact same object"() {
        when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route6:          2001:1578:0200::/40
                    descr:           TEST-ROUTE6
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    source:          TEST
                    password:        update
                """.stripIndent(true)))

        then:
        response =~ /No operation: \[route6\] 2001:1578:200::\/40AS12726/
    }

    def "modify route6 fail on route-set reference"() {
        when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route6:           9999::/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    member-of:       RS-BLA123
                    source:          TEST
                    password:        update
                """.stripIndent(true)))

        then:
        response =~ /Modify FAILED: \[route6\] 9999::\/24AS12726/
        response.contains(
                "***Error:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }

    def "modify route6 fail on route-set reference, using override"() {
        when:
        def response = syncUpdate(new SyncUpdate(data: """\
                    route6:           9999::/24
                    descr:           Test route
                    origin:          AS12726
                    mnt-by:          TEST-MNT
                    member-of:       RS-BLA123
                    source:          TEST
                    override:     denis,override1
                """.stripIndent(true)))

        then:
        response =~ /Modify SUCCEEDED: \[route6\] 9999::\/24AS12726/
        response.contains(
                "***Warning:   Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                        "            referenced set [RS-BLA123]")
    }


    def "modify route6 change maintainers"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route6:           9999::/24
                        descr:           Test route
                        origin:          AS12726
                        mnt-by: ROUTES-MNT
                        source: TEST
                        password: update
                        password: emptypassword
                        """.stripIndent(true))
        then:
        response =~ /SUCCESS/
        response =~ /Modify SUCCEEDED: \[route6\] 9999::\/24AS12726/
    }

    def "modify route6 fail on pingables "() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route6:         9999::/24
                        descr:          Test route
                        origin:         AS12726
                        mnt-by:         TEST-MNT
                        pingable:       9998::
                        source:         TEST
                        password:       update
                        """.stripIndent(true))
        then:
        response =~ /FAIL/
        response =~ /Error:   9998:: is outside the range of this object/
    }

    def "modify route6 fail on holes"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route6: 9999::/24
                        descr: Test route
                        origin: AS12726
                        mnt-by: TEST-MNT
                        holes: 9998::/32
                        source: TEST
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /FAIL/
        response =~ /Error:   9998::\/32 is outside the range of this object/
    }

    def "modify route6 "() {
        given:
        def create = syncUpdate new SyncUpdate(data: """\
                        route6: dddd::0/24
                        descr: Test route
                        origin: AS123
                        mnt-by: TEST-MNT
                        source: TEST
                        password: update
                        password: emptypassword
                        """.stripIndent(true))
        expect:
        create =~ /SUCCESS/

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                        route6: dddd::0/24
                        descr: Test route
                        origin: AS123
                        mnt-by: LOWER-MNT
                        source: TEST
                        password: update
                        """.stripIndent(true))
        then:
        response =~ /Modify SUCCEEDED: \[route6\] dddd::\/24AS123/
    }

    def "create route, delete referenced autnum object, delete route object"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route6: 5353::0/24
                            descr: Test route6
                            origin: AS12726
                            pingable: 5353::0
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent(true)))
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
                            """.stripIndent(true))

        then:
        def responseAutnum = syncUpdate deleteAutnum

        then:
        responseAutnum =~ /SUCCESS/

        when:
        def deleteRoute = new SyncUpdate(data: """\
                            route6: 5353::/24
                            descr: Test route6
                            origin: AS12726
                            pingable: 5353::0
                            mnt-by: TEST-MNT
                            source: TEST
                            delete: reason
                            password: update
                            """.stripIndent(true))

        then:
        def responseRoute = syncUpdate deleteRoute

        then:
        responseRoute =~ /SUCCESS/
    }

    def "create route6 prefix not allowed"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                route6:          2001:1578:0200::/11
                descr:           ROUTE6 TEST
                origin:          AS123
                mnt-by:          TEST-MNT
                source:          TEST
                password:        update
                """.stripIndent(true))
        then:
        response =~ /FAIL/
        response =~ /Automatic creation of route6 objects of this size in not allowed,
            please contact lir-help@ripe.net for further information./
    }

    def "create route6 prefix allowed"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                route6:          2000::/12
                descr:           ROUTE6 TEST
                origin:          AS123
                mnt-by:          TEST-MNT
                source:          TEST
                password:        update
                password:        emptypassword
                """.stripIndent(true))
        then:
        response =~ /Create SUCCEEDED: \[route6\] 2000::\/12AS123/
    }

    def "create route6"() {
        given:
        def response = syncUpdate(new SyncUpdate(data: """\
                route6: 5353::0/24
                descr: TEST-ROUTE6
                origin: AS456
                mnt-by: TEST-MNT2
                source: TEST
                password: update
                password: emptypassword
                password: update2
                """.stripIndent(true)))
        expect:
        response =~ /SUCCESS/
    }

    def "create route6 with non-existant origin aut-num"() {
        given:
        queryObjectNotFound("-r -T aut-num AS76", "aut-num", "AS76")
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route6:  2001:1578:0200::/40
                            descr:  Test route6
                            origin: AS76
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent(true)))
      expect:
        insertRoute =~ /SUCCESS/
    }

    def "create route6 and notify origin aut-num"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route6:  2001:1578:0200::/40
                            descr:  Test route6
                            origin: AS103
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent(true)))
      expect:
        insertRoute =~ /SUCCESS/

        def notif = notificationFor "notify@as103.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "route6" && it.key == "2001:1578:200::/40" }

        noMoreMessages()

      when:
        def updateRoute = syncUpdate(new SyncUpdate(data: """\
                            route6:  2001:1578:0200::/40
                            descr:  Test route6
                            origin: AS103
                            remarks: updated
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent(true)))
        then:
          updateRoute =~ /SUCCESS/

          noMoreMessages()
    }

    def "create route6 without notify origin aut-num"() {
        given:
        def insertRoute = syncUpdate(new SyncUpdate(data: """\
                            route6:  2001:1578:0200::/40
                            descr:  Test route6
                            origin: AS123
                            mnt-by: TEST-MNT
                            source: TEST
                            password: update
                            password: emptypassword
                            """.stripIndent(true)))
      expect:
        insertRoute =~ /SUCCESS/

        noMoreMessages()
    }

    def "create route6 with bogon prefix"() {
        when:
        def create = new SyncUpdate(data: """\
                route6:          2001:2::/48
                descr:           ROUTE6 TEST
                origin:          AS123
                mnt-by:          TEST-MNT
                source:          TEST
                password:        update
                """.stripIndent(true))

        then:
        def response = syncUpdate create

        then:
        response =~ /FAIL/
        response.contains("***Error:   Bogon prefix 2001:2::/48 is not allowed.")
    }

}
