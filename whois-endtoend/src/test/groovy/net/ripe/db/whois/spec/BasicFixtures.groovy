package net.ripe.db.whois.spec

class BasicFixtures {
    static def basicFixtures = [
            // these are the permanent basic fixture objects
            "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "OWNER-MNT": """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner@ripe.net
                mnt-nfy:     mntnfy_owner@ripe.net
                notify:      notify_owner@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "OWNER2-MNT": """\
                mntner:      OWNER2-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner2@ripe.net
                mnt-nfy:     mntnfy_owner2@ripe.net
                notify:      notify_owner2@ripe.net
                auth:        MD5-PW \$1\$9vNwegLB\$SrX4itajapDaACGZaLOIY1  #owner2
                mnt-by:      OWNER2-MNT
                source:      TEST
                """,
            "OWNER3-MNT": """\
                mntner:      OWNER3-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner3@ripe.net
                upd-to:      updto2_owner3@ripe.net
                notify:      notify_owner3@ripe.net
                auth:        MD5-PW \$1\$u/Ttxt8r\$zeII/ZqRwC2PuRyGyv0U51  #owner3
                mnt-by:      OWNER3-MNT
                source:      TEST
                """,
            "OWNER4-MNT": """\
                mntner:      OWNER4-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner4@ripe.net
                upd-to:      updto2_owner4@ripe.net
                mnt-nfy:     mntnfy_owner4@ripe.net
                mnt-nfy:     mntnfy2_owner4@ripe.net
                notify:      notify_owner4@ripe.net
                auth:        MD5-PW \$1\$69rcgbTr\$Sh0R8PKbHIN5jf/Nv.FKk/  #owner4
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "RIPE-NCC-HM-MNT": """\
                mntner:      RIPE-NCC-HM-MNT
                descr:       hostmaster MNTNER
                admin-c:     TP1-TEST
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$mV2gSZtj\$1oVwjZr0ecFZQHsNbw2Ss.  #hm
                mnt-by:      RIPE-NCC-HM-MNT
                source:      TEST
                """,
            "LIR-MNT": """\
                mntner:      LIR-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$epUPWc4g\$/6BKqK4lKR/lNqLa7K5qT0  #lir
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "END-USER-MNT": """\
                mntner:      END-USER-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$4qnKkEY3\$9NduUoRMNiBbAX9QEDMkh1  #end
                mnt-by:      END-USER-MNT
                source:      TEST
                """,
            "ORGLIR": """\
                organisation:    ORG-LIR1-TEST
                org-type:        LIR
                org-name:        Local Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ROOT4": """\
                inetnum:      0.0.0.0 - 255.255.255.255
                netname:      IANA-BLK
                descr:        The whole IPv4 address space
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                mnt-routes:   RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "ROOT6": """\
                inet6num:     0::/0
                netname:      IANA-BLK
                descr:        The whole IPv6 address space
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                remarks:      This network in not allocated.
                remarks:      This object is here for Database
                remarks:      consistency and to allow hierarchical
                remarks:      authorisation checks.
                source:       TEST
                """,
            // end of the permanent basic fixture objects
            "TST-MNT": """\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "TEST-PN2": """\
                person:  Test Person2
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: TP2-TEST
                mnt-by:  TST-MNT
                source:  TEST
                """,
            "TEST-PN3": """\
                person:  Test Person3
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: TP3-TEST
                mnt-by:  TST-MNT
                source:  TEST
                """,
            "LOWER-MNT": """\
                mntner:      LOWER-MNT
                descr:       used for mnt-lower
                admin-c:     TP1-TEST
                upd-to:      updto_lower@ripe.net
                mnt-nfy:     mntnfy_lower@ripe.net
                notify:      notify_lower@ripe.net
                auth:        MD5-PW \$1\$dYNAtacz\$p4AOgwz3Igu5CiCVzs4Hz.  #lower
                mnt-by:      LOWER-MNT
                source:      TEST
                """,
            "ROUTES-MNT": """\
                mntner:      ROUTES-MNT
                descr:       used for mnt-routes
                admin-c:     TP1-TEST
                upd-to:      updto_routes@ripe.net
                mnt-nfy:     mntnfy_routes@ripe.net
                notify:      notify_routes@ripe.net
                auth:        MD5-PW \$1\$bCCnYJ3M\$uAVVUpzdGA9TOecv9L.KD/  #routes
                mnt-by:      ROUTES-MNT
                source:      TEST
                """,
            "DOMAINS-MNT": """\
                mntner:      DOMAINS-MNT
                descr:       used for mnt-domains
                admin-c:     TP1-TEST
                upd-to:      updto_domains@ripe.net
                mnt-nfy:     mntnfy_domains@ripe.net
                notify:      notify_domains@ripe.net
                auth:        MD5-PW \$1\$anTWxMgQ\$8aBWq5u5ZFHLA5aeZsSxG0  #domains
                mnt-by:      DOMAINS-MNT
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
                source:      TEST
                """,
            "LIR2-MNT": """\
                mntner:      LIR2-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$m4UsfkN3\$kLY5AaJuJrxaTR94HW5Ad0  #lir2
                mnt-by:      LIR2-MNT
                source:      TEST
                """,
            "LIR3-MNT": """\
                mntner:      LIR3-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$6WUBtqxZ\$eXeV7vu4Soq7tGjUF0kmr.  #lir3
                mnt-by:      LIR3-MNT
                source:      TEST
                """,
            "RIPE-DBM-MNT": """\
                mntner:      RIPE-DBM-MNT
                descr:       Mntner for creating as-objects.
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$6C2pGjXQ\$NwOQteHu2M//N34BfZCEB1 # dbm
                notify:      unread@ripe.net
                mnt-by:      RIPE-DBM-MNT
                source:      TEST
                """,
            "RIPE-NCC-LOCKED-MNT": """\
                mntner:      RIPE-NCC-LOCKED-MNT
                descr:       Mntner for creating as-objects.
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$3XUSpceh\$LbPI6.J1IUGLNA2rmSKA3. # locked
                notify:      dbtest@ripe.net
                mnt-by:      RIPE-DBM-MNT
                source:      TEST
                """,
            "RIPE-NCC-END-MNT": """\
                mntner:      RIPE-NCC-END-MNT
                descr:       Mntner for creating aut-num objects
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$bzCpMX7h\$wl3EmBzNXG..8oTMmGVF51 # nccend
                org:         ORG-OTO1-TEST
                mnt-by:      RIPE-NCC-END-MNT
                source:      TEST
                """,
            "RIPE-NCC-LEGACY-MNT": """\
                mntner:      RIPE-NCC-LEGACY-MNT
                descr:       RIPE NCC LEGACY resource maintainer
                upd-to:      updto_legacy@ripe.net
                mnt-nfy:     mntnfy_legacy@ripe.net
                notify:      notify_legacy@ripe.net
                auth:        MD5-PW \$1\$gTs46J2Z\$.iohp.IUDhNAMj7evxnFS1   # legacy
                org:         ORG-OTO1-TEST
                mnt-by:      RIPE-NCC-LEGACY-MNT
                source:      TEST
                """,
            "TST-MNT2": """\
                mntner:      TST-MNT2
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$bnGNJ2PC\$4r38DENnw07.9ktKP//Kf1  #test2
                mnt-by:      TST-MNT2
                source:      TEST
                """,
            "TST-MNT3": """\
                mntner:      TST-MNT3
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$p4syt8vq\$AOwjgBvR4MA3o4ccMSMvh0  #test3
                mnt-by:      OWNER3-MNT
                source:      TEST
                """,
            "TST-MNT4": """\
                mntner:      TST-MNT4
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst4@ripe.net
                notify:      notify_tst4@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "TST-MNT5": """\
                mntner:      TST-MNT5
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst5@ripe.net
                notify:      notify_tst5@ripe.net
                notify:      notify2_tst5@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "TST-MNT6": """\
                mntner:      TST-MNT6
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst6@ripe.net
                notify:      notify_tst6@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER3-MNT
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "LIM-MNT": """\
                mntner:       LIM-MNT
                descr:        description
                admin-c:      TP1-TEST
                mnt-by:       LIM-MNT
                upd-to:       dbtest@ripe.net
                auth:         MD5-PW \$1\$QYTtsWT5\$85vDaZp72krdzAYa7F3X20 # lim
                source:       TEST
                """,
            "PGP-MNT": """\
                mntner:      PGP-MNT
                descr:       used for testing PGP signed messages
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        PGPKEY-5763950D
                mnt-by:      PGP-MNT
                source:      TEST
                """,
            "TST": """\
                mntner:      TST
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_tst6@ripe.net
                notify:      notify_tst6@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      TST
                source:      TEST
                """,
            "CHILD-MB-MNT": """\
                mntner:      CHILD-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$28Jr/8MuLiKvwiHIYC1C21  # mb-child
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "PARENT-MB-MNT": """\
                mntner:      PARENT-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$G.oSSx.FZsRJWiXhhfDD21  # mb-parent
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "PARENT-ML-MNT": """\
                mntner:      PARENT-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$/iBm2/pCiHFyPqSAiUHyI1  # ml-parent
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "PARENT-MR-MNT": """\
                mntner:      PARENT-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$9CtqUE3cI5b86PUAhP90p0  # mr-parent
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "EXACT-MB-MNT": """\
                mntner:      EXACT-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$aSbOhtjqSm2lwyTQ7Bur/.  # mb-exact
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "EXACT-ML-MNT": """\
                mntner:      EXACT-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$3Xfx8h0lFJhhi2tnbPXAF0  # ml-exact
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "EXACT-MR-MNT": """\
                mntner:      EXACT-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$rojUwYzu10ruwqO00IKh41  # mr-exact
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "EXACT-INETNUM-MB-MNT": """\
                mntner:      EXACT-INETNUM-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$AFX.FXWoEsfqJUYWs4hX8.  # mbi-exact
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "PARENT-INETNUM-MB-MNT": """\
                mntner:      PARENT-INETNUM-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$gstFsI6qeiN9pwDIth8pr1  # mbi-parent
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "ORIGIN-MB-MNT": """\
                mntner:      ORIGIN-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$trXVUy8x5HyizFaU0CHT51  # mb-origin
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "ORIGIN-ML-MNT": """\
                mntner:      ORIGIN-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$0D0reGeFBPYowpN2PHKDP/  # ml-origin
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "ORIGIN-MR-MNT": """\
                mntner:      ORIGIN-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$gKLHiPhaIfaniZB0kSZuB1  # mr-origin
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "DOMAIN-MB-MNT": """\
                mntner:      DOMAIN-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$Sc/q29n8\$r5Ydny1YhQM4/m1mRWXr0/  # mb-dom
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "GII-MNT": """\
                mntner:      RIPE-GII-MNT
                descr:       used for enum tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$MrTjfqBG\$rOJhT7Qo9cLNbwalAt9Dd/  # gii
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "PARENT-ROUTE": """\
                route:       20.0.0.0/8
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                source:      TEST
                """,
            "PARENT-ROUTE99": """\
                route:       99.0.0.0/8
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                source:      TEST
                """,
            "EXACT-ROUTE": """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                """,
            "ROUTE-NON-ORIGIN": """\
                route:       255.13.0.0/16
                descr:       exact match route object
                origin:      AS999000
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                """,
            "AS1000": """\
                aut-num:     AS1000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      PARENT-MB-MNT
                source:      TEST
                """,
            "AS2000": """\
                aut-num:     AS2000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      CHILD-MB-MNT
                source:      TEST
                """,
            "AS3000": """\
                aut-num:     AS3000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                """,
            "ORG1": """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                abuse-c:         AH1-TEST
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGHR": """\
                organisation:    ORG-HR1-TEST
                org-type:        LIR
                org-name:        Regional Internet Registry
                country:         NL
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGSUB": """\
                organisation:    ORG-SUB1-TEST
                org-type:        other
                org-name:        S U B
                address:         RIPE NCC
                abuse-c:         AH1-TEST
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGRIR": """\
                organisation:    ORG-RIR1-TEST
                org-type:        RIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGNIR": """\
                organisation:    ORG-NIR1-TEST
                org-type:        NIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGIANA": """\
                organisation:    ORG-IANA1-TEST
                org-type:        IANA
                org-name:        IANA Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ORGLIR-A": """\
                organisation: ORG-LIRA-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                """,
            "ORGLIR-NO-A": """\
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                source:       TEST
                """,
            "ORG-OTHER-NO-A": """\
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
            "ORG-OTH-A": """\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
            "ORG-OTH-COUNTRY": """\
                organisation: ORG-OFA11-TEST
                org-type:     OTHER
                org-name:     Organisation for country and Abuse
                country:      NL
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
            "ORG-END-A": """\
                organisation: ORG-END1-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH200-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                source:       TEST
                """,
            "ROLE-A": """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "ROLE-A200": """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:my_abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH200-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "ROLE-NO-A": """\
                role:         Standard Role
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      SR1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "SLASH8": """\
                inetnum:     10.0.0.0 - 10.255.255.255
                netname:     TestInetnum
                descr:       Inetnum for testing
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                org:         ORG-LIR1-TEST
                status:      ALLOCATED PA
                mnt-by:      OWNER-MNT
                mnt-lower:   OWNER2-MNT
                mnt-routes:  OWNER3-MNT
                mnt-domains: OWNER4-MNT
                source:      TEST
                """,
            "NONE": """\
                inetnum:      25.168.0.0 - 25.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "KC": """\
                key-cert:     PGPKEY-D83C3FBD
                method:       PGP
                owner:        Test User (testing) <dbtest@ripe.net>
                fingerpr:     F127 F439 9286 0A5E 06D0  809B 471A AB9F D83C 3FBD
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG v1.2.4 (GNU/Linux)
                certif:
                certif:       mQGiBEhGaqcRBAC5Ml+/hCos6WbrISc2egEjQaMi5hcCURSZap2ZkYv4HFG1UiQ4
                certif:       SJ77YIWKRjtAugBKI0OtPc3+CG28l3Qjy3Af6ibRsMjokP77A3CldqrevHzVb11e
                certif:       g4uUBVDl1Z29LrczQ/36tizcA3Ae0GYTVGUqL7Ud9+VVw1hDLvbSbB3qfwCgkWC4
                certif:       TybC7d9RgrLHSLLxMdw6Z5kD/j1BQNT6GhVTeOssxtatkWl6XNulVP/KXGNj+fBi
                certif:       2f2987PdjogojvOEX1ISZg9r+dfJJpA1fREqxcmxOwnQqtyQg3P4gomkxgJ/Fgv9
                certif:       tcIsxUE4mdOyc4ndbMBcnC0qOb83i0ucxuFC97v0y1bzuagCvb3CMGNPZ6yAOcu4
                certif:       qPhiA/42xJngH7cTiNEu2fF1y0kzbg6v68Bs2e//4KKzuqnaWtEgWlh/azQntqs7
                certif:       Z8EONGIUWL78w4j41OLqW80SDrmhxXm4D4fkbmQ7PUxLJxRGlx92uAJb6BBn03F1
                certif:       vCKgNQ2dathLwEOOJaSZkO9QHnNKh37Vc1utDbeBJPLpR8mwN7YAAAAlVGVzdCBV
                certif:       c2VyICh0ZXN0aW5nKSA8ZGJ0ZXN0QHJpcGUubmV0PoheBBMRAgAeBQJIRmqnAhsD
                certif:       BgsJCAcDAgMVAgMDFgIBAh4BAheAAAoJEEcaq5/YPD+9w5MAnR1IX8Ukxn2tJwp9
                certif:       6qdgtXve8wQRAJ9awkI6XNo/VqSNUuXpy766K1X177kBDQRIRmqoEAQAisQxRI6G
                certif:       g97kmHgdhDSMw1Qj5AVsQU/1IZIdh40TRr7+zU5xen6EQLGy1PRJonKi1q9MymXA
                certif:       z7i5HO/1tJ/UzOxcXHKFWoCw0TyMNjoXQq/AQ8nzeJkTnfUuojTMMDwH4z78WYRp
                certif:       Pn7p2POUz2DMzVdosnKDY5ULlWJGxbl1SE8AAwUEAIlLU832nn5Po7MqpFHdiAuQ
                certif:       JB5JmNxbM18+QdadswYHJS2f7pNM1zChVdF3ZRdwbj0PliWd5727sHXj9EyAuJJF
                certif:       JDHVkA4i34cdnHnkjpCEEcemtUvtdynxhVHq1Y4TH8J3UB6pegwdiOMXTRkJCP03
                certif:       m+11oz9PCgQ2o3ANoebwiEkEGBECAAkFAkhGaqgCGwwACgkQRxqrn9g8P72xCgCf
                certif:       fjgQsp5kSoH1TKyP75YIcmUBBdwAn0UcPnm1xjJTVRqZZr7tpkKijF8I
                certif:       =8uv/
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       owner-MNT
                source:       TEST
                """,
            "KC-RSA": """\
                key-cert:     PGPKEY-5763950D
                method:       PGP
                owner:        noreply@ripe.net <noreply@ripe.net>
                fingerpr:     884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                              Version: GnuPG v1
                +
                              mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                              yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                              2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                              E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                              A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                              YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                              ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                              CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                              Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                              0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                              xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                              0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                              xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                              uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                              eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                              pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                              xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                              YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                              dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                              GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                              HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                              eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                              6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                              RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                              a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                              =HQmg
                              -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       owner-MNT
                source:       TEST
                """,
            "OLD-V6-STATUS": """\
                inet6num:     1981:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       SUBTLA
                source:       TEST
                """,
    ]


