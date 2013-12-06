package net.ripe.db.whois.spec.integration
import com.google.common.io.Resources
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

@org.junit.experimental.categories.Category(IntegrationTest.class)
class KeycertIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "ADMIN-PN": """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: TEST-RIPE
            mnt-by:  UPD-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """
        ];
    }

    def "create keycert success"() {
      given:
        def request = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-28F6CD6C/
    }

    def "create keycert no pgp key"() {
      given:
        def update = new SyncUpdate(data: """\
            key-cert:     PGPKEY-28F6CD6C
            method:       PGP
            owner:        DFN-CERT (2003), ENCRYPTION Key
            fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C
            certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
            certif:       Version: GnuPG v1.4.11 (Darwin)
            certif:
            certif:       -----END PGP PUBLIC KEY BLOCK-----
            mnt-by:       UPD-MNT
            notify:       eshryane@ripe.net
            changed:      eshryane@ripe.net 20120213
            source:       TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create FAILED: \[key-cert\] PGPKEY-28F6CD6C/
        response =~ /\*\*\*Error:   The supplied object has no key/
    }

    def "create x509 keycert no certificate"() {
      given:
        def update = new SyncUpdate(data: """\
            key-cert:        AUTO-1
            method:          X509
            owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8
            fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C
            certif:          -----BEGIN CERTIFICATE-----
            certif:          -----END CERTIFICATE-----
            mnt-by:          UPD-MNT
            remarks:         remark
            changed:         noreply@ripe.net 20121001
            source:          TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create FAILED: \[key-cert\] AUTO-1/
        response =~ /\*\*\*Error:   Invalid X509 Certificate/
    }

    def "create x509 keycert no certificate attributes"() {
      given:
        def update = new SyncUpdate(data: """\
            key-cert:        AUTO-1
            method:          X509
            owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8
            fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C
            mnt-by:          UPD-MNT
            remarks:         remark
            changed:         noreply@ripe.net 20121001
            source:          TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create FAILED: \[key-cert\] AUTO-1/
        response =~ /\*\*\*Error:   Mandatory attribute \"certif\" is missing/
    }

    def "create x509 keycert one empty certificate attributes"() {
      given:
        def update = new SyncUpdate(data: """\
            key-cert:        AUTO-1
            method:          X509
            owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8
            fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C
            certif:
            mnt-by:          UPD-MNT
            remarks:         remark
            changed:         noreply@ripe.net 20121001
            source:          TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create FAILED: \[key-cert\] AUTO-1/
        response =~ /\*\*\*Error:   The supplied object has no key/
    }

    def "create keycert fails on multiple public keys"() {
      given:
        def request = getResource("keycerts/PGPKEY-MULTIPLE-PUBLIC-KEYS.TXT") + "password: update";
        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /\*\*\*Error:   The supplied object has multiple keys/
    }

    def "create keycert succeeds on multiple subkeys"() {
      given:
        def request = getResource("keycerts/PGPKEY-A8D16B70.TXT") + "\npassword: update";
        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-A8D16B70/
    }

    def "create keycert add generated fields"() {
      given:
        def request = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";

        request = request.replaceAll("(?m)fingerpr:.*\n", "")
        request = request.replaceAll("(?m)method:.*\n", "")
        request = request.replaceAll("(?m)owner:.*\n", "")

        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-28F6CD6C/
    }

    def "create keycert replace generated fields"() {
      given:
        def request = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-28F6CD6C/
        response =~ /\*\*\*Warning: Supplied attribute 'method' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'owner' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'fingerpr' has been replaced with a generated
            value/
    }

    def "create keycert replace multiple owner fields"() {
      given:
        def response = syncUpdate new SyncUpdate(data:  """
            key-cert:       PGPKEY-459F13C0
            method:         PGP
            owner:          Test User (testing) <dbtest@ripe.net>
            owner:          Another owner
            fingerpr:       F127 F439 9286 0A5E 06D0  809B 471A AB9F D83C 3FBD
            certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
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
            mnt-by:         UPD-MNT
            changed:        noreply@ripe.net
            source:         TEST
            password: update
            """.stripIndent())
      expect:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-459F13C0/
        response =~ /\*\*\*Warning: Supplied attribute 'method' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'owner' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'fingerpr' has been replaced with a generated
            value/
    }

    def "noop keycert replace generated fields"() {
      given:
        def request = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-28F6CD6C/
        response =~ /\*\*\*Warning: Supplied attribute 'method' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'owner' has been replaced with a generated value/
        response =~ /\*\*\*Warning: Supplied attribute 'fingerpr' has been replaced with a generated
            value/
    }

    def "update keycert replace certif fields"() {
      given:
        def create = syncUpdate new SyncUpdate(data:  """
            key-cert:       PGPKEY-81CCF97D
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
            mnt-by:         UPD-MNT
            notify:         noreply@ripe.net
            changed:        noreply@ripe.net 20120213
            source:         TEST
            password:       update
        """.stripIndent())
      expect:
        create =~ /Create SUCCEEDED: \[key-cert\] PGPKEY-81CCF97D/
      when:
        def update =  syncUpdate new SyncUpdate(data:  """
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
            mnt-by:         UPD-MNT
            notify:         noreply@ripe.net
            changed:        noreply@ripe.net 20120213
            source:         TEST
            password:       update
        """.stripIndent())
      then:
        update =~ /Modify SUCCEEDED: \[key-cert\] PGPKEY-81CCF97D/
    }

    def "update keycert replace modified generated fields"() {
      given:
        def request = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: request.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        request = request.replaceAll("(?m)method:.*\n", "method: INVALIDMETHOD\n")
        request = request.replaceAll("(?m)owner:.*\n", "owner: INVALID\n")
        request = request.replaceAll("(?m)fingerpr:.*\n", "fingerpr: INVALIDFINGERPR\n")

        def updateResponse = syncUpdate(new SyncUpdate(data: request.stripIndent()));

      then:
        updateResponse =~ /No operation: \[key-cert\] PGPKEY-28F6CD6C/
        updateResponse =~ /\*\*\*Warning: Supplied attribute 'owner' has been replaced with a generated value/
        updateResponse =~ /\*\*\*Warning: Supplied attribute 'method' has been replaced with a generated value/
        updateResponse =~ /\*\*\*Warning: Supplied attribute 'fingerpr' has been replaced with a generated
            value/
    }

    def "update PGP keycert certificate changed has subkeys"() {
      given:
        def insertRequest = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def anotherKeycert = getResource("keycerts/PGPKEY-A8D16B70.TXT")
        def updateRequest = insertRequest.replace(getAttributes(insertRequest, "certif"), getAttributes(anotherKeycert, "certif"))
        def updateResponse = syncUpdate(new SyncUpdate(data: updateRequest.stripIndent()));

      then:
        updateResponse.contains("Modify SUCCEEDED: [key-cert] PGPKEY-28F6CD6C")
    }

    def "update PGP keycert certificate changed has multiple public keys"() {
      given:
        def insertRequest = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def anotherKeycert = getResource("keycerts/PGPKEY-MULTIPLE-PUBLIC-KEYS.TXT")
        def updateRequest = insertRequest.replace(getAttributes(insertRequest, "certif"), getAttributes(anotherKeycert, "certif"))
        def updateResponse = syncUpdate(new SyncUpdate(data: updateRequest.stripIndent()));

      then:
        updateResponse.contains("Modify FAILED: [key-cert] PGPKEY-28F6CD6C")
        updateResponse.contains("***Error:   The supplied object has multiple keys")
    }


    def "update X509 keycert certificate changed"() {
      given:
        def insertRequest = getResource("keycerts/AUTO-1-X509.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def anotherKeycert = getResource("keycerts/X509-1.TXT") + "password: update"
        def updateResponse = syncUpdate(new SyncUpdate(data: anotherKeycert.stripIndent()));

      then:
        updateResponse.contains("Modify SUCCEEDED: [key-cert] X509-1")
    }

    def "delete existing X509"() {
      given:
        def insertRequest = getResource("keycerts/AUTO-1-X509.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def updateRequest = insertRequest.replace("AUTO-1", "X509-1") + "\ndelete: some reason";
        def updateResponse = syncUpdate(new SyncUpdate(data: updateRequest.stripIndent()))

      then:
        updateResponse =~ /Delete SUCCEEDED: \[key-cert\] X509-1/
    }

    def "delete existing X509 case insensitive"() {
      given:
        def insertRequest = getResource("keycerts/AUTO-1-X509.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def updateRequest = insertRequest.replace("AUTO-1", "X509-1") + "\nDelete: some reason";
        updateRequest = updateRequest.replace("method:          X509", "method:          x509")
        def updateResponse = syncUpdate(new SyncUpdate(data: updateRequest.stripIndent()))

      then:
        updateResponse =~ /Delete SUCCEEDED: \[key-cert\] X509-1/
    }

    def "delete non-existing X509"() {
      when:
        def deleteRequest = getResource("keycerts/AUTO-1-X509.TXT") + "delete: reason\npassword: update";
        def deleteResponse = syncUpdate(new SyncUpdate(data: deleteRequest.stripIndent()))

      then:
        deleteResponse =~ /Delete FAILED: \[key-cert\] AUTO-1/
        deleteResponse =~ /Object \[key-cert\] AUTO-1 does not exist in the database/
    }

    def "delete existing PGP"() {
      given:
        def insertRequest = getResource("keycerts/PGPKEY-28F6CD6C.TXT") + "password: update";
        def insertResponse = syncUpdate(new SyncUpdate(data: insertRequest.stripIndent()));

      expect:
        insertResponse =~ /SUCCESS/

      when:
        def updateRequest = "" +
                "key-cert:       PGPKEY-28F6CD6C\n" +
                "method:         PGP\n" +
                "owner:          Ed Shryane <eshryane@ripe.net>\n" +
                "fingerpr:       1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.11 (Darwin)\n" +
                "certif:\n" +
                "certif:         mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV\n" +
                "certif:         4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF\n" +
                "certif:         7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz\n" +
                "certif:         RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0\n" +
                "certif:         VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt\n" +
                "certif:         Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz\n" +
                "certif:         aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL\n" +
                "certif:         BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi\n" +
                "certif:         Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX\n" +
                "certif:         /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV\n" +
                "certif:         gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK\n" +
                "certif:         yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC\n" +
                "certif:         H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP\n" +
                "certif:         ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1\n" +
                "certif:         qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl\n" +
                "certif:         sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN\n" +
                "certif:         LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e\n" +
                "certif:         27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah\n" +
                "certif:         P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ\n" +
                "certif:         EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml\n" +
                "certif:         xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q\n" +
                "certif:         mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM\n" +
                "certif:         QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh\n" +
                "certif:         4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj\n" +
                "certif:         kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=\n" +
                "certif:         =XRho\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:         UPD-MNT\n" +
                "notify:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120213\n" +
                "source:         TEST\n" +
                "password:       update\n" +
                "deLete:         some reason";

        def updateResponse = syncUpdate(new SyncUpdate(data: updateRequest.stripIndent()))

      then:
        updateResponse =~ /SUCCES/
    }

    def "create keycert with AUTO-1 and PGP KEY"() {
      given:
        def request = """\
            key-cert:     AUTO-1
            certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
            certif:       Version: GnuPG v1.4.11 (Darwin)
            certif:
            certif:       mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV
            certif:       4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF
            certif:       7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz
            certif:       RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0
            certif:       VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt
            certif:       Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz
            certif:       aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL
            certif:       BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi
            certif:       Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX
            certif:       /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV
            certif:       gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK
            certif:       yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC
            certif:       H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP
            certif:       ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1
            certif:       qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl
            certif:       sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN
            certif:       LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e
            certif:       27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah
            certif:       P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ
            certif:       EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml
            certif:       xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q
            certif:       mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM
            certif:       QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh
            certif:       4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj
            certif:       kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=
            certif:       =XRho
            certif:       -----END PGP PUBLIC KEY BLOCK-----
            mnt-by:       UPD-MNT
            notify:       noreply@ripe.net
            changed:      noreply@ripe.net 20120213
            source:       TEST

            password:     update
            """

        def update = new SyncUpdate(data: request.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Create FAILED: [key-cert] AUTO-1")
        response.contains("***Error:   AUTO-nnn can only be used with X509 key-cert")
    }

    def "create X509 keycert and add AUTO-nnn reference"() {
      given:
        def update = new SyncUpdate(data: """\
            irt:          irt-test
            address:      RIPE NCC
            e-mail:       irt-dbtest@ripe.net
            auth:         AUTO-1
            irt-nfy:      irt_nfy1_dbtest@ripe.net
            notify:       nfy_dbtest@ripe.net
            admin-c:      TEST-RIPE
            tech-c:       TEST-RIPE
            mnt-by:       UPD-MNT
            changed:      dbtest@ripe.net 20020101
            source:       TEST

            key-cert:        AUTO-1
            certif:          -----BEGIN CERTIFICATE-----
            certif:          MIIDjjCCAnagAwIBAgIIL6SpWNLhypwwDQYJKoZIhvcNAQEFBQAwXjELMAkGA1UE
            certif:          BhMCTkwxETAPBgNVBAoMCFJJUEUgTkNDMR0wGwYDVQQLDBRSSVBFIE5DQyBMSVIg
            certif:          TmV0d29yazEdMBsGA1UEAwwUUklQRSBOQ0MgTElSIFJvb3QgQ0EwHhcNMTIxMDI1
            certif:          MDgwNjQ5WhcNMTQxMDI1MDgwNjQ5WjAvMS0wKwYDVQQDDCQ0YTk2ZWVjZi05ZDFj
            certif:          LTRlMTItOGFkZC01ZWE1NTIyOTc2ZDgwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw
            certif:          ggEKAoIBAQDJrhi5iDxCxtqFCMWSKkJ36gUp3SGuL26Kb4GMX0QI6/9M4GMqWOPL
            certif:          lztJBzBQX04nOMkZ4ayXxB9C6F6P2TN4KtMvH+K5Lcnf2pfRInE9TEjptkjHI1Pi
            certif:          BIgDqhWlSd31LqvER37OhKmHtmep6Wt2u1lYbRNFE/DWrZ0ATH56yRimRxkTPniW
            certif:          R5DHnYFmbyFUmt/Pj/u3o+mTSenFqsWyGgRZ4C/0NXUqnI2kVOz+UCDfJA3Zg+Ur
            certif:          WNavLlJ/5qYvAKJVB5KXmdNOgBd1gfFeQkTKg5SEiR+WRKu6zwYUcSsYxcW763V8
            certif:          ey65UUA83yGMD1SjRMHFy73aFmnmTqw9AgMBAAGjfzB9MB0GA1UdDgQWBBSwoueg
            certif:          pJSRZINXQOtHytX37Nmg9zAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFEkT+tYV
            certif:          4RUo849V6Tk2vPtam1lAMA4GA1UdDwEB/wQEAwIF4DAdBgNVHSUEFjAUBggrBgEF
            certif:          BQcDAgYIKwYBBQUHAwQwDQYJKoZIhvcNAQEFBQADggEBACYtmXxBLvosiYOD8m2G
            certif:          KOHay4F/2agzZES4ix9ndIfKj26MoaUGamP1rSQUni3bNUYKNpRx0SRw+hg/tpI0
            certif:          m/r7KVLN7GRNKpUsmAXAzgAwc3dbp1jXQ4RscLyQzPsrjdv/+6R6dE8r/0ssqF0r
            certif:          d1808kervulYvXTRFvsUpy4dXY8BNc8D9RM/nYV+dBoKVb/euCTTTrMSnP2KPyl6
            certif:          W4OWwGVKuwaEQfPwoi6ROD7vbP7YBtANwlIZUdlrIBg5ssv/UjsS6U9m7yGo4hvI
            certif:          yzYYLT/sLpHsaVIxU0xWEJVYabWVVWnnM30PIsq58IiqZj4lTEAHoBCDyOE/izvM
            certif:          t8Q=
            certif:          -----END CERTIFICATE-----
            mnt-by:          UPD-MNT
            remarks:         Gabriel Barazer
            changed:         gabriel@oxeva.fr 20121025
            source:          TEST

            password:     update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Create SUCCEEDED: [key-cert] X509-1")
        response.contains("Create SUCCEEDED: [irt] irt-test")
    }

    private String getAttributes(final String originalObject, final String attributeName) {
        StringBuilder builder = new StringBuilder();
        Matcher m = Pattern.compile("(?im)^" + attributeName + ":.*\\s?").matcher(originalObject);
        while (m.find()) {
            builder.append(m.group(0));
        }
        return builder.toString();
    }


    private String getResource(final String resourceName) {
        return Resources.toString(Resources.getResource(resourceName), Charset.defaultCharset());
    }
}
