package spec.integration
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.source.Source
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class VersionQuerySpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        [
                "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
                "TST-MNT": """\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "TEST-RL": """\
                role:    Test Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: TR1-TEST
                abuse-mailbox: abuse@test.net
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
                "TEST-PN2": """\
                person:  Test Person2
                address: Hebrew Road
                address: Burnley
                address: UK
                phone:   +44 282 411141
                nic-hdl: TP2-TEST
                mnt-by:  TST-MNT
                changed: dbtest@ripe.net 20120101
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
                changed: dbtest@ripe.net 20120101
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
                referral-by: OWNER-MNT
                changed:     dbtest@ripe.net
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
                referral-by: OWNER2-MNT
                changed:     dbtest@ripe.net
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
                referral-by: OWNER3-MNT
                changed:     dbtest@ripe.net
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
                referral-by: OWNER4-MNT
                changed:     dbtest@ripe.net
                source:      TEST
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
                referral-by: LOWER-MNT
                changed:     dbtest@ripe.net
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
                referral-by: ROUTES-MNT
                changed:     dbtest@ripe.net
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
                referral-by: DOMAINS-MNT
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
                "RIPE-NCC-HM-MNT": """\
                mntner:      RIPE-NCC-HM-MNT
                descr:       hostmaster MNTNER
                admin-c:     TP1-TEST
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$mV2gSZtj\$1oVwjZr0ecFZQHsNbw2Ss.  #hm
                mnt-by:      RIPE-NCC-HM-MNT
                referral-by: RIPE-NCC-HM-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "RIPE-NCC-HM2-MNT": """\
                mntner:      RIPE-NCC-HM2-MNT
                descr:       hostmaster MNTNER
                admin-c:     TP1-TEST
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                auth:        MD5-PW \$1\$GAdTrvdG\$SEqxCNjKkR3ogcIq7teRv0  #hm2
                mnt-by:      RIPE-NCC-HM2-MNT
                referral-by: RIPE-NCC-HM2-MNT
                changed:     dbtest@ripe.net
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
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
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
                referral-by: LIR2-MNT
                changed:     dbtest@ripe.net
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
                referral-by: LIR3-MNT
                changed:     dbtest@ripe.net
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
                referral-by: END-USER-MNT
                changed:     dbtest@ripe.net
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
                changed:     dbtest@ripe.net
                changed:     dbtest@ripe.net
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
                changed:     dbtest@ripe.net
                changed:     dbtest@ripe.net
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
                auth:        MD5-PW # Filtered
                mnt-by:      RIPE-NCC-END-MNT
                changed:     dbtest@ripe.net
                referral-by: RIPE-DBM-MNT
                source:      TEST
                """,
                "TST-MNT2": """\
                mntner:      TST-MNT2
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$bnGNJ2PC\$4r38DENnw07.9ktKP//Kf1  #test2
                mnt-by:      TST-MNT2
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "TST-MNT3": """\
                mntner:      TST-MNT3
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$p4syt8vq\$AOwjgBvR4MA3o4ccMSMvh0  #test3
                mnt-by:      OWNER3-MNT
                referral-by: TST-MNT3
                changed:     dbtest@ripe.net
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
                referral-by: TST-MNT4
                changed:     dbtest@ripe.net
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
                referral-by: TST-MNT5
                changed:     dbtest@ripe.net 20120101
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
                referral-by: TST-MNT6
                changed:     dbtest@ripe.net 20120101
                source:      TEST
                """,
                "LIM-MNT": """\
                mntner:       LIM-MNT
                descr:        description
                admin-c:      TP1-TEST
                mnt-by:       LIM-MNT
                referral-by:  LIM-MNT
                upd-to:       dbtest@ripe.net
                auth:         MD5-PW \$1\$QYTtsWT5\$85vDaZp72krdzAYa7F3X20 # lim
                changed:      dbtest@ripe.net 20120707
                source:       TEST
            """,
                "PGP-MNT": """\
                mntner:      PGP-MNT
                descr:       used for testing PGP signed messages
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        PGPKEY-459F13C0    #RSA
                mnt-by:      PGP-MNT
                referral-by: PGP-MNT
                changed:     dbtest@ripe.net 20120101
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
                referral-by: TST
                changed:     dbtest@ripe.net 20120101
                source:      TEST
                """,
                "CHILD-MB-MNT": """\
                mntner:      CHILD-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$28Jr/8MuLiKvwiHIYC1C21  # mb-child
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-MB-MNT": """\
                mntner:      PARENT-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$G.oSSx.FZsRJWiXhhfDD21  # mb-parent
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-ML-MNT": """\
                mntner:      PARENT-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$/iBm2/pCiHFyPqSAiUHyI1  # ml-parent
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-MR-MNT": """\
                mntner:      PARENT-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:    MD5-PW \$1\$12345678\$9CtqUE3cI5b86PUAhP90p0  # mr-parent
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "EXACT-MB-MNT": """\
                mntner:      EXACT-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$aSbOhtjqSm2lwyTQ7Bur/.  # mb-exact
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "EXACT-ML-MNT": """\
                mntner:      EXACT-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$3Xfx8h0lFJhhi2tnbPXAF0  # ml-exact
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "EXACT-MR-MNT": """\
                mntner:      EXACT-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$rojUwYzu10ruwqO00IKh41  # mr-exact
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "EXACT-INETNUM-MB-MNT": """\
                mntner:      EXACT-INETNUM-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$AFX.FXWoEsfqJUYWs4hX8.  # mbi-exact
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-INETNUM-MB-MNT": """\
                mntner:      PARENT-INETNUM-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$gstFsI6qeiN9pwDIth8pr1  # mbi-parent
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ORIGIN-MB-MNT": """\
                mntner:      ORIGIN-MB-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$trXVUy8x5HyizFaU0CHT51  # mb-origin
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ORIGIN-ML-MNT": """\
                mntner:      ORIGIN-ML-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$0D0reGeFBPYowpN2PHKDP/  # ml-origin
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ORIGIN-MR-MNT": """\
                mntner:      ORIGIN-MR-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$12345678\$gKLHiPhaIfaniZB0kSZuB1  # mr-origin
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "DOMAIN-MB-MNT": """\
                mntner:      DOMAIN-MNT
                descr:       used for route tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$Sc/q29n8\$r5Ydny1YhQM4/m1mRWXr0/  # mb-dom
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "GII-MNT": """\
                mntner:      RIPE-GII-MNT
                descr:       used for enum tests
                admin-c:     TP1-TEST
                upd-to:      updto_lir@ripe.net
                auth:        MD5-PW \$1\$MrTjfqBG\$rOJhT7Qo9cLNbwalAt9Dd/  # gii
                mnt-by:      LIR-MNT
                referral-by: LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-ROUTE": """\
                route:       20.0.0.0/8
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "PARENT-ROUTE99": """\
                route:       99.0.0.0/8
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "EXACT-ROUTE": """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ROUTE-NON-ORIGIN": """\
                route:       255.13.0.0/16
                descr:       exact match route object
                origin:      AS999000
                mnt-by:      EXACT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "AS1000": """\
                aut-num:     AS1000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      PARENT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "AS2000": """\
                aut-num:     AS2000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      CHILD-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "AS3000": """\
                aut-num:     AS3000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      EXACT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
                "ORG1": """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST
                """,
                "ORGHR": """\
                organisation:    ORG-HR1-TEST
                org-type:        LIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST
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
                changed: denis@ripe.net 20121016
                source:  TEST
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
                "ORGRIR": """\
                organisation:    ORG-RIR1-TEST
                org-type:        RIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
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
                changed: denis@ripe.net 20121016
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
                changed: denis@ripe.net 20121016
                source:  TEST
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
                changed:     dbtest@ripe.net
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST
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
                changed:      dbtest@ripe.net 20020101
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
                changed:      ripe@test.net 20120505
                remarks:      This network in not allocated.
                remarks:      This object is here for Database
                remarks:      consistency and to allow hierarchical
                remarks:      authorisation checks.
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
                changed:      bit-bucket@ripe.net
                source:       TEST
                """,
                "KC-RSA": """\
                key-cert:     PGPKEY-459F13C0
                method:       PGP
                owner:        DB Test (RSA key for DB testing) <dbtest@ripe.net>
                fingerpr:     3706 556B E8CA 6E6D CDE7  4312 72C4 E6C3 459F 13C0
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                              Version: GnuPG v1.4.12 (Darwin)
                +
                              mI0EUM8WtAEEALnqIV3MGrTZpzspsUPFozlNYts2KK136IvmHNjySNSlp8inLTTq
                              hOU+6bdpQYsLJOhzlFwoH/RXdCouRJ64Xq3VginxqpYfww5PKuO3MHs6hkBZgted
                              I/+/qcBvK4PWTNeD6xEWvKFZiBPsijU7etXbo+K2hQOSu2LrbDncLFkBABEBAAG0
                              MkRCIFRlc3QgKFJTQSBrZXkgZm9yIERCIHRlc3RpbmcpIDxkYnRlc3RAcmlwZS5u
                              ZXQ+iLkEEwECACMFAlDPFrQCGy8HCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAK
                              CRByxObDRZ8TwLPkA/42vdjRKQ3zQmFYcjszCy5L/MLlj4gYjZkOJICVudLMz3c1
                              Ztda5JaUu+KnFZ664ekVLxLJY6coH1N9bxWKNSzKaoEx4WhV8OHGk2xdSkJHK887
                              f4UYpA4085JxwkgzljzxAxfLf1GQuSNw3eY0b3T2GDgXRQwcSl4xdufto0zERQ==
                              =t1N2
                              -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       owner-MNT
                changed:      bit-bucket@ripe.net
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
                changed:      dbtest@ripe.net
                source:       TEST
                """
        ]
    }

    def setup() {
        whoisFixture.getSourceContext().setCurrent(Source.slave("TEST"))
    }

    def cleanup() {
        whoisFixture.getSourceContext().removeCurrentSource()
    }

    def header = """\
% This is the RIPE Database query service.
% The objects are in RPSL format.
%
% The RIPE Database is subject to Terms and Conditions.
% See http://www.ripe.net/db/support/db-terms-conditions.pdf

"""

    def advert = "% Latest versions of objects shown. You can look up older versions of single objects using the \"--list-versions\" option.\n"

    def "--list-versions <pkey>"() {
      when:
        def response = query "--list-versions " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Version history for MNTNER object \"" + pkey + "\""
        response =~ "% You can use \"--show-version rev#\" to get an exact version of the object."
        response =~ /rev#\s+Date\s+Op./

      where:
        pkey << ["TST-MNT"]
    }

    def "-V foo --list-versions <pkey>"() {
      when:
        def response = query "-V foo --list-versions " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Version history for AUT-NUM object \"" + pkey + "\""
        response =~ "% You can use \"--show-version rev#\" to get an exact version of the object."
        response =~ /rev#\s+Date\s+Op./

      where:
        pkey << ["AS1000"]
    }

    def "--list-versions <person|role pkey>"() {
      when:
        def response = query "--list-versions " + pkey

      then:
        response =~ header
        !(response =~ advert)

        response =~ "History not available for PERSON and ROLE objects"

      where:
        pkey << ["TP1-TEST", "TP2-TEST", "TR1-TEST"]
    }

    def "--list-versions -T <person|role> <pkey>"() {
      when:
        def response = query "--list-versions " + "-T " + type + " " + pkey

      then:
        response =~ header
        !(response =~ advert)

        response =~ "%ERROR:116: unsupported query"

      where:
        pkey << ["TP1-TEST", "TR1-TEST"]
        type << ["person", "role"]
    }

    def "--show-version 1 <pkey>"() {
      when:
        def response = query "--show-version 1 " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Version 1 of object \"" + pkey + "\""
        response =~ /% This version was a (UPDATE|DELETE) operation on \d+-\d+-\d+ \d+:\d+/
        response =~ "% You can use \"--list-versions\" to get a list of versions for an object."

      where:
        pkey << ["AS1000", "TST-MNT", "ORG-OTO1-TEST", "10.0.0.0 - 10.255.255.255"]
    }

    def "-V foo --show-version 1 <pkey>"() {
      when:
        def response = query "-V foo --show-version 1 " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Version 1 of object \"" + pkey + "\""
        response =~ /% This version was a (UPDATE|DELETE) operation on \d+-\d+-\d+ \d+:\d+/
        response =~ "% You can use \"--list-versions\" to get a list of versions for an object."

      where:
        pkey << ["AS1000", "TST-MNT", "ORG-OTO1-TEST", "10.0.0.0 - 10.255.255.255"]
    }

    def "--show-version 1 -T <person|role> <pkey>"() {
      when:
        def response = query "-T " + type + " --show-version 1 " + pkey

      then:
        response =~ header
        !(response =~ advert)

        response =~ "%ERROR:116: unsupported query"

      where:
        pkey << ["TP1-TEST", "TR1-TEST"]
        type << ["person", "role"]
    }

    def "--version/--list-versions advert"() {
      when:
        def response = query input

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

      where:
        input << ["AS1000", "TST-MNT", "ORG-OTO1-TEST", "10.0.0.0 - 10.255.255.255", "TP1-TEST", "TR1-TEST"]
    }

    def "--show-version 3 -B AS1000"() {
      when:
        def updateResponse = syncUpdate new SyncUpdate(data: """\
            aut-num:     AS1000
            as-name:     TEST-AS
            descr:       Testing Authorisation code
            admin-c:     TP1-TEST
            tech-c:      TP1-TEST
            mnt-by:      PARENT-MB-MNT
            changed:     dbtest@ripe.net
            source:      TEST
            remarks:     updated version
            password:    mb-parent
            """.stripIndent())
      then:
        updateResponse =~ "SUCCESS"

      when:
        def response = query "--show-version 3 -B AS1000"

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ /remarks:\s+updated version/

        // no related objects
        !(response =~ /person:\s+/)
    }

    def "do not display versions for deleted object"() {
      when:
        def before = query "--list-versions TST"
      then:
        before =~ "% Version history for MNTNER object \"TST\""
      then:
        removeObject(oneBasicFixture("TST"))
      then:
        def after = query "--list-versions TST"
      then:
        println(after)
        after =~ "This object was deleted on"
    }

    def "do not show previous versions for recreated object"() {
      when:
        def before = query "--list-versions TST"
      then:
        before =~ "% Version history for MNTNER object \"TST\""
      then:
        removeObject(oneBasicFixture("TST"))
      then:
        addObject(oneBasicFixture("TST"))
        def after = query "--list-versions TST"
      then:
        after =~ /1.*ADD\/UPD/
        after =~ /2.*ADD\/UPD/
        after != ~/3.*ADD\/UPD/
    }

    def "--diff-versions 1:2 TST-MNT"() {
      when:
        def response = query "--diff-versions 1:2 " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Difference between version 1 and 2 of object \"TST-MNT\""
        response =~ "@@ -1,2 \\+1,10 @@\n" +
                " mntner:         TST-MNT\n" +
                "\\+descr:          MNTNER for test\n" +
                "\\+admin-c:        TP1-TEST\n" +
                "\\+upd-to:         dbtest@ripe.net\n" +
                "\\+auth:           MD5-PW \\\$1\\\$d9fKeTr2\\\$Si7YudNf4rUGmR71n/cqk/  #test\n" +
                "\\+mnt-by:         OWNER-MNT\n" +
                "\\+referral-by:    TST-MNT\n" +
                "\\+changed:        dbtest@ripe.net\n" +
                "\\+source:         TEST"

      where:
        pkey << ["TST-MNT"]
    }

    def "--diff-versions 2:1 TST-MNT"() {
      when:
        def response = query "--diff-versions 2:1 " + pkey

      then:
        response =~ header
        !(response =~ advert)
        !(response =~ /ERROR:/)

        response =~ "% Difference between version 2 and 1 of object \"TST-MNT\""
        response =~ "@@ -1,10 \\+1,2 @@\n" +
                " mntner:         TST-MNT\n"
                "-descr:          MNTNER for test\n" +
                "-admin-c:        TP1-TEST\n" +
                "-upd-to:         dbtest@ripe.net\n" +
                "-auth:           MD5-PW \\\$1\\\$d9fKeTr2\\\$Si7YudNf4rUGmR71n/cqk/  #test\n" +
                "-mnt-by:         OWNER-MNT\n" +
                "-referral-by:    TST-MNT\n" +
                "-changed:        dbtest@ripe.net\n" +
                "-source:         TEST"

      where:
        pkey << ["TST-MNT"]
    }

    def "--diff-versions 0:1 TST-MNT"() {
      when:
        def response = query "--diff-versions 0:1 " + pkey

      then:
        response =~ header
        response =~ "% diff version number must be greater than 0\n%\n" +
                "%ERROR:111: invalid option supplied"

      where:
        pkey << ["TST-MNT"]
    }

    def "--diff-versions 1:3 TST-MNT"() {
      when:
        def response = query "--diff-versions 1:3 " + pkey

      then:
        response =~ header
        response =~ "%ERROR:117: version cannot exceed 2 for this object"

      where:
        pkey << ["TST-MNT"]
    }

    def "--diff-versions 1:2 TP1-TEST"() {
      when:
        def response = query "--diff-versions 1:2 " + pkey

      then:
        response =~ header
        response =~ "% Version history for PERSON object \"TP1-TEST\"\n" +
                "% History not available for PERSON and ROLE objects."

      where:
        pkey << ["TP1-TEST"]
    }

    def "--diff-versions 1:2 TP3-TEST (deleted)"() {
     given:
        syncUpdate(new SyncUpdate(data: """\
                    person:         Test Person3
                    address:        Hebrew Road
                    address:        Burnley
                    address:        UK
                    phone:          +44 282 411141
                    nic-hdl:        TP3-TEST
                    mnt-by:         TST-MNT
                    changed:        dbtest@ripe.net 20120101
                    source:         TEST
                    password: test
                    delete: reason
                    """.stripIndent()))

      when:
        def response = query "--diff-versions 1:2 " + pkey

      then:
        response =~ header
        response =~ "% This object was deleted on \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}\n"

      where:
        pkey << ["TP3-TEST"]
    }


}
