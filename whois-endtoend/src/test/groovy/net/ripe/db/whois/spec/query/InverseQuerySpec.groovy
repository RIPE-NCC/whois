package net.ripe.db.whois.spec.query

import net.ripe.db.whois.spec.BaseSpec
import net.ripe.db.whois.spec.BasicFixtures

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 12/04/2013
 * Time: 13:19
 * To change this template use File | Settings | File Templates.
 */

class InverseQuerySpec extends BaseSpec {

    @Override
    Map<String, String> getBasicFixtures() {
        return BasicFixtures.permanentFixtures
    }

    @Override
    Map<String, String> getFixtures() {
        [
            "INVERSE-PN1": """\
                person:  Inverse Person
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: IP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
            "INVERSE-PN2": """\
                person:  Inverse Person2
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: IP2-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
            "BY-MNT": """\
                mntner:      BY-MNT
                descr:       MNTNER for mnt-by
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                mnt-nfy:     mntnfy_inverse@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      BY-MNT
                referral-by: BY-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "LOWER-MNT": """\
                mntner:      LOWER-MNT
                descr:       used for mnt-lower
                admin-c:     TP1-TEST
                upd-to:      updto_inverse@ripe.net
                mnt-nfy:     dbtest@ripe.net
                notify:      notify_lower@ripe.net
                auth:        MD5-PW \$1\$dYNAtacz\$p4AOgwz3Igu5CiCVzs4Hz.  #lower
                mnt-by:      LOWER-MNT
                referral-by: LOWER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "ROUTES-MNT": """\
                mntner:      ROUTES-MNT
                descr:       used for mnt-routes
                admin-c:     TP1-TEST
                upd-to:      updto_inverse@ripe.net
                mnt-nfy:     mntnfy_inverse@ripe.net
                notify:      notify_routes@ripe.net
                auth:        MD5-PW \$1\$bCCnYJ3M\$uAVVUpzdGA9TOecv9L.KD/  #routes
                mnt-by:      ROUTES-MNT
                referral-by: ROUTES-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "DOMAINS-MNT": """\
                mntner:      DOMAINS-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_inverse@ripe.net
                mnt-nfy:     mntnfy_inverse@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$anTWxMgQ\$8aBWq5u5ZFHLA5aeZsSxG0  #domains
                mnt-by:      DOMAINS-MNT
                referral-by: DOMAINS-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "REF-MNT": """\
                mntner:      REF-MNT
                descr:       MNTNER for mnt-ref
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                mnt-nfy:     mntnfy_inverse@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      REF-MNT
                referral-by: REF-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "MBRS-MNT": """\
                mntner:      MBRS-MNT
                descr:       MNTNER for mbrs-by-ref
                admin-c:     TP1-TEST
                upd-to:      updto_inverse@ripe.net
                mnt-nfy:     dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      MBRS-MNT
                referral-by: MBRS-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "ORGSUB": """\
                organisation:    ORG-SUB1-TEST
                org-type:        other
                org-name:        S U B
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         org-inverse@ripe.net
                notify:          notify-inverse@ripe.net
                mnt-ref:         ref-mnt
                mnt-by:          lir-mnt
                changed: denis@ripe.net 20121016
                source:  TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
                "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      IP1-TEST
                tech-c:       IP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LOWER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "SUB-LOW-R-D": """\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LOWER-MNT
                mnt-routes:   ROUTES-MNT
                mnt-domains:  DOMAINS-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "SUB-LOW": """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LOWER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS-END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      IP2-TEST
                tech-c:       IP2-TEST
                notify:       notify-inverse@ripe.net
                status:       ASSIGNED PA
                mnt-by:       BY-MNT
                mnt-routes:   ROUTES-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "PING": """\
                route:          192.168.200.0/24
                descr:          Route6
                origin:         AS1000
                mnt-by:         LIR-MNT
                ping-hdl:       IP1-test
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "PING2": """\
                route:          192.168.0.0/16
                descr:          Route6
                origin:         AS2000
                mnt-by:         LIR-MNT
                ping-hdl:       IP2-test
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "AS1000": """\
                aut-num:     AS1000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     IP1-TEST
                admin-c:     IP2-TEST
                tech-c:      IP1-TEST
                tech-c:      IP2-TEST
                notify:      notify-inverse@ripe.net
                mnt-by:      LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "AS2000": """\
                aut-num:     AS2000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      BY-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "ZONE": """\
                domain:         168.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         IP2-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "AS-SET-ANY": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mbrs-by-ref:  ANY
                changed:      dbtest@ripe.net 20120101
                source:  TEST
                """,
            "ROUTE-SET": """\
                route-set:    AS1000:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mbrs-by-ref:  MBRS-MNT
                changed:      dbtest@ripe.net 20120101
                source:  TEST
                """,
            "IRT": """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      irtnfy-inverse@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
        ]
    }