    static def permanentFixtures = [
            "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "OWNER-MNT": """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner@ripe.net
                mnt-nfy:     mntnfy_owner@ripe.net
                notify:      notify_owner@ripe.net
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "OWNER2-MNT": """\
                mntner:      OWNER2-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner2@ripe.net
                mnt-nfy:     mntnfy_owner2@ripe.net
                notify:      notify_owner2@ripe.net
                auth:        MD5-PW \$1\$9vNwegLB\$SrX4itajapDaACGZaLOIY1  #owner2
                mnt-by:      OWNER2-MNT
                source:      TEST
                """,
            "OWNER3-MNT": """\
                mntner:      OWNER3-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner3@ripe.net
                upd-to:      updto2_owner3@ripe.net
                notify:      notify_owner3@ripe.net
                auth:        MD5-PW \$1\$u/Ttxt8r\$zeII/ZqRwC2PuRyGyv0U51  #owner3
                mnt-by:      OWNER3-MNT
                source:      TEST
                """,
            "OWNER4-MNT": """\
                mntner:      OWNER4-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                upd-to:      updto_owner4@ripe.net
                upd-to:      updto2_owner4@ripe.net
                mnt-nfy:     mntnfy_owner4@ripe.net
                mnt-nfy:     mntnfy2_owner4@ripe.net
                notify:      notify_owner4@ripe.net
                auth:        MD5-PW \$1\$69rcgbTr\$Sh0R8PKbHIN5jf/Nv.FKk/  #owner4
                mnt-by:      OWNER4-MNT
                source:      TEST
                """,
            "RIPE-NCC-HM-MNT": """\
                mntner:      RIPE-NCC-HM-MNT
                descr:       hostmaster MNTNER
                admin-c:     TP1-TEST
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$mV2gSZtj\$1oVwjZr0ecFZQHsNbw2Ss.  #hm
                mnt-by:      RIPE-NCC-HM-MNT
                source:      TEST
                """,
            "LIR-MNT": """\
                mntner:      LIR-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$epUPWc4g\$/6BKqK4lKR/lNqLa7K5qT0  #lir
                mnt-by:      LIR-MNT
                source:      TEST
                """,
            "END-USER-MNT": """\
                mntner:      END-USER-MNT
                descr:       used for lir
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                mnt-nfy:     mntnfy_lir@ripe.net
                notify:      notify_lir@ripe.net
                auth:        MD5-PW \$1\$4qnKkEY3\$9NduUoRMNiBbAX9QEDMkh1  #end
                mnt-by:      END-USER-MNT
                source:      TEST
                """,
            "ORGLIR": """\
                organisation:    ORG-LIR1-TEST
                org-type:        LIR
                org-name:        Local Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "ROOT4": """\
                inetnum:      0.0.0.0 - 255.255.255.255
                netname:      IANA-BLK
                descr:        The whole IPv4 address space
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                mnt-routes:   RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "ROOT6": """\
                inet6num:     0::/0
                netname:      IANA-BLK
                descr:        The whole IPv6 address space
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                remarks:      This network in not allocated.
                remarks:      This object is here for Database
                remarks:      consistency and to allow hierarchical
                remarks:      authorisation checks.
                source:       TEST
                """,
    ]
}
