package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import java.time.LocalDateTime
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Ignore

@org.junit.jupiter.api.Tag("IntegrationTest")
class SignedMessageIntegrationSpec extends BaseWhoisSourceSpec {
    //FIXME [TP] this workaround with the authenticator and the principalsMap is a hack to...
    //FIXME [TP] ...temporarilly allow hierarchical *mail*updates with power maintainers. Do not replicate this logic.

    static net.ripe.db.whois.update.authentication.Authenticator authenticator
    static Map principalsMap

    def setupSpec(){
        resetTime()
        authenticator = getApplicationContext().getBean(net.ripe.db.whois.update.authentication.Authenticator.class)
        principalsMap = ReflectionTestUtils.getField(authenticator, "principalsMap")
    }

    def setup(){
        restorePowerMaintainers()
    }

    private static void clearPowerMaintainers() {
        ReflectionTestUtils.setField(authenticator, "principalsMap", Collections.emptyMap())
    }

    private static void restorePowerMaintainers() {
        ReflectionTestUtils.setField(authenticator, "principalsMap", principalsMap)
    }

  @Override
  Map<String, String> getFixtures() {
    return [
            "TEST-PN"             : """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "ROOT4"               : """\
                inetnum: 0.0.0.0 - 255.255.255.255
                netname: IANA-BLK
                descr: The whole IPv4 address space
                country: NL
                org: ORG-HR1-TEST
                admin-c: TP1-TEST
                tech-c: TP1-TEST
                status: ALLOCATED UNSPECIFIED
                mnt-by: RIPE-NCC-HM-MNT
                source: TEST
                """,
            "ORGHR"               : """\
                organisation:    ORG-HR1-TEST
                org-type:        LIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         RIPE-NCC-HM-MNT
                mnt-by:          RIPE-NCC-HM-MNT
                source:  TEST
                """,
            "RIPE-NCC-HM-MNT"     : """\
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
            "OWNER-MNT"           : """\
                mntner:      OWNER-MNT
                descr:       used to maintain other MNTNERs
                admin-c:     TP1-TEST
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                upd-to:      dbtest@ripe.net
                source:      TEST
                """,
            "INVALID-PGP-KEYCERT" : """\
                key-cert:       PGPKEY-57639544
                method:         PGP
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
            "INVALID-X509-KEYCERT": """\
                key-cert:       X509-111
                method:         X509
                certif:         -----BEGIN CERTIFICATE-----
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
            "PGPKEY-5763950D": """\
                key-cert:       PGPKEY-5763950D
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
            "PGPKEY-28F6CD6C": """\
                key-cert:       PGPKEY-28F6CD6C
                method:         PGP
                owner:          Ed Shryane <eshryane@ripe.net>
                fingerpr:       1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV
                certif:         4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF
                certif:         7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz
                certif:         RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0
                certif:         VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt
                certif:         Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz
                certif:         aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL
                certif:         BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi
                certif:         Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX
                certif:         /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV
                certif:         gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK
                certif:         yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC
                certif:         H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP
                certif:         ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1
                certif:         qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl
                certif:         sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN
                certif:         LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e
                certif:         27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah
                certif:         P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ
                certif:         EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml
                certif:         xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q
                certif:         mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM
                certif:         QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh
                certif:         4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj
                certif:         kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7K5AQ0EUK5TZgEIAN3AW1KG5ooZ
                certif:         Hh9OqDfyM5VxhSvcx+ZqiPUyX4bMXENSF5PQvlcySJxx+KFWj3Pa8xMZwA3HpPJt
                certif:         fs9v+mA8ph+zyYh1W0cqsC9rAOy236LdvZi1nIw/kA8rgOJxcfMEpmYIkSkfFg+G
                certif:         gTE7YCMtRxvWJfrZS0M1wTrVhOGgD3jgJW5n+lKZ0E8AzHhNqdV+Z2dtyNDQznNT
                certif:         7609FVzXyph3zImC/DH33iq6ISZzy7zHePdFyLVyVeB8ru/BejkzDd8L+sgjxkeE
                certif:         pGv/crX98bRE9cIbJp/z8V/VEdbghXI79wDcpZ9WX1fvBhK7fKVqDzKw9z41HcU6
                certif:         acC9mQwMC6cAEQEAAYkBHwQYAQIACQUCUK5TZgIbDAAKCRDumR7iKPbNbEeOCAC7
                certif:         NcLqNUxICkMO14Vlas03N2/aRqqIJWqXOv4LLsO7YOpPDhaZltZaMUM275p0fIR8
                certif:         DUs6we7MaTvlXdpaWY6oKwOuH4TX4DZrRMhfB1lkwg63WEhMxY/RKKZ0EPm6ztCk
                certif:         A0+gn1AETOtWffHmGbjTtcFvRQhgHDR50B/IDN2deM3a2QDjFAQzunrwoJrpZx0M
                certif:         R+8mWF4q7jLcfY5YuE1sNilXJayeeD1noM1+9Bz8KoXPsCIQDmatRLbv/DDGhuCG
                certif:         pVCQtAEMbznmrO7ah+hbG+pRsF/KLHYafYRSzFnA+fR1VZ7nhiemBk2jDrlMeDSd
                certif:         +2zV4xrBsgyoXIHB/Rel
                certif:         =Aova
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
            "PGPKEY-C88CA438": """\
                key-cert:       PGPKEY-C88CA438
                method:         PGP
                owner:          Expired <expired@ripe.net>
                fingerpr:       610A 2457 2BA3 A575 5F85  4DD8 5E62 6C72 C88C A438
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mI0EUOoKSgEEAMvJBJzUBKDA8BGK+KpJMuGSOXnQgvymxgyOUOBVkLpeOcPQMy1A
                certif:         4fffXJ4V0xdlqtikDATCnSIBS17ihi7xD8fUvKF4dJrq+rmaVULoy06B68IcfYKQ
                certif:         yoRJqGii/1Z47FuudeJp1axQs1JER3OJ64IHuLblFIT7oS+YWBLopc1JABEBAAG0
                certif:         GkV4cGlyZWQgPGV4cGlyZWRAcmlwZS5uZXQ+iL4EEwECACgFAlDqCkoCGwMFCQAB
                certif:         UYAGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEF5ibHLIjKQ4tEMD/j8VYxdY
                certif:         V6JM8rDokg+zNE4Ifc7nGaUrsrF2YRmcIg6OXVhPGLIqfQB2IsKub595sA1vgwNs
                certif:         +Cg0tzaQfzWh2Nz5NxFGnDHm5tPfOfiADwpMuLtZby390Wpbwk7VGZMqfcDXt3uy
                certif:         Ch4rvayDTtzQqDVqo1kLgK5dIc/UIlX3jaxWuI0EUOoKSgEEANYcEMxrEGD4LSgk
                certif:         vHVECSOB0q32CN/wSrvVzL6hP8RuO0gwwVQH1V8KCYiY6kDEk33Qb4f1bTo+Wbi6
                certif:         9yFvn1OvLh3/idb3U1qSq2+Y6Snl/kvgoVJQuS9x1NePtCYL2kheTAGiswg6CxTF
                certif:         RZ3c7CaNHsCbUdIpQmNUxfcWBH3PABEBAAGIpQQYAQIADwUCUOoKSgIbDAUJAAFR
                certif:         gAAKCRBeYmxyyIykON13BACeqmXZNe9H/SK2AMiFLIx2Zfyw/P0cKabn3Iaan7iF
                certif:         kSwrZQhF4571MBxb9U41Giiyza/t7vLQH1S+FYFUqfWCa8p1VQDRavi4wDgy2PDp
                certif:         ouhDqH+Mfkqb7yv40kYOUJ02eKkdgSgwTEcpfwq9GU4kJLVO5O3Y3nOEAx736gPQ
                certif:         xw==
                certif:         =XcVO
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         OWNER-MNT
                source:         TEST
                """,
            "PGPKEY-8947C26B": """\
                key-cert:       PGPKEY-8947C26B
                method:         PGP
                owner:          Test User <revoked@ripe.net>
                fingerpr:       610A 2457 2BA3 A575 5F85  4DD8 5E62 6C72 C88C A438
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:         
                certif:         mI0EXFm2FwEEALc4QJzSrefgg33AOHhS45L2kbSNTcXNVmVfk5ra2h3kr9ia8C5I
                certif:         yLBz78108XD+0QwdMM3/acaJPqUxOkVzmwf5ydd1nJn1ZeLznfrSWnvb4DSxNGeU
                certif:         yVm8j6I53Ay5WDEJWUu3XQzUHnqYeb3Fcwa5MjPzf8iBbFmdi6riLLBHABEBAAGI
                certif:         tgQgAQgAIBYhBHSJUuDx58YlX1PWwpKhmKCJR8JrBQJcWbZ5Ah0AAAoJEJKhmKCJ
                certif:         R8Jr6kkD/10EHYfhXVxwF5zeH6hMKEBQLYtJMo2fcK7055njT6PTS3tVWnjQ2UDB
                certif:         8ExA34/LJuDXn19qZSpAM4NP2SwxpC8kPecvY+0Akdu3mwV8X525/A4eQ1l0+pF6
                certif:         TL0gF9+kLLRyIg9Qbme1tf2734gu8JfKZek83O/9prv1xnsYz3ArtBxUZXN0IFVz
                certif:         ZXIgPHJldm9rZWRAcmlwZS5uZXQ+iM4EEwEIADgWIQR0iVLg8efGJV9T1sKSoZig
                certif:         iUfCawUCXFm2FwIbAwULCQgHAgYVCgkICwIEFgIDAQIeAQIXgAAKCRCSoZigiUfC
                certif:         a2BDBACEP9SlUsPCRcsAFlM/lg/7prXSLzZhi3gpVYKkEDKRafpBBa5XcfmoSSiY
                certif:         r6hcwq5r5O0ezfLVi75VeZq+R8CsaSWDqp0FFS7n/2o87PIyZog0fyqrJIt/97Tn
                certif:         mOpX1wWbFEBC25k52jUP10VA7jPDq12b/8BrqCSD+aD5y7rVR7iNBFxZthcBBADO
                certif:         z3HfUAcmk/DeFOJWjYhUj/b0m+pAG/2PLEUrj9DelJeRwEa8dTUN1AaTdn5pvf2p
                certif:         qPXPr9EHRSdhum5kFq4SkrEW9wYrvdfYVAs4UTCC/xjP6JDAYRWc153yzJaFeRk4
                certif:         TZn76PA957bekHTKk1jwOgJmqQ+Mjpzv1IK4vZnvswARAQABiLYEGAEIACAWIQR0
                certif:         iVLg8efGJV9T1sKSoZigiUfCawUCXFm2FwIbDAAKCRCSoZigiUfCa5mzA/sF3aZW
                certif:         m2+4zh9w1qWHkARTu4aB8YzaT7cLihMYS94h/wzcJPbMDmhUJZNtVkKC2OEvaeSw
                certif:         RpZrD3N2Cq5uuELopJhaDFpnKntc2NmmUn8P06gW0Ep1uyObPJfID/xDWznZH8SQ
                certif:         357CfW0mENdBquWnAtGxABuv//JeCp0Ar3WkEg==
                certif:         =Ofyo
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         OWNER-MNT
                source:         TEST
                """
    ]
  }

