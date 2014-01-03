package net.ripe.db.whois.spec.update
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

class KeycertSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST
                """,
            "X509-1": """\
                key-cert:     AUTO-1
                method:       X509
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=uk.bt.test-receiver/EMAILADDRESS=test-receiver@linux.testlab.ripe.net
                fingerpr:     D5:92:29:08:F8:AB:75:5F:42:F5:A8:5F:A3:8D:08:2E
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST
                """,
            "X509-2": """\
                key-cert:     AUTO-1
                method:       X509
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=uk.bt.test-receiver/emailAddress=test-receiver@linux.testlab.ripe.net
                fingerpr:     D5:92:29:08:F8:AB:75:5F:42:F5:A8:5F:A3:8D:08:2E
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST
                """
    ]}

    def "create X509 key-cert object, no gen attrs, no X509 exists, X509-1 created"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object with auto-20, X509-1 already exists, X509-2 created"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T key-cert X509-2", "key-cert", "X509-2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-20
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-2" }

        queryObject("-rGBT key-cert X509-2", "key-cert", "X509-2")
    }

    def "create X509 key-cert objects with auto-20 & auto-1, X509-1 & X509-2 exist, X509-3 & X509-4 created"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")
        syncUpdate(getTransient("X509-2") + "password: lir")

      expect:
        queryObject("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObject("-r -T key-cert X509-2", "key-cert", "X509-2")
        queryObjectNotFound("-r -T key-cert X509-3", "key-cert", "X509-3")
        queryObjectNotFound("-r -T key-cert X509-4", "key-cert", "X509-4")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-20
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-3" }
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-4" }

        queryObject("-rGBT key-cert X509-3", "key-cert", "X509-3")
        queryObject("-rGBT key-cert X509-4", "key-cert", "X509-4")
    }

    def "create X509 key-cert objects with auto-1 & auto-1, none exist"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T key-cert X509-2", "key-cert", "X509-2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") == [
                "Key AUTO-1 already used (AUTO-nnn must be unique per update message)"]

        queryObject("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T key-cert X509-2", "key-cert", "X509-2")
    }

    def "create X509 key-cert objects AUTO-1 & auto-2, modify MNTNER, add refs to AUTO-1 & auto-2"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T key-cert X509-2", "key-cert", "X509-2")
        query_object_not_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:        X509-1")
        query_object_not_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:        X509-2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID8zCCA1ygAwIBAgICAIIwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODEwMjYxMloXDTA0MDkwNzEwMjYxMlowfTELMAkGA1UEBhMC
                certif:       TkwxETAPBgNVBAoTCFJJUEUgTkNDMRAwDgYDVQQLEwdNZW1iZXJzMRgwFgYDVQQD
                certif:       Ew91ay5idC50ZXN0LXVzZXIxLzAtBgkqhkiG9w0BCQEWIHRlc3QtdXNlckBsaW51
                certif:       eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                certif:       AQEArv3srxyl1QA3uS4dxdZbSsGrfBrMRjMb81Gnx0nqa6i+RziIf13lszB/EYy0
                certif:       PgLpQFdGLdhUQ52YsiGOUmMtnaWNHnEJrBUc8/fdnA6GVdfF8AEw1PTfJ6t2Cdc9
                certif:       2SwaF+5kCaUDwmlOgbM333IQmU03l3I1ILs32RpQyZ+df/ovHNrVzeLc2P59isac
                certif:       bfjM2S0SXPQzHjuVLH40eOgVuXA/5LAYs51eXqwtKszSxFhqekf+BAEcRDrXmIT4
                certif:       e3zfiZOsXKe0UfaEABgHUMrYjsUCJ8NTMg6XiVSNwQQmXCdUbRvK7zOCe2iCX15y
                certif:       9hNXxhY/q/IW54W5it7jGXq/7wIDAQABo4IBCDCCAQQwCQYDVR0TBAIwADARBglg
                certif:       hkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIBDQQNFgtSSVBF
                certif:       IE5DQyBDQTAdBgNVHQ4EFgQUzdajNaRorkDTAW5O6Hpa3z9pP3AwgZsGA1UdIwSB
                certif:       kzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNVBAYTAkVVMRAw
                certif:       DgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYDVQQDExRTb2Z0
                certif:       d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGllc0ByaXBlLm5l
                certif:       dIIBADANBgkqhkiG9w0BAQQFAAOBgQByg8L8RaiIz5k7n5jVwM/0oHSf48KRMBdn
                certif:       YdN2+eoEjVQbz48NtjbBTsOiUYj5AQWRHJrKtDQ+odbog0x7UsvhXjjBo/abJ6vI
                certif:       AupjnxP3KpSe73zmBUiMU8mvXLibPP1xuI2FPM70Y7fgeUehbmT7wdgqs7TEtYww
                certif:       PeUqjPPTZg==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                password: lir2

                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-1
                auth:        AuTo-2
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                password:    owner

                key-cert:     auto-2
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 2, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-2" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }

        queryObject("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObject("-r -T key-cert X509-2", "key-cert", "X509-2")
        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:           X509-1")
        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:           X509-2")
    }

    def "create X509 key-cert objects AUTO-1 & auto-2, modify 2 MNTNERs, add ref to AUTO-1 & auto-2"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T key-cert X509-2", "key-cert", "X509-2")
        query_object_not_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:        X509-1")
        query_object_not_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:        X509-2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        aUtO-1
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                password:    owner

                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID8zCCA1ygAwIBAgICAIIwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODEwMjYxMloXDTA0MDkwNzEwMjYxMlowfTELMAkGA1UEBhMC
                certif:       TkwxETAPBgNVBAoTCFJJUEUgTkNDMRAwDgYDVQQLEwdNZW1iZXJzMRgwFgYDVQQD
                certif:       Ew91ay5idC50ZXN0LXVzZXIxLzAtBgkqhkiG9w0BCQEWIHRlc3QtdXNlckBsaW51
                certif:       eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                certif:       AQEArv3srxyl1QA3uS4dxdZbSsGrfBrMRjMb81Gnx0nqa6i+RziIf13lszB/EYy0
                certif:       PgLpQFdGLdhUQ52YsiGOUmMtnaWNHnEJrBUc8/fdnA6GVdfF8AEw1PTfJ6t2Cdc9
                certif:       2SwaF+5kCaUDwmlOgbM333IQmU03l3I1ILs32RpQyZ+df/ovHNrVzeLc2P59isac
                certif:       bfjM2S0SXPQzHjuVLH40eOgVuXA/5LAYs51eXqwtKszSxFhqekf+BAEcRDrXmIT4
                certif:       e3zfiZOsXKe0UfaEABgHUMrYjsUCJ8NTMg6XiVSNwQQmXCdUbRvK7zOCe2iCX15y
                certif:       9hNXxhY/q/IW54W5it7jGXq/7wIDAQABo4IBCDCCAQQwCQYDVR0TBAIwADARBglg
                certif:       hkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIBDQQNFgtSSVBF
                certif:       IE5DQyBDQTAdBgNVHQ4EFgQUzdajNaRorkDTAW5O6Hpa3z9pP3AwgZsGA1UdIwSB
                certif:       kzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNVBAYTAkVVMRAw
                certif:       DgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYDVQQDExRTb2Z0
                certif:       d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGllc0ByaXBlLm5l
                certif:       dIIBADANBgkqhkiG9w0BAQQFAAOBgQByg8L8RaiIz5k7n5jVwM/0oHSf48KRMBdn
                certif:       YdN2+eoEjVQbz48NtjbBTsOiUYj5AQWRHJrKtDQ+odbog0x7UsvhXjjBo/abJ6vI
                certif:       AupjnxP3KpSe73zmBUiMU8mvXLibPP1xuI2FPM70Y7fgeUehbmT7wdgqs7TEtYww
                certif:       PeUqjPPTZg==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                password: lir2
                mntner:      TST-MNT2
                descr:       MNTNER for test
                admin-c:     TP2-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$bnGNJ2PC\$4r38DENnw07.9ktKP//Kf1  #test2
                auth:        aUtO-2
                mnt-by:      TST-MNT2
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                key-cert:     auto-2
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password:    test2
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 2, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-2" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT2" }

        queryObject("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObject("-r -T key-cert X509-2", "key-cert", "X509-2")
        query_object_matches("-rBGT mntner TST-MNT", "mntner", "TST-MNT", "auth:           X509-1")
        query_object_matches("-rBGT mntner TST-MNT2", "mntner", "TST-MNT2", "auth:           X509-2")
    }

    def "create X509 key-cert object, all gen attrs included, X509-1 created"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                method:       X509
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=uk.bt.test-receiver/EMAILADDRESS=test-receiver@linux.testlab.ripe.net
                fingerpr:     D5:92:29:08:F8:AB:75:5F:42:F5:A8:5F:A3:8D:08:2E
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object, invalid gen attrs included, X509-1 created"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                method:       AAAA
                owner:        CN=uk.bt.test-receiver/emailAddress=test-receiver@linux.testlab.ripe.net
                fingerpr:     29:08:F8:AB:75:5F:42:F5:A8:5F:A3:8D:08:2E
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.warningSuccessMessagesFor("Create", "[key-cert] X509-1") == [
                "Supplied attribute 'method' has been replaced with a generated value",
                "Supplied attribute 'owner' has been replaced with a generated value",
                "Supplied attribute 'fingerpr' has been replaced with a generated value"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert obj auto-1, create person obj auto-2, modify mntner ref auto-1 & auto-2"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                tech-c:      AuTo-2
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-1
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-2
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 2, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        queryObject("-rGBT person FP1-TEST", "person", "First Person")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "tech-c:\\s*FP1")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "auth:\\s*X509-1")
    }

    def "create X509 key-cert obj auto-1, create person obj auto-2, modify mntner ref auto-2 & auto-1 reversed"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-rGBT person FP1-TEST", "person", "First Person")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                tech-c:      AuTo-1
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-2
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-2
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errors.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }
        ack.errorMessagesFor("Modify", "[mntner] TST-MNT") ==
                ["Invalid reference to [key-cert] X509-1",
                        "Invalid reference to [person] FP1-TEST"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        queryObject("-rGBT person FP1-TEST", "person", "First Person")
        query_object_not_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "tech-c:\\s*FP1")
        query_object_not_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "auth:\\s*X509-1")
    }

    def "create X509 key-cert obj auto-0"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-0
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-0" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-0") ==
                ["Syntax error in AUTO-0"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert obj auto-1, create person obj auto-1"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-1
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.errors.any { it.operation == "Create" && it.key == "[person] aUtO-1   First Person" }
        ack.errorMessagesFor("Create", "[person] aUtO-1") ==
                ["Key aUtO-1 already used (AUTO-nnn must be unique per update message)"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "create X509 key-cert obj auto-1, modify mntner ref auto-1 in tech-c"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                tech-c:      AuTo-1
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.errors.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }
        ack.errorMessagesFor("Modify", "[mntner] TST-MNT") ==
                ["Invalid reference to [key-cert] X509-1"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        query_object_not_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "tech-c:\\s*AuTo-1")
    }

    def "create X509 key-cert obj X509-99"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-99", "key-cert", "X509-99")

      when:
        def response = syncUpdate("""\
                key-cert:     X509-99
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] X509-99" }
        ack.errorMessagesFor("Create", "[key-cert] X509-99") ==
                ["Syntax error in X509-99 (must be AUTO-nnn for create)"]

        queryObjectNotFound("-rGBT key-cert X509-99", "key-cert", "X509-99")
    }

    def "create X509 key-cert objs with AUTO- errors"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1DW
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                key-cert:     AUTO2
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                key-cert:     AUTO--3
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                key-cert:     AUTO_4
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 4, 0, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1DW" }
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO2" }
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO--3" }
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO_4" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1DW") ==
                ["Syntax error in AUTO-1DW"]
        ack.errorMessagesFor("Create", "[key-cert] AUTO2") ==
                ["Syntax error in AUTO2"]
        ack.errorMessagesFor("Create", "[key-cert] AUTO--3") ==
                ["Syntax error in AUTO--3"]
        ack.errorMessagesFor("Create", "[key-cert] AUTO_4") ==
                ["Syntax error in AUTO_4"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create person auto-1, create organisation auto-2 & ref auto-1, create X509 key-cert auto-3 & ref auto-1 auto-2, modify mntner ref auto-1 auto-2 auto-3"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-rGBT person FP1-TEST", "person", "First Person")

      when:
        def response = syncUpdate("""\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-1
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                organisation:    auto-2
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                admin-c:         AUTO-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2

                key-cert:     AUTo-3
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                password:     owner3
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                admin-c:      auto-1
                org:          AUTO-2
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                tech-c:      AuTo-1
                org:         auto-2
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-3
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 3, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO1-TEST" }
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }

        queryObject("-rGBT person FP1-TEST", "person", "First Person")
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        query_object_matches("-rGBT organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST", "admin-c:\\s*FP1-TEST")
        query_object_matches("-rGBT key-cert X509-1", "key-cert", "X509-1", "admin-c:\\s*FP1-TEST")
        query_object_matches("-rGBT key-cert X509-1", "key-cert", "X509-1", "org:\\s*ORG-FO1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "tech-c:\\s*FP1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "org:\\s*ORG-FO1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "auth:\\s*X509-1")
    }

    def "modify mntner ref auto-1 auto-2 auto-3, create X509 key-cert auto-3 & ref auto-1 auto-2, create organisation auto-2 & ref auto-1, create person auto-1"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-rGBT person FP1-TEST", "person", "First Person")

      when:
        def response = syncUpdate("""\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                tech-c:      AuTo-1
                org:         auto-2
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-3
                mnt-by:      OWNER-MNT
                referral-by: TST-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                key-cert:     AUTo-3
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                password:     owner3
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                admin-c:      auto-1
                org:          AUTO-2
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                organisation:    auto-2
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                admin-c:         AUTO-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-1
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 3, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO1-TEST" }
        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] X509-1" }
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }

        queryObject("-rGBT person FP1-TEST", "person", "First Person")
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
        query_object_matches("-rGBT organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST", "admin-c:\\s*FP1-TEST")
        query_object_matches("-rGBT key-cert X509-1", "key-cert", "X509-1", "admin-c:\\s*FP1-TEST")
        query_object_matches("-rGBT key-cert X509-1", "key-cert", "X509-1", "org:\\s*ORG-FO1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "tech-c:\\s*FP1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "org:\\s*ORG-FO1-TEST")
        query_object_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "auth:\\s*X509-1")
    }

    def "modify X509 key-cert object, no gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, correct gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                method:       Placeholder
                owner:        Placeholder
                fingerpr:     Placeholder
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }
        ack.warningSuccessMessagesFor("Modify", "[key-cert] X509-1") == [
                "Supplied attribute 'method' has been replaced with a generated value",
                "Supplied attribute 'owner' has been replaced with a generated value",
                "Supplied attribute 'fingerpr' has been replaced with a generated value"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, wrong gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                method:       PGP
                owner:        /CN=uk.bt.test-receiver/emailAddress=test-receiver@linux.testlab.ripe.net
                fingerpr:     D5:92:29:08:F8:AB:75:5F:42:F5:A8:5F:
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }
        ack.warningSuccessMessagesFor("Modify", "[key-cert] X509-1") == [
                "Supplied attribute 'method' has been replaced with a generated value",
                "Supplied attribute 'owner' has been replaced with a generated value",
                "Supplied attribute 'fingerpr' has been replaced with a generated value"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, change certif data, no gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID8zCCA1ygAwIBAgICAIIwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODEwMjYxMloXDTA0MDkwNzEwMjYxMlowfTELMAkGA1UEBhMC
                certif:       TkwxETAPBgNVBAoTCFJJUEUgTkNDMRAwDgYDVQQLEwdNZW1iZXJzMRgwFgYDVQQD
                certif:       Ew91ay5idC50ZXN0LXVzZXIxLzAtBgkqhkiG9w0BCQEWIHRlc3QtdXNlckBsaW51
                certif:       eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                certif:       AQEArv3srxyl1QA3uS4dxdZbSsGrfBrMRjMb81Gnx0nqa6i+RziIf13lszB/EYy0
                certif:       PgLpQFdGLdhUQ52YsiGOUmMtnaWNHnEJrBUc8/fdnA6GVdfF8AEw1PTfJ6t2Cdc9
                certif:       2SwaF+5kCaUDwmlOgbM333IQmU03l3I1ILs32RpQyZ+df/ovHNrVzeLc2P59isac
                certif:       bfjM2S0SXPQzHjuVLH40eOgVuXA/5LAYs51eXqwtKszSxFhqekf+BAEcRDrXmIT4
                certif:       e3zfiZOsXKe0UfaEABgHUMrYjsUCJ8NTMg6XiVSNwQQmXCdUbRvK7zOCe2iCX15y
                certif:       9hNXxhY/q/IW54W5it7jGXq/7wIDAQABo4IBCDCCAQQwCQYDVR0TBAIwADARBglg
                certif:       hkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIBDQQNFgtSSVBF
                certif:       IE5DQyBDQTAdBgNVHQ4EFgQUzdajNaRorkDTAW5O6Hpa3z9pP3AwgZsGA1UdIwSB
                certif:       kzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNVBAYTAkVVMRAw
                certif:       DgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYDVQQDExRTb2Z0
                certif:       d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGllc0ByaXBlLm5l
                certif:       dIIBADANBgkqhkiG9w0BAQQFAAOBgQByg8L8RaiIz5k7n5jVwM/0oHSf48KRMBdn
                certif:       YdN2+eoEjVQbz48NtjbBTsOiUYj5AQWRHJrKtDQ+odbog0x7UsvhXjjBo/abJ6vI
                certif:       AupjnxP3KpSe73zmBUiMU8mvXLibPP1xuI2FPM70Y7fgeUehbmT7wdgqs7TEtYww
                certif:       PeUqjPPTZg==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, change certif data, correct gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                method:       X509
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=uk.bt.test-user/EMAILADDRESS=test-user@linux.testlab.ripe.net
                fingerpr:     AC:B5:B1:36:95:F3:46:93:B1:2D:58:EB:E1:46:DA:3F
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID8zCCA1ygAwIBAgICAIIwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODEwMjYxMloXDTA0MDkwNzEwMjYxMlowfTELMAkGA1UEBhMC
                certif:       TkwxETAPBgNVBAoTCFJJUEUgTkNDMRAwDgYDVQQLEwdNZW1iZXJzMRgwFgYDVQQD
                certif:       Ew91ay5idC50ZXN0LXVzZXIxLzAtBgkqhkiG9w0BCQEWIHRlc3QtdXNlckBsaW51
                certif:       eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                certif:       AQEArv3srxyl1QA3uS4dxdZbSsGrfBrMRjMb81Gnx0nqa6i+RziIf13lszB/EYy0
                certif:       PgLpQFdGLdhUQ52YsiGOUmMtnaWNHnEJrBUc8/fdnA6GVdfF8AEw1PTfJ6t2Cdc9
                certif:       2SwaF+5kCaUDwmlOgbM333IQmU03l3I1ILs32RpQyZ+df/ovHNrVzeLc2P59isac
                certif:       bfjM2S0SXPQzHjuVLH40eOgVuXA/5LAYs51eXqwtKszSxFhqekf+BAEcRDrXmIT4
                certif:       e3zfiZOsXKe0UfaEABgHUMrYjsUCJ8NTMg6XiVSNwQQmXCdUbRvK7zOCe2iCX15y
                certif:       9hNXxhY/q/IW54W5it7jGXq/7wIDAQABo4IBCDCCAQQwCQYDVR0TBAIwADARBglg
                certif:       hkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIBDQQNFgtSSVBF
                certif:       IE5DQyBDQTAdBgNVHQ4EFgQUzdajNaRorkDTAW5O6Hpa3z9pP3AwgZsGA1UdIwSB
                certif:       kzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNVBAYTAkVVMRAw
                certif:       DgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYDVQQDExRTb2Z0
                certif:       d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGllc0ByaXBlLm5l
                certif:       dIIBADANBgkqhkiG9w0BAQQFAAOBgQByg8L8RaiIz5k7n5jVwM/0oHSf48KRMBdn
                certif:       YdN2+eoEjVQbz48NtjbBTsOiUYj5AQWRHJrKtDQ+odbog0x7UsvhXjjBo/abJ6vI
                certif:       AupjnxP3KpSe73zmBUiMU8mvXLibPP1xuI2FPM70Y7fgeUehbmT7wdgqs7TEtYww
                certif:       PeUqjPPTZg==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, change certif data, invalid gen attrs supplied"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                method:       PGP
                owner:        /C=NL/O=RIPE NCC/OU=Members/CN=uk.bt.test-user/emailA
                fingerpr:     AC:B5:B1:36:95:F3:46:93:B1DA:3F
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID8zCCA1ygAwIBAgICAIIwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODEwMjYxMloXDTA0MDkwNzEwMjYxMlowfTELMAkGA1UEBhMC
                certif:       TkwxETAPBgNVBAoTCFJJUEUgTkNDMRAwDgYDVQQLEwdNZW1iZXJzMRgwFgYDVQQD
                certif:       Ew91ay5idC50ZXN0LXVzZXIxLzAtBgkqhkiG9w0BCQEWIHRlc3QtdXNlckBsaW51
                certif:       eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                certif:       AQEArv3srxyl1QA3uS4dxdZbSsGrfBrMRjMb81Gnx0nqa6i+RziIf13lszB/EYy0
                certif:       PgLpQFdGLdhUQ52YsiGOUmMtnaWNHnEJrBUc8/fdnA6GVdfF8AEw1PTfJ6t2Cdc9
                certif:       2SwaF+5kCaUDwmlOgbM333IQmU03l3I1ILs32RpQyZ+df/ovHNrVzeLc2P59isac
                certif:       bfjM2S0SXPQzHjuVLH40eOgVuXA/5LAYs51eXqwtKszSxFhqekf+BAEcRDrXmIT4
                certif:       e3zfiZOsXKe0UfaEABgHUMrYjsUCJ8NTMg6XiVSNwQQmXCdUbRvK7zOCe2iCX15y
                certif:       9hNXxhY/q/IW54W5it7jGXq/7wIDAQABo4IBCDCCAQQwCQYDVR0TBAIwADARBglg
                certif:       hkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIBDQQNFgtSSVBF
                certif:       IE5DQyBDQTAdBgNVHQ4EFgQUzdajNaRorkDTAW5O6Hpa3z9pP3AwgZsGA1UdIwSB
                certif:       kzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNVBAYTAkVVMRAw
                certif:       DgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYDVQQDExRTb2Z0
                certif:       d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGllc0ByaXBlLm5l
                certif:       dIIBADANBgkqhkiG9w0BAQQFAAOBgQByg8L8RaiIz5k7n5jVwM/0oHSf48KRMBdn
                certif:       YdN2+eoEjVQbz48NtjbBTsOiUYj5AQWRHJrKtDQ+odbog0x7UsvhXjjBo/abJ6vI
                certif:       AupjnxP3KpSe73zmBUiMU8mvXLibPP1xuI2FPM70Y7fgeUehbmT7wdgqs7TEtYww
                certif:       PeUqjPPTZg==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }
        ack.warningSuccessMessagesFor("Modify", "[key-cert] X509-1") == [
                "Supplied attribute 'method' has been replaced with a generated value",
                "Supplied attribute 'owner' has been replaced with a generated value",
                "Supplied attribute 'fingerpr' has been replaced with a generated value"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "modify X509 key-cert object, corrupt cert data"() {
      given:
        syncUpdate(getTransient("X509-1") + "password: lir")

      expect:
        queryObject("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     X509-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                remarks:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[key-cert] X509-1" }
        ack.errorMessagesFor("Modify", "[key-cert] X509-1") == [
                "Error parsing X509 certificate from key-cert object"]

        queryObject("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object, corrupt cert data"() {
      expect:
        queryObjectNotFound("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") == [
                "Error parsing X509 certificate from key-cert object"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object, missing -----BEGIN CERTIFICATE----- line"() {
      expect:
        queryObjectNotFound("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") == [
                "The supplied object has no key"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object, corrupt -----BEGIN CERTIFICATE----- line"() {
      expect:
        queryObjectNotFound("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") == [
                "The supplied object has no key"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert object, missing -----END CERTIFICATE----- line"() {
      expect:
        queryObjectNotFound("-rBG -T key-cert x509-1", "key-cert", "X509-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") == [
                "The supplied object has no key"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
    }

    def "create X509 key-cert obj auto-1 ref auto-2 auto-3, create person obj auto-2"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")
        queryObjectNotFound("-r -T person FP1-TEST", "person", "FP1-TEST")

      when:
        def response = syncUpdate("""\
                key-cert:     AUTO-1
                certif:       -----BEGIN CERTIFICATE-----
                certif:       MIID/DCCA2WgAwIBAgICAIQwDQYJKoZIhvcNAQEEBQAwcTELMAkGA1UEBhMCRVUx
                certif:       EDAOBgNVBAgTB0hvbGxhbmQxEDAOBgNVBAoTB25jY0RFTU8xHTAbBgNVBAMTFFNv
                certif:       ZnR3YXJlIFBLSSBUZXN0aW5nMR8wHQYJKoZIhvcNAQkBFhBzb2Z0aWVzQHJpcGUu
                certif:       bmV0MB4XDTAzMDkwODE1NTMyOFoXDTA0MDkwNzE1NTMyOFowgYUxCzAJBgNVBAYT
                certif:       Ak5MMREwDwYDVQQKEwhSSVBFIE5DQzEQMA4GA1UECxMHTWVtYmVyczEcMBoGA1UE
                certif:       AxMTdWsuYnQudGVzdC1yZWNlaXZlcjEzMDEGCSqGSIb3DQEJARYkdGVzdC1yZWNl
                certif:       aXZlckBsaW51eC50ZXN0bGFiLnJpcGUubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOC
                certif:       AQ8AMIIBCgKCAQEAwYAvr71Mkw68CoMKmrHs8rHbMlLotPVqx5RuJ4d+IomL0i2i
                certif:       F7NVBkg1VLuAER1wl1X2pK746ptevTzwWi/QmgFZajTqLjCfW1sou2TXEA5s80t3
                certif:       JXRNk9xF6VXnggxCiqeWyfdC9Q7yOnlNdkJgzmQ/OuE9EVkKaY2kcnMU4NVyvbmD
                certif:       DtgdgSEuvRlgyeDi2gTh79QAfTnzH2d2SFGt1lZT48PuwCXl485pxyu+gVmykEMr
                certif:       EAgG6H/Dpl7t/jyV9w/HRAFaSV8mzpaLg6rxM03ThOPl6R61RJzEqTi0zX4OHkxV
                certif:       q7m1aniNJIvWefU1Yfvdv3zzTcmxWmA3yhOt6wIDAQABo4IBCDCCAQQwCQYDVR0T
                certif:       BAIwADARBglghkgBhvhCAQEEBAMCBaAwCwYDVR0PBAQDAgXgMBoGCWCGSAGG+EIB
                certif:       DQQNFgtSSVBFIE5DQyBDQTAdBgNVHQ4EFgQU/EdNYQO8tjU3p1uJLsYn4f0bmmAw
                certif:       gZsGA1UdIwSBkzCBkIAUHpLUfvaBVfxXVCcT0kh9NJeH7ouhdaRzMHExCzAJBgNV
                certif:       BAYTAkVVMRAwDgYDVQQIEwdIb2xsYW5kMRAwDgYDVQQKEwduY2NERU1PMR0wGwYD
                certif:       VQQDExRTb2Z0d2FyZSBQS0kgVGVzdGluZzEfMB0GCSqGSIb3DQEJARYQc29mdGll
                certif:       c0ByaXBlLm5ldIIBADANBgkqhkiG9w0BAQQFAAOBgQCEve6deqF0nvHKFJ0QfEJS
                certif:       UkRTCF7YCx7Jb2tKIHfMgbrUs3x9bmpShpBkJwjEsNYp0Vvk7hfhiFgKM4AGyYd3
                certif:       hZNmF5c/d0gauqvL+egb+3V+Zg+sJTzHMVKQLF1ybWgJjU75Pi+mO7BG0zsQ13pT
                certif:       YxuZCR2W15nwt7zLiHtmfw==
                certif:       -----END CERTIFICATE-----
                remarks:      Sample Key Certificate
                notify:       dbtest@ripe.net
                admin-c:      auto-2
                tech-c:       auto-3
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20040101
                source:       TEST

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-2
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: lir
                password: owner
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] AUTO-1" }
        ack.errorMessagesFor("Create", "[key-cert] AUTO-1") ==
                ["Reference \"auto-3\" not found"]

        queryObjectNotFound("-rGBT key-cert X509-1", "key-cert", "X509-1")
        queryObject("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "modify key-cert with single key, wrong generated values"() {
      expect:
        queryObject("-rBT key-cert PGPKEY-459F13C0", "key-cert", "PGPKEY-459F13C0")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     PGPKEY-459F13C0
                method:       PGP
                owner:        Test User (testing) <dbtest@ripe.net>
                fingerpr:     F127 F439 9286 0A5E 06D0  809B 471A AB9F D83C 3FBD
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

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)

        ack.successes.any { it.operation == "No operation" && it.key == "[key-cert] PGPKEY-459F13C0" }
        ack.warningSuccessMessagesFor("No operation", "[key-cert] PGPKEY-459F13C0") == [
                "Supplied attribute 'owner' has been replaced with a generated value",
                "Supplied attribute 'fingerpr' has been replaced with a generated value",
                "Submitted object identical to database object"]

        query_object_matches("-rBT key-cert PGPKEY-459F13C0", "key-cert", "PGPKEY-459F13C0", "DB Test \\(RSA key for DB testing\\) <dbtest@ripe.net>")
    }

    def "modify key-cert with multiple master keys, long certif data, wrong generated values"() {
      expect:
        queryObject("-rBT key-cert PGPKEY-459F13C0", "key-cert", "PGPKEY-459F13C0")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     PGPKEY-459F13C0
                method:       PGP
                owner:        Test User (testing) <dbtest@ripe.net>
                fingerpr:     F127 F439 9286 0A5E 06D0  809B 471A AB9F D83C 3FBD
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                              Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                              Comment: GPGTools - http://gpgtools.org
                +
                              mQMuBExtIfMRCACe8xHPWBciPfCkdN+4TNUPW04ahDdlSJk5fmzaFcx8GnJILvzt
                              +0Vbs4HPLUj0yJQz0ZXz+8EPqzs/sqBYZjh5doyGFqXG/Q3oD4Yxru9+msvTQSd4
                              yWQUl+2C/wYcomhHp3pRRbbLPn4/UYxOQtOeoOP5inr9DIPzQ4Ejz744DSSR5SAN
                              AuYhFqN6XxOqQ04RxdlvxMQG3KIVsFGZ5IUBaY3ZplO/ul65nYgyE9jujaBBF1O6
                              OvEIUIHE0CwHReCsNSDZgS89lWfMDKFBcmfCP7hGfHE2sOWcTGVC38aaPjFQe76c
                              WIXp191xySc71qVUeuKMWBLWLwSX+jZCFxWDAQDzDYJSwjdDvtGvF1X2ddcsylki
                              /pK7uz5E4AuVzxoaSQf/WfSpc6VfWPIXkbPvstGq8sxx4cClylur11bpQ8ZsqzSL
                              EBOK2v+f2wnRGJAXy+Jq3Ur+mQhfZJiq+sM4gCCcVl9/uKnQrGWYKnZ8E9v3q0ot
                              O9i0/23HX9oFK8A11q6eJpXF89Y1cUwHVmAGaDoqMEc00vHYSZc+TMfzDR1PW9AD
                              O8wzkL1FYzSX8/5iV73PPpy1K4QCecKt6ejXg85WpEbl4HCBXotcQLL9J0+NFbaX
                              bypy0fvVTYQyHSbY5m5UhHXd7VHFo86UvUP6mym+UjfULMvScKf9DpjWqNEcjdoG
                              D1tMCguMV9bgM/vG6K35U5DOeA8JdTIy7jPNHDsVywf6A+pawnU/rDhUvlUIea6U
                              mbW08vZ9DyUAtCtb2/BnZ20FDBCqp3OdIfjg9XLnQrI8GdQlfVDDoEQxy+9QhjM3
                              JFB0apZIjvrMfdALq5+ywH9I/4xn7GPiIg2LDkFMtFCVA1ON6HNJz1flrjELt1cf
                              t3z8aevkAdBctsppzSZjdGigJcBoQunhE2E/JHG7hcun6LAIYyZXec5KbJjHUJq+
                              2YcM6u2T2gsE3FR/fIIh4JJ/Q2zI41R3m4Ao64XU0DtIYwHUo+6JcKi4d7aIiObA
                              8mmdLQesyamdA/E0taUu1htRY64yeWicgrBtKbY40/GKiW+4h5HIUjyl6rJW9mvC
                              LrROR1BHVG9vbHMgUHJvamVjdCBUZWFtIChPZmZpY2lhbCBPcGVuUEdQIEtleSkg
                              PGdwZ3Rvb2xzLW9yZ0BsaXN0cy5ncGd0b29scy5vcmc+iIMEExEIACsCGwMFCQlm
                              AYAGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheABQJNNNmvAhkBAAoJEHbXjwUA0CbE
                              ROkA/1HxARfN23sZi9s+7Si1YlSsVCiokXUbFKSAwGb4W+osAP4n62nfLY7i1n1f
                              lY+FspLmP7BRIoTGRNNpmqED0BQHC4kCIgQTAQIADAUCTToVKQWDCJkOSgAKCRDa
                              hwwTRqlXsIGLEACApX2L9d/A4HLewtw0xradGEr5THysBrE3wMxjeXBBF2/0jVsW
                              qrTjpLZE/Jc6xH8qK+X1j6zP6nQrHenWJnSMM3cGsqqW2hjB5+iMNqAgm0I0sOAG
                              NX8N9EOpy/r8/IV5w/3maoPOfEdjIcQrrsFu9EPk3i2dEMWDY8p+zAhuNcit5v6L
                              3dNFQxA34hWfTGsditdojefTkDTpcgc+yeatKHWU7LVj9NEwsU13M5j5m4c1/Vmj
                              kf9z9ZIY53LyjpFYOIFO3q8ZTWMSbc1N166d0vTGkBAcoN9ADhBHL27sB2vxDV5B
                              lb9uAK8CIZSU4+eb+02T73os2JdtTPYHQwUpq8mSanZG0hyOsNm5AKkrIQcgPdyQ
                              V06uhPuOdd7aosF9UXRSoMLaAyphL8h2zCyWOfiSnrVhCIWmqLIP62DhFPInf3yB
                              vmV90DeIFJS03tcEC3JIXzBid1bHEQBu703ij7n3NTBiIk5vO9PdKa73sxAopI0i
                              egECcMeyue07si7EyVMR2lHyLSVcZWPRBv1jepsQLryOXB2KwpQCuoqg3MN8jMLy
                              PUDUbUXmoesQc6jZXCmd+wQKQMY92331CoQXlOen+G+OhAcCJ6tZ57gLrEVVuFL1
                              1+QiqDauEEa3/soyPZC/QgsvsOK2PJmFaMfXEtZSNpz8HqrR9bO/NEzCKIhMBBMR
                              AgAMBQJNcC31BYMIYvV+AAoJEN0ckHpQ/p0yVa8AoMumOpcCY40ZoUTuZkeh0Ilg
                              zUBHAKC18+sUZhnR0ZqhEFNUXjOa+ElHfohMBBMRAgAMBQJNcDSLBYMIYu7oAAoJ
                              ECo4IGJOjjyFAi0An3FcwwJDq/yPCLryj1rmnwb3Z1T6AJwLGwSnIRJSlrn5+YIR
                              7XdDitDhDohGBBARAgAGBQJNcH14AAoJEDbt/tCleo76Ab8AoLjiCT/OvkRYSpoj
                              mufWBkXZXMrXAJ4z4ygpUU9YF/QpFeMHT+ZmV0VNPIhMBBIRAgAMBQJNcKEABYMI
                              YoJzAAoJEOrAwtx5kch1huEAn28v73FgzL2l80nEMh7YAVFJ/NqlAKCNbvedx1VI
                              naSaLJisyB3+Ma/7iIhMBBIRAgAMBQJNcKHABYMIYoGzAAoJEKbkO86DXeH17MMA
                              oL0Q9AFJmUMOPX/vcKngroyrGCNxAKCHnjDjVp5vjRvJ+eNkV2hEuBFrO4kCIgQS
                              AQIADAUCTXC2VQWDCGJtHgAKCRBk9yqkNJ4jA3D/D/91ELWv5VhYM2uMWnmR0a5X
                              GRK4ZMmeNNCwiL2C6fiUb8la9cUTVBG0CEsE0EoxoAqIaMraBkBNJRw5gjKLbS9R
                              6S72FMIluVJr2Du5P8K8VhzNUSo3rfDUfcE4CNnrIkxWX3/YFJOnjAjP/rdXKSFa
                              jdsr/oe+IrlpPI/ZZe8stkQ9S9t5eII77prtwqgxf9T4/ZHHYDLGzitw2FZOw1Ox
                              ttDSeQX0S9N+fbCgWLSRwYSvAf17Lw+y+a3q4CdALFccXQJSRcI5f1o8Z1TOcgz0
                              5hU2q/8JbBzbDKT8XydX1eqGHxRUsVZG201vDGeLm8YxE36oPn8tSKC5N1lJvXb/
                              GiCh5NtWDpKsTMGNi5kLvQxNXKvUOSwcRkeQmzyyExD8ufuelx0TqVxUEA3dYtSw
                              5RB2hrmeyiCmMJlC22N2tcTA55rm+M+sqs1J5w5o+glYvs8wOory9xa4sBeSiah1
                              meOqg0Ji2Tdj8ArmAWf2yAaQ7C2aTyoe26U76uAAnluJnBIVUH9gjeBGOrAd0hC8
                              rwepwVB78Lm2BmbPDz+WoCOJDGOGkEZvf5tTQhnIYmb1mYYbCVvg4Fgws7bdZKn3
                              yeCBnIxiLt9L0Pk2XAEhJ6/s1PL7aYgwTepg/bcNHKEHCkvD94bvUK6u9jVvA/zB
                              i+/rNjzU6uy0EzLtJj6894kCIgQTAQIADAUCTXFHwQWDCGHbsgAKCRBaVLu4eCJe
                              CGbZD/9rSmmxKmecbAmOfoyuPBskvDvA1MBN+tksN8ZCjQyuItA6SNRk2rEdSC8g
                              NvY3v/g+q6+HnzWSbgXqN4ia4mrur6uL2UX08qBYglnDDN5hzCKDEtC7Uhc+hjOQ
                              8J7XhiJPT7KR1BI51i82ANYFoKOxDJ8MwTmosEyvXjltYV5kVhOyJ6PJKzItUyGO
                              PMq6Xl5/BRyQqdrkpO1QHA3L3mUnQG7c2Ud0tvky+UmwfLWammGTyRlUMoMey5Zj
                              pyEDgk6yxuHLgWtjxnQ/ouGhsXImRlnzYf5ktdaFUwlheB0afETwsI4IR49KLnT6
                              v7VEZcsmxDkyIUOPQZjAm+vJbBUB8WbpwGTX7D+pERm/ISCYr0yCKfQVDKE1+2RT
                              p0FUwT5zFH2llnSB+LkKtI3NLwaUSyE5JCCeq7nkWHFwrG5/fyO2Z5N4XHmjaXqY
                              xMerwIFmk4fM9iEhicsDZXqcMFRS7mkAvtbMXjPwm0xxjx8CH4xuVXTEs234Pm+k
                              QDWjPmE//9J+YlIfgY2rdCGOaTD7+SGJwr4YO8r3/eJJG81Hslf1hDLVixMfcia3
                              0npgGqj0zmEp6hWQIP99DIdWO4fYtw378xpEw0V8Xlc01+HNiM9rm6drR1Q+J57m
                              urtMCpdronrR6z9WjKWWJY31lBkdktcdwqseEgREX+pJwBw+B4hkBBMRCAAMBQJN
                              OhVVBYMImQ4eAAoJEPAEkOHBTWtrqhgBAIwJy1CV/uuY2gGFY+2iY6VsGgXRbCz2
                              TzA8CMIKk8VxAP4z2LJnzEo1Kk1qf/g4mSgW9WTRiI4TFXdZwCoF3jjqHokCIgQS
                              AQgADAUCTXCXgQWDCGKL8gAKCRAy5nrarOTt1gIXD/9C7w5I6CoJ2FE2IkJP8WRp
                              ffIM1rU0sBBPh37Lwio2iajSPLT5e+6KcBmoMXXO348qkfOUcu2WZqEkEA/nBVyx
                              wuofHHnh+k+p2KNULYy5afjuBDu7H/nmH9I4b9esRqLE6Y/EzTB8v540AUCEvw0O
                              ZWZfRWeRlxEznl3PuWMCutnhssm4B76jkZvXgyczZxDxxOpwMoQhoXkO50Q6VgPP
                              R3k4vuRqTqllmvByjxm4B7pbETF82p/S8UGo8sfcan5d3bVEikwQf3nELJopax/x
                              +R7ZbQGoTFESNY+zUlxZ1zWQmhPan8AlOyyh7FUTtFC7qcjYyMwpgZtgbHETtvlc
                              kV8DWa8PxxLccTf7ksQfX6hAv9z2hrw9hymm5Agu3h5PXGafE56CntWJYfq7gri+
                              Qt2gtJLe6D8wv6ZnuBdeW3u/vP6dmZIvQ0DTgael4ksKoEoh0hig8QPD7zExSUf5
                              3Iafp/p4thPh0aYL/gcQL6M+lGzAoq8jDA8rGa0MOhZlkEtXOJpA/UtxqtPCbABU
                              2+4885n11vwNeV+SnranRIQ5o5W/Ams3AXAU8zetmwrI2iLsEsc1ku8lrBHTHV+I
                              wZFHmbZXa2sQqkkAV8dynjt/hqf85t81OTuNCgCtj0dZ4iDpaHcrvMYPCiSweSoA
                              zPs7tNYZOKq4s+2IlMD5BLRNR1BHTWFpbCBQcm9qZWN0IFRlYW0gKE9mZmljaWFs
                              IE9wZW5QR1AgS2V5KSA8Z3BnbWFpbC1kZXZlbEBsaXN0cy5ncGdtYWlsLm9yZz6I
                              gAQTEQgAKAUCTG0h8wIbAwUJCWYBgAYLCQgHAwIGFQgCCQoLBBYCAwECHgECF4AA
                              CgkQdtePBQDQJsRU3AEAvFkGuZWss09WEOzgE3CdoURSTTgevd26vkRlJg/GYioB
                              AOB3Dm3ZeDcQhsz8MwP/YgYvzLLKmkVl2VX/y9WK9vbaiEYEExECAAYFAkxtIrUA
                              CgkQ3RyQelD+nTI0xgCggSIUHv5TjEOGyxptCqwvA/SXtZoAoNVlvx7HgKHbLJi6
                              +G8kNvmB6dSOiGQEExEIAAwFAkzbnyEFgwj3hFIACgkQ1nmLjHgqz5CzRQD/QaMA
                              oY/13mcxqIddEv9PZy/XmVbqzUR1WIJGoMOfyggA/A4hfhDgWqIW5O8bEWH4pCwy
                              esNYwxCGEXBO6BqHVlf6iQIiBBMBAgAMBQJNBJQ8BYMIzo83AAoJENqHDBNGqVew
                              TJsQALe+NvsBS6q3gXa6vKpHHyF3GvvWNhoMRo1Vm18JErGjoCcOZk0CYJqAeS4j
                              zwGCUQVo905WP0sOu8dJXHaa46MqFa3xE9quAdbpTLeXWED2HGbM2lz5Ff8ryoa7
                              KvT5QWfIUSoc2nKtmzSt/VHEbtRsLHDEy8YXPHCSsxS2Zs1RETUeETKj/TkjdiAy
                              J1vL/Fnr17JKxpDsAKnCV94S3NrOT+3sNO/5XPWQgvfhtS3dhdGRN31TUrXw6R6T
                              Ev30BKJu2jvgK+L+Dqm6B9bp73VEElnKpBfMS7EzdV5hsPGJVuii8U0aZNPKgjiq
                              4QZ5M9aFePbBrl/85S0TGYT6gRK6c1pjjYZlGDdyxZW/gJmlQFASyStwTbE0Fi6/
                              n0vjYxo1cWvlfEDOQKotshR/6BfZdYFIAx39/TD5m4I2r2E70kcE44hs3Tb+TAB2
                              EStTZN6UvnJLRAqCzf4irmf4jlMbr7qQG2xz7ML8jwPRB12iPGMOG2nCFjhMORCe
                              qWmMjrwcd5zims8wko99G5L2U10eHYjkaXNmbArFJ2EnITUuzUbxtJDVhmDk0yy2
                              c7XjOhgdLctprD9UCa9ZXvCzEd94l+mTj8CrhJiRz9pSJPa5bSUrStM9ZcoeMt4y
                              hgUEwBKldRTKrRcBiVMYEizGQ84ibh986/x63FnAjZbTGM+ZiEwEExECAAwFAk1w
                              NIsFgwhi7ugACgkQKjggYk6OPIWr0gCg5BkbQezK5qpO/Sr+neaV/H6kncUAn1c4
                              Xc2gBCte6Lfp9G60Pwscp94jiEYEEBECAAYFAk1wfXgACgkQNu3+0KV6jvpDQwCf
                              dqoMsEUGdwISXIZKDpcYVuPceg8Aniywf6kIsMLQ3WclYihbRTVX4NU0iEsEEhEC
                              AAwFAk1wocAFgwhigbMACgkQpuQ7zoNd4fUlNACWMySpYkExmFMZp1JaR7uPDAzV
                              twCgng1qfRDisPnlp3AUC8Un1jS4V2KITAQSEQIADAUCTXChAAWDCGKCcwAKCRDq
                              wMLceZHIdRPYAKCem3cB/mvri3ENTD93CMHw4VfU8wCgxSUymzQ2rM4DCwP0ho0f
                              rWtzYoOJAiIEEgECAAwFAk1wtlUFgwhibR4ACgkQZPcqpDSeIwODNg//caMbnHFV
                              KitU4ldRWjn7ZRlay8f/d8sepl03YeQN62HHL74iQ65ugLCrYdIoftdCcmRs2x12
                              jHRtTGQFtyxhQfF39Du4MHlnn6tsXbPsl8shsT9hAW2kijMcOVcRkoZIl10eH6PZ
                              7vZLueytlao+H1Biq6yNLXQh9Ki3RN/shXqvOTjmFSw6l97fiXq1kSI6tsPL69ur
                              yajv4c/Y4yb8e2iqRyH1PQOjLuBQfssBPiMF9/biZxAfNaWOg5rujueP6Ir3igNd
                              rTZ6Y2791uTk36uLcCrWuEPIE2kdfggXdYJv8Od9srJrQ3sOSSewQfDAMsNG2L5D
                              HAvVjjBJ6M5CLY4Lh0nOZmePwMqBi2YmjJkfAj23pI+w877PHVIp1OAjpzzoZJOm
                              vY1opMsLbb/XtryspHwfADk6ZZqUujEdl9erwnrMi63gDAylihwIibp6Oato268B
                              eM9ObJrRpdEgsPcCWDN7ScHBISDrX/GakAabOYrrIsE5TMsgZUo3MsNpnWY1QmlN
                              b2F872bm91ayzU8OQ3LhrR4dDjab9mCxw2ZoFwwvkpg7NO2PrC4/NFBRqNIIY/2F
                              jeLbICMCqiNRZ2Tro856pehue7sLRZ6fEAuT3iCdZATA3qEw/Qxj5T9eI/BvZe2H
                              w1gI57t2zQLetPqdr1DA63t5ASMzmS/DBzWJAiIEEwECAAwFAk1xR8EFgwhh27IA
                              CgkQWlS7uHgiXghfOBAArDlrNEVxwSdC+ipQa1Hh5mRDmNUzo7oGb3rQuMeuQl6x
                              l/hbPFaqlSeXFLvZSkEDbG4OqS7MNeoqqoGqlLV8pwOiTqGWtFrwf/E+CIIR10iY
                              a2RphA4wvkvU9SGoXYxMe7WGg7ShUV2GrorlivBh4ibpBJaBuUkXRr32GUDSk8D6
                              Ix6MP6W4n/QfCJOdxaR0ouMt4+Xw5c+4XgeF5QiVgct09dYuvp+98wBMxIvP1x3z
                              pCLzzqmGSr/0zbsLiTxv68A+tbAl7AvrKq22ljtc/QUX7xDxjdx/ZUfMapLLaZXP
                              aO1/XpPMO0VpaQLXFuoF54YzG6r/p2un4tmqjyeeSf15CQZ25uwPNW4/2ZoxoJ/b
                              Xovk5oZ7exB8POzT8AXKqDShAEstGmNKD9MFmeLNHE/P0rbg1SOi6WlzpxySh2Z5
                              e4hZ68P7evVPPmrHQqXFFGOigTKD7+0hC5oJI4+ckRIjKNFoZcq08dKMMUubdJHI
                              HiWG4uax+ZNIflp3DsmHK0X6azsECdwjyxoORFK1jouhDiLc1n4SJF5RZ+VQ4ilF
                              e2hjxYnVzD7HwAeRIwlDxCCzqasYop5W1MFFy8FhytZ2n+BumZcybWOow1FA+Pkr
                              rOfl9UmO1jbZIBMr0B2dTmeia3kzmdDtxDk3Q47r4a/QXBDGUcZS8gTZpFhIrzmJ
                              AiIEEgEIAAwFAk1wl4EFgwhii/IACgkQMuZ62qzk7dY2KA/9HCIFPgwDFa9/2zpN
                              vKyBrOrbgtOCMz2JprsOw+f3KjyGB858BO3+NmtJQ/D1H9aRfy1PW8KwCc/a9PlT
                              6f89FuwTv1hfCSveeOUHEP0u3UIJwJMB1aQc3YXNAlTfVoEAOtusL7AbXwSj52Ut
                              uAEvqdaM5FF5Erog+VUyVnUlVWU2kUzQn0ib9KVC28izKzjGEiCr0EtR3B927nvn
                              iWIh/FvqjIAYWVKGk300jzStf1FJcYOfJhAqBKZ6t1nJE6smtd8ZTA+86FTe4Cij
                              +52LsKRFAxDFBBMpTr5Ne6Z9QpVt+ItIE0ahLD0rcYFuRKfo1g7IL3r/i+JuDIOj
                              V++ixMY39iBYot4O5AOswuAaixvqPrBkF6sOtMCxNLr3p4BlXaerpiVYQENgodnF
                              cUKI0nE0g/FSO8USboxFVWH66YR0DMyWFFldSgRRcvY5bdyFUpax5gTGVYhMQV94
                              g3qje77TzirvBQyP8vqzlLdOaMu5FSWNsXwwYQiJSufcI6b/1wP7+PMkEfvsOO66
                              e9v1C0264k09FzldNET29D8WEh+ffBpoJCOzU513ZHpy6vvGdfKzs5E0DCkD+gFv
                              nkiLm9kHrsPVYAn9n5Ts+WlfdUAx5pfwhwmFn9pSCK+Ti03EjRL5kC6mEoGfWA4r
                              hVtqyvzEVgGyj8zTdYqDfSDvSS25Ag0ETG0h8xAIAJRp6DW+NamD4fxmZ14lg6LK
                              tqw18nyZj5MhWRxT/O8xzRwox6W67GKL30lY4SiqZZlRYwh9mYtNyR9Ix9Mw73p8
                              oZqq0pDCHXIPC1EjgbnxlMEYtX1CnUCwn/Izhatk/b1OPOiDvQ/CCpKc9vWJpPNy
                              q4w92Hjv3jwgYV1+e+kA/HYrRUipTUJ7blnNoGxfpp+WbJr9O5EeOBkyLHMP8sGE
                              AGJQeB7cWMKQGZG00XsbA7W7fW/NQVJAfGs5AAtjDCROnAcILeWhe+YhkYpaxWUJ
                              WDTRJQEJPibu0DirhUQwhbijSenLomSQjw62D0mvqC/ILxhcjz5ndrc5vPdwSVcA
                              BREIAIWDzQ55WG4TG46879v8BiC8CMyKlSSuidrXADWJvJShxVY7EigSYoGLDBK1
                              0XYLTejFFDaqN0Bk5i87jagr3D8tVj1lviQivi8zjF54qY17Vi8MNefslWMEMSGA
                              MGn0Kd/Ap6jDnOasB6Z2B7C1aQO5bv+KAPgoduqFCAr3R9Lhkp5evoRMnNPbduBM
                              h0u/ioo4TXSy1QqXgzeH3V2RXpsacSpw3IvPPLikVkN/l2qv5SuO9lWL2Cci1ii1
                              ocIRisXicCO3M0rW4pLaNrqCYQ6nsP84kttnpcbxKeludUPWWVfhJcY5ooNvrPQ7
                              CKUfvAQzjR/FylgfUD34gJ+wsoKIZwQYEQgADwUCTG0h8wIbDAUJCWYBgAAKCRB2
                              148FANAmxIFkAP9WDPMOIaC27XuriVk9ZTA29qbSPbuHAbfJBUDJUQaUFgEAv4hc
                              G8hEwsWbB0FD01ZPjkkMaPY/bpECewaPNuwnTlyYjQRQzxa0AQQAueohXcwatNmn
                              OymxQ8WjOU1i2zYorXfoi+Yc2PJI1KWnyKctNOqE5T7pt2lBiwsk6HOUXCgf9Fd0
                              Ki5EnrherdWCKfGqlh/DDk8q47cwezqGQFmC150j/7+pwG8rg9ZM14PrERa8oVmI
                              E+yKNTt61duj4raFA5K7YutsOdwsWQEAEQEAAbQyREIgVGVzdCAoUlNBIGtleSBm
                              b3IgREIgdGVzdGluZykgPGRidGVzdEByaXBlLm5ldD6IuQQTAQIAIwUCUM8WtAIb
                              LwcLCQgHAwIBBhUIAgkKCwQWAgMBAh4BAheAAAoJEHLE5sNFnxPAs+QD/ja92NEp
                              DfNCYVhyOzMLLkv8wuWPiBiNmQ4kgJW50szPdzVm11rklpS74qcVnrrh6RUvEslj
                              pygfU31vFYo1LMpqgTHhaFXw4caTbF1KQkcrzzt/hRikDjTzknHCSDOWPPEDF8t/
                              UZC5I3Dd5jRvdPYYOBdFDBxKXjF25+2jTMRF
                              =4TVE
                              -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       owner-MNT
                changed:      bit-bucket@ripe.net
                source:       TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[key-cert] PGPKEY-459F13C0" }
        ack.errorMessagesFor("Modify", "[key-cert] PGPKEY-459F13C0") ==
                ["The supplied object has multiple keys"]

        query_object_not_matches("-rBT key-cert PGPKEY-459F13C0", "key-cert", "PGPKEY-459F13C0", "Test User \\(testing\\) <dbtest@ripe.net>")
    }
}
