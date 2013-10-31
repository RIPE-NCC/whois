package net.ripe.db.whois.spec.query

import net.ripe.db.whois.spec.BaseQueryUpdateSpec

class LongOptionsSpec extends BaseQueryUpdateSpec {
    @Override
    Map<String, String> getTransients() {
        [
            "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-PA-A": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "LIR-PART-PA": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ASS-END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ASS-END-NOTIFY": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                notify:       test-inverse-notify@ripe.net
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "DOM": """\
                domain:         192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
        ]
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --exact"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --exact 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "query address contained within ASSIGNED PA, parent ALLOCATED PA, with --exact"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObjectNotFound("-rBG -T inetnum --exact 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --one-less"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "query specific ASSIGNED PA range, parent LIR-PARTITIONED PA and ALLOCATED PA, with --one-less"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --one-less as first option"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("--one-less -rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --one-less after search string"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255 --one-less", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --one-less combined with short options"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG--one-less -T inetnum 192.168.200.0 - 192.168.200.255", "%ERROR:111: invalid option supplied")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --ONE-LESS in upper case"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG --ONE-LESS -T inetnum 192.168.200.0 - 192.168.200.255", "%ERROR:111: invalid option supplied")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with -one-less with single '-'"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG -one-less -T inetnum 192.168.200.0 - 192.168.200.255", "%ERROR:111: invalid option supplied")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --T with two '--'"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG --one-less --T inetnum 192.168.200.0 - 192.168.200.255", "%ERROR:111: invalid option supplied")
    }

    def "query address contained within ASSIGNED PA, parent ALLOCATED PA, with --one-less"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-rBG -T inetnum --one-less 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "query specific ASSIGNED PA range, parent LIR-PARTITIONED PA and ALLOCATED PA, with --all-less"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --all-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rBG -T inetnum --all-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-rBG -T inetnum --all-less 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "query specific ALLOCATED PA range, child LIR-PARTITIONED PA and ASSIGNED PA, with --one-more"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObjectNotFound("-rBG -T inetnum --one-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rBG -T inetnum --one-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-rBG -T inetnum --one-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "query specific ALLOCATED PA range, child LIR-PARTITIONED PA and ASSIGNED PA, with --all-more"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --all-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rBG -T inetnum --all-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-rBG -T inetnum --all-more 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "query specific ALLOCATED PA range, child LIR-PARTITIONED PA and ASSIGNED PA, with --exact --all-more"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG -T inetnum --exact --all-more 192.168.0.0 - 192.169.255.255", "%ERROR:901: duplicate IP flags passed")
    }

    def "query --abuse-contact ALLOCATED PA, with abuse-c"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-A") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA-A"
        query_object_matches("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "ORG-LIRA-TEST")
        // ORGANISATION with abuse-c
        query_object_matches("-rBG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST", "abuse-c")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        query_object_matches("-T inetnum  --abuse-contact 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "abuse-mailbox:\\s*abuse@lir.net")
        query_object_not_matches("-T inetnum  --abuse-contact 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "source:\\s*ripe")
    }

    def "query specific ASSIGNED PA range, parent LIR-PARTITIONED PA and ALLOCATED PA, reverse DOMAIN for alloc, with --all-less --reverse-domain"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("DOM") + "password: owner\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "DOM"
        queryObject("-rBG -T domain 192.in-addr.arpa", "domain", "192.in-addr.arpa")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum,domain --all-less --reverse-domain 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rBG -T inetnum,domain --all-less --reverse-domain 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-rBG -T inetnum,domain --all-less --reverse-domain 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rBG -T inetnum,domain --all-less --reverse-domain 192.168.200.0 - 192.168.200.255", "domain", "192.in-addr.arpa")
    }

    def "query specific ASSIGNED PA range, with --all-less--reverse-domain, no space"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("DOM") + "password: owner\npassword: hm")
        syncUpdate(getTransient("LIR-PART-PA") + "password: lir2\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: lir2\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "DOM"
        queryObject("-rBG -T domain 192.in-addr.arpa", "domain", "192.in-addr.arpa")
        // "LIR-PART-PA"
        queryObject("-rBG -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryError("-rBG -T inetnum,domain --all-less--reverse-domain 192.168.200.0 - 192.168.200.255", "%ERROR:111: invalid option supplied")
    }

    def "query inverse notify with --inverse notify"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END-NOTIFY") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END-NOTIFY"
        query_object_matches("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "notify:\\s*test-inverse-notify@ripe.net")

      and:
        queryObject("-rBG -T inetnum --inverse notify test-inverse-notify@ripe.net", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --brief"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-rBG -T inetnum --brief 192.168.200.0 - 192.168.200.255", "in", "192.168.200.0 - 192.168.200.255")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with persistent connection"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        objectMatches(queryPersistent(["-krBG -T inetnum 192.168.200.0 - 192.168.200.255",
                                       "-rkBG -T inetnum 192.168.0.0 - 192.169.255.255"])
                              .get(1), "inetnum","192.168.0.0 - 192.169.255.255")


        objectMatches(queryPersistent(["-krBG -T inetnum 192.168.200.0 - 192.168.200.255",
                                       "-rBG -T inetnum 192.168.0.0 - 192.169.255.255",
                                       "-k"])
                              .get(1), "inetnum","192.168.0.0 - 192.169.255.255")

        objectMatches(queryPersistent(["-k",
                                       "-rBG -T inetnum 192.168.200.0 - 192.168.200.255",
                                       "-rBG -T inetnum 192.168.0.0 - 192.169.255.255",
                                       "-k"])
                              .get(2), "inetnum","192.168.0.0 - 192.169.255.255")

        objectMatches(queryPersistent(["-rBG -T inetnum --persistent-connection 192.168.200.0 - 192.168.200.255",
                                       "-rBG -T inetnum 192.168.0.0 - 192.169.255.255",
                                       "--persistent-connection"])
                              .get(1), "inetnum","192.168.0.0 - 192.169.255.255")

//        "-rBG -T inetnum -k 192.168.200.0 - 192.168.200.255\n\n-rBG -T inetnum 192.168.0.0 - 192.169.255.255\n\n-k", "inetnum", "192.168.0.0 - 192.169.255.255")
//        "-rBG -T inetnum --persistent-connection 192.168.200.0 - 192.168.200.255\n\n-rBG -T inetnum 192.168.0.0 - 192.169.255.255\n\n--persistent-connection", "inetnum", "192.168.0.0 - 192.169.255.255")

    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --no-grouping --no-filtering --no-referenced"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        query_object_not_matches("--no-grouping --no-filtering --no-referenced -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "person:")
        query_object_not_matches("--no-grouping --no-filtering --no-referenced -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "Information related to:")
        query_object_not_matches("--no-grouping --no-filtering --no-referenced -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE # Filtered:")
    }

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --select-types"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryObject("-GBr --select-types inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-GBr --select-types route 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    // -a ToDo

    // -s ToDo

    def "query specific ASSIGNED PA range, parent ALLOCATED PA, with --sources --version"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end\npassword: owner3")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      and:
        queryMatches("--list-sources", "TEST:")
        queryMatches("--version", "whois-server-")
        queryMatches("--types", "inet6num")
    }

    def "query inetnum template with --template and --verbose"() {
      expect:
        queryMatches("--template organisation", "abuse-c:")
        queryMatches("--template organisation", "optional")
        queryMatches("--verbose organisation", "abuse-c:")
        queryMatches("--verbose organisation", "optional")
        queryMatches("--verbose organisation", "Specifies the ID of an organisation object")
    }

    def "query inetnum pass client tag with --client"() {
      expect:
        queryError("-GBr --client testing,193.0.0.1  TST-MNT", "%ERROR:203: you are not allowed to act as a proxy")
    }

}