  def "inline pgp signed mailupdate"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:06:50")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-AAAAAAAA       # primary key doesn't match public key id
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-AAAAAAAA\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTJbeAAoJELvMuy1XY5UNz4kH/1u8udH8GzJItsFxuwrJgSHO
                J86uYoIUReXj075zwPJOiGQcXgF1hSUno1KkE/LjHrAADLhTwOi58hvdmnWSHqb4
                nZslzwNc527BDqxroygwVZNzOoB8QFwfLqujr+F1CmbFSd5RX6vVF8xGmf5QYYdd
                tPhj9kGJpt8TvamQI5mSPqWpmeeaZqHLHSHDWrmmo10suhyrcWz6/hCUReuxJmFx
                3/Af/WhlINZ7LILo7Ni23BhltZTWfXQZuDOsSRRRU47e4au+Err9ubBRPYZaZvwj
                kichVGo7GB7vQjpdBmQUpK6O0Rpjejsq7qkEsyFuHZulpL5rcnGtPsQUO/FXU1k=
                =jZ51
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 0, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "inline pgp signed mailupdate with DSA key and RIPEMD160 Hash"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:06:50")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-E5F13570
                method:         PGP
                owner:          DSA Key <dsa@ripe.net>
                fingerpr:       FCEC 5061 27C4 1BCD 18F8  AE6D 446C D3BD E5F1 3570
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:
                certif:         mQGiBFFkCDwRBADNr7Cs+XQ1uy1qeG913uc/HBPUgffA7i7BOBWCLTkDw2nbVhDO
                certif:         ASKW+S2CutgI/ufWYlyrSYklE8zyhYC8m/Lz6G9ZV0V7jNro0NJHV+QaPNi1TQ7u
                certif:         0MB+u1U3O4CvhOgNYn4v6GJxZq73kfsbhWEVN2m3xRuwU7WuK3DjyfdOYwCgzUhB
                certif:         ncQB+l3Wqqf353u1Z+zz+yUEAMkM42BBKeK3k0iS6yBvNx0fo0RW7ZX7WzOgX033
                certif:         BFbDoi1PTE584iK+/fMIKtpR+RIiim0EcPOCIGVs4eyt0Iktji/wjmk2zcEKm2Ls
                certif:         kSOo3tsKtc0LDrE56tAyQwtTxhFG00i8v0E0j9k01GQIxkcYgaPP1X8x/+TlokVv
                certif:         g2GdBACGuBP0KIQTos3k9rNkyFdw7pM6Qy9SS6Qf1hlYbYkmCITEbwOT1K1mfv2v
                certif:         Bqz2pcaAhOHdBgUvZnHugCbKHFplBQ4I+hl9m7B7CAKPWHuIoRe9CboAENl8YMaj
                certif:         mBOUdBr4ZyXhbdDKNvlMvZHtJmHw0+/OAlQwG6imqSVWFykrw7QWRFNBIEtleSA8
                certif:         ZHNhQHJpcGUubmV0PohiBBMRAgAiBQJRZAg8AhsDBgsJCAcDAgYVCAIJCgsEFgID
                certif:         AQIeAQIXgAAKCRBEbNO95fE1cNmPAKCldAcZtWRG8tiJkx/fPjFBlq7uFgCfX0Cj
                certif:         AQJkry0+lVRZdbNeeNsT0jA=
                certif:         =7PXv
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-E5F13570\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: RIPEMD160

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iEYEAREDAAYFAlZMohoACgkQRGzTveXxNXA7bACfdQfGg8GDPNj9mJL6lRCj9fLU
                tmkAoIkbaEShTyG3wS/uCqQpzKFsOROt
                =q0GT
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "inline pgp signed syncupdate with invalid existing key"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).replaceAll("source:\\s*TEST", "auth: PGPKEY-57639544\nsource: TEST") + "password: owner")

      def ack = syncUpdate new SyncUpdate(data: """
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1.4.12 (Darwin)
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJQwIPwAAoJELvMuy1XY5UNmTgH/3dPZOV5DhEP7qYS9PvgFnK+
                fVpmdXnI6IfzGiRrbOJWCpiu+vFT0QzKU22nH/JY7zDH77pjBlOQ5+WLG5/R2XYx
                cy35J7HwKwChUg3COEV5XAnmiNxom8FnfimKTPdwNVLBZ6UmVSP5u2ua4uheTclR
                71wej5okzHGtOyLVLH6YV1/p4/TNJOG6nDnABrowzsZqIMQ43N1+LHs4kfqyvJux
                4xsP+PH9Tqiw1L8wVn/4XefLraawiPMLB1hLgPz6bTcoHXMEY0/BaKBOIkI3d49D
                2I65qVJXecj9RSbkLZung8o9ItXzPooEXggQCHHq93EvwCcgKi8s4OTWqUfje5Y=
                =it26
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      ack.contains("Create FAILED: [person] FP1-TEST   First Person")

      ack.contains("" +
              "***Error:   Authorisation for [person] FP1-TEST failed\n" +
              "            using \"mnt-by:\"\n" +
              "            not authenticated by: OWNER-MNT")

      ack.contains("" +
              "***Warning: The public key data held in the key-cert object PGPKEY-57639544 has\n" +
              "            syntax errors")
  }

  def "inline pgp signed syncupdate"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:12:27")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-AAAAAAAA       # primary key doesn't match public key id
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-AAAAAAAA\nsource: TEST")
                      + "password: owner")
    then:
      def ack = syncUpdate new SyncUpdate(data: """
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKNrAAoJELvMuy1XY5UNXowH/A5w/goXTy/t4tgYR8uFEznD
                dZz3tTT1LOTPA4HBSrbeIWVSBH9gWaOQz775B9DVjks2+YjORXD+UbnYZja0xioy
                0oKAC/X5If6Ock5NBlUNz8eOWO+G9BMleJwD/tkBbEw7MgqRzK5/I2wbVjnmmN/z
                g4gs1QX5LE5GApiYzzRhEfST2ugti3yAqwW6ohz+T85iHZYamo00ft0CEOGD9mkt
                bv8IVqGN7tghLvLsjwua1J+yVWm6iLQ32I0xkBANX+6qCh7HAUX2FTJt0iTfdKnz
                f30WkWsIMzmKAvIe0kGqQ2DVWiYqxektx1MgElhz6+fZWjLNcJQ3HycG9PF+oYs=
                =EeZC
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "inline pgp signed syncupdate including spaces and extra header lines"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:15:43")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def ack = syncUpdate new SyncUpdate(data: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1
                Comment: another header

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKQvAAoJELvMuy1XY5UNnfAH/3MJ88FLgBB5vQXEXnALMrke
                Q7wtsogCR1Uh4hOpG5Zw3CzKVz7cFBEnerlrh+uUIAJCRqW2GM2UuPGZ2qiCSG1A
                j6J9w+67409yuztBdfB/Mr7dKqn/y9dDzXu4h/UEfK9Shvpziv2nBrJGuIg05NCj
                3JBW4NW6uktQedJsrp6pTFp2bUFvCZ4IYdJpyTNT8lSjFsDUPaEE60Kk4QqEkPrn
                XWMLzp+pr/aXEmf73DxnKwrBtjzq9rN0etSPWg2CanRsewJlIZK3ze2qpJw88Fgx
                Aud70JClL28GkoykbGqv9tf8mw78YuazTxhhNK4lw5YTDNNWahA/r9rDj21XiY8=
                =KrRX
                -----END PGP SIGNATURE-----
            """.stripIndent(true).replaceAll("\n\n", "\n  \t  \n"))
    then:
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "inline pgp signed mailupdate with extra empty lines in content"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:15:43")) // current time must be within 1 hour of signing time

    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                    -----BEGIN PGP SIGNED MESSAGE-----
                    Hash: SHA1




                    person:  First Person
                    address: St James Street
                    address: Burnley
                    address: UK
                    phone:   +44 282 420469
                    nic-hdl: FP1-TEST
                    mnt-by:  OWNER-MNT
                    source:  TEST





                    -----BEGIN PGP SIGNATURE-----
                    Version: GnuPG v1
                    Comment: GPGTools - http://gpgtools.org

                    iQEcBAEBAgAGBQJWTKRtAAoJELvMuy1XY5UNx94IAInpIGqdsz8Di4cY5Sdt1zL2
                    m54vg2bgEWY8mZ1RNTA0MgIFf5MD4hm+MZsW8DzKcqorfwDaxNgx8STgJt1xjuEi
                    ho+t8e39bFDYQBnNIz/hai4yCraBixs4FjKavL9wviJRV2InPMlvHbFwIptw3oC7
                    kloPtdhg+ALtwwuHgY2D+GBgIrTEAnTP32pBe4YCTn+9n2AmljKNACSr6U1z++qB
                    XYkKHQacwYOvo8LgVjEjNOfXe7OO3bIUv5KK2D7Rip90ypq3+pPl/KVe8XVggtB/
                    i+UXPIrHzJ3HUw0w+gLEPMJOoGLhClFua/MFoROLG3Ka9RKve0ayDFWRjeVQt5k=
                    =kiEz
                    -----END PGP SIGNATURE-----
                    """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 0, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "inline pgp signed mailupdate with invalid keycert referenced by mntner"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-57639544\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKUKAAoJELvMuy1XY5UNyZYH/RvVPXa8FX+Xhe600HvTM2JQ
                4tUSqJJpvL5lJL2y1Jr4NZso+V9Vr7VpURsakFbNzsgNeJfhdF/w4YhekVyXZ+V1
                ad+VhVmfGjVGh47jyI+Dak0RKz06IBkWsl6ZFD4VhnGuTelj4VumhqNflciDBg70
                WlKjVcbdL9bEIFK67NSBNhGqIVb4eg/0mnGUqL70MKpuRHZpANgBLSguXMduWyir
                yFMJ6ELTJ9VC18lKbNMvtBH0osaow5A3Sa3NAOJmjIekuB+qdghZtoXwTE3gY2GR
                38ZFNyJmTcoQ4nV3X6glwAg/lfWPhG0EByV/uhzmilv1Midetv2626/Cecn+tS0=
                =0+bz
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "inline pgp signed syncupdate with SHA512 hash"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:30:38")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def ack = syncUpdate new SyncUpdate(data: """\
            -----BEGIN PGP SIGNED MESSAGE-----
            Hash: SHA512

            person:  First Person
            address: St James Street
            address: Burnley
            address: UK
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            source:  TEST
            -----BEGIN PGP SIGNATURE-----
            Version: GnuPG v1
            Comment: GPGTools - http://gpgtools.org

            iQEcBAEBCgAGBQJWTKeuAAoJELvMuy1XY5UNQGUH/1uTFrSPwgktyJ9LLYhjEHuQ
            oQRIxwpI6dNR1zhOHZppkUYyG3L3yl/qHSbs/Or/roEwUjUEKeeQ9ROS+XrgFIAL
            EVNQViS8y4lCoZE/Cp2emMKWmX5eZPIH81nH66Vhd0vDn6p5dytUxWMKJyrRSH4s
            akQKe3fGyYrbZJ35csTFHYJcc0KdbyrlC/c4jQhwvRGBwVdM5+5L20MALZTQ6UWC
            eKHeOkkezteFpBoDJJIHdroFnqKgEs97rkEle6cLhyH7lXk6csQy6UdV9kwyelCj
            PZk4tkptHQHJKRRBBTnoTWO0DWpspaFoXf2pdPnluL8XB7IKCi9FQrm4Atc13o4=
            =VzjZ
            -----END PGP SIGNATURE-----
            """.stripIndent(true))
    then:
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "inline pgp signed syncupdate with 4096 bit public key"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:31:42")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-E7220D0A
                method:         PGP
                owner:          Testing 4096 bit key <long-key@ripe.net>
                fingerpr:       2C4A A5B5 19BC D919 9585  0A90 69FE 3396 E722 0D0A
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQINBFDqnZUBEADY+QqoS4tE7SmAGvwgOCC5d4sTctRVF27lrNaxGivPde+e2IDK
                certif:         LL1k3c0RTTbULurYr9KSLisSRo/Rucj1PjMPdwaUbMGE2WoBNXSWb2B2aoWxw6W2
                certif:         t0knjtDRgFXe8n3lHLlXk+NgJ7Cz3FmgoavNtkFeYHnHt5ulMeGug4iOGdyZs+Ly
                certif:         5YKjnnXd1HO2KBkG1QzFM2uVYpIeA9DuzLSkWN7R2avW8rmj5lLg21XTmtOwiq2u
                certif:         vSSaKTzr4JwuJ/mKgIhJFGaho6YCzZ7MKKBevHpqze2/JPvyR7IX27qZVqwLJQ3Z
                certif:         3CEByLqn07WjsvaeE+yFmzwsX9dfhz4nP53475Bddk60eHq32rn0ctFqk3h5aZV4
                certif:         aTUyK2uWM7SNeJJBxl4IyvKU5fKUwZXYendt0UPzXRqQOf7L1eL8vy2FqXiWkJZd
                certif:         Sxzo7kl4E0Zn7tJWii2qkCp0sx4zeozcIIkDb9SUJo5WMzfqRGeZccxLuAJMIdA5
                certif:         9AmCQ71ZPwPsE/A/KrxM7sI+Xq2A49+OMOQAXdHxVmg6tNZuHTB1Cj8TJhQ1eMzd
                certif:         +NLPLcu34tOTJjw7L9xFHCR8UjE/cfmH3bGCCYzth6ywFg2/OCvToWv0tQjIkp6p
                certif:         HCKmdy57PXxTA3WVHSMpQ8yxa6E9Yf2wgcx4xptej/ljcVuFyQ6B4ZkbLwARAQAB
                certif:         tChUZXN0aW5nIDQwOTYgYml0IGtleSA8bG9uZy1rZXlAcmlwZS5uZXQ+iQI4BBMB
                certif:         AgAiBQJQ6p2VAhsDBgsJCAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRBp/jOW5yIN
                certif:         ChibEADBvlU/8fWNQ0ozu+EAf6vj7QmeG2VF/rXxfhu4ha5VPMLT4v94L3autmay
                certif:         BfH09ZIpgvHFWjwMS/8oFnhTm2U1kvRGHrtLKupGWzeoX2bHB6ZXfx56xW56hzjc
                certif:         r1LOODJe+mBCmiEYQNuJMf0lpw8BF74Na6iWtsHJGPQcvcjN8mHc3PmxD94tfTY2
                certif:         d/0uJaceif5sVKt4sCBqLdXgeqgNC7RxQyVgkGM+3Gfxwar9v/c2Pq9/n1MOZEI0
                certif:         lpoVMZUftUssi2/7Et9DJk8X1+oTCLzYtofK9CKPD0LEoBvvvMOSetiDwId5qVfV
                certif:         KjCngnX0WKCIkPouYAIGE2nrELGTX7WrkMYFhomkoBcBc+GMWDo1c6SzV0VuvYvn
                certif:         gZUwZN5D+SUA2jIT42JaV3mV2zZc8slsrj8dErakTjMQII9i6o29q7VKtx0zr+gV
                certif:         TEmZ1ZVSxK+vs1ZKdSCP0OFiiyxEYuErmJH008Gt/Kw432utnx7v7CbvZC4omUNt
                certif:         +K6yhXcyLZwdf+N+SM/2mgI2S0Eq3UhtYMBnxu/i1v+NaExvzVEJHDspFvaV0/PR
                certif:         ma9QXAmreeLeRTQ1irubsz75pPqSamzh5gMHgr6e5JDEJIRfVtwOj9bigDqNDM5o
                certif:         9xgg7J69qoSiGtvQoWYEfISfHovcqY4QNn4iWebVaCDIP2HkgbkCDQRQ6p2VARAA
                certif:         6kaXaUHPmpMpgsRsCTRZgwzXSFT93/Rj7VVP7R3hUtt19c9tR6qq4vcXqIa/QAr7
                certif:         eeWrxQm1F0vdB5boLua7VNf/fdB5G6dCA8PIN8NPnLPFGA4PPCTAHj78h61BeFLB
                certif:         WmXlTvtvc3M33k0upW0t7X3MBHNyLGejA9mv9gPQuAMk4+GZbLYggkxaPMXjOS2u
                certif:         L/jqah7nUQlFIc5ScNMV6E1mLfeCpBAXmijnDkVwb8R00/fcdHVl5OSbYXZACLXI
                certif:         X50dzjIZQ7k1UUStw0knoZz33LJjhJ5zdxHLSjeT2ozqRjT9tJaB8NzpP2sa2qTX
                certif:         5EXPHUy5OjX2BsToQcTsBvGXSquB0Z2u1h6RD23CQNkFgah3WE6kCAz1n1im+/Uy
                certif:         BnOgVKmsbaKsyd2ZtuLwBQRRmU4Qlb+2lIId9rM6fG9VmS2A3KsP1DwphmLvTwT0
                certif:         p2ubCi1Lm5wPouZftsrt9mrr1Q9xDBHZleWjXZFrVKxoKzdtwarnw9sHr3oXnLul
                certif:         00iP2gXyXlknPA1Msq8eXcC6iX/ofTTgQF2xUqBrhq5exOwiSD0jQA2iklZTYjw8
                certif:         EXkHImnkIS4k66QEjVnoEqVxCR7JJqD+BatMLvmv+2tGJXYFsxFQ8Zk0IwuJKbv6
                certif:         0OQHFOhSlbW2F5GDKeb0ZfVoGcXQndxiW4vhrsRP88cAEQEAAYkCHwQYAQIACQUC
                certif:         UOqdlQIbDAAKCRBp/jOW5yINCvlRD/0fViWEjt1QLxdj1/ml4B7SiS5fabE9fnRj
                certif:         UtFjPHGI+tJb2aAelOPChuxTh0T5YxFHaIMjRA+a9DjutBjnmYoSGLlp3ZIFrAzF
                certif:         qOXP4DYP/Teg+QgbatjsZMdU8K6B6G7JlJWrLGMURSsZ3sKVyv6NlKAGkfKJiKyL
                certif:         ss8wsTDm8VVQJH+WXntKMdFUdL1K6MGq4ijm3b3lFMvjZrP12T6l0rr5t/lihPhc
                certif:         MfXfw/idD5y3d3gavW0klfLf3iZE9SXKaumNPw6CDAMqegeSiXQVxFoal1vO7rSm
                certif:         CifYePPc7VMLDy+1JW9IM8rzaYUpf7FXkY07GIInFNe4ibV0P40oGxspdFR6irwj
                certif:         LwHPxeHap17N3ca/xdesm5kIfbxE/c7Q2FeZ7rYN88ElVMJiC+ETJC4dYqR5S4sG
                certif:         OMvMoIkjA8I17rBiNE65voCRgUxoqj5sbAXnPZQf+rG7IeWdiCvT50gMTKG4exrG
                certif:         LcT0ByruHmAfISqDvhUqrV9RoH0oBWyV/LKa0DDwsUusJFDuvIxCq1fqKHv6IF+q
                certif:         Edjou/3zMx/gqnq6Hudtfs78Rbw1REJPidEmPh3k9GV7tPwSFGYaSvWfB1gbVond
                certif:         ps58MLBDWKEJyHm0GZq2ookGyZg5Fy0SBBJ3r932OxT4UFoSZn7Evrkzkw3WzpZ0
                certif:         xulqMmMYIQ==
                certif:         =Iya+
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-E7220D0A\nsource: TEST")
                      + "password: owner")
    then:
      def ack = syncUpdate new SyncUpdate(data: """\
            -----BEGIN PGP SIGNED MESSAGE-----
            Hash: SHA1

            person:  First Person
            address: St James Street
            address: Burnley
            address: UK
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            source:  TEST
            -----BEGIN PGP SIGNATURE-----
            Version: GnuPG v1
            Comment: GPGTools - http://gpgtools.org

            iQIcBAEBAgAGBQJWTKfuAAoJEGn+M5bnIg0KL/0QALvdeDkq9/ZHuDL72pAr0BP9
            jhDOtN3ghS5m21r7yg4AzX2ayq/VYuZ8VgL5AYEHdPiSp9SVb+PcKXeMc8aO3sj5
            9W424gp5Wiz+dstfZnA3aN1vaAgiOdLfk66gP2eOapwxPlBUZuV/DfhN9RtXvH1l
            nAPUtRoQc8gqxE64F7S3ZkVEQHCzK2GM6KQOd98VX0X90GXEl54nBE/oCuKffBcb
            +Soqa9/upADC6Z/RrQrzmz/VEf1toq424YvyiO+KyzPhuOsEfR1H9AEUgKZo6f1D
            uVzDEalplhyBq7wj+MH5YGXnCVzTwPrFtWsNJ11UPJXkg6yeX+kOSVu9bZIcvRhM
            C6cJ/6ADP+qRZo4Eaq4XBhbE9cmlwSYXZmokmdln25MJhhYoGdDxWdiu9bxOwqFw
            m8j0RXSuOC4Z5H5DibFLXMggkDy+B0r0xMG6Y815Cnu0tTMuXw4UfggiCUWzpstb
            YJsxjGyA4As1KivN8REL9uB6lHT/OF0fJh6exYBTu71x9WIHURSdpmgrDZQXZ8/w
            nz9vVtebGLpT2G2AVfDfOGBX9iR7uR/kcZCxny/MCdC4+qZBqWQCNeSyu5zyEJ10
            BFeBwH1dJ/r7H+uNbVJVGnu7U5et9B/xiKRSm6MkBIMxbDK+8oSfHMWHY4NTSsjW
            GrQn43LxX0fhr4sadDvs
            =/OHS
            -----END PGP SIGNATURE-----
            """.stripIndent(true))
    then:
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "inline pgp signed mailupdate signed by second subkey"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:33:17")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-28F6CD6C
                method:         PGP
                owner:          Ed Shryane <eshryane@ripe.net>
                fingerpr:       1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C
                remarks:        keycert contains 1 master key (28F6CD6C) and 2 subkeys (413AEB52 and 80274330)
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV
                certif:         4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF
                certif:         7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz
                certif:         RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0
                certif:         VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt
                certif:         Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz
                certif:         aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL
                certif:         BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi
                certif:         Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX
                certif:         /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV
                certif:         gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK
                certif:         yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC
                certif:         H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRQ
                certif:         rlNmAQgA3cBbUobmihkeH06oN/IzlXGFK9zH5mqI9TJfhsxcQ1IXk9C+VzJInHH4
                certif:         oVaPc9rzExnADcek8m1+z2/6YDymH7PJiHVbRyqwL2sA7Lbfot29mLWcjD+QDyuA
                certif:         4nFx8wSmZgiRKR8WD4aBMTtgIy1HG9Yl+tlLQzXBOtWE4aAPeOAlbmf6UpnQTwDM
                certif:         eE2p1X5nZ23I0NDOc1PvrT0VXNfKmHfMiYL8MffeKrohJnPLvMd490XItXJV4Hyu
                certif:         78F6OTMN3wv6yCPGR4Ska/9ytf3xtET1whsmn/PxX9UR1uCFcjv3ANyln1ZfV+8G
                certif:         Ert8pWoPMrD3PjUdxTppwL2ZDAwLpwARAQABiQEfBBgBAgAJBQJQrlNmAhsMAAoJ
                certif:         EO6ZHuIo9s1sR44IALs1wuo1TEgKQw7XhWVqzTc3b9pGqoglapc6/gsuw7tg6k8O
                certif:         FpmW1loxQzbvmnR8hHwNSzrB7sxpO+Vd2lpZjqgrA64fhNfgNmtEyF8HWWTCDrdY
                certif:         SEzFj9EopnQQ+brO0KQDT6CfUARM61Z98eYZuNO1wW9FCGAcNHnQH8gM3Z14zdrZ
                certif:         AOMUBDO6evCgmulnHQxH7yZYXiruMtx9jli4TWw2KVclrJ54PWegzX70HPwqhc+w
                certif:         IhAOZq1Etu/8MMaG4IalUJC0AQxvOeas7tqH6Fsb6lGwX8osdhp9hFLMWcD59HVV
                certif:         nueGJ6YGTaMOuUx4NJ37bNXjGsGyDKhcgcH9F6W5Ay4EUSt7dhEIAL4SXoQvBJmB
                certif:         DMgMnZYEBUCGq839SsNdOOHvOwxTCSOqmQxNyKjR/oNRWwo5K1VdPbh2xN0b8kvW
                certif:         ixoF03PIRc1jby70U1JNt5c4T34mpXcBG5AUI2zuWKmb3PdVQy8n3+WYLZNP0SGO
                certif:         3LwDmVkpMcrWkNRJ6kjc5F7XsGUPaVq6QxKGRMW0wpD7Yi1gys3+CBBu7wETW4Me
                certif:         Yrg5llSskNVdU1aG2s2i+Kq80WFMxFDm/vPOArm6i/Gj7PA6FXV7lsQ9wl9EXQXT
                certif:         ds3DBLAttppgzACm+3qdqssTJITowA1ebMcnULBS4q8JDCar2cSHR7S14P7NFs7l
                certif:         V3ioN6La9FMBAIv44F0On/HBqY5d7jpK4aHVFysST9BAfqIoNPUWl0ExB/9xl9FX
                certif:         dnPnwP/UbGujbUF7e6jYw069FFvaOBeEjLxoe/OMhsyMJcwVu5WQ7ZQh4hibynBo
                certif:         SYQ/5C3IWK5sDEdwSSuq60Jp282AVBN/pwOpJsws87O7NtyMLDgqpEMp8z/782po
                certif:         gFFl/gSPl+r0KFJCgu13+gb96IdOw966B2jy57XBYF/pqSI2uapwaMi8r5vvp5ax
                certif:         Tc4+oVH4xzIuX5MSp/M7+MGCx0kdfMSun4u3grv8bykTRWXcD1RmDdIADpLbnizo
                certif:         OjkzZjxNJVIPJ7QD9yWeP9dPbviLhdLz5hxE+8VtNZRIsm6pSNhVIJs078pTpSej
                certif:         w3WjQijAwqEoVN+OB/4l4JwH8nY1xcB4RqUJWRYGyiDJIV4lyoMtzlr23vK76iND
                certif:         30FOrZJR/CqrTHIWmJDAOWWG6wLrM3pMXqDrpFGnz5q4KBcX4XutMfyGYA2wGklO
                certif:         Jnp8Zz4PE0TN0hHm+4XMTncWRUlI1jZcwFidvvX0bR7tE60VuQRMsuggc5j2htF8
                certif:         nIo1Wp/nPP6bA/94SvQQsEf9ZNeWhAXi+TVGAXb/Gll/PO96xcUFaWPr7QsgoJWM
                certif:         J7OJYbaR+BAhPsG/jLuu5sOtZZe+P8/54ZWFP/kw7a0guMBq8qEyh+H1n2W+BkeF
                certif:         L6u+ToTaW36QpeKOxK0xI/4cpKX46H4lSSg7HLAliQF/BBgBAgAJBQJRK3t2AhsC
                certif:         AGoJEO6ZHuIo9s1sXyAEGREIAAYFAlEre3YACgkQVt3x5YAnQzCxCwD+ODGiuuBd
                certif:         Em3Y+UboJI5ps/dc+C5srCN/WyCR9DbjBNEA/3E/+bTTUqQUH4aruFHeAqxsIOPB
                certif:         pzCZFwzqfKibT7u+/98IALJGnCgiSy0ORsMvnSBLIRJsEoiBTApog2UqJuIRI1WW
                certif:         HDX46VIYMTZKgUQeQC84W/+CKqr29a5YP2wxHDDoJn+K3kHDcr5JsUlJjDpU+Ku3
                certif:         8M2OMGMI8ikxlmKxH7Hshxps8krsjoduVcfL/gF1f4/IuZOBrpLId/45I+A6DSJl
                certif:         7wBR/rZ8/6P+9FIwDP689xHtz9VIBQ7zjrM5aUYcAztslZyQ+nBMVGFsM6RMilIT
                certif:         h7DsDU/AIXqe5dZhflI3AGVmlhNS4sNzBrcKa/3FhII3txQUeF5hTz63eT6E/isO
                certif:         WE20wimmrDx9Ri/uTD80S7Jq5IZXxkP9d5p6AvH4hGo=
                certif:         =GpqF
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iF4EAREIAAYFAlZMqE0ACgkQVt3x5YAnQzALlQD/bMJWRsyFc7rbqoShzd5Rfky5
                8HgTc4f3Dl9H3tvvOnUA/3YAkKqJGFdzkienbsX66RvSwdRP36gPyWCw6rQnkyaR
                =espv
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "inline pgp signed mailupdate with missing keycert"() {
    when:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKjPAAoJELvMuy1XY5UNB+sH/3EgO1BjfyIPYmu40bTYiZha
                DPFA/8/wtQoutWg9aO8+LlaDbs4xNqQuiZz4Gsj7Kta597UqkIxNyr+FiP5qRBJn
                nTFYI/5Euw+JqfVD/we/e56rfmL8tb4ZwdP5KIpBO58tgJwqeQ0x7vdvt1/vZ9Yx
                uVaxcUrMzKZFT+7yJaEUCpF+alN2oC4DY6bxRGddrKDUhHTZbMEsLTdIE7rJCY+G
                wclNvPKW42t8lT4ajloQCT6dK+aZ3YUFRRzPobxXHvrUqPJsIuPN1V2V1YdNg2Iz
                ucVz+7OCUmT+dI15jXlZSLWwaMZ6KHafgPio5ZuACE5GeqjC0m/vG5QWXOTiHYw=
                =OMjb
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "inline pgp signed mailupdate when maintainer only has x509 keycert"() {
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKlBAAoJELvMuy1XY5UNzFIH/2djgHjZqE+K2aC3R9UzIxtn
                0IdBrKedFAFVNsULEWuHUj054FRcjO4KpRgG21lVEaWWEAPqhVC/Mq+CFs+7AbKf
                A9HLYTbV857aWkuTSvb0wEZ4jCIqxAtgScnVTsDS8v95Qk1qJD+GbsvrQkdCoLXB
                QoegvM4UeKe64bKgBxRgFELuOUgSyd0hJ7SILQoMGx+oIp1Yn47SJrbIu8IP7WrM
                zhRTqCWoRmbhXHSJwOSxtT7pBCowIS+/RqQ5aqrMZEgtOEshcx+neFp4WKiDOPtk
                iQa32gXdGvAA0RcELaA1kH5ZJvWaZwY1I9mnrFNgkqbANmUqbmogOpku/8d7fu0=
                =OlpN
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "inline pgp signed mailupdate but maintainer doesnt reference keycert"() {
    when:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKmMAAoJELvMuy1XY5UNQNcIAJBXmEK8fo60um1SbCPZEk48
                OizfjkPqD7X1WtUKd3xUbkYbWDxQxK2lWTgzuXXavVja4zXPgp0HpmBbSvJfY8cH
                eyaKH+RXldPjI3huFmXiEVwEfRpoo33/WKgRGg72dnbw8V4yfoqdRIvZRsawwHkU
                +mZcqPVABigHn6p4YcVhM5A9MQEq9Ifh8+gR1TgYlYiQvQDQp+xF+6dq35dWEQ+J
                CpDsQzmjf+ow+KVq9uR082QdXvs8I9e+lXT3s9P8YWqaL33qhW8YeuI9kZaTBVq/
                KY69NNAejyQ16FDY4pQdDxhJsCmDcq+h72SZlLd3LKboMuGmbl2cgeIe65LSc0E=
                =UB8z
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "inline pgp signed mailupdate with invalid signature format"() {
    when:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: text/plain; charset=us-ascii\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <90563E66-2415-4A49-B8DF-3BD1CBB8868C@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "-----BEGIN PGP SIGNED MESSAGE-----\n" +
              "Hash: SHA1\n" +                     // no empty line between header and content
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "source:  TEST\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1\n" +               // no empty line after headers
              "iQEcBAEBAgAGBQJWTKmMAAoJELvMuy1XY5UNQNcIAJBXmEK8fo60um1SbCPZEk48\n" +
              "OizfjkPqD7X1WtUKd3xUbkYbWDxQxK2lWTgzuXXavVja4zXPgp0HpmBbSvJfY8cH\n" +
              "eyaKH+RXldPjI3huFmXiEVwEfRpoo33/WKgRGg72dnbw8V4yfoqdRIvZRsawwHkU\n" +
              "+mZcqPVABigHn6p4YcVhM5A9MQEq9Ifh8+gR1TgYlYiQvQDQp+xF+6dq35dWEQ+J\n" +
              "CpDsQzmjf+ow+KVq9uR082QdXvs8I9e+lXT3s9P8YWqaL33qhW8YeuI9kZaTBVq/\n" +
              "KY69NNAejyQ16FDY4pQdDxhJsCmDcq+h72SZlLd3LKboMuGmbl2cgeIe65LSc0E=\n" +
              "=UB8z\n" +
              "-----END PGP SIGNATURE-----"

    then:
      def ack = ackFor message

      ack.summary.nrFound == 0
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.subject == "FAILED: NEW"

      ack.contents =~ "(?s)~~~~\nThe following paragraph\\(s\\) do not look like objects\n" +
              "and were NOT PROCESSED:\n\n" +
              "-----BEGIN PGP SIGNED MESSAGE-----\n" +
              "Hash: SHA1\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   \\+44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "source:  TEST\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1\n" +
              "iQEcBAEBAgAGBQJWTKmMAAoJELvMuy1XY5UNQNcIAJBXmEK8fo60um1SbCPZEk48\n" +
              "OizfjkPqD7X1WtUKd3xUbkYbWDxQxK2lWTgzuXXavVja4zXPgp0HpmBbSvJfY8cH\n" +
              "eyaKH\\+RXldPjI3huFmXiEVwEfRpoo33/WKgRGg72dnbw8V4yfoqdRIvZRsawwHkU\n" +
              "\\+mZcqPVABigHn6p4YcVhM5A9MQEq9Ifh8\\+gR1TgYlYiQvQDQp\\+xF\\+6dq35dWEQ\\+J\n" +
              "CpDsQzmjf\\+ow\\+KVq9uR082QdXvs8I9e\\+lXT3s9P8YWqaL33qhW8YeuI9kZaTBVq/\n" +
              "KY69NNAejyQ16FDY4pQdDxhJsCmDcq\\+h72SZlLd3LKboMuGmbl2cgeIe65LSc0E=\n" +
              "=UB8z\n" +
              "-----END PGP SIGNATURE-----" +
              "\n\n.+?~+~~~~\n"
  }

  def "inline pgp signed mailupdate when maintainer has multiple pgp auth lines"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:45:48")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-AAAAAAAA       # primary key doesn't match public key id 5763950D
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
                """.stripIndent(true))

    and:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-BBBBBBBB             # public key id 81CCF97D
                method:         PGP
                owner:          Unknown <unread@ripe.net>
                fingerpr:       EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yfkBCAC/zYZw2vDpNF2Q7bfoTeTmhEPERzUX3y1y0jJhGEdbp3re4v0i
                certif:         XWDth4lp9Rr8RimoqQFN2JNFuUWvohiDAT91J+vAG/A67xuTWXACyAPhRtRIFhxS
                certif:         tBu8h/qEv8yhudhjYfVHu8rUbm59BXzO80KQA4UP5fQeDVwGFbvB+73nF1Pwbg3n
                certif:         RzgLvKZlxdgV2RdU+DvxabkHgiN0ybcJx3nntL3Do2uZEdkkDDKkN6hkUJY0cFbQ
                certif:         Oge3AK84huZKnIFq8+NA/vsE3dg3XhbCYUlS4yMe0cvnZrH23lnu4Ubp1KBILHVW
                certif:         K4vWnMEXcx/T2k4/vpXogZNUH6E3OjtlyjX5ABEBAAG0GVVua25vd24gPHVucmVh
                certif:         ZEByaXBlLm5ldD6JATgEEwECACIFAlC0yfkCGwMGCwkIBwMCBhUIAgkKCwQWAgMB
                certif:         Ah4BAheAAAoJEHbKke+BzPl9UasH/1Tc2YZiJHw3yaKvZ8jSXDmZKmO69C7YvgsX
                certif:         B72w4K6d92vy8dLLreqEpzXKtWB1+K6bLZv6MEdNbvQReG3rw1i2Io7kdsKFn9QC
                certif:         OeY4OwpzBMZIJGWWXxOLz9Auo9a43xU+wL92/oCqFJrLuuppgOIVkL0pBWRDQYqp
                certif:         3MqyHdsUOEdd7pwUlGJlfLqa7wmO+r04EG1OBRLBg5p4gVARqDrVMA3ym9KF750T
                certif:         78Il1eWrceLglI5F0h4RYEmQ3amF/ukbPyzf26+J6MnWeDSO3Q8P/aDO3L7ccNoC
                certif:         VwyHxUumWgfQVEnt6IaKLSjxVPhhAFO0wLd2tgaUH1y/ug1RgJe5AQ0EULTJ+QEI
                certif:         APgAjb0YCTRvIdlYFfKQfLCcIbifwFkBjaH9fN8A9ZbeXSWtO7RXEvWF70/ZX69s
                certif:         1SfQyL4cnIUN7hEd7/Qgx63IXUfNijolbXOUkh+S41tht+4IgJ7iZsELuugvbDEb
                certif:         VynMXFEtqCXm1zLfd0g2AsWPFRczkj7hWE0gNs7iKvEiGrjFy0eSd/q07oWLxJfq
                certif:         n4GBBPMGkfKxWhy5AXAkPZp1mc7mlYuNP9xrn76bl69T0E69kDPS3JetSaVWj0Uh
                certif:         NSJSjP1Zc8g+rvkeum3HKLoW0svRo2XsldjNMlSuWb/oxeaTdGZV6SxTJ+T1oHAi
                certif:         tovyQHusvGu3D9dfvTcW3QsAEQEAAYkBHwQYAQIACQUCULTJ+QIbDAAKCRB2ypHv
                certif:         gcz5fe7cB/9PrDR7ybLLmNAuoafsVQRevKG8DfVzDrgThgJz0jJhb1t74qy5xXn+
                certif:         zW8d/f/JZ8jr7roWA64HKvdvo8ZXuGEf6H20p1+HbjYpT52zteNU/8ljaqIzJBes
                certif:         tl8ecFB7qg3qUSDQseNaA1uHkZdxGybzgI69QlOyh8fRfOCh/ln9vAiL0tW+Kzjg
                certif:         8VMY0N3HzBcAPSB7U8wDf1qMzS5Lb1yNunD0Ut5qxCq3fxcdLBk/ZagHmtXoelhH
                certif:         Bng8TRND/cDUWWH7Rhv64NxUiaKsrM/EmrHFOpJlXuMRRx4FtRPZeXTOln7zTmIL
                certif:         qqHWqaQHNMKDq0pf24NFrIMLc2iXCSh+
                certif:         =FPEl
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
                """.stripIndent(true))
    and:
      syncUpdate new SyncUpdate(data: (getFixtures().get("OWNER-MNT").stripIndent(true).
              replaceAll("source:\\s*TEST", "auth: PGPKEY-AAAAAAAA\nauth: PGPKEY-BBBBBBBB\nsource: TEST")
              + "password: owner"))
    and:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKs8AAoJEHbKke+BzPl93YwH/3Aid3KhprTZ2Z/ejlRZ6nFW
                P944UdsFxNLZBKMe3Kyt0h16bKejMsuh2AOIMiSmP0cfCylKybRA1OwuKH7rY8ZP
                +5LPubbwXTcDFP01aQrzrIkKEy4RIPAqeydvcVGIPx7MePPH9fjvgsAvuMwKXEMT
                IYXhw8UDv2kFt4WsVKI6T+TidAsqTaSADhpK+OaILtfycDjNXp8a60kHM3C0hiP0
                BtMzMYU6IwQ8+umXAOXQAFXdJZEYAvHyi8OPTTyWXivR8dzhbzHkzAiPY6pX9iPf
                RhzeD+qm1Vt8Zl9Xgo2++SoD4Xxw6wfujhl9NLBUt92KtgjEPjrkqnYhwVPyEqE=
                =TfFa
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
      )

    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 0, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "inline pgp signed mailupdate with double pgp signed update"() {
    given:
      setTime(LocalDateTime.parse("2015-11-18T17:50:33")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-81CCF97D
                method:         PGP
                owner:          Unknown <unread@ripe.net>
                fingerpr:       EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
                certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
                certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
                certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
                certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
                certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
                certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
                certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
                certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
                certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
                certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
                certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
                certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
                certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
                certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
                certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
                certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
                certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
                certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
                certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
                certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
                certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
                certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
                certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
                certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
                certif:         =HQmg
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         unread@ripe.net
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
                """.stripIndent(true))
    and:
      syncUpdate new SyncUpdate(data: (getFixtures().get("OWNER-MNT").stripIndent(true).
              replaceAll("source:\\s*TEST", "auth: PGPKEY-81CCF97D\nsource: TEST")
              + "password: owner"))
    and:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                - -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                - -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKxZAAoJELvMuy1XY5UNC4YH/0s93Ow7fvH8zaycT/iQplCG
                L/vIELulL4DwoqSfCSv0V8teDW+AGftbdjGHDS38UIkdI1XB6b9KcPhcg7uuwMLR
                8YUum+/HWXw4JHXnlJ9Z35DlXuUssTYFU8QdUBg8fNM2Aad0gRSZhkIRugq0a4Il
                OW+vfHRtuIc9hT57Gwh3UQ5593B3D/H30vPDHuAbiMVtYl2yb31tbKds8elPBGRp
                1G5xJm6Tyv9L0vhWKshf2+Po2WqO/kf49EEc+fGiXUYh8fJkL86NR9o8KiAm1Zot
                mIUHA/ODZ6WISI5VeQ3nZV2L+FueuCyC19be17huyuLKZJljH1ZHEqXtOSLi1w8=
                =7+/2
                - -----END PGP SIGNATURE-----
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJWTKxoAAoJEHbKke+BzPl9EMQH/jwS55/t7NwyTp2IkcIzs0ar
                e/lxx6YqqedPzhsDwUx3zJVNPkzYVHmrMaHvJBU4ETkMfvHSjnRebjrmtzqZIfas
                c8lDhe9G9AiQEMp6hhZdl0taUkd1OuQs3Fwl2oyEEfQhXtzsyGTOi9+HGT7i5GEz
                NFZT6tBjdPkyTVQeObU5EPNhRur/AW9Lo/c5gJTeR0F/Fqr/mdM0VzLzI78I45a4
                6WVOvh1xmYyjAqtkeRBs7K3t1/axgLqipYacBAO7ojn2pTwlxfGNe6P4aPeqPp5R
                L0feEtd9bj9AVsizm9TSI2lPM+AMuqDykwmn/FJTw0LUpohSTy0EEvpuU2qriKU=
                =k2zm
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
      )

    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 0, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  @Ignore("TODO")
  def "multipart pgp signed message with crlf stripped from content"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: noreply@ripe.net\n" +
              "To: test-dbm@ripe.net\n" +
              "Subject: NEW\n" +
              "Message-ID: <1361277612.10298.12.camel@galileo.millnert.se>\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Content-Type: multipart/signed; micalg=\"pgp-sha1\"; protocol=\"application/pgp-signature\";\n" +
              "\tboundary=\"=-8YSQO2TBX+Ao8EuQQc6k\"\n" +
              "Mime-Version: 1.0\n" +
              "\n" +
              "--=-8YSQO2TBX+Ao8EuQQc6k\n" +
              "Content-Type: text/plain; charset=\"UTF-8\"\n" +
              "Content-Transfer-Encoding: quoted-printable\n" +
              "\n" +
              "person: First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone: +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by: OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source: TEST\n" +
              "--=-8YSQO2TBX+Ao8EuQQc6k\n" +
              "Content-Type: application/pgp-signature; name=\"signature.asc\"\n" +
              "Content-Description: This is a digitally signed message part\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "iQEcBAEBAgAGBQJRJiUQAAoJEO6ZHuIo9s1sRUwIAKPZTXKmubl/lpWSdUdF5/wj\n" +
              "JtaeUI5ag/2kHY7KoMMhy7qUnmMJY+INWXsfXQLLev6envjzxzlLUPDC+H5nBXHz\n" +
              "sW/FfzzJPKLYzmZuJcGqPJDJRzWTmqt2I3/QMuxg3jyS7kneVfM3/WQ8fKP6pCgF\n" +
              "Z9BeapgwjZGgRx4JCW22oHmsR8X6i9qTnZyTm9zHsYZCK8hAhHQaWGfgx55/x1iR\n" +
              "RPA5nanT93lZSmnYdoHfFkHv/7ugylV74ubpd3eftL8hrlvyGLnj54R1dkBdjJ0f\n" +
              "Uef0jxzAqSU60cgAL5V2X3WaGBm7yf+gDxxjRsLMsH+M/y4EdbjDpqoDEJIBeVU=\n" +
              "=jvFg\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--=-8YSQO2TBX+Ao8EuQQc6k--\n\n"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)
  }

  def "multipart mixed pgp signed message with base64 encoded signature part"() {
    given:
      setTime(LocalDateTime.parse("2014-09-22T17:33:40")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: upd@ripe.net\n" +
              "User-Agent: Mozilla/5.0 (Windows NT 6.1; rv:24.0) Gecko/20100101 Thunderbird/24.6.0\n" +
              "MIME-Version: 1.0\n" +
              "To: test-dbm@ripe.net\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\\n\" +\n" +
              "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\\n\" +\n" +
              "Content-Type: multipart/mixed;\n" +
              " boundary=\"------------090202010406090002050801\"\n" +
              "\n" +
              "This is a multi-part message in MIME format.\n" +
              "--------------090202010406090002050801\n" +
              "Content-Type: text/plain; charset=ISO-8859-1; format=flowed\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "--------------090202010406090002050801\n" +
              "Content-Type: application/pgp-signature;\n" +
              " name=\"Attached Message Part\"\n" +
              "Content-Transfer-Encoding: base64\n" +
              "Content-Disposition: attachment;\n" +
              " filename=\"Attached Message Part\"\n" +
              "\n" +
              "LS0tLS1CRUdJTiBQR1AgU0lHTkFUVVJFLS0tLS0NClZlcnNpb246IEdudVBHIHYxDQpDb21t\n" +
              "ZW50OiBHUEdUb29scyAtIGh0dHA6Ly9ncGd0b29scy5vcmcNCg0KaVFFY0JBRUJBZ0FHQlFK\n" +
              "VUlFRlVBQW9KRUx2TXV5MVhZNVVORkxNSC9ScGxMd2pLYzZaK2tVZG16Uzk3c25uTQ0KS1l6\n" +
              "c0VXQlFvUEhQZUJXNFFPZUxsUnFXcUsxelU0QzcyYmhEK0doVVB0S1ZuYS9IOHh1NzR6WjVS\n" +
              "dUw0N082aA0KUENNWDhCVlZMUktyNUxGVk00SjJ6K0lRbjVodUhqaUlKRERGUU05Ry84M2Js\n" +
              "WTV2dHUrMVNFVFpWWVJHTUdvTQ0KU25pSEowd0tVa2V1RnpBM3VsWG1sTVpwNy9Kc1A5MU8y\n" +
              "eEVNb3VtSlE2bWRJZGFVS0NtNDlpOUc5cmp2SStUSQ0KVjZOWEM4RmJMc1ZQVHZLUTRoZVpM\n" +
              "VFVNZEtTM0dkVklLY2hrUkIwa01yZkdTc3J3QXZlMThRTXpOZVdhLzlabg0KK2JEbkNtTlhs\n" +
              "NGZSbXJ6Qjd4RUVWb0FNam5lbVNySWMyOWFnVnQ5Q2pNaiszVk5CekYxemh0Q3lnaXdDRU9z\n" +
              "PQ0KPVIvWDQNCi0tLS0tRU5EIFBHUCBTSUdOQVRVUkUtLS0tLQ0K" +
              "--------------090202010406090002050801--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack =~ /Create SUCCEEDED: \[person\] FP1-TEST   First Person/
  }

  def "multipart alternative pgp signed message"() {
    given:
      setTime(LocalDateTime.parse("2013-01-02T16:53:25")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\";\n" +
              "\tprotocol=\"application/pgp-signature\";\n" +
              "\tmicalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\n" +
              "Content-Type: multipart/alternative;\n" +
              "\tboundary=\"Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\"\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/html;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "<html><head></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;" +
              " \"><div style=\"font-size: 13px; \"><b>person: &nbsp;First Person</b></div><div style=\"font-size: 13px; \"><b>" +
              "address: St James Street</b></div><div style=\"font-size: 13px; \"><b>address: Burnley</b></div><div style=\"font-size: 13px; \">" +
              "<b>address: UK</b></div><div style=\"font-size: 13px; \"><b>phone: &nbsp; +44 282 420469</b></div><div style=\"font-size: 13px; \">" +
              "<b>nic-hdl: FP1-TEST</b></div><div style=\"font-size: 13px; \"><b>mnt-by: &nbsp;OWNER-MNT</b></div><div style=\"font-size: 13px; \">" +
              "<b>changed: <a href=\"mailto:denis@ripe.net\">denis@ripe.net</a> 20121016</b></div><div style=\"font-size: 13px; \">" +
              "<b>source: &nbsp;TEST</b></div><div><br></div></body></html>\n" +
              "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA--\n" +
              "\n" +
              "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ5Ff1AAoJEO6ZHuIo9s1sQxoIAJYdnvbYjCwRyKgz7sB6/Lmh\n" +
              "Ca7A9FrKuRFXHH2IUM6FIlC8hvFpAlXfkSWtJ03PL4If3od0jL9pwge8hov75+nL\n" +
              "FnhCG2ktb6CfzjoaeumTvzbt5oSbq2itgvaQ15V6Rpb2LIh7yfAcoJ7UgK5X1XEI\n" +
              "OhZvuGy9M49unziI3oF0WwHl4b2bAt/r7/7DNgxlT00pMFqrcI3n00TXEAJphpzH\n" +
              "7Ym5+7PYvTtanxb5x8pMCmgtsKgF5RoHQv4ZBaSS0z00WVivk3cuCugziyTrwI2+\n" +
              "4IkFu75GfD+xKAldd2of09SrFEaOJfXNslq+BZoqc3hGOV+b7vpNARp0s7zsq4E=\n" +
              "=O7qu\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 1, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Deprecated attribute \"changed\". This attribute has been removed."]
  }

  def "multipart plaintext pgp signed message"() {
    given:
      setTime(LocalDateTime.parse("2013-01-03T09:17:29")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\";\n" +
              "\tprotocol=\"application/pgp-signature\";\n" +
              "\tmicalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <6DBC05F5-9DFF-4FAA-BFAE-223F456A1AA5@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ5T6ZAAoJEO6ZHuIo9s1sBSgH/24dCeqOG3eQDHx4FUAvzvjB\n" +
              "qiaKG6yV9/VBeuR1ZeeKsdsRNfBD8XxQp5BFR/W4WdmFDqP+ZubjOECcoPQ76+kG\n" +
              "l8dduuv9q43PvnGlBClavJKVyAEZlw6CY8AVTa/mYQnRNU/vju4fCuWm/TNXLsfm\n" +
              "uZOade0+AViAD0uCxsh8oF3fix2oQZ7RfTEhEvSTtVsN+mJ5yhCC9jn4HJtrJoLS\n" +
              "O0Vc26WPlcoaq/ye3BVlx77yWqCDkljE83uhIfens+gR8nJEek9UGVj6nXACNA7d\n" +
              "KwyBsVF9yz2oZUWObuA774yJLh8xa5DaFhcyBP0wZE4T1oGMvnqJKCtVeEzhyqo=\n" +
              "=Ftin\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 1, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
        "Deprecated attribute \"changed\". This attribute has been removed."]
  }

  def "multipart plaintext pgp signed message with unknown encoding"() {
    given:
      setTime(LocalDateTime.parse("2013-01-08T15:05:05")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed; micalg=pgp-md5;\n" +
              "\tprotocol=\"application/pgp-signature\"; boundary=\"vkogqOf2sHV7VnPd\"\n" +
              "Content-Disposition: inline\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <6DBC05F5-9DFF-4FAA-BFAE-223F456A1AA5@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--vkogqOf2sHV7VnPd\n" +
              "Content-Type: text/plain; charset=unknown-8bit\n" +
              "Content-Disposition: inline\n" +
              "Content-Transfer-Encoding: quoted-printable\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "--vkogqOf2sHV7VnPd\n" +
              "Content-Type: application/pgp-signature\n" +
              "Content-Disposition: inline\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ7CeRAAoJEO6ZHuIo9s1sFLoH/jpT0lzGqjeoPwZIVNlxt2S/\n" +
              "y5t7w5RdRvsBub6yiQhb0ZJGGzna2xlB3IBjt23iEZYwYQ6EOBwCn5JN+VTfhGyM\n" +
              "zXLE+eSmW+LQEuUDPerxRPrdxbBIbX89mCsIFtPC0QxaybOKAAnAw/nEJ27nWPvs\n" +
              "JQORsMPtGF1DXmdz3XWWh1nAhtKYycbLDvTYnICaeSwLSaKJxInRoGc0fSkut1m+\n" +
              "aGTaYPJSNMOOoS74tmYB4Fh+e/SvX9d8PqDOpl6ZQL+ROkL1J9VtEv2RMcqucOGN\n" +
              "7w88Iobba2WhADvBJ7HP7SjS0Go9mmu6r5byD11cWoIHZUYziejpXFNdNn5c200=\n" +
              "=s6S9\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--vkogqOf2sHV7VnPd--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "multipart plaintext pgp signed message and not authorised"() {
    when:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\";\n" +
              "\tprotocol=\"application/pgp-signature\";\n" +
              "\tmicalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <6DBC05F5-9DFF-4FAA-BFAE-223F456A1AA5@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ5T6ZAAoJEO6ZHuIo9s1sBSgH/24dCeqOG3eQDHx4FUAvzvjB\n" +
              "qiaKG6yV9/VBeuR1ZeeKsdsRNfBD8XxQp5BFR/W4WdmFDqP+ZubjOECcoPQ76+kG\n" +
              "l8dduuv9q43PvnGlBClavJKVyAEZlw6CY8AVTa/mYQnRNU/vju4fCuWm/TNXLsfm\n" +
              "uZOade0+AViAD0uCxsh8oF3fix2oQZ7RfTEhEvSTtVsN+mJ5yhCC9jn4HJtrJoLS\n" +
              "O0Vc26WPlcoaq/ye3BVlx77yWqCDkljE83uhIfens+gR8nJEek9UGVj6nXACNA7d\n" +
              "KwyBsVF9yz2oZUWObuA774yJLh8xa5DaFhcyBP0wZE4T1oGMvnqJKCtVeEzhyqo=\n" +
              "=Ftin\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart plaintext pgp signed message when keycert is missing and is not authorised"() {
    when:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\";\n" +
              "\tprotocol=\"application/pgp-signature\";\n" +
              "\tmicalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <6DBC05F5-9DFF-4FAA-BFAE-223F456A1AA5@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ5T6ZAAoJEO6ZHuIo9s1sBSgH/24dCeqOG3eQDHx4FUAvzvjB\n" +
              "qiaKG6yV9/VBeuR1ZeeKsdsRNfBD8XxQp5BFR/W4WdmFDqP+ZubjOECcoPQ76+kG\n" +
              "l8dduuv9q43PvnGlBClavJKVyAEZlw6CY8AVTa/mYQnRNU/vju4fCuWm/TNXLsfm\n" +
              "uZOade0+AViAD0uCxsh8oF3fix2oQZ7RfTEhEvSTtVsN+mJ5yhCC9jn4HJtrJoLS\n" +
              "O0Vc26WPlcoaq/ye3BVlx77yWqCDkljE83uhIfens+gR8nJEek9UGVj6nXACNA7d\n" +
              "KwyBsVF9yz2oZUWObuA774yJLh8xa5DaFhcyBP0wZE4T1oGMvnqJKCtVeEzhyqo=\n" +
              "=Ftin\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_E682976F-F49E-487F-82D0-51D5A41A8E35--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart alternative X509 signed message"() {
    given:
      setTime(LocalDateTime.parse("2013-01-03T09:32:01")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_98BF68D9-C4D6-4B86-8375-38E333B918EF\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
              "Message-Id: <2067A1C5-50F3-46D1-9EA8-A6C260C259A4@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_98BF68D9-C4D6-4B86-8375-38E333B918EF\n" +
              "Content-Type: multipart/alternative;\n" +
              "\tboundary=\"Apple-Mail=_C45B4ECE-1DB6-4DDE-8B6D-4DB0BB0CDC8E\"\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_C45B4ECE-1DB6-4DDE-8B6D-4DB0BB0CDC8E\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_C45B4ECE-1DB6-4DDE-8B6D-4DB0BB0CDC8E\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/html;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "<html><head></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">" +
              "<div style=\"font-size: 13px; \">person: &nbsp;First Person</div><div style=\"font-size: 13px; \">address: St James Street</div>" +
              "<div style=\"font-size: 13px; \">address: Burnley</div><div style=\"font-size: 13px; \">address: UK</div><div style=\"font-size: 13px; \">" +
              "phone: &nbsp; +44 282 420469</div><div style=\"font-size: 13px; \">nic-hdl: FP1-TEST</div><div style=\"font-size: 13px; \">" +
              "mnt-by: &nbsp;OWNER-MNT</div><div style=\"font-size: 13px; \">changed: <a href=\"mailto:denis@ripe.net\">denis@ripe.net</a> 20121016</div>" +
              "<div style=\"font-size: 13px; \">source: &nbsp;TEST</div><div><br></div></body></html>\n" +
              "--Apple-Mail=_C45B4ECE-1DB6-4DDE-8B6D-4DB0BB0CDC8E--\n" +
              "\n" +
              "--Apple-Mail=_98BF68D9-C4D6-4B86-8375-38E333B918EF\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzIwMVowIwYJKoZIhvcNAQkEMRYEFD4JhAipdRLJoj18\n" +
              "lGrkGzK74L00MIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgGlxIaAcSIDw5PUw7JscCO7waLRubOusGlg7KaQOodLNAItiqU1xE8jTDmHXt97RTbRG\n" +
              "AXWPFW9ByXburQmxCSSxxOnIey5ta8qlP8wXQrp86aKVYO9iUWDRH8B7C1R/ApHWhRsIHadscpDn\n" +
              "0dZdWzSqRcNJzOJjna7eHLz8SEDFAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_98BF68D9-C4D6-4B86-8375-38E333B918EF--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 1, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Deprecated attribute \"changed\". This attribute has been removed."]
  }

  def "multipart plaintext X509 signed message"() {
    given:
      setTime(LocalDateTime.parse("2013-01-03T09:33:44")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 1, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Deprecated attribute \"changed\". This attribute has been removed."]
  }

  def "multipart plaintext X509 signed message has expired"() {
    given:
      setTime(LocalDateTime.parse("2013-01-03T10:34:44")) // current time is more than 1 hour after signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Message was signed more than one hour ago"]
  }

  def "multipart plaintext X509 signed message in the future"() {
    given:
      setTime(LocalDateTime.parse("2013-01-03T08:32:44")) // current time is more than 1 hour *before* signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Message was signed more than one hour ago"]
  }

  def "multipart plaintext X509 signed message when maintainer only has pgp keycert"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart plaintext X509 signed message with invalid keycert"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-111\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart plaintext x509 signed message not signed by keycert and keycert certificate has expired"() {
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:     AUTO-1
                method:       X509
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=zz.example.denis/Email=denis@ripe.net
                fingerpr:     E7:0F:3B:D4:2F:DD:F5:84:3F:4C:D2:98:78:F3:10:3D
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIIC8DCCAlmgAwIBAgICBIQwDQYJKoZIhvcNAQEEBQAwXjELMAkGA1UEBhMCTkwx
                certif:       ETAPBgNVBAoTCFJJUEUgTkNDMR0wGwYDVQQLExRSSVBFIE5DQyBMSVIgTmV0d29y
                certif:       azEdMBsGA1UEAxMUUklQRSBOQ0MgTElSIFJvb3QgQ0EwHhcNMDQwOTI3MTI1NDAx
                certif:       WhcNMDUwOTI3MTI1NDAxWjBsMQswCQYDVQQGEwJOTDERMA8GA1UEChMIUklQRSBO
                certif:       Q0MxEDAOBgNVBAsTB01lbWJlcnMxGTAXBgNVBAMTEHp6LmV4YW1wbGUuZGVuaXMx
                certif:       HTAbBgkqhkiG9w0BCQEWDmRlbmlzQHJpcGUubmV0MFwwDQYJKoZIhvcNAQEBBQAD
                certif:       SwAwSAJBAKdZEYY0pCb5updB808+y8CjNsnraQ/3sBL3/184TqD4AE/TSOdZJ2oU
                certif:       HmEpfm6ECkbHOJ1NtMwRjAbkk/rWiBMCAwEAAaOB8jCB7zAJBgNVHRMEAjAAMBEG
                certif:       CWCGSAGG+EIBAQQEAwIFoDALBgNVHQ8EBAMCBeAwGgYJYIZIAYb4QgENBA0WC1JJ
                certif:       UEUgTkNDIENBMB0GA1UdDgQWBBQk0+qAmXPImzyVTOGARwNPHAX0GTCBhgYDVR0j
                certif:       BH8wfYAUI8r2d8CnSt174cfhUw2KNga3Px6hYqRgMF4xCzAJBgNVBAYTAk5MMREw
                certif:       DwYDVQQKEwhSSVBFIE5DQzEdMBsGA1UECxMUUklQRSBOQ0MgTElSIE5ldHdvcmsx
                certif:       HTAbBgNVBAMTFFJJUEUgTkNDIExJUiBSb290IENBggEAMA0GCSqGSIb3DQEBBAUA
                certif:       A4GBAAxojauJHRm3XtPfOCe4B5iz8uVt/EeKhM4gjHGJrUbkAlftLJYUe2Vx8HcH
                certif:       O4b+9E098Rt6MfFF+1dYNz7/NgiIpR7BlmdWzPCyhfgxJxTM9m9B7B/6noDU+aaf
                certif:       w0L5DyjKGe0dbjMKtaDdgQhxj8aBHNnQVbS9Oqhvmc65XgNi
                certif:       -----END CERTIFICATE-----
                mnt-by:       OWNER-MNT
                source:       TEST
                password:     owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

      // warning on certificate expired should NOT appear if the keycert doesn't authorise the update
      !(ack.contents =~ "Certificate in keycert X509-1 has expired")
  }

  def "multipart plaintext x509 signed message update fails when keycert is missing"() {
    when:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart plaintext x509 signed message and is not authorised"() {
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))
    then:
      def message = send "From: Edward Shryane <eshryane@ripe.net>\n" +
              "Content-Type: multipart/signed;\n" +
              "\tboundary=\"Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\";\n" +
              "\tprotocol=\"application/pkcs7-signature\";\n" +
              "\tmicalg=sha1\n" +
              "X-Smtp-Server: mailhost.ripe.net\n" +
              "Subject: NEW\n" +
              "X-Universally-Unique-Identifier: 656e8d4c-e258-4fcd-a830-6a7d39584a7a\n" +
              "Date: Thu, 3 Jan 2013 09:33:44 +0100\n" +
              "Message-Id: <321C9378-C9AC-4ED9-B3D0-C97A79FB6CBA@ripe.net>\n" +
              "To: Edward Shryane <eshryane@ripe.net>\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwMzA4MzM0NFowIwYJKoZIhvcNAQkEMRYEFF8/6nTWJD4Fl2J0\n" +
              "sgOOpFsmJg/DMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJOTl3PkpLoOo5MRWaPs/2OHXOzg+Oj9OsNEB326bvl0e7ULuWq2SqVY44LKb6JM5nm9\n" +
              "6lHk5PJqv6xZq+m1pUYlCqJKFQTPsbnoA3zjrRCDghWc8CZdsK2F7OajTZ6WV98gPeoCdRhvgiU3\n" +
              "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_8FA167DD-A449-4501-AD62-60D012085607--"
    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(0, 0, 0, 0, 0)
      ack.summary.assertErrors(1, 1, 0, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
  }

  def "multipart plaintext x509 signed message keycert is expired"() {
    given:
      setTime(LocalDateTime.parse("2013-01-11T14:27:09")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:     AUTO-1
                method:       X509
                owner:        /CN=localhost
                fingerpr:     91:8A:B7:02:D0:77:6C:49:74:FE:9F:B3:68:8E:AB:F8
                remarks:      certificate has expired (was valid from 01/01/1970 00:00:00 for 365 days)
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIIBnTCCAQagAwIBAgIC8fAwDQYJKoZIhvcNAQEFBQAwFDESMBAGA1UEAxMJbG9jYWxob3N0MB4X
                certif:       DTY5MTIzMTIzMDAwMFoXDTcwMTIzMTIzMDAwMFowFDESMBAGA1UEAxMJbG9jYWxob3N0MIGfMA0G
                certif:       CSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHJFR5u0rjQpceH9t/GZBBCZVymaUCDoPNDs9o//SR1jU+
                certif:       rZYoYVnRgfMuErZNI8ImBcdhsRdoP/cvy7wZrW7TOfzeWrGWeyn7a3NUx68aVotymrT0IO9Sa09x
                certif:       c2NJuACNIsOXwA/JxYKjcdiCTJAqOSjJ0rJmnxJ+kRRovyVCXwIDAQABMA0GCSqGSIb3DQEBBQUA
                certif:       A4GBAEDEuIaLftZViS+dbMvAL9DFhRNiQDsBSL1xZG7cJ7CSPrkXvUGK7b4GOlrhV0Xo3aONtUEk
                certif:       vZyhD6V8R5G4Aaj0JpDFXUAujTbl2eaW/BLV2pj7Wy5K2oUiyQYgHdPgDUTGV3hguV3eOroUWShh
                certif:       +o7S6hzwbNsUve3+zPyhSf50
                certif:       -----END CERTIFICATE-----
                mnt-by:       OWNER-MNT
                source:       TEST
                override: denis, override1 
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: testing@ripe.net\n" +
              "Content-Type: multipart/signed; protocol=\"application/x-pkcs7-signature\"; micalg=\"sha1\"; boundary=\"----505692B81E452CF4C7D142C86D23FE2A\"\n" +
              "Subject: NEW\n" +
              "Date: Mon, 7 Jan 2013 10:48:20 +0100\n" +
              "Message-Id: <78AA6582-CFC9-49EF-AEFF-59F454E9B76C@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0\n" +
              "\n" +
              "This is an S/MIME signed message\n" +
              "\n" +
              "------505692B81E452CF4C7D142C86D23FE2A\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "------505692B81E452CF4C7D142C86D23FE2A\n" +
              "Content-Type: application/x-pkcs7-signature; name=\"smime.p7s\"\n" +
              "Content-Transfer-Encoding: base64\n" +
              "Content-Disposition: attachment; filename=\"smime.p7s\"\n" +
              "\n" +
              "MIIDdAYJKoZIhvcNAQcCoIIDZTCCA2ECAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3\n" +
              "DQEHAaCCAaEwggGdMIIBBqADAgECAgLx8DANBgkqhkiG9w0BAQUFADAUMRIwEAYD\n" +
              "VQQDEwlsb2NhbGhvc3QwHhcNNjkxMjMxMjMwMDAwWhcNNzAxMjMxMjMwMDAwWjAU\n" +
              "MRIwEAYDVQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGB\n" +
              "AMckVHm7SuNClx4f238ZkEEJlXKZpQIOg80Oz2j/9JHWNT6tlihhWdGB8y4Stk0j\n" +
              "wiYFx2GxF2g/9y/LvBmtbtM5/N5asZZ7Kftrc1THrxpWi3KatPQg71JrT3FzY0m4\n" +
              "AI0iw5fAD8nFgqNx2IJMkCo5KMnSsmafEn6RFGi/JUJfAgMBAAEwDQYJKoZIhvcN\n" +
              "AQEFBQADgYEAQMS4hot+1lWJL51sy8Av0MWFE2JAOwFIvXFkbtwnsJI+uRe9QYrt\n" +
              "vgY6WuFXRejdo421QSS9nKEPpXxHkbgBqPQmkMVdQC6NNuXZ5pb8EtXamPtbLkra\n" +
              "hSLJBiAd0+ANRMZXeGC5Xd46uhRZKGH6jtLqHPBs2xS97f7M/KFJ/nQxggGbMIIB\n" +
              "lwIBATAaMBQxEjAQBgNVBAMTCWxvY2FsaG9zdAIC8fAwCQYFKw4DAhoFAKCB2DAY\n" +
              "BgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xMzAxMTEx\n" +
              "MzI3MDlaMCMGCSqGSIb3DQEJBDEWBBRlZib9QZab2LET+heP/XM0aAY7kzB5Bgkq\n" +
              "hkiG9w0BCQ8xbDBqMAsGCWCGSAFlAwQBKjALBglghkgBZQMEARYwCwYJYIZIAWUD\n" +
              "BAECMAoGCCqGSIb3DQMHMA4GCCqGSIb3DQMCAgIAgDANBggqhkiG9w0DAgIBQDAH\n" +
              "BgUrDgMCBzANBggqhkiG9w0DAgIBKDANBgkqhkiG9w0BAQEFAASBgFCNAw2Kyw5h\n" +
              "a6TGZgNSgD65b0bTcVumIXlMjERqQayrrH/oKRqRwCUgdraChyCE7JuJC2Oiqz0g\n" +
              "0WKryhHeC2cRRz3KMvtrftjwpDxfHWMi1ez7etFpxqdeg66aYqcJGwgqcMklgWVm\n" +
              "7SJUB29nMQ5tybCrsuzIJcMDB9FEkRg0\n" +
              "\n" +
              "------505692B81E452CF4C7D142C86D23FE2A--"
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.contents =~ "Error:   Certificate in keycert X509-1 has expired"
  }

  def "multipart plaintext x509 signed message keycert is not yet valid"() {
    given:
      setTime(LocalDateTime.parse("2013-01-11T12:40:44")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:     AUTO-1
                method:       X509
                owner:        /CN=localhost
                fingerpr:     97:A3:AF:11:34:00:25:1F:0F:24:4A:BF:CD:A6:E3:A5
                remarks:      certificate is not yet valid (only from 01/01/2100 00:00:00)
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIIBozCCAQygAwIBAgIE9IZI8DANBgkqhkiG9w0BAQUFADAUMRIwEAYDVQQDEwlsb2NhbGhvc3Qw
                certif:       IhgPMjA5OTEyMzEyMzAwMDBaGA8yMTg5MDkxNzIzMDAwMFowFDESMBAGA1UEAxMJbG9jYWxob3N0
                certif:       MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLh8DrNKmCq/lr2WWt6Ez+YcwdPYaxXzSO5ewc
                certif:       eDUrRfAm1YBecHaOhThNFW1FL4omtMOVylHEm5vPvVDwaCvat2yvtl7bTdS4Oao9j5epUqLYh9UF
                certif:       osYVMYXbmY01tEEm8fCvbz6jJA3KRgqBMjvUuaMWULreCaoO8+FzITQGCQIDAQABMA0GCSqGSIb3
                certif:       DQEBBQUAA4GBAKJYR2oUqfoBNmReWFxRF+u3t/84AGEhVJLlz8JwiSxBjKBWQi73FuPiCPO5yaZc
                certif:       sI1XV77r00RS+hBsiJJaWk+wgDJ+Z9+1Fe1MLBhAtnbFASbm6PwK4uRu6G1H6cF0KYdmkfOLK9XR
                certif:       SSZdF8vYxaL77qAvHUqerGo2H3K660GF
                certif:       -----END CERTIFICATE-----
                mnt-by:       OWNER-MNT
                source:       TEST
                password:     owner
             """.stripIndent(true))
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: testing@ripe.net\n" +
              "Content-Type: multipart/signed; protocol=\"application/x-pkcs7-signature\"; micalg=\"sha1\"; boundary=\"----ADB5359C8B6D785530E972C7D18D375B\"\n" +
              "Subject: NEW\n" +
              "Date: Mon, 7 Jan 2013 10:48:20 +0100\n" +
              "Message-Id: <78AA6582-CFC9-49EF-AEFF-59F454E9B76C@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0\n" +
              "\n" +
              "This is an S/MIME signed message\n" +
              "\n" +
              "------ADB5359C8B6D785530E972C7D18D375B\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "changed: denis@ripe.net 20121016\n" +
              "source:  TEST\n" +
              "\n" +
              "------ADB5359C8B6D785530E972C7D18D375B\n" +
              "Content-Type: application/x-pkcs7-signature; name=\"smime.p7s\"\n" +
              "Content-Transfer-Encoding: base64\n" +
              "Content-Disposition: attachment; filename=\"smime.p7s\"\n" +
              "\n" +
              "MIIDfAYJKoZIhvcNAQcCoIIDbTCCA2kCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3\n" +
              "DQEHAaCCAacwggGjMIIBDKADAgECAgT0hkjwMA0GCSqGSIb3DQEBBQUAMBQxEjAQ\n" +
              "BgNVBAMTCWxvY2FsaG9zdDAiGA8yMDk5MTIzMTIzMDAwMFoYDzIxODkwOTE3MjMw\n" +
              "MDAwWjAUMRIwEAYDVQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0A\n" +
              "MIGJAoGBAMuHwOs0qYKr+WvZZa3oTP5hzB09hrFfNI7l7Bx4NStF8CbVgF5wdo6F\n" +
              "OE0VbUUviia0w5XKUcSbm8+9UPBoK9q3bK+2XttN1Lg5qj2Pl6lSotiH1QWixhUx\n" +
              "hduZjTW0QSbx8K9vPqMkDcpGCoEyO9S5oxZQut4Jqg7z4XMhNAYJAgMBAAEwDQYJ\n" +
              "KoZIhvcNAQEFBQADgYEAolhHahSp+gE2ZF5YXFEX67e3/zgAYSFUkuXPwnCJLEGM\n" +
              "oFZCLvcW4+II87nJplywjVdXvuvTRFL6EGyIklpaT7CAMn5n37UV7UwsGEC2dsUB\n" +
              "Jubo/Ari5G7obUfpwXQph2aR84sr1dFJJl0Xy9jFovvuoC8dSp6sajYfcrrrQYUx\n" +
              "ggGdMIIBmQIBATAcMBQxEjAQBgNVBAMTCWxvY2FsaG9zdAIE9IZI8DAJBgUrDgMC\n" +
              "GgUAoIHYMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8X\n" +
              "DTEzMDExMTExNDA0NFowIwYJKoZIhvcNAQkEMRYEFGVmJv1BlpvYsRP6F4/9czRo\n" +
              "BjuTMHkGCSqGSIb3DQEJDzFsMGowCwYJYIZIAWUDBAEqMAsGCWCGSAFlAwQBFjAL\n" +
              "BglghkgBZQMEAQIwCgYIKoZIhvcNAwcwDgYIKoZIhvcNAwICAgCAMA0GCCqGSIb3\n" +
              "DQMCAgFAMAcGBSsOAwIHMA0GCCqGSIb3DQMCAgEoMA0GCSqGSIb3DQEBAQUABIGA\n" +
              "ntPMEumC30sv/4cT7xpLfZUSu99qXHn9owz+Bjtb7rqSjeQwvUTt7rqqhMqhV0Od\n" +
              "0WZZ0YlqhNO8JsZVfUHm6Cn3qaRtPb2TFrcGKzBMyhxsuPTAd4hGYLu+gKMhq8CE\n" +
              "qA6WlAMvLqDyAU4BKJvlT45CWZI+utwKSCrgtFlrhyE=\n" +
              "\n" +
              "------ADB5359C8B6D785530E972C7D18D375B--"
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.contents =~ "Error:   Certificate in keycert X509-1 is not yet valid"
  }

  def "multipart plaintext X509 signed message with hierarchical authentication"() {
    given:
      setTime(LocalDateTime.parse("2013-01-07T10:44:27")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       AUTO-1
                method:         X509
                owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net
                fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2
                certif:         -----BEGIN CERTIFICATE-----
                certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M
                certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP
                certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG
                certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy
                certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k
                certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy
                certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN
                certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv
                certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON
                certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9
                certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W
                certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1
                certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj
                certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx
                certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT
                certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC
                certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF
                certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1
                certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo
                certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==
                certif:         -----END CERTIFICATE-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))

      syncUpdate new SyncUpdate(data: """
                inetnum: 192.0.0.0 - 193.0.0.255
                netname: RIPE-NCC
                country: EU
                org:     ORG-HR1-TEST
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ALLOCATED PA
                mnt-by:  RIPE-NCC-HM-MNT
                mnt-by:  OWNER-MNT
                source:  TEST
                password: hm
                password: owner
                """.stripIndent(true))

      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: owner")

      syncUpdate new SyncUpdate(data:
              getFixtures().get("RIPE-NCC-HM-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: X509-1\nsource: TEST")
                      + "password: hm")

      clearPowerMaintainers()

      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed; boundary=\"Apple-Mail=_93B09F74-BFD6-4EDB-9C10-C12CBBB1B61A\"; " +
              "protocol=\"application/pkcs7-signature\"; micalg=sha1\n" +
              "Subject: NEW\n" +
              "Date: Mon, 7 Jan 2013 10:44:26 +0100\n" +
              "Message-Id: <1B28B20A-E47B-476F-ACEF-68504C8C40CC@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "X-Mailer: Apple Mail (2.1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_93B09F74-BFD6-4EDB-9C10-C12CBBB1B61A\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "inetnum: 193.0.0.0 - 193.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: NL\n" +
              "org: ORG-HR1-TEST\n" +
              "admin-c: TP1-TEST\n" +
              "tech-c: TP1-TEST\n" +
              "status: ASSIGNED PA\n" +
              "mnt-by: OWNER-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_93B09F74-BFD6-4EDB-9C10-C12CBBB1B61A\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=smime.p7s\n" +
              "Content-Type: application/pkcs7-signature;\n" +
              "\tname=smime.p7s\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIDtTCCA7Ew\n" +
              "ggMaoAMCAQICAgF8MA0GCSqGSIb3DQEBBAUAMIGFMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9v\n" +
              "cmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMREwDwYDVQQKEwhSSVBFIE5DQzEMMAoGA1UE\n" +
              "CxMDT1BTMQwwCgYDVQQDEwNDQTIxGzAZBgkqhkiG9w0BCQEWDG9wc0ByaXBlLm5ldDAeFw0xMTEy\n" +
              "MDExMjM3MjNaFw0yMTExMjgxMjM3MjNaMIGAMQswCQYDVQQGEwJOTDEWMBQGA1UECBMNTm9vcmQt\n" +
              "SG9sbGFuZDERMA8GA1UEChMIUklQRSBOQ0MxCzAJBgNVBAsTAkRCMRcwFQYDVQQDEw5FZHdhcmQg\n" +
              "U2hyeWFuZTEgMB4GCSqGSIb3DQEJARYRZXNocnlhbmVAcmlwZS5uZXQwgZ8wDQYJKoZIhvcNAQEB\n" +
              "BQADgY0AMIGJAoGBAMNs8uEHIiGdYms93PWA4bysV3IUwsabb+fwP6ACS9Jc8OaRQkT+1oK1sfw+\n" +
              "yX0YloCwJXtnAYDeKHoaPNiJ+dro3GN6BiijjV72KyKThHxWmZwXeqOKVCReREVNNlkuJTvziani\n" +
              "cZhlr9+bxhwRho1mkEvBmxEzPZTYKl0vQpd1AgMBAAGjggExMIIBLTAJBgNVHRMEAjAAMCwGCWCG\n" +
              "SAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUwSiXle3q\n" +
              "9cufhKDVUCxrEPhj/9cwgbIGA1UdIwSBqjCBp4AUviRV1EFXFxVsA8ildF9OwyDKY9ihgYukgYgw\n" +
              "gYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJk\n" +
              "YW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqG\n" +
              "SIb3DQEJARYMb3BzQHJpcGUubmV0ggEAMB4GA1UdEQQXMBWCE2UzLTIuc2luZ3cucmlwZS5uZXQw\n" +
              "DQYJKoZIhvcNAQEEBQADgYEAU5D2f5WK8AO5keVVf1/kj+wY/mRis9C7nXyhmQgrNcxzxqq+0Q7Q\n" +
              "mAYsNWMKDLZRemks4FfTGTmVT2SosQntXQBUoR1FGD3LQYLDZ2p1d7A6falUNu0PR8yAqJpT2Q2E\n" +
              "acKOic/UwHXhVorC1gbKZ5fN0dbUHqSk1zm41VQpobAxggLWMIIC0gIBATCBjDCBhTELMAkGA1UE\n" +
              "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UE\n" +
              "ChMIUklQRSBOQ0MxDDAKBgNVBAsTA09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxv\n" +
              "cHNAcmlwZS5uZXQCAgF8MAkGBSsOAwIaBQCgggGfMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEw\n" +
              "HAYJKoZIhvcNAQkFMQ8XDTEzMDEwNzA5NDQyN1owIwYJKoZIhvcNAQkEMRYEFFP5sA8UIg1bjKVv\n" +
              "GVjlFBi3ZwKeMIGdBgkrBgEEAYI3EAQxgY8wgYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1O\n" +
              "b29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYD\n" +
              "VQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDCB\n" +
              "nwYLKoZIhvcNAQkQAgsxgY+ggYwwgYUxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xs\n" +
              "YW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAPBgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMx\n" +
              "DDAKBgNVBAMTA0NBMjEbMBkGCSqGSIb3DQEJARYMb3BzQHJpcGUubmV0AgIBfDANBgkqhkiG9w0B\n" +
              "AQEFAASBgJZtbjfmMMFYBGBVNskAzCmddmLIvgPy3y4E3IxlUGIKrX47HUPw2oD9m07eqgpfrW4T\n" +
              "Ex2waz+gLO7uzL8Um+LByfwBlSFk5gUJRAGpkbR7yys9iSQAUqLGmBtym5DGHbrHrZeVXkf6WKIp\n" +
              "F3tPjx4hbd3QC5mYCRm6eR4UbIf4AAAAAAAA\n" +
              "\n" +
              "--Apple-Mail=_93B09F74-BFD6-4EDB-9C10-C12CBBB1B61A--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 193.0.0.0 - 193.0.0.255" }
  }

  def "multipart plaintext PGP signed message with hierarchical authentication"() {
    given:
      setTime(LocalDateTime.parse("2013-01-07T10:48:20")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")

      syncUpdate new SyncUpdate(data:
              getFixtures().get("RIPE-NCC-HM-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: hm")

      syncUpdate new SyncUpdate(data: """
                  inetnum: 192.0.0.0 - 193.0.0.255
                  netname: RIPE-NCC
                  country: EU
                  org:     ORG-HR1-TEST
                  admin-c: TP1-TEST
                  tech-c:  TP1-TEST
                  status:  ALLOCATED PA
                  mnt-by:  RIPE-NCC-HM-MNT
                  mnt-by:  OWNER-MNT
                  source:  TEST
                  password: hm
                  password: owner
                  """.stripIndent(true))

    clearPowerMaintainers()
    then:
      def message = send "From: noreply@ripe.net\n" +
              "Content-Type: multipart/signed; boundary=\"Apple-Mail=_5C37A745-48FA-47C6-8B90-EB93253082EB\"; " +
              "protocol=\"application/pgp-signature\"; micalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Mon, 7 Jan 2013 10:48:20 +0100\n" +
              "Message-Id: <D60E6BB3-15F8-438B-AEB0-70B9817E5ED5@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_5C37A745-48FA-47C6-8B90-EB93253082EB\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "inetnum: 193.0.0.0 - 193.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: NL\n" +
              "org: ORG-HR1-TEST\n" +
              "admin-c: TP1-TEST\n" +
              "tech-c: TP1-TEST\n" +
              "status: ASSIGNED PA\n" +
              "mnt-by: OWNER-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_5C37A745-48FA-47C6-8B90-EB93253082EB\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ6pnkAAoJEO6ZHuIo9s1s5lEH/RNr6UWbKz/UkJ1PbfJYwed0\n" +
              "xJcLk4pEih2B/gd2FZ3RD8TEg0AZ2jdJF4uOwi7+d+w+vLu8YNc20e5UAdhIKD7v\n" +
              "mj+r1q7N9/xEvOq+GzsL+vOJP+DAvbLOgzRHO84p5xyuo5r0y4tpPq0C8hHkAm/T\n" +
              "MKwXt45Yif3A4DlMud4ii9uWSEAuAOPFCcSRnYUUC++mFxb7YVWMZbL+URh2X3eR\n" +
              "V2hKkomlvnuQhpJyuhZuO7DNKTYMClF72wxH4MZkwAb3GTvbEQEe6YGDmV4UF9BO\n" +
              "vcqRB0IOcOII39zpva372rPy3yeorUcyKeTyO8tQHDwU20G0qL4aeNUOnVeYx7o=\n" +
              "=0qC6\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_5C37A745-48FA-47C6-8B90-EB93253082EB--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 193.0.0.0 - 193.0.0.255" }
  }

  def "multipart plaintext PGP signed syncupdate with hierarchical authentication"() {
    given:
      setTime(LocalDateTime.parse("2013-04-08T17:00:29"))
    when:
      syncUpdate new SyncUpdate(data: """
                key-cert:       PGPKEY-E06D7E01
                method:         PGP
                owner:          No Reply <noreply@ripe.net>
                fingerpr:       C4A4 A9E3 499C 6CBF 05D3  3D44 0B9F 6E90 E06D 7E01
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                certif:         Comment: GPGTools - http://gpgtools.org
                certif:
                certif:         mI0EUWKzVQEEANvpzfk+XnbyKDtA91SNokanRSy+hHkRtbOyfkMSOg5P1Jri7m1F
                certif:         L9xTrJ7VS+FsLt71kYUDGtIUf7zV7xxZwZIsJABP9s4F+BYDMHfMH4zYnjz6wWtF
                certif:         yRtoWxwtgie+SxA+OAjNbb82FBNL7oLfr8IKadjH+Pq98tF2gKqJnXvrABEBAAG0
                certif:         G05vIFJlcGx5IDxub3JlcGx5QHJpcGUubmV0Poi5BBMBAgAjBQJRYrNVAhsDBwsJ
                certif:         CAcDAgEGFQgCCQoLBBYCAwECHgECF4AACgkQC59ukOBtfgEldAP/T/fK4r2SXmYr
                certif:         333K+FFHCzIaMvEMjOAhlMGCuLXrN1+aZyjAItaPCOzLg4gDu8ojTUnPcd74150E
                certif:         0J4Jajbf+spFWZY5riDUMzZyy7cZEiHlPoVWa01uNpxMUJ2RSf5LcMcCh3abERpX
                certif:         zF8QrEcN1RjaEDdeKolGua8oei/mFG24jQRRYrNVAQQAlv5HKVYPHdi63kClHDm5
                certif:         ds0wIigyAr9QIV44+7sp0Z6bNzb+Kf0DpTSWNj++NWJT8mfXAepnMMEtLnycRTGf
                certif:         KzDT1u5rUsI1NyMS0RAmNJv1xybX6zLAR+sOyQcnqzqe4IQBwQ/VJgwtLsIb4ACm
                certif:         7klSwbBuHsadyi8/C1gBrfMAEQEAAYifBBgBAgAJBQJRYrNVAhsMAAoJEAufbpDg
                certif:         bX4B0gAEAIwsjSDrnEIDNvgG/GIJ/Qj1fGSv/JqKMRFYebJrqG0Znj61/+X9PD3e
                certif:         IGXJ80L++iS0c9trv5ZIcVorw0+5k4H4NFZQApAuBYWpvqF+wp9I09ri4iCtyYl7
                certif:         TjxeoP49PSDTqaYtLdWNqHvIa0Ri2Qck1M8ukloiaA8nAMyf1Amj
                certif:         =p8dm
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         OWNER-MNT
                source:         TEST
                password:       owner
             """.stripIndent(true))

      syncUpdate new SyncUpdate(data: """
                  inetnum: 192.0.0.0 - 193.0.0.255
                  netname: RIPE-NCC
                  country: EU
                  org:     ORG-HR1-TEST
                  admin-c: TP1-TEST
                  tech-c:  TP1-TEST
                  status:  ALLOCATED PA
                  mnt-by:  RIPE-NCC-HM-MNT
                  mnt-by:  OWNER-MNT
                  source:  TEST
                  password: hm
                  password: owner
                  """.stripIndent(true))

    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-E06D7E01\nsource: TEST")
                      + "password: owner")
    then:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("RIPE-NCC-HM-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-E06D7E01\nsource: TEST")
                      + "password: hm")
    then:
      def message = syncUpdate new SyncUpdate(data:
              """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                inetnum: 193.0.0.0 - 193.0.0.255
                netname: RIPE-NCC
                descr: description
                country: NL
                org: ORG-HR1-TEST
                admin-c: TP1-TEST
                tech-c: TP1-TEST
                status: ASSIGNED PA
                mnt-by: OWNER-MNT
                changed: ripe@test.net 20120505
                source: TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                Comment: GPGTools - http://gpgtools.org

                iJwEAQECAAYFAlFi240ACgkQC59ukOBtfgGaVwP/fo+Ii+OycsOWOGrsqFX7YjHY
                klNspWS9Gh0WplRo/bv7TSLOLlkmFCGpCUrd3MMJTqUHaLr+1fIHzWUcLVGjKcbw
                4gVRQo+lXGkBOuYs55GBmJrxBFIRUkQPmbIPPTWM1TQQcKNMXlVLyAGR8EFyS5g1
                8jnvRrjSfhMzuaXuPOM=
                =B14w
                -----END PGP SIGNATURE-----
                """.stripIndent(true))

    then:
      message.contains("Create SUCCEEDED: [inetnum] 193.0.0.0 - 193.0.0.255")
  }

  def "multipart plaintext PGP signed message with hierarchical authentication and different signers"() {
    given:
      setTime(LocalDateTime.parse("2013-01-08T16:09:06")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-28F6CD6C\nsource: TEST")
                      + "password: owner")
      syncUpdate new SyncUpdate(data:
              getFixtures().get("RIPE-NCC-HM-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: hm")

      syncUpdate new SyncUpdate(data: """
                  inetnum: 192.0.0.0 - 193.0.0.255
                  netname: RIPE-NCC
                  country: EU
                  org:     ORG-HR1-TEST
                  admin-c: TP1-TEST
                  tech-c:  TP1-TEST
                  status:  ALLOCATED PA
                  mnt-by:  RIPE-NCC-HM-MNT
                  mnt-by:  OWNER-MNT
                  source:  TEST
                  password: hm
                  password: owner
                  """.stripIndent(true))

    clearPowerMaintainers()

    then:
          def message = send  "From: inetnum@ripe.net\n" +
              "Content-Type: multipart/signed; boundary=\"Apple-Mail=_02EDC824-733F-459F-93D6-8E066E37EFC8\"; " +
              "protocol=\"application/pgp-signature\"; micalg=pgp-sha1\n" +
              "Subject: NEW\n" +
              "Date: Mon, 7 Jan 2013 10:48:20 +0100\n" +
              "Message-Id: <78AA6582-CFC9-49EF-AEFF-59F454E9B76C@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Mime-Version: 1.0\n" +
              "\n" +
              "\n" +
              "--Apple-Mail=_02EDC824-733F-459F-93D6-8E066E37EFC8\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Type: text/plain;\n" +
              "\tcharset=us-ascii\n" +
              "\n" +
              "-----BEGIN PGP SIGNED MESSAGE-----\n" +
              "Hash: SHA1\n" +
              "\n" +
              "inetnum: 193.0.0.0 - 193.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: NL\n" +
              "org: ORG-HR1-TEST\n" +
              "admin-c: TP1-TEST\n" +
              "tech-c: TP1-TEST\n" +
              "status: ASSIGNED PA\n" +
              "mnt-by: OWNER-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ7DaSAAoJELvMuy1XY5UNStgH/Rc0SKMmn2QPE23d/E2bLRAr\n" +
              "bYi6BmrfQ4cZH+5MaDJ7/NLdGTXk+Nf/7PBz2vA9kC2O9g8nD89lgKj/H7WEVSkI\n" +
              "dkq+M1LlOZgxcPOo8iuLkme2N4pTKsnVeoeJuLecrl3mH5nxPw4oEQ03hzQ7bq1N\n" +
              "xa3yw/rdWYA+BdvWQJM7TsfC2H5HyokeFDJZGG5GvE6EaFjQEN4JlEplONZ/jkWJ\n" +
              "PYfWydSQPXU+xbGnptFmRGPGo4KMfJxaJEHVuHZeGxC+dvTs26WG2KuvhNTQS/4g\n" +
              "qimC9COWPgwV4c83hOlRfX/6XZMrM3XFVMxIO8b7POXtdJb9z1NY9P2JWSxl5Ac=\n" +
              "=FPqF\n" +
              "-----END PGP SIGNATURE-----\n" +
              "--Apple-Mail=_02EDC824-733F-459F-93D6-8E066E37EFC8\n" +
              "Content-Transfer-Encoding: 7bit\n" +
              "Content-Disposition: attachment;\n" +
              "\tfilename=signature.asc\n" +
              "Content-Type: application/pgp-signature;\n" +
              "\tname=signature.asc\n" +
              "Content-Description: Message signed with OpenPGP using GPGMail\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1.4.12 (Darwin)\n" +
              "\n" +
              "iQEcBAEBAgAGBQJQ7Da6AAoJEO6ZHuIo9s1sA5sIALd4RobeYcdftxReJEt+TprK\n" +
              "ufTUMpj4PggvUt1akK48NpD+WNXKT9C9eqJPXpcNauMJklYJ2OlIWxcaYYZWUo6B\n" +
              "TezSptlPLL8ETZsnyJJBUdBErbRwtN+XVRv4glG1yFvQ0icfnum0sJkzZzFLWsVO\n" +
              "kjixHjo7VlDEDv7xhqdGTCFnRgzll/CpDrldJans/Mf+5STzC31AlUpxStutIma0\n" +
              "DQdNy0QKAWneyJRjv9KYpvJWWjQKSN9h6ODd6V0BWHf+MYu/AUZw4Z3DsKZQurJC\n" +
              "YqORgqdUYpJeUO92JXSSdQ9uZg6olt49nPJpF1i98JgHHcQGwrlLL+kD169a9TU=\n" +
              "=GmBR\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--Apple-Mail=_02EDC824-733F-459F-93D6-8E066E37EFC8--"
    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 193.0.0.0 - 193.0.0.255" }
  }

  def "inline plaintext PGP signed message with obsolete application/pgp content-type"() {
    given:
      setTime(LocalDateTime.parse("2015-11-20T15:22:20")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: personupdatex@ripe.net\n" +
              "Content-Type: application/pgp; x-action=sign; format=text\n" +
              "To: auto-dbm@ripe.net\n" +
              "Subject: NEW\n" +
              "Message-ID: <20121204141256.GM1426@f17.dmitry.net>\n" +
              "Mime-Version: 1.0\n" +
              "Content-Type: application/pgp; x-action=sign; format=text\n" +
              "Content-Disposition: inline; filename=\"msg.pgp\"\n" +
              "User-Agent: Mutt/1.4.2.3i\n" +
              "\n" +
              "-----BEGIN PGP SIGNED MESSAGE-----\n" +
              "Hash: SHA1\n\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "source:  TEST\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "iQEcBAEBAgAGBQJWTyycAAoJELvMuy1XY5UNgIMH/RnFAmwKu8rs6eqzWlWIGgZ+\n" +
              "QkCxy/cdWn8LOyUw77JtJ+gXGsjcZjy1rRCtHlYGnM97I/oiAwDCaiPGnCzD6bjI\n" +
              "A0zGc21K/cZjBZmjB2+fOouNbLnn2PE/XQi6JR/tX4VIN7FOXQqjk1i7Rpwy/yug\n" +
              "aHepJIlIyrgGKEyhunojctk1VUdNwOlp0yaS0sPWjJLldEFxq2HbDnSRcgRtfB5z\n" +
              "ep2YNgDgCuVZLkF6GCIQ0cXSelIYhkwfpcgxIcEmwI2frPYAm+uTxTe5J2sjJNDz\n" +
              "a52BaUJYffxrAAXSo1NWX4XQCDL632NRAJ6aLU79+MBrETkPxyebd4DyG0lyHBE=\n" +
              "=xrVg\n" +
              "-----END PGP SIGNATURE-----"

    then:
      def ack = ackFor message

      ack.success
      ack.summary.nrFound == 1
      ack.summary.assertSuccess(1, 1, 0, 0, 0)
      ack.summary.assertErrors(0, 0, 0, 0)

      ack.countErrorWarnInfo(0, 0, 0)
      ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
  }

  def "pgp signed message with public key attached is not supported"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: personupdatex@ripe.net\n" +
              "To: auto-dbm@ripe.net\n" +
              "Subject: NEW\n" +
              "Message-ID: <20121204141256.GM1426@f17.test.net>\n" +
              "Mime-Version: 1.0\n" +
              "Content-Type: multipart/signed; micalg=pgp-sha1;\n" +
              " protocol=\"application/pgp-signature\";\n" +
              " boundary=\"oDBhsOJvnMW4uj7OE4r7Skx6vtnqGcFMG\"\n"
              "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:24.0) Gecko/20100101 Thunderbird/24.4.0\n" +
              "X-Enigmail-Version: 1.6\n" +
              "\n" +
              "This is an OpenPGP/MIME signed message (RFC 4880 and 3156)\n" +
              "--oDBhsOJvnMW4uj7OE4r7Skx6vtnqGcFMG\n" +
              "Content-Type: multipart/mixed;\n" +
              " boundary=\"------------070902000202090608000102\"\n" +
              "\n" +
              "This is a multi-part message in MIME format.\n" +
              "--------------070902000202090608000102\n" +
              "Content-Type: text/plain; charset=ISO-8859-1\n" +
              "Content-Transfer-Encoding: quoted-printable\n" +
              "\n" +
              "person:  First Person\n" +
              "address: St James Street\n" +
              "address: Burnley\n" +
              "address: UK\n" +
              "phone:   +44 282 420469\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "source:  TEST\n" +
              "\n" +
              "\n" +
              "--------------070902000202090608000102\n" +
              "Content-Type: application/pgp-keys;\n" +
              " name=\"0x5763950D.asc\"\n" +
              "Content-Transfer-Encoding: quoted-printable\n" +
              "Content-Disposition: attachment;\n" +
              " filename=\"0x5763950D.asc\"\n" +
              "\n" +
              "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
              "Version: GnuPG v1\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
              "yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
              "2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
              "E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
              "A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
              "YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
              "ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
              "CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
              "Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
              "0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
              "xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
              "0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
              "xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
              "uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
              "eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
              "pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
              "xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
              "YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
              "dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
              "GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
              "HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
              "eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
              "6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
              "RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
              "a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ=3D=3D\n" +
              "=3DHQmg\n" +
              "-----END PGP PUBLIC KEY BLOCK-----\n" +
              "\n" +
              "--------------070902000202090608000102--\n" +
              "\n" +
              "--oDBhsOJvnMW4uj7OE4r7Skx6vtnqGcFMG\n" +
              "Content-Type: application/pgp-signature; name=\"signature.asc\"\n" +
              "Content-Description: OpenPGP digital signature\n" +
              "Content-Disposition: attachment; filename=\"signature.asc\"\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Version: GnuPG v1\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "Comment: Using GnuPG with Thunderbird - http://www.enigmail.net/\n" +
              "\n" +
              "iQEcBAEBAgAGBQJTKyb0AAoJELvMuy1XY5UNvRYH/1D3Kj7pwM6flT/h+KqJ0JOd\n" +
              "NOma/qNvc33u/wYstFPIhCj2qzdpQ1Pwat6kzVQmwiqkUMeB0V7asGe0Wi2KXt13\n" +
              "uqScGR9BTVWLzpcq/axykxJ8ThGu1kE7weA4LrZSvn2hn4Mjfu3epddRdlLcFcDo\n" +
              "2/DQFMOfbwirLmobYaq+H74OBQ4hFRb3z7H0TNDkYr5pIp+EmcZ29wznKRQWk6+C\n" +
              "9GYpzxqUb8Vc5WHtreW5WYlxkme5f1B/G0NnbZHi9y3uwvgtdUp+7JXuw1dBGpOF\n" +
              "v0hAfHFkttdiCCYcI8dUzQZjsNthjV/27EMwjvvZ46Iq6P8LhgTo3nH1pqBLRNw=\n" +
              "=APKb\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--oDBhsOJvnMW4uj7OE4r7Skx6vtnqGcFMG--"

    then:
      def ack = ackFor message

      ack.failed
      ack.summary.nrFound == 0
      ack =~ /\*\*\*Error:   No valid update found/
  }

  def "PGP signed mailupdate with non-ASCII character succeeds"() {
    given:
      setTime(LocalDateTime.parse("2020-03-10T10:23:56")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "From: personupdatex@ripe.net\n" +
              "To: auto-dbm@ripe.net\n" +
              "Subject: NEW\n" +
              "Message-ID: <20121204141256.GM1426@f17.test.net>\n" +
              "Mime-Version: 1.0\n" +
              "Content-Type: text/plain; charset=\"ISO-8859-1\"\n" +
              "Content-Transfer-Encoding: quoted-printable\n" +
              "\n" +
              "-----BEGIN PGP SIGNED MESSAGE-----\n" +
              "Hash: SHA256\n" +
              "\n" +
              "person:  First Person\n" +
              "address: Sl=FCnstrasse 10\n" +
              "phone:   +49 123456\n" +
              "nic-hdl: FP1-TEST\n" +
              "mnt-by:  OWNER-MNT\n" +
              "source:  TEST\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "Comment: GPGTools - http://gpgtools.org\n" +
              "\n" +
              "iQEzBAEBCAAdFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAl5nXCkACgkQu8y7LVdj\n" +
              "lQ0gEggApgg+eXo7HK7i6jvpmL4NDPTLaj3yh7REkU/QXxXSO9ygfXo6aT7OtxWY\n" +
              "E1YrZ48vKoIjW0rx7ojF6wJ9XgFZ3/UGiPoH2NmMq819LTP2qtlcUh8Dv1x8EI1i\n" +
              "X96Q2r+v+XH+uD/TJVsVsJb+9UDeTvCtVR2GNPx/Q44g8e2BDlu3BUvQVbBJtTMr\n" +
              "x8sLDoRaPPyQdtfgNnMLpzD7uIU5OPJRShQNoODuK5Hra/FkujeIO4rHukX5lp6a\n" +
              "CXnmSnXDTnENLyl/voMphxsnCP1ntRUpCEOs6RYGq6ibBeOSJ8UngIL/vSTGhEjp\n" +
              "7ez9mNBag29CsrbNy5ZLm5NORabDCA=3D=3D\n" +
              "=3DUdKK\n" +
              "-----END PGP SIGNATURE-----"
    then:
      def ack = ackFor message
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "pgp signed message with invalid signature"() {
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v2

                nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.contents.contains("Create FAILED: [person] FP1-TEST   First Person")
      ack.contents.contains(
              "***Error:   Authorisation for [person] FP1-TEST failed\n" +
              "            using \"mnt-by:\"\n" +
              "            not authenticated by: OWNER-MNT")
  }

  def "pgp signed message with expired key"() {
    given:
      setTime(LocalDateTime.parse("2019-02-05T17:02:00")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-C88CA438\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256
                
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Comment: GPGTools - http://gpgtools.org
                
                iMUEAQEIAC8WIQRhCiRXK6OldV+FTdheYmxyyIykOAUCXFmzeBEcZXhwaXJlZEBy
                aXBlLm5ldAAKCRBeYmxyyIykOD52A/0RfF5+gnNrJEd4FhS9In/MDoBJi08C3pHp
                q5wCuzwv9RP9vnN/pUSsNDfCYvOJnDLceBaceIvszpSfRxts73PwqFEA/KRcqmy1
                0deBMOpFzP2z+LpMBPVRxrWPv2lEaaf5KGopThQOmrFXjYYsyuruf20UEtzMhWX2
                cjfleMW4Xw==
                =BT/+
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Public key in keycert PGPKEY-C88CA438 has expired"]
  }

  def "pgp signed message with revoked key"() {
    given:
      setTime(LocalDateTime.parse("2019-02-05T17:02:00")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-8947C26B\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256
                
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Comment: GPGTools - http://gpgtools.org
                
                iLMEAQEIAB0WIQR0iVLg8efGJV9T1sKSoZigiUfCawUCXFm2NAAKCRCSoZigiUfC
                a6pyBACKVnh3pmxhhanWEY2YpCSXRB12YHlOgXmNtrPgDuGmYta9qUHDiH0OM30P
                SvWG48UXXDc+eH3UmjJOuPM1AJ6Ms3totvdxDj1QkSzckkrVqxbz5UZ0bnfxea6O
                XttJxq5FbaSzl1IR7FYu9kT8afqKm45R1mjogG35tp2J5ApX4Q==
                =5zUI
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Public key in keycert PGPKEY-8947C26B is revoked"]
  }

  def "pgp signed message has expired"() {
    given:
      setTime(LocalDateTime.parse("2019-02-05T18:38:00")) // current time is >1 hour after signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send new Message(
              subject: "",
              body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256
                
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Comment: GPGTools - http://gpgtools.org
                
                iQEzBAEBCAAdFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAlxZu6IACgkQu8y7LVdj
                lQ2+lgf/TbF6zpMUzfMEwT/bzHgpLuk5HcQ6x8d959WRIHYmpf/835T8I4E57l9Q
                HfmMQfJ18H3emZeUro1FoiR9Zn5yfuoufJLjKgZDubJEteOfGiPn5qSb6+qGUs+l
                Gyx36Y7McqQBhXx1GOX4yPY92yceXVQjBrEzFnZactF8Nw1Qcd+wzUvJ1Wkp9427
                6Sh5KrTwrZv7GzU7LFFpg4zjIdg/kw2KAYykVjlgJduDOmKbZr0Bg/zVL2kgTPId
                iWmi1ezPchI/egyWOk4LhQUo5pt6Umz0filn4biM/D7eJQltShzKH+0GZNYBe2g7
                9RtMtNMzojs5RXER+S8U07RhVgYtrg==
                =yqKe
                -----END PGP SIGNATURE-----
                """.stripIndent(true))
    then:
      def ack = ackFor message

      ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
      ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
              ["Message was signed more than one hour ago"]
  }


  def "pgp signed multipart/mixed nested part"() {
    given:
      setTime(LocalDateTime.parse("2016-03-31T17:25:15")) // current time must be within 1 hour of signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST")
                      + "password: owner")
    then:
      def message = send "" +
                "To: auto-dbm@ripe.net\n" +
                "From: No Reply <noreply@ripe.net>\n" +
                "Subject: NEW\n" +
                "Message-ID: <56FCE84F.2010807@ripe.net>\n" +
                "Date: Thu, 31 Mar 2016 11:05:19 +0200\n" +
                "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:38.0) Gecko/20100101\n" +
                " Thunderbird/38.5.1\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/signed; micalg=pgp-sha1;\n" +
                " protocol=\"application/pgp-signature\";\n" +
                " boundary=\"JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\"\n" +
                "\n" +
                "This is an OpenPGP/MIME signed message (RFC 4880 and 3156)\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\n" +
                "Content-Type: multipart/mixed; boundary=\"SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\"\n" +
                "From: No Reply <noreply@ripe.net>\n" +
                "To: auto-dbm@ripe.net\n" +
                "Message-ID: <56FCE84F.2010807@ripe.net>\n" +
                "Subject: NEW\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh--\n" +
                "\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\n" +
                "Content-Type: application/pgp-signature; name=\"signature.asc\"\n" +
                "Content-Description: OpenPGP digital signature\n" +
                "Content-Disposition: attachment; filename=\"signature.asc\"\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBCAAGBQJW/UFbAAoJELvMuy1XY5UN8sIH/jknae8l8dK4pJnf1CK4fXLq\n" +
                "YedvGzo10gpLKlSu0UnBRapE6aZcPNmYLMJReP/JhbPfRAr7XQCRJKVwOngntmwi\n" +
                "b4p9C+GYOUEUScLbBYe4FS70xPQZBEqDEB+pPEQI7DSRMQs/aF3fSXGHygJA2EGp\n" +
                "QE5e2i0OwL1usnkRb87IXxFoL/LAalHogaU7m+vwJADD5ERqF1jNfiw4B8LQRF8x\n" +
                "Yq5BtICOBOj7sL/JCKL17KrGlcQFWL3StLGf6IghltSSnAVQesKY4k2SzyyCulKL\n" +
                "v3REE7AAtRIih8l+VP4dL09AD1/mWT38D24gjOC+HOHcdGbl7YmE9cHr3xwetgY=\n" +
                "=GA9B\n" +
                "-----END PGP SIGNATURE-----\n" +
                "\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg--"
    then:
      def ack = ackFor message
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }

  def "pgp signed message with base64 encoded content"() {
    given:
      setTime(LocalDateTime.parse("2021-09-29T11:23:22")) // current time is >1 hour after signing time
    when:
      syncUpdate new SyncUpdate(data:
              getFixtures().get("OWNER-MNT").stripIndent(true).
                      replaceAll("source:\\s*TEST", "auth: PGPKEY-5763950D\nsource: TEST") +
                      "password: owner")
    then:
      def message = send "" +
              "Message-ID: <a64222cd-6cb7-4a6a-33ec-3111e9e79331@ripe.net>\n" +
              "Date: Wed, 29 Sep 2021 11:23:17 +0200\n" +
              "MIME-Version: 1.0\n" +
              "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:91.0)\n" +
              " Gecko/20100101 Thunderbird/91.1.2\n" +
              "Content-Language: en-US\n" +
              "To: test-dbm@ripe.net\n" +
              "From: No Reply <noreply@ripe.net>\n" +
              "Subject: NEW\n" +
              "Content-Type: multipart/signed; micalg=pgp-sha256;\n" +
              " protocol=\"application/pgp-signature\";\n" +
              " boundary=\"------------z60obBv7fVfOOgKt1tN8jpOM\"\n" +
              "\n" +
              "This is an OpenPGP/MIME signed message (RFC 4880 and 3156)\n" +
              "--------------z60obBv7fVfOOgKt1tN8jpOM\n" +
              "Content-Type: multipart/mixed; boundary=\"------------Vea17wsKD2zUMOZmbw2ly2r0\";\n" +
              " protected-headers=\"v1\"\n" +
              "From: No Reply <noreply@ripe.net>\n" +
              "To: test-dbm@ripe.net\n" +
              "Message-ID: <a64222cd-6cb7-4a6a-33ec-3111e9e79331@ripe.net>\n" +
              "Subject: NEW\n" +
              "\n" +
              "--------------Vea17wsKD2zUMOZmbw2ly2r0\n" +
              "Content-Type: text/plain; charset=UTF-8; format=flowed\n" +
              "Content-Transfer-Encoding: base64\n" +
              "\n" +
              "cGVyc29uOsKgIEZpcnN0IFBlcnNvbg0KYWRkcmVzczogU3QgSmFtZXMgU3RyZWV0DQphZGRy\n" +
              "ZXNzOiBCdXJubGV5DQphZGRyZXNzOiBVSw0KcGhvbmU6wqDCoCArNDQgMjgyIDQyMDQ2OQ0K\n" +
              "bmljLWhkbDogRlAxLVRFU1QNCm1udC1ieTrCoCBPV05FUi1NTlQNCnNvdXJjZTrCoCBURVNU\n" +
              "DQoNCg==\n" +
              "\n" +
              "--------------Vea17wsKD2zUMOZmbw2ly2r0--\n" +
              "\n" +
              "--------------z60obBv7fVfOOgKt1tN8jpOM\n" +
              "Content-Type: application/pgp-signature; name=\"OpenPGP_signature.asc\"\n" +
              "Content-Description: OpenPGP digital signature\n" +
              "Content-Disposition: attachment; filename=\"OpenPGP_signature\"\n" +
              "\n" +
              "-----BEGIN PGP SIGNATURE-----\n" +
              "\n" +
              "wsB5BAABCAAjFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAmFUMIUFAwAAAAAACgkQu8y7LVdjlQ0X\n" +
              "dAf/TqDMoPLSVz54m6LpJi1/CZ2aG5aEWAZUUkz1Qjf0KbPN6i3tJRvzovb8KWTyge8/JtSIA3tW\n" +
              "3Srq1OJE9dimlC7cTAjRtk+V5FPn35vWGyH8SbQPZ2GtcIXY7BvChfXFykXZO4nt47+oU2EN+V1V\n" +
              "Y9HnLnpR6atGTqdGWAGjcHTd5b2wMzxFaXyeTBNehDNAFiiIeKupdoVy8dOES4mW9lgmDIVFIzNq\n" +
              "gAxdlPUCJoquVHwM+g5VZG6Wb1ahUs/c3ACnb7iw/x/TfnUbtryy22q9m1sgBHRGgUpc6Zp7XyGB\n" +
              "PbhoRb5m4s95oxOh5crFMcRveIx8SCj45YqX0ONuew==\n" +
              "=mjpm\n" +
              "-----END PGP SIGNATURE-----\n" +
              "\n" +
              "--------------z60obBv7fVfOOgKt1tN8jpOM--\n"

    then:
      def ack = ackFor message
      ack =~ "Create SUCCEEDED: \\[person\\] FP1-TEST   First Person"
  }
}
