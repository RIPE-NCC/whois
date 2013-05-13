package net.ripe.db.whois.spec.query

import net.ripe.db.whois.spec.BaseSpec
import net.ripe.db.whois.spec.BasicFixtures

class TagsSpec extends BaseSpec {

    @Override
    Map<String, String> getBasicFixtures() {
        return BasicFixtures.permanentFixtures
    }

    @Override
    Map<String, String> getFixtures() {
        [
                "LIR2-MNT": """\
                mntner:      LIR2-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$m4UsfkN3\$kLY5AaJuJrxaTR94HW5Ad0  #lir2
                mnt-by:      LIR2-MNT
                referral-by: LIR2-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "SUB-MNT": """\
                mntner:      SUB-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_domains@ripe.net
                mnt-nfy:     mntnfy_domains@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$63qqt67X\$irszXgCNN2RdN6cZC12pK1  #sub
                mnt-by:      SUB-MNT
                referral-by: SUB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ORGSUB": """\
                organisation:    ORG-SUB1-TEST
                org-type:        other
                org-name:        S U B
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST
                """,
                "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.250.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "PART-PA": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "SUB-ALLOC-PA": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    SUB-MNT
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
                "PART-PA2": """\
                inetnum:      192.169.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS-END2": """\
                inetnum:      192.169.50.0 - 192.169.50.255
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
                "ALLOC-PA-192-56": """\
                inetnum:      192.56.0.0 - 192.60.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "SUB-ALLOC-PA-192-57": """\
                inetnum:      192.57.0.0 - 192.57.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS-END-192-57": """\
                inetnum:      192.57.0.0 - 192.57.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ALLOC-PA-44": """\
                inetnum:      44.100.0.0 - 44.105.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS-END-44": """\
                inetnum:      44.100.20.0 - 44.100.20.255
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
                "ALLOC-PI": """\
                inetnum:      25.0.0.0 - 25.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSPI": """\
                inetnum:      25.0.200.0 - 25.0.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "AS1000": """\
                aut-num:     AS1000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
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
        ]
    }

    def "query --show-taginfo, no tag info available"() {
      given:
        1 == 1

      expect:
        queryObject("-rGB 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountObjects("-rGB 192.168.0.0 - 192.169.255.255") == 1
        queryCountErrors("-rGB 192.168.0.0 - 192.169.255.255") == 0

        ! queryLineMatches("-GBr --show-taginfo 192.168.0.0 - 192.169.255.255",
                "^% Tags relating to ‘192.168.0.0 - 192.169.255.255'")
    }

    def "query --show-taginfo, 1 object, 1 tag"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "Registration entered by registry")

      then:
        queryObject("-rGB --show-taginfo 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountObjects("-GBr --show-taginfo 192.168.0.0 - 192.169.255.255") == 1
        queryCountErrors("-GBr --show-taginfo 192.168.0.0 - 192.169.255.255") == 0

        queryCommentMatches("-GBr --show-taginfo 192.168.0.0 - 192.169.255.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED # Registration entered by registry")
    }

    def "query --show-taginfo, 2 objects, 1 tag each"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")

      then:
        queryObject("-rGB -L --show-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rGB -L --show-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountErrors("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% RIPE-USER-REGISTERED")
    }

    def "query --show-taginfo, 2 objects, 2 tags each"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "Registration entered by registry")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "Registration entered by user")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")

      then:
        queryObject("-rGB -L --show-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountErrors("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "^% RIPE-USER-REGISTERED #")
        queryCommentMatches("-GBr -L --show-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "^% ASSIGNED")
    }

    def "query --no-taginfo, 2 objects, 1 tag each"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "Registration entered by registry")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "Registration entered by user")

      then:
        queryObject("-rGB -L --no-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-GBr -L --no-taginfo 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountErrors("-GBr -L --no-taginfo 192.168.200.0 - 192.168.200.255") == 0

        queryCommentNotMatches("-GBr -L --no-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentNotMatches("-GBr -L --no-taginfo 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "^% RIPE-USER-REGISTERED #")
    }

    def "query --show-taginfo --filter-tag-include, 2 tagged objects, 2 tags each, filter on one tag"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")

      then:
        queryMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): RIPE-REGISTERED")
        ! queryMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "% Note: tag filtering is enabled,.*?% Note: tag filtering is enabled,")

        queryObject("-rGB -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGB -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryCountObjects("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255") == 1
        queryCountErrors("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")


        queryMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): RIPE-USER-REGISTERED")

        queryObjectNotFound("-rGB -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGB -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryCountObjects("-GBr -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 1
        queryCountErrors("-GBr -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% RIPE-USER-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% ASSIGNED")
    }

    def "query --show-taginfo --filter-tag-include --filter-tag-include, 2 tagged objects, 2 tags each"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")

      then:
        queryMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): RIPE-REGISTERED, RIPE-USER-REGISTERED")

        queryObject("-rGB -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-rGB -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountObjects("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 2
        queryCountErrors("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% RIPE-USER-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-include RIPE-REGISTERED --filter-tag-include RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% ASSIGNED")
    }

    def "query --show-taginfo --filter-tag-exclude, 2 tagged objects, 2 tags each, filter on one tag"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")

      then:
        queryMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITHOUT tag\\(s\\): RIPE-REGISTERED")

        queryObject("-rGB -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-rGB -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountObjects("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255") == 5
        queryCountErrors("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255") == 0

        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% RIPE-USER-REGISTERED")
        queryCommentMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% ASSIGNED")
        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
    }

    def "query --show-taginfo --filter-tag-exclude --filter-tag-exclude, 2 tagged objects, 2 tags each"() {
      when:
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")

      then:
        queryMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITHOUT tag\\(s\\): RIPE-REGISTERED, RIPE-USER-REGISTERED")

        queryObjectNotFound("-rGB -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-rGB -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryCountObjects("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 4
        queryCountErrors("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255") == 0

        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% RIPE-USER-REGISTERED")
        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.200.0 - 192.168.200.255", "% ASSIGNED")
        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentNotMatches("-GBr -L --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-exclude RIPE-USER-REGISTERED 192.168.200.0 - 192.168.200.255",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
    }

    def "query --show-taginfo --filter-tag-include, case 1 single tag"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-GBr -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): RIPE-REGISTERED")

        queryObject("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8", "inetnum", "192.56.0.0 - 192.60.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8") == 2
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8") == 0

        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8",
                "^% Tags relating to", "192.56.0.0 - 192.60.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8",
                "^% Tags relating to", "192.56.0.0 - 192.60.255.255", "% ALLOCATED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-REGISTERED 192/8",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
    }

    def "query --show-taginfo --filter-tag-include, case 2 csl of tags"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): PLACEHOLDER, ALLOCATED")

        queryObject("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8", "inetnum", "192.0.0.0 - 192.250.255.255")
        queryObject("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8", "inetnum", "192.56.0.0 - 192.60.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8") == 3
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8") == 0

        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "^% Tags relating to", "192.0.0.0 - 192.250.255.255", "% PLACEHOLDER")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% ALLOCATED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "^% Tags relating to", "192.168.0.0 - 192.169.255.255", "% RIPE-REGISTERED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "^% Tags relating to", "192.56.0.0 - 192.60.255.255", "% ALLOCATED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED 192/8",
                "^% Tags relating to", "192.56.0.0 - 192.60.255.255", "% RIPE-REGISTERED")
    }

    def "query --show-taginfo --filter-tag-include --filter-tag-exclude, case 3 include with csl, exclude after include"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")


      then:
        queryMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED --filter-tag-exclude RIPE-REGISTERED 192/8",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): PLACEHOLDER, ALLOCATED.%\\s*Only showing objects WITHOUT tag\\(s\\): RIPE-REGISTERED")

        queryObject("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED --filter-tag-exclude RIPE-REGISTERED 192/8", "inetnum", "192.0.0.0 - 192.250.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED --filter-tag-exclude RIPE-REGISTERED 192/8") == 1
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED --filter-tag-exclude RIPE-REGISTERED 192/8") == 0

        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include PLACEHOLDER,ALLOCATED --filter-tag-exclude RIPE-REGISTERED 192/8",
                "^% Tags relating to", "192.0.0.0 - 192.250.255.255", "% PLACEHOLDER")
    }

    def "query --show-taginfo --filter-tag-exclude --filter-tag-include, case 4 include with csl, reverse csl, exclude before include"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-include ALLOCATED,PLACEHOLDER 192/8",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): ALLOCATED, PLACEHOLDER.%\\s*Only showing objects WITHOUT tag\\(s\\): RIPE-REGISTERED")

        queryObject("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-include ALLOCATED,PLACEHOLDER 192/8", "inetnum", "192.0.0.0 - 192.250.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-include ALLOCATED,PLACEHOLDER 192/8") == 1
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-include ALLOCATED,PLACEHOLDER 192/8") == 0

        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED --filter-tag-include ALLOCATED,PLACEHOLDER 192/8",
                "^% Tags relating to", "192.0.0.0 - 192.250.255.255", "% PLACEHOLDER")
    }

    def "query --show-taginfo --filter-tag-exclude, single tag, untagged objects should still be shown"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192/8",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITHOUT tag\\(s\\): RIPE-REGISTERED")

        queryObjectNotFound("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192/8", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192/8") == 8
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192/8") == 0

        queryCommentNotMatches("-rGB -M --show-taginfo --filter-tag-exclude RIPE-REGISTERED 192/8",
                "^% Tags relating to", ".*?", "% RIPE-REGISTERED")
    }

    def "query --show-taginfo --filter-tag-include, single tag, untagged objects should NOT be shown"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): RIPE-USER-REGISTERED")

        queryObjectNotFound("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryCountObjects("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8") == 6
        queryCountErrors("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8") == 0

        queryCommentNotMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8",
                "^% Tags relating to", ".*?", "% RIPE-REGISTERED")
        queryCommentMatches("-rGB -M --show-taginfo --filter-tag-include RIPE-USER-REGISTERED 192/8",
                "^% Tags relating to", ".*?", "% RIPE-USER-REGISTERED")
    }

    def "query --filter-tag-include, single tag, filter on tags but don't show tag info"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        queryMatches("-rGB -M --filter-tag-include ALLOCATED 44/8",
              "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): ALLOCATED")

        queryObject("-rGB -M --filter-tag-include ALLOCATED 44/8", "inetnum", "44.100.0.0 - 44.105.255.255")
        queryCountObjects("-rGB -M --filter-tag-include ALLOCATED 44/8") == 1
        queryCountErrors("-rGB -M --filter-tag-include ALLOCATED 44/8") == 0

        ! queryLineMatches("-rGB -M --filter-tag-include ALLOCATED 44/8", "^% Tags relating to")
    }

    def "query --show-taginfo --filter-tag-include  --filter-tag-exclude, same tag on include & exclude"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

      then:
        ! queryLineMatches("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8",
              "% Note: tag filtering is enabled,")
        ! queryLineMatches("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8",
                "%\\s*Only showing objects WITH tag\\(s\\): ALLOCATED")
        ! queryLineMatches("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8",
                "%\\s*Only showing objects WITHOUT tag\\(s\\): ALLOCATED")

        queryObjectNotFound("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8", "inetnum", "44.100.0.0 - 44.105.255.255")
        queryCountObjects("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8") == 0
        queryCountErrors("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8") == 1

        queryError("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8", "%ERROR:109: invalid combination of flags passed")
        queryCommentMatches("-rGB -M --filter-tag-include ALLOCATED --filter-tag-exclude ALLOCATED 44/8",
                "%ERROR:109: invalid combination of flags passed", ".*?", "% The flags \"--filter-tag-include \\(ALLOCATED\\)\" and \"--filter-tag-exclude \\(ALLOCATED\\)\" cannot be used together")
    }

    def "query --show-taginfo, tag related objects"() {
      when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

      then:
        ! queryLineMatches("-GB --show-taginfo 192.168.0.0 - 192.169.255.255",
              "% Note: tag filtering is enabled,")

        queryObject("-GB --show-taginfo 192.168.0.0 - 192.169.255.255", "person", "Test Person")
        queryCountObjects("-GB --show-taginfo 192.168.0.0 - 192.169.255.255") == 3
        queryCountErrors("-GB --show-taginfo 192.168.0.0 - 192.169.255.255") == 0

        queryCommentMatches("-GB --show-taginfo 192.168.0.0 - 192.169.255.255",
                "^% Tags relating to", "TP1-TEST", "% personal")
    }

    def "query --show-taginfo --filter_tag-include, tag related objects, only include personal tag"() {
        when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

        then:
        queryMatches("-GB --show-taginfo --filter-tag-include Personal 192.168.0.0 - 192.169.255.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): Personal")

        queryObject("-GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255", "person", "Test Person")
        queryCountObjects("-GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255") == 1
        queryCountErrors("-GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255") == 0

        queryCommentMatches("-GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255",
                "^% Tags relating to", "TP1-TEST", "% personal")
    }

    def "query --show-taginfo --filter_tag-include, tag related objects, only include personal tag & -r"() {
        when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

        then:
        queryMatches("-r -GB --show-taginfo --filter-tag-include Personal 192.168.0.0 - 192.169.255.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): Personal")

        queryObjectNotFound("-r -GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255", "person", "Test Person")
        queryCountObjects("-r -GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255") == 0
        queryCountErrors("-r -GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255") == 1

        queryCommentMatches("-r -GB --show-taginfo --filter-tag-include personal 192.168.0.0 - 192.169.255.255",
                "^%ERROR:101: no entries found", ".*?", "% No entries found in source TEST")
    }

    def "query --show-taginfo --filter_tag-include, tag related objects, only include non existing tag"() {
        when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

        then:
        queryMatches("-GB --show-taginfo --filter-tag-include fred 192.168.0.0 - 192.169.255.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): fred")

        queryObjectNotFound("-GB --show-taginfo --filter-tag-include fred 192.168.0.0 - 192.169.255.255", "person", "Test Person")
        queryCountObjects("-GB --show-taginfo --filter-tag-include fred 192.168.0.0 - 192.169.255.255") == 0
        queryCountErrors("-GB --show-taginfo --filter-tag-include fred 192.168.0.0 - 192.169.255.255") == 1

        queryCommentMatches("-r -GB --show-taginfo --filter-tag-include fred 192.168.0.0 - 192.169.255.255",
                "^%ERROR:101: no entries found", ".*?", "% No entries found in source TEST")
    }

    def "query --show-taginfo --filter_tag-include, tag related objects, include valid & non existing tag"() {
        when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

        then:
        queryMatches("-GB --show-taginfo --filter-tag-include fred,personal 192.168.0.0 - 192.169.255.255",
                "% Note: tag filtering is enabled,.%\\s*Only showing objects WITH tag\\(s\\): fred, personal")

        queryObject("-GB --show-taginfo --filter-tag-include fred,personal 192.168.0.0 - 192.169.255.255", "person", "Test Person")
        queryCountObjects("-GB --show-taginfo --filter-tag-include fred,personal 192.168.0.0 - 192.169.255.255") == 1
        queryCountErrors("-GB --show-taginfo --filter-tag-include fred,personal 192.168.0.0 - 192.169.255.255") == 0

        queryCommentMatches("-GB --show-taginfo --filter-tag-include fred,personal 192.168.0.0 - 192.169.255.255",
                "^% Tags relating to", "TP1-TEST", "% personal")
    }

    def "query --show-taginfo --no_taginfo"() {
        when:
        // "ALLOC-UNS"
        addTag("192.0.0.0 - 192.250.255.255", "PLACEHOLDER", "")
        // "ALLOC-PA"
        addTag("192.168.0.0 - 192.169.255.255", "RIPE-REGISTERED", "")
        addTag("192.168.0.0 - 192.169.255.255", "ALLOCATED", "")
        // "PART-PA"
        // 192.168.0.0 - 192.168.255.255 no tags
        // "SUB-ALLOC-PA"
        addTag("192.168.128.0 - 192.168.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.128.0 - 192.168.255.255", "SUB-ALLOCATED", "")
        // "ASS-END"
        addTag("192.168.200.0 - 192.168.200.255", "RIPE-USER-REGISTERED", "")
        addTag("192.168.200.0 - 192.168.200.255", "ASSIGNED", "")
        // "PART-PA2"
        addTag("192.169.0.0 - 192.169.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.0.0 - 192.169.255.255", "LIR-PARTITIONED", "")
        // "ASS-END2"
        addTag("192.169.50.0 - 192.169.50.255", "RIPE-USER-REGISTERED", "")
        addTag("192.169.50.0 - 192.169.50.255", "ASSIGNED", "")
        // "ALLOC-PA-192-56"
        addTag("192.56.0.0 - 192.60.255.255", "RIPE-REGISTERED", "")
        addTag("192.56.0.0 - 192.60.255.255", "ALLOCATED", "")
        // "SUB-ALLOC-PA-192-57"
        addTag("192.57.0.0 - 192.57.255.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.255.255", "SUB-ALLOCATED", "")
        // "ASS-END-192-57"
        addTag("192.57.0.0 - 192.57.0.255", "RIPE-USER-REGISTERED", "")
        addTag("192.57.0.0 - 192.57.0.255", "ASSIGNED", "")
        // "ALLOC-PA-44"
        addTag("44.100.0.0 - 44.105.255.255", "RIPE-REGISTERED", "")
        addTag("44.100.0.0 - 44.105.255.255", "ALLOCATED", "")
        // "ASS-END-44"
        addTag("44.100.20.0 - 44.100.20.255", "RIPE-USER-REGISTERED", "")
        addTag("44.100.20.0 - 44.100.20.255", "ASSIGNED", "")
        // "ALLOC-PI"
        addTag("25.0.0.0 - 25.255.255.255", "RIPE-REGISTERED", "")
        addTag("25.0.0.0 - 25.255.255.255", "ALLOCATED", "")
        // "ASSPI"
        addTag("25.0.200.0 - 25.0.200.255", "RIPE-REGISTERED", "")
        addTag("25.0.200.0 - 25.0.200.255", "ASSIGNED", "")

        addTag("TP1-TEST", "RIPE-USER-REGISTERED", "")
        addTag("TP1-TEST", "personal", "")

        then:
        ! queryMatches("-rGB  --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255",
                "% Note: tag filtering is enabled,")

        queryObjectNotFound("-rGB --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255", "inetnum", "192.0.0.0 - 192.250.255.255")
        queryCountObjects("-rGB --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255") == 0
        queryCountErrors("-rGB --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255") == 1

        ! queryCommentMatches("-rGB --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255",
                "^% Tags relating to", "192.0.0.0 - 192.250.255.255", "% PLACEHOLDER")
        queryCommentMatches("-rGB --show-taginfo --no-taginfo 192.0.0.0 - 192.250.255.255",
                "%ERROR:109: invalid combination of flags passed", ".*?", "% The flags \"--show-taginfo\" and \"--no-taginfo\" cannot be used together")
    }

}
