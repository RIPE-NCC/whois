package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class SetsIntegrationSpec extends BaseSpec {
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
                "UPD-MNT": """\
                    mntner: UPD-MNT
                    descr: description
                    admin-c: AP1-TEST
                    mnt-by: UPD-MNT
                    upd-to: noreply@ripe.net
                    auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: dbtest@ripe.net 20120707
                    source: TEST
                """,

                "A-MNT": """\
                    mntner: A-MNT
                    descr: description
                    admin-c: AP1-TEST
                    mnt-by: A-MNT
                    upd-to: noreply@ripe.net
                    auth:    MD5-PW \$1\$fq/gSvz/\$bcLSK.MKFwznLJFsmOGIh1 # password
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
                "AUT-NUM": """\
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
                "SLASH8": """\
                    inetnum:     10.0.0.0 - 10.255.255.255
                    netname:     TestInetnum
                    descr:       Inetnum for testing
                    country:     NL
                    admin-c:     AP1-TEST
                    tech-c:      AP1-TEST
                    status:      ALLOCATED PA
                    mnt-by:      UPD-MNT
                    changed:     dbtest@ripe.net 20120101
                    source:      TEST
                """,
                "RouteSlash8": """\
                    route:       10.0.0.0/8
                    descr:       dummy route
                    origin:      AS101
                    mnt-by:      UPD-MNT
                    changed:     agoston@ripe.net 20120101
                    source:      TEST
                """
        ]
    }

    def "delete set"() {
      given:
        def data = """\
                    filter-set: FLTR-ND
                    descr:      some description
                    filter:     {1.0.0.0/8^- , 2.0.0.0/8^- }
                    tech-c:     AP1-TEST
                    admin-c:    AP1-TEST
                    mnt-by:     UPD-MNT
                    changed:    test@ripe.net 20120202
                    source:     TEST
                    password:   update
                """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/
        createResponse =~ /Create SUCCEEDED: \[filter-set\] FLTR-ND/

      when:
        def deleteData = """\
                    filter-set: FLTR-ND
                    descr:      some description
                    filter:     {1.0.0.0/8^- , 2.0.0.0/8^- }
                    tech-c:     AP1-TEST
                    admin-c:    AP1-TEST
                    mnt-by:     UPD-MNT
                    changed:    test@ripe.net 20120202
                    source:     TEST
                    password:   update
                    delete: YES
                """
        def deleteResponse = syncUpdate(new SyncUpdate(data: deleteData.stripIndent()))

      then:
        deleteResponse =~ /SUCCESS/
        deleteResponse =~ /Delete SUCCEEDED: \[filter-set\] FLTR-ND/
    }

    def "delete, set is referenced"() {
      given:
        def createData = """\
                    rtr-set:        rtrs-ripetest
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    mbrs-by-ref:    A-MNT
                    source:         TEST
                    password:       emptypassword
                    """
        def createResponse = syncUpdate(new SyncUpdate(data: createData.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/

      when:
        def inetData = """\
            inet-rtr:        test.ripe.net
            descr:           test
            local-as:        AS101
            ifaddr:          192.168.0.1 masklen 22
            admin-c:         AP1-TEST
            tech-c:          AP1-TEST
            mnt-by:          A-MNT
            member-of:       rtrs-ripetest
            changed:         test@ripe.net 20120622
            source:          TEST
            password:        password
        """
        def inetResponse = syncUpdate(new SyncUpdate(data: inetData.stripIndent()))

      then:
        inetResponse =~ /SUCCESS/

      when:
        def deleteData = """\
                    rtr-set:        rtrs-ripetest
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    mbrs-by-ref:    A-MNT
                    source:         TEST
                    password:       emptypassword
                    delete:         true
                    """
        def deleteResponse = syncUpdate(new SyncUpdate(data: deleteData.stripIndent()))

      then:
        deleteResponse =~ /FAIL/
        deleteResponse =~ /Object \[rtr-set\] rtrs-ripetest is referenced from other objects/
    }

    def "create, parent must be present"() {
      given:
        def data = """\
                    route-set:      AS53922:RS-PROD
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        createResponse =~ /FAIL/
        createResponse =~ /Parent object AS53922 not found/
    }

    def "create, parent is present"() {
      given:
        def data = """\
                    route-set:      AS101:RS-PROD
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    password:       update
                    """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/
        createResponse =~ /Create SUCCEEDED: \[route-set\] AS101:RS-PROD/
    }

    def "create, parent does not need to be present"() {
      given:
        def data = """\
                    peering-set:    PRNG-RIPETEST
                    descr:          description
                    mp-peering:     AS702:PRNG-AT-CUSTOMER
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        createResponse =~ /SUCCESS/
        createResponse =~ /Create SUCCEEDED: \[peering-set\] PRNG-RIPETEST/
    }

    def "create, parent, child, grandchild"() {
      given:
        def parentData = """\
                    rtr-set:        RTRS-RIPETEST
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: parentData.stripIndent()))

      expect:
        parentResponse =~ /SUCCESS/

      when:
        def childData = """\
                    rtr-set:        RTRS-RIPETEST:RTrS-BULLDOG
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         UPD-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    password:       update
                    """
        def childResponse = syncUpdate(new SyncUpdate(data: childData.stripIndent()))

      then:
        childResponse =~ /SUCCESS/

      when:
        def grandchildData = """\
                    rtr-set:        RTRS-RIPETEST:RTrS-BULLDOG:rtrs-PUPPY
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    password:       update
                    """
        def grandchildResponse = syncUpdate(new SyncUpdate(data: grandchildData.stripIndent()))

      then:
        grandchildResponse =~ /SUCCESS/
    }

    def "create, authenticate against mnt-lower fail"() {
      given:
        def parentData = """\
                    filter-set:     fltr-parent
                    descr:          description
                    mp-filter:      {2a00:10c0::/32^+}
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: parentData.stripIndent()))
      expect:
        parentResponse =~ /SUCCESS/

      when:
        def childData = """\
                    filter-set:     fltr-parent:fltr-child
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def childResponse = syncUpdate(new SyncUpdate(data: childData.stripIndent()))

      then:
        childResponse =~ /Create FAILED: \[filter-set\] fltr-parent:fltr-child/
        childResponse =~ /Authorisation for parent \[filter-set\] fltr-parent failed/
        childResponse =~ /using "mnt-lower:"/
        childResponse =~ /not authenticated by: A-MNT/

    }

    def "create, authenticate successfully against mnt-lower"() {
      given:
        def parentData = """\
                    filter-set:     fltr-parent
                    descr:          description
                    mp-filter:     {2a00:10c0::/32^+}
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: parentData.stripIndent()))

      expect:
        parentResponse =~ /SUCCESS/

      when:
        def childData = """\
                    filter-set:     fltr-parent:fltr-child
                    descr:          description
                    mp-filter:      {2a00:10c0::/32^+}
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    password:       password
                    """
        def childResponse = syncUpdate(new SyncUpdate(data: childData.stripIndent()))

      then:
        childResponse =~ /SUCCESS/
    }

    def "create, authenticate fail against mnt-lower"() {
      given:
        def parentData = """\
                    filter-set:     fltr-parent
                    descr:          description
                    mp-filter:      {2a00:10c0::/32^+}
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: parentData.stripIndent()))

      expect:
        parentResponse =~ /SUCCESS/

      when:
        def childData = """\
                    filter-set:     fltr-parent:fltr-child
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mnt-lower:      A-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    password:       update
                    """
        def childResponse = syncUpdate(new SyncUpdate(data: childData.stripIndent()))

      then:
        childResponse =~ /FAIL/
        childResponse =~ /Authorisation for parent \[filter-set\] fltr-parent failed/
        childResponse =~ /using "mnt-lower:"/
        childResponse =~ /not authenticated by: A-MNT/
    }

    def "modify, straight forward"() {
      given:
        def data = """\
                    peering-set:    prng-ripe
                    descr:          description
                    mp-peering:     AS702:PRNG-AT-CUSTOMER
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        parentResponse =~ /SUCCESS/

      when:
        def updateData = """\
                    peering-set:    prng-ripe
                    descr:          description
                    mp-peering:     AS702:PRNG-AT-CUSTOMER
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        bob@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def updateResponse = syncUpdate(new SyncUpdate(data: updateData.stripIndent()))

      then:
        updateResponse =~ /SUCCESS/
        updateResponse =~ /Modify SUCCEEDED: \[peering-set\] prng-ripe/
    }

    def "modify, removed parent still allows for update of child"() {
      given:
        def parent = """\
                    rtr-set:        rtrs-parent
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def parentResponse = syncUpdate(new SyncUpdate(data: parent.stripIndent()))

      expect:
        parentResponse =~ /SUCCESS/

      when:
        def child = """\
                    rtr-set:        rtrs-parent:rtrs-child
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         UPD-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       update
                    password:       emptypassword
                    """
        def updateResponse = syncUpdate(new SyncUpdate(data: child.stripIndent()))

      then:
        updateResponse =~ /SUCCESS/

      when:
        def deleteParent = """\
                    rtr-set:        rtrs-parent
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    delete:         YES
                    """
        def deleteResponse = syncUpdate(new SyncUpdate(data: deleteParent.stripIndent()))

      then:
        deleteResponse =~ /SUCCESS/

      when:
        def updateChild = """\
                    rtr-set:        rtrs-parent:rtrs-child
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         UPD-MNT
                    changed:        bob@test.net 20120101
                    source:         TEST
                    password:       update
                    """
        def updateChildResponse = syncUpdate(new SyncUpdate(data: updateChild.stripIndent()))

      then:
        updateChildResponse =~ /SUCCESS/
        updateChildResponse =~ /Modify SUCCEEDED: \[rtr-set\] rtrs-parent:rtrs-child/
    }

    def "modify, referenced set can still be modified"() {
      given:
        def data = """\
                    as-set:         as-ripe
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mbrs-by-ref:    UPD-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def asResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

      expect:
        asResponse =~ /Create SUCCEEDED: \[as-set\] as-ripe/

      when:
        def autnumData = """\
                        aut-num:        AS123
                        as-name:        Some-User
                        member-of:      AS-ripe
                        descr:          description
                        admin-c:        AP1-TEST
                        tech-c:         AP1-TEST
                        mnt-by:         UPD-MNT
                        changed:        noreply@ripe.net 20120101
                        source:         TEST
                        password:       update
                        """
        def autnumDataResponse = syncUpdate(new SyncUpdate(data: autnumData.stripIndent()))

      then:
        autnumDataResponse =~ /Create SUCCEEDED: \[aut-num\] AS123/

      when:            //prove that the set is referenced
        def delete = """\
                    as-set:         as-ripe
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mbrs-by-ref:    UPD-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    delete:         YES
                    """
        def deleteResponse = syncUpdate(new SyncUpdate(data: delete.stripIndent()))

      then:
        deleteResponse =~ /FAIL/
        deleteResponse =~ /Object \[as-set\] as-ripe is referenced from other objects/

      when:
        def updateData = """\
                    as-set:         as-ripe
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    mnt-by:         TEST-MNT
                    mbrs-by-ref:    UPD-MNT
                    changed:        bob@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def updateDataResponse = syncUpdate(new SyncUpdate(data: updateData.stripIndent()))

      then:
        updateDataResponse =~ /Modify SUCCEEDED: \[as-set\] as-ripe/
    }

    def "create, add to set via members:, delete, add nonexistant reference"() {
        given:
        def data = """\
                    route-set:      RS-PROD
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    members:        10.0.0.0/8
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent()))

        expect:
        createResponse =~ /Create SUCCEEDED: \[route-set\] RS-PROD/

        when:
        def deldata = """\
                    route:       10.0.0.0/8
                    descr:       dummy route
                    origin:      AS101
                    mnt-by:      UPD-MNT
                    changed:     agoston@ripe.net 20120101
                    source:      TEST
                    delete:      no reason, really
                    password:    update
                    """
        def deleteResponse = syncUpdate(new SyncUpdate(data: deldata.stripIndent()))

        then:
        deleteResponse =~ /Delete SUCCEEDED: \[route\] 10.0.0.0\/8AS101/

        when:
        def moddata = """\
                    route-set:      RS-PROD
                    descr:          description
                    tech-c:         AP1-TEST
                    admin-c:        AP1-TEST
                    members:        10.0.0.0/8, 10.0.0.1/8
                    mnt-by:         TEST-MNT
                    changed:        ripe@test.net 20120101
                    source:         TEST
                    password:       emptypassword
                    """
        def modResponse = syncUpdate(new SyncUpdate(data: moddata.stripIndent()))

        then:
        modResponse =~ /Modify SUCCEEDED: \[route-set\] RS-PROD/
    }
}
