package net.ripe.db.whois.update.keycert;

import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.x509.util.StreamParsingException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class X509SignedMessageTest {

    @Test
    public void verify_smime_plaintext() throws Exception {
        final String signedData = (
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
                        "source:  TEST\n\n").replaceAll("\\n", "\r\n");
        final String signature =
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
                        "1jpwXyycrnAxekeLNqiX0/hldjkhAAAAAAAA\n";
        final X509Certificate certificate = getCertificate(
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M\n" +
                        "MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP\n" +
                        "BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG\n" +
                        "CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy\n" +
                        "ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k\n" +
                        "MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy\n" +
                        "ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN\n" +
                        "BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv\n" +
                        "5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON\n" +
                        "XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9\n" +
                        "lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W\n" +
                        "HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1\n" +
                        "y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj\n" +
                        "2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx\n" +
                        "EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT\n" +
                        "A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC\n" +
                        "AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF\n" +
                        "AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1\n" +
                        "YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo\n" +
                        "mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==\n" +
                        "-----END CERTIFICATE-----");

        X509SignedMessage subject = new X509SignedMessage(signedData, signature);

        assertThat(subject.verify(certificate), is(true));
    }

    @Test
    public void verify_smime_plaintext_multiple_paragraphs() throws Exception {
        final String signedData =
                ("Content-Type: text/plain; charset=\"iso-8859-1\"\n" +
                        "Content-Transfer-Encoding: 7bit\n" +
                        "Content-Language: de-DE\n" +
                        "\n" +
                        "inetnum: 193.0.0.0 - 193.0.0.255\n" +
                        "netname: NETNAME\n" +
                        "descr: Description\n" +
                        "country: NL\n" +
                        "admin-c: TEST-RIPE\n" +
                        "tech-c: TEST-RIPE\n" +
                        "status: ASSIGNED PA\n" +
                        "mnt-by: TEST-MNT\n" +
                        "changed: test@foo.com\n" +
                        "source: RIPE\n" +
                        "\n" +
                        "changed: test@test.net\n" +
                        "\n").replaceAll("\\n", "\r\n");
        final String signature = "" +
                "MIIDsAYJKoZIhvcNAQcCoIIDoTCCA50CAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3\n" +
                "DQEHAaCCAd8wggHbMIIBRAIJAJrBRtmpa9a7MA0GCSqGSIb3DQEBBQUAMDIxCzAJ\n" +
                "BgNVBAYTAk5MMRMwEQYDVQQIEwpTb21lLVN0YXRlMQ4wDAYDVQQKEwVCT0dVUzAe\n" +
                "Fw0xMzA0MTgxNTQyMjBaFw0xNDA0MTgxNTQyMjBaMDIxCzAJBgNVBAYTAk5MMRMw\n" +
                "EQYDVQQIEwpTb21lLVN0YXRlMQ4wDAYDVQQKEwVCT0dVUzCBnzANBgkqhkiG9w0B\n" +
                "AQEFAAOBjQAwgYkCgYEAp9e053n54CuwZNjY0bgslBQDRSjN+f2wZ8ut0Xt5aj3j\n" +
                "RpjhWVUF4isUzqMm30E0HHev1meDOWQIgzyhnqDJF/+UUjzKBrZ47OvpBR/9W1WY\n" +
                "EI4xbUHEBd8LIxPuMrY8+l7xJcPw1D1W/MNs/wscUDTMBX63/qq+WkZcl+31idsC\n" +
                "AwEAATANBgkqhkiG9w0BAQUFAAOBgQBHLN6q1lPTRQ6+VLPc0dK5fQAaGI5C9d6L\n" +
                "Yb1fAn0vt1eBt6HnAK+H2PX19yaaCmItEh6KPNzX1uT9uBh12sAff47KMGwftGZU\n" +
                "cw7cVdulM2qr3UTL98AuF1O2XbRE2dtET+IWXCgjnqKID11NNCzksfgEaesrpdkr\n" +
                "TFrB+nHvuTGCAZkwggGVAgEBMD8wMjELMAkGA1UEBhMCTkwxEzARBgNVBAgTClNv\n" +
                "bWUtU3RhdGUxDjAMBgNVBAoTBUJPR1VTAgkAmsFG2alr1rswCQYFKw4DAhoFAKCB\n" +
                "sTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xMzA0\n" +
                "MTkwOTU5MDdaMCMGCSqGSIb3DQEJBDEWBBS/9gqYJMaWAm8sp2Rf99Oh06aRATBS\n" +
                "BgkqhkiG9w0BCQ8xRTBDMAoGCCqGSIb3DQMHMA4GCCqGSIb3DQMCAgIAgDANBggq\n" +
                "hkiG9w0DAgIBQDAHBgUrDgMCBzANBggqhkiG9w0DAgIBKDANBgkqhkiG9w0BAQEF\n" +
                "AASBgJ8sFkzA3mksoazwIc/eNpMy20wQh1CKtiGU+xTzErkurSg8Z+7pIkod1bbq\n" +
                "k6tiSWnWhRzmz/YFqgGuzk+O3MyRt3YqU9nZpdaZVZxepN/p/gjQI1gfTXK+7WJ/\n" +
                "OcKukh/onU6eXZOD50r1RdgFPL4+lXpe0VrWjUOT3CoglnU1";
        X509Certificate certificate = getCertificate("" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIB2zCCAUQCCQCawUbZqWvWuzANBgkqhkiG9w0BAQUFADAyMQswCQYDVQQGEwJO\n" +
                "TDETMBEGA1UECBMKU29tZS1TdGF0ZTEOMAwGA1UEChMFQk9HVVMwHhcNMTMwNDE4\n" +
                "MTU0MjIwWhcNMTQwNDE4MTU0MjIwWjAyMQswCQYDVQQGEwJOTDETMBEGA1UECBMK\n" +
                "U29tZS1TdGF0ZTEOMAwGA1UEChMFQk9HVVMwgZ8wDQYJKoZIhvcNAQEBBQADgY0A\n" +
                "MIGJAoGBAKfXtOd5+eArsGTY2NG4LJQUA0Uozfn9sGfLrdF7eWo940aY4VlVBeIr\n" +
                "FM6jJt9BNBx3r9ZngzlkCIM8oZ6gyRf/lFI8yga2eOzr6QUf/VtVmBCOMW1BxAXf\n" +
                "CyMT7jK2PPpe8SXD8NQ9VvzDbP8LHFA0zAV+t/6qvlpGXJft9YnbAgMBAAEwDQYJ\n" +
                "KoZIhvcNAQEFBQADgYEARyzeqtZT00UOvlSz3NHSuX0AGhiOQvXei2G9XwJ9L7dX\n" +
                "gbeh5wCvh9j19fcmmgpiLRIeijzc19bk/bgYddrAH3+OyjBsH7RmVHMO3FXbpTNq\n" +
                "q91Ey/fALhdTtl20RNnbRE/iFlwoI56iiA9dTTQs5LH4BGnrK6XZK0xawfpx77k=\n" +
                "-----END CERTIFICATE-----");

        X509SignedMessage subject = new X509SignedMessage(signedData, signature);

        assertThat(subject.verify(certificate), is(true));
    }

    @Test
    public void verify_smime_multipart_inetnum() throws Exception {
        final String signedData = ("" +
                "Content-Type: multipart/alternative;\n" +
                "boundary=\"_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\"\n" +
                "Content-Language: de-DE\n" +
                "\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\n" +
                "Content-Type: text/plain; charset=\"us-ascii\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "inetnum: 193.0.0.0 - 193.0.0.255\n" +
                "netname: NETNAME\n" +
                "descr: Description\n" +
                "country: NL\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "status: ASSIGNED PA\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: test@foo.com\n" +
                "source: RIPE\n" +
                "\n" +
                "\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\n" +
                "Content-Type: text/html; charset=\"us-ascii\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Dus-ascii\">\n" +
                "<meta name=3D\"Generator\" content=3D\"Microsoft Exchange Server\">\n" +
                "<!-- converted from rtf -->\n" +
                "<style><!-- .EmailQuote { margin-left: 1pt; padding-left: 4pt; border-left:#800000 2px solid; } --></style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<font face=3D\"TheSans\" size=3D\"2\"><span style=3D\"font-size:10pt;\">\n" +
                "<div>inetnum: 193.0.0.0 - 193.0.0.255</div>\n" +
                "<div>netname: NETNAME</div>\n" +
                "<div>descr: Description</div>\n" +
                "<div>country: NL</div>\n" +
                "<div>admin-c: TEST-RIPE</div>\n" +
                "<div>tech-c: TEST-RIPE</div>\n" +
                "<div>status: ASSIGNED PA</div>\n" +
                "<div>mnt-by: TEST-MNT</div>\n" +
                "<div>changed: test@foo.com</div>\n" +
                "<div>source: RIPE</div>\n" +
                "<div><font face=3D\"Calibri\" size=3D\"2\"><span style=3D\"font-size:11pt;\">&nbsp;</span></font></div>\n" +
                "</span></font>\n" +
                "</body>\n" +
                "</html>\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\n").replaceAll("\\n", "\r\n");
        final String signature = "" +
                "MIIDsAYJKoZIhvcNAQcCoIIDoTCCA50CAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3\n" +
                "DQEHAaCCAd8wggHbMIIBRAIJAJrBRtmpa9a7MA0GCSqGSIb3DQEBBQUAMDIxCzAJ\n" +
                "BgNVBAYTAk5MMRMwEQYDVQQIEwpTb21lLVN0YXRlMQ4wDAYDVQQKEwVCT0dVUzAe\n" +
                "Fw0xMzA0MTgxNTQyMjBaFw0xNDA0MTgxNTQyMjBaMDIxCzAJBgNVBAYTAk5MMRMw\n" +
                "EQYDVQQIEwpTb21lLVN0YXRlMQ4wDAYDVQQKEwVCT0dVUzCBnzANBgkqhkiG9w0B\n" +
                "AQEFAAOBjQAwgYkCgYEAp9e053n54CuwZNjY0bgslBQDRSjN+f2wZ8ut0Xt5aj3j\n" +
                "RpjhWVUF4isUzqMm30E0HHev1meDOWQIgzyhnqDJF/+UUjzKBrZ47OvpBR/9W1WY\n" +
                "EI4xbUHEBd8LIxPuMrY8+l7xJcPw1D1W/MNs/wscUDTMBX63/qq+WkZcl+31idsC\n" +
                "AwEAATANBgkqhkiG9w0BAQUFAAOBgQBHLN6q1lPTRQ6+VLPc0dK5fQAaGI5C9d6L\n" +
                "Yb1fAn0vt1eBt6HnAK+H2PX19yaaCmItEh6KPNzX1uT9uBh12sAff47KMGwftGZU\n" +
                "cw7cVdulM2qr3UTL98AuF1O2XbRE2dtET+IWXCgjnqKID11NNCzksfgEaesrpdkr\n" +
                "TFrB+nHvuTGCAZkwggGVAgEBMD8wMjELMAkGA1UEBhMCTkwxEzARBgNVBAgTClNv\n" +
                "bWUtU3RhdGUxDjAMBgNVBAoTBUJPR1VTAgkAmsFG2alr1rswCQYFKw4DAhoFAKCB\n" +
                "sTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xMzA0\n" +
                "MTkxMDI2MDhaMCMGCSqGSIb3DQEJBDEWBBQcYbZgEwjIMEBgIQW5vU9ZO2vyETBS\n" +
                "BgkqhkiG9w0BCQ8xRTBDMAoGCCqGSIb3DQMHMA4GCCqGSIb3DQMCAgIAgDANBggq\n" +
                "hkiG9w0DAgIBQDAHBgUrDgMCBzANBggqhkiG9w0DAgIBKDANBgkqhkiG9w0BAQEF\n" +
                "AASBgGh1N+W4rzQ6n9+dvLY4jfP4lavYdcaNKNVZf0Yhx14vW5LQ1iggqtB+ZYtM\n" +
                "Xzs8SdGSM9FtepvHkLSYtZId0odF/eB6rY7DN3O69TQN7GhmYeICtevT3bkzL950\n" +
                "P3FhHeI3vHvnW1Xb+fdg46213Ym1tVw3V0caqGdp5Cw5vry5";
        X509Certificate certificate = getCertificate("" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIB2zCCAUQCCQCawUbZqWvWuzANBgkqhkiG9w0BAQUFADAyMQswCQYDVQQGEwJO\n" +
                "TDETMBEGA1UECBMKU29tZS1TdGF0ZTEOMAwGA1UEChMFQk9HVVMwHhcNMTMwNDE4\n" +
                "MTU0MjIwWhcNMTQwNDE4MTU0MjIwWjAyMQswCQYDVQQGEwJOTDETMBEGA1UECBMK\n" +
                "U29tZS1TdGF0ZTEOMAwGA1UEChMFQk9HVVMwgZ8wDQYJKoZIhvcNAQEBBQADgY0A\n" +
                "MIGJAoGBAKfXtOd5+eArsGTY2NG4LJQUA0Uozfn9sGfLrdF7eWo940aY4VlVBeIr\n" +
                "FM6jJt9BNBx3r9ZngzlkCIM8oZ6gyRf/lFI8yga2eOzr6QUf/VtVmBCOMW1BxAXf\n" +
                "CyMT7jK2PPpe8SXD8NQ9VvzDbP8LHFA0zAV+t/6qvlpGXJft9YnbAgMBAAEwDQYJ\n" +
                "KoZIhvcNAQEFBQADgYEARyzeqtZT00UOvlSz3NHSuX0AGhiOQvXei2G9XwJ9L7dX\n" +
                "gbeh5wCvh9j19fcmmgpiLRIeijzc19bk/bgYddrAH3+OyjBsH7RmVHMO3FXbpTNq\n" +
                "q91Ey/fALhdTtl20RNnbRE/iFlwoI56iiA9dTTQs5LH4BGnrK6XZK0xawfpx77k=\n" +
                "-----END CERTIFICATE-----");

        X509SignedMessage subject = new X509SignedMessage(signedData, signature);

        assertThat(subject.verify(certificate), is(true));
    }

    @Test
    public void verify_smime_multipart_person() throws Exception {
        final String signedData = (
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
                        "--Apple-Mail=_C45B4ECE-1DB6-4DDE-8B6D-4DB0BB0CDC8E--\n").replaceAll("\\n", "\r\n");
        final String signature =
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
                        "0dZdWzSqRcNJzOJjna7eHLz8SEDFAAAAAAAA\n";
        X509Certificate certificate = getCertificate("-----BEGIN CERTIFICATE-----\n" +
                "MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M\n" +
                "MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP\n" +
                "BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG\n" +
                "CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy\n" +
                "ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k\n" +
                "MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy\n" +
                "ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN\n" +
                "BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv\n" +
                "5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON\n" +
                "XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9\n" +
                "lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W\n" +
                "HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1\n" +
                "y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj\n" +
                "2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx\n" +
                "EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT\n" +
                "A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC\n" +
                "AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF\n" +
                "AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1\n" +
                "YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo\n" +
                "mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==\n" +
                "-----END CERTIFICATE-----");

        X509SignedMessage subject = new X509SignedMessage(signedData, signature);

        assertThat(subject.verify(certificate), is(true));
    }

    @Test
    public void isEquals() {
        X509SignedMessage first = new X509SignedMessage("First Test", "First Signature");
        X509SignedMessage second = new X509SignedMessage("Second Test", "Second Signature");

        assertThat(first.equals(first), is(true));
        assertThat(first.equals(second), is(false));
    }

    private X509Certificate getCertificate(String certificate) throws StreamParsingException {
        X509CertParser parser = new X509CertParser();
        parser.engineInit(new ByteArrayInputStream(certificate.getBytes()));
        return (X509Certificate) parser.engineRead();
    }
}