    def "query -i admin-c, -i ac, IP1-RIPE"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")

      when:
        def longObjCount = queryCountObjects("-rGB -i admin-c IP1-TEST")
        def shortObjCount = queryCountObjects("-rGB -i ac IP1-TEST")

      then:
        longObjCount == 2
        shortObjCount == 2

        query_object_matches("-rGB -i admin-c IP1-TEST", "inetnum", "192.168.0.0 - 192.169.255.255", "admin-c:\\s*IP1-TEST")
        query_object_matches("-rGB -i admin-c IP1-TEST", "aut-num", "AS1000", "admin-c:\\s*IP1-TEST")
        query_object_matches("-rGB -i ac IP1-TEST", "inetnum", "192.168.0.0 - 192.169.255.255", "admin-c:\\s*IP1-TEST")
        query_object_matches("-rGB -i ac IP1-TEST", "aut-num", "AS1000", "admin-c:\\s*IP1-TEST")
    }

    def "query -i tech-c, -i tc, IP2-RIPE"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")

      when:
        def longObjCount = queryCountObjects("-rGB -i tech-c IP2-TEST")
        def shortObjCount = queryCountObjects("-rGB -i tc IP2-TEST")

      then:
        longObjCount == 2
        shortObjCount == 2

        query_object_matches("-rGB -i tech-c IP2-TEST", "inetnum", "192.168.200.0 - 192.168.200.255", "tech-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i tech-c IP2-TEST", "aut-num", "AS1000", "tech-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i tc IP2-TEST", "inetnum", "192.168.200.0 - 192.168.200.255", "tech-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i tc IP2-TEST", "aut-num", "AS1000", "tech-c:\\s*IP2-TEST")
    }

    def "query -i zone-c, -i zc, IP2-RIPE"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i zone-c IP2-TEST")
        def shortObjCount = queryCountObjects("-rGB -i zc IP2-TEST")
        def shortObjCount2 = queryCountObjects("-rGB -i zc IP1-TEST")

      then:
        longObjCount == 1
        shortObjCount == 1
        shortObjCount2 == 0

        query_object_matches("-rGB -i zone-c IP2-TEST", "domain", "168.192.in-addr.arpa", "zone-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i zc IP2-TEST", "domain", "168.192.in-addr.arpa", "zone-c:\\s*IP2-TEST")
    }

    def "query -i person, -i pn, IP2-RIPE"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i person IP2-TEST")
        def shortObjCount = queryCountObjects("-rGB -i pn IP2-TEST")

      then:
        longObjCount == 4
        shortObjCount == 4

        query_object_matches("-rGB -i person IP2-TEST", "inetnum", "192.168.200.0 - 192.168.200.255", "tech-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i person IP2-TEST", "aut-num", "AS1000", "admin-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i person IP2-TEST", "route", "192.168.0.0/16", "ping-hdl:\\s*IP2-TEST")
        query_object_matches("-rGB -i person IP2-TEST", "domain", "168.192.in-addr.arpa", "zone-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i pn IP2-TEST", "inetnum", "192.168.200.0 - 192.168.200.255", "tech-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i pn IP2-TEST", "aut-num", "AS1000", "admin-c:\\s*IP2-TEST")
        query_object_matches("-rGB -i pc IP2-TEST", "route", "192.168.0.0/16", "ping-hdl:\\s*IP2-TEST")
        query_object_matches("-rGB -i pn IP2-TEST", "domain", "168.192.in-addr.arpa", "zone-c:\\s*IP2-TEST")
    }

    def "query -i ping-hdl, -i pc, IP1-RIPE"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i ping-hdl IP1-TEST")
        def shortObjCount = queryCountObjects("-rGB -i pc IP1-TEST")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i ping-hdl IP1-TEST", "route", "192.168.200.0/24", "ping-hdl:\\s*IP1-TEST")
        query_object_matches("-rGB -i pc IP1-TEST", "route", "192.168.200.0/24", "ping-hdl:\\s*IP1-TEST")
    }

    def "query -i mnt-by, -i mb, BY-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-by BY-MNT")
        def shortObjCount = queryCountObjects("-rGB -i mb BY-MNT")

      then:
        longObjCount == 3
        shortObjCount == 3

        query_object_matches("-rGB -i mnt-by BY-MNT", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by:\\s*BY-MNT")
        query_object_matches("-rGB -i mnt-by BY-MNT", "aut-num", "AS2000", "mnt-by:\\s*BY-MNT")
        query_object_matches("-rGB -i mnt-by BY-MNT", "mntner", "BY-MNT", "mnt-by:\\s*BY-MNT")
        query_object_matches("-rGB -i mb BY-MNT", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by:\\s*BY-MNT")
        query_object_matches("-rGB -i mb BY-MNT", "aut-num", "AS2000", "mnt-by:\\s*BY-MNT")
        query_object_matches("-rGB -i mb BY-MNT", "mntner", "BY-MNT", "mnt-by:\\s*BY-MNT")
    }

    def "query -i mnt-lower, -i ml, LOWER-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-lower LOWER-MNT")
        def shortObjCount = queryCountObjects("-rGB -i ml LOWER-MNT")

      then:
        longObjCount == 3
        shortObjCount == 3

        query_object_matches("-rGB -i mnt-lower LOWER-MNT", "inetnum", "192.168.0.0 - 192.169.255.255", "mnt-lower:\\s*LOWER-MNT")
        query_object_matches("-rGB -i mnt-lower LOWER-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-lower:\\s*LOWER-MNT")
        query_object_matches("-rGB -i mnt-lower LOWER-MNT", "inetnum", "192.168.0.0 - 192.168.127.255", "mnt-lower:\\s*LOWER-MNT")
        query_object_matches("-rGB -i ml LOWER-MNT", "inetnum", "192.168.0.0 - 192.169.255.255", "mnt-lower:\\s*LOWER-MNT")
        query_object_matches("-rGB -i ml LOWER-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-lower:\\s*LOWER-MNT")
        query_object_matches("-rGB -i ml LOWER-MNT", "inetnum", "192.168.0.0 - 192.168.127.255", "mnt-lower:\\s*LOWER-MNT")
    }

    def "query -i mnt-routes, -i mu, ROUTES-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-routes ROUTES-MNT")
        def shortObjCount = queryCountObjects("-rGB -i mu ROUTES-MNT")

      then:
        longObjCount == 2
        shortObjCount == 2

        query_object_matches("-rGB -i mnt-routes ROUTES-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-routes:\\s*ROUTES-MNT")
        query_object_matches("-rGB -i mnt-routes ROUTES-MNT", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-routes:\\s*ROUTES-MNT")
        query_object_matches("-rGB -i mu ROUTES-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-routes:\\s*ROUTES-MNT")
        query_object_matches("-rGB -i mu ROUTES-MNT", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-routes:\\s*ROUTES-MNT")
    }

    def "query -i mnt-domains, -i md, DOMAINS-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-domains DOMAINS-MNT")
        def shortObjCount = queryCountObjects("-rGB -i md DOMAINS-MNT")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i mnt-domains DOMAINS-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-domains:\\s*DOMAINS-MNT")
        query_object_matches("-rGB -i md DOMAINS-MNT", "inetnum", "192.168.200.0 - 192.168.255.255", "mnt-domains:\\s*DOMAINS-MNT")
    }

    def "query -i mnt-ref, -i mz, REF-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("PING") + "override: override1")
        syncUpdate(getTransient("PING2") + "override: override1")
        syncUpdate(getTransient("ZONE") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "PING"
        queryObject("-rBG -T route 192.168.200.0/24", "route", "192.168.200.0/24")
        // "PING2"
        queryObject("-rBG -T route 192.168.0.0/16", "route", "192.168.0.0/16")
        // "ZONE"
        queryObject("-rBG -T domain 168.192.in-addr.arpa", "domain", "168.192.in-addr.arpa")

      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-ref REF-MNT")
        def shortObjCount = queryCountObjects("-rGB -i mz REF-MNT")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i mnt-ref REF-MNT", "organisation", "ORG-SUB1-TEST", "mnt-ref:\\s*REF-MNT")
        query_object_matches("-rGB -i mz REF-MNT", "organisation", "ORG-SUB1-TEST", "mnt-ref:\\s*REF-MNT")
    }

    def "query -i mbrs-by-ref, -i mr, ANY"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("AS-SET-ANY") + "override: override1")
        syncUpdate(getTransient("ROUTE-SET") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "AS-SET-ANY"
        queryObject("-rBG -T as-set AS-TEST", "as-set", "AS-TEST")
        // "ROUTE-SET"
        queryObject("-rBG -T route-set AS1000:RS-CUSTOMERS", "route-set", "AS1000:RS-CUSTOMERS")

      when:
        def longObjCount = queryCountObjects("-rGB -i mbrs-by-ref ANY")
        def shortObjCount = queryCountObjects("-rGB -i mr ANY")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i mbrs-by-ref ANY", "as-set", "AS-TEST", "mbrs-by-ref:\\s*ANY")
        query_object_matches("-rGB -i mr ANY", "as-set", "AS-TEST", "mbrs-by-ref:\\s*ANY")
    }

    def "query -i mbrs-by-ref, -i mr, MBRS-MNT"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("SUB-LOW-R-D") + "override: override1")
        syncUpdate(getTransient("SUB-LOW") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")
        syncUpdate(getTransient("AS-SET-ANY") + "override: override1")
        syncUpdate(getTransient("ROUTE-SET") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "SUB-LOW-R-D"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        // "SUB-LOW"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")
        // "AS-SET-ANY"
        queryObject("-rBG -T as-set AS-TEST", "as-set", "AS-TEST")
        // "ROUTE-SET"
        queryObject("-rBG -T route-set AS1000:RS-CUSTOMERS", "route-set", "AS1000:RS-CUSTOMERS")

      when:
        def longObjCount = queryCountObjects("-rGB -i mbrs-by-ref MBRS-MNT")
        def shortObjCount = queryCountObjects("-rGB -i mr MBRS-MNT")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i mbrs-by-ref MBRS-MNT", "route-set", "AS1000:RS-CUSTOMERS", "mbrs-by-ref:\\s*MBRS-MNT")
        query_object_matches("-rGB -i mr MBRS-MNT", "route-set", "AS1000:RS-CUSTOMERS", "mbrs-by-ref:\\s*MBRS-MNT")
    }

    def "query -i upd-to, -i dt, updto_inverse@ripe.net"() {
      when:
        def longObjCount = queryCountObjects("-rGB -i upd-to updto_inverse@ripe.net")
        def shortObjCount = queryCountObjects("-rGB -i dt updto_inverse@ripe.net")

      then:
        longObjCount == 4
        shortObjCount == 4

        query_object_matches("-rGB -i upd-to updto_inverse@ripe.net", "mntner", "LOWER-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i upd-to updto_inverse@ripe.net", "mntner", "ROUTES-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i upd-to updto_inverse@ripe.net", "mntner", "DOMAINS-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i upd-to updto_inverse@ripe.net", "mntner", "MBRS-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i dt updto_inverse@ripe.net", "mntner", "LOWER-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i dt updto_inverse@ripe.net", "mntner", "ROUTES-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i dt updto_inverse@ripe.net", "mntner", "DOMAINS-MNT", "upd-to:\\s*updto_inverse@ripe.net")
        query_object_matches("-rGB -i dt updto_inverse@ripe.net", "mntner", "MBRS-MNT", "upd-to:\\s*updto_inverse@ripe.net")
    }

    def "query -i mnt-nfy, -i mn, mntnfy_inverse@ripe.net"() {
      when:
        def longObjCount = queryCountObjects("-rGB -i mnt-nfy mntnfy_inverse@ripe.net")
        def shortObjCount = queryCountObjects("-rGB -i mn mntnfy_inverse@ripe.net")

      then:
        longObjCount == 4
        shortObjCount == 4

        query_object_matches("-rGB -i mnt-nfy mntnfy_inverse@ripe.net", "mntner", "BY-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mnt-nfy mntnfy_inverse@ripe.net", "mntner", "ROUTES-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mnt-nfy mntnfy_inverse@ripe.net", "mntner", "DOMAINS-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mnt-nfy mntnfy_inverse@ripe.net", "mntner", "REF-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mn mntnfy_inverse@ripe.net", "mntner", "BY-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mn mntnfy_inverse@ripe.net", "mntner", "ROUTES-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mn mntnfy_inverse@ripe.net", "mntner", "DOMAINS-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
        query_object_matches("-rGB -i mn mntnfy_inverse@ripe.net", "mntner", "REF-MNT", "mnt-nfy:\\s*mntnfy_inverse@ripe.net")
    }

    def "query -i notify, -i ny, notify-inverse@ripe.net"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")

      when:
        def longObjCount = queryCountObjects("-rGB -i notify notify-inverse@ripe.net")
        def shortObjCount = queryCountObjects("-rGB -i ny notify-inverse@ripe.net")

      then:
        longObjCount == 3
        shortObjCount == 3

        query_object_matches("-rGB -i notify notify-inverse@ripe.net", "inetnum", "192.168.200.0 - 192.168.200.255", "notify:\\s*notify-inverse@ripe.net")
        query_object_matches("-rGB -i notify notify-inverse@ripe.net", "aut-num", "AS1000", "notify:\\s*notify-inverse@ripe.net")
        query_object_matches("-rGB -i notify notify-inverse@ripe.net", "organisation", "ORG-SUB1-TEST", "notify:\\s*notify-inverse@ripe.net")
        query_object_matches("-rGB -i ny notify-inverse@ripe.net", "inetnum", "192.168.200.0 - 192.168.200.255", "notify:\\s*notify-inverse@ripe.net")
        query_object_matches("-rGB -i ny notify-inverse@ripe.net", "aut-num", "AS1000", "notify:\\s*notify-inverse@ripe.net")
        query_object_matches("-rGB -i ny notify-inverse@ripe.net", "organisation", "ORG-SUB1-TEST", "notify:\\s*notify-inverse@ripe.net")
    }

    def "query -i ref-nfy, -i rn, org-inverse@ripe.net"() {
      when:
        def longObjCount = queryCountObjects("-rGB -i ref-nfy org-inverse@ripe.net")
        def shortObjCount = queryCountObjects("-rGB -i rn org-inverse@ripe.net")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i ref-nfy org-inverse@ripe.net", "organisation", "ORG-SUB1-TEST", "ref-nfy:\\s*org-inverse@ripe.net")
        query_object_matches("-rGB -i rn org-inverse@ripe.net", "organisation", "ORG-SUB1-TEST", "ref-nfy:\\s*org-inverse@ripe.net")
    }

    def "query -i irt-nfy, -i iy, irtnfy-inverse@ripe.net"() {
      given:
        syncUpdate(getTransient("IRT") + "override: override1")

      expect:
        // "IRT"
        queryObject("-rBG -T irt IRT-TEST", "irt", "IRT-TEST")

      when:
        def longObjCount = queryCountObjects("-rGB -i irt-nfy irtnfy-inverse@ripe.net")
        def shortObjCount = queryCountObjects("-rGB -i iy irtnfy-inverse@ripe.net")

      then:
        longObjCount == 1
        shortObjCount == 1

        query_object_matches("-rGB -i irt-nfy irtnfy-inverse@ripe.net", "irt", "IRT-TEST", "irt-nfy:\\s*irtnfy-inverse@ripe.net")
        query_object_matches("-rGB -i iy irtnfy-inverse@ripe.net", "irt", "IRT-TEST", "irt-nfy:\\s*irtnfy-inverse@ripe.net")
    }

}
