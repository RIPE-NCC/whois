package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.jupiter.api.Tag("IntegrationTest")
class MailMessageIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
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
                auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
                mnt-by:      OWNER-MNT
                upd-to:      dbtest@ripe.net
                source:      TEST
                """,
                "SSO-MNT": """\
                mntner:    SSO-MNT
                admin-c:   TP1-TEST
                upd-to:    person@net.net
                auth:      SSO 906635c2-0405-429a-800b-0602bd716124 # person@net.net
                mnt-by:    SSO-MNT
                mnt-by:    OWNER-MNT
                source:    TEST
                """
        ]
    }

    def "uppercase content type is supported"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
                "User-Agent: Alpine 2.00 (LFD 1167 2008-08-23)\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: TEXT/PLAIN; format=flowed; charset=US-ASCII\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n"
        "\n"
      then:
        def ack = ackFor message

        ack.success
    }

    def "multipart/alternative is supported"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/related;\n" +
                "\ttype=\"multipart/alternative\"; charset=\"UTF-8\"; boundary=\"b1_9f813eab50ec99dee5c1dfc5b10d4b3f\"\n" +
                "Content-Disposition: inline\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "--b1_9f813eab50ec99dee5c1dfc5b10d4b3f\n" +
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"b3_9f813eab50ec99dee5c1dfc5b10d4b3f\"\n" +
                "\n" +
                "--b3_9f813eab50ec99dee5c1dfc5b10d4b3f\n" +
                "Content-Type: text/plain; format=flowed; charset=\"UTF-8\"\n" +
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
                "password: owner\n" +
                "\n" +
                "--b3_9f813eab50ec99dee5c1dfc5b10d4b3f\n" +
                "Content-Type: text/html; charset=\"UTF-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html><head></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">\n" +
                "</body></html>\n" +
                "--b3_9f813eab50ec99dee5c1dfc5b10d4b3f--\n" +
                "\n" +
                "--b1_9f813eab50ec99dee5c1dfc5b10d4b3f\n" +
                "Content-Type: image/jpeg; name=\"eacadfb9f4a94666afca66383ea4a630\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-ID: <eacadfb9f4a94666afca66383ea4a630>\n" +
                "\n" +
                "/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAABQAAD/4QMpaHR0cDov\n" +
                "YFVDUKH8v/MwRYwIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgIgI\n" +
                "gIgIgIgIgIgIgIgIgIgIgIgIgIv/2Q==\n" +
                "\n" +
                "--b1_9f813eab50ec99dee5c1dfc5b10d4b3f--\n"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
    }

    def "multipart/mixed is supported"() {
      when:
        def message = send "Content-Type: multipart/mixed;\n" +
                "\tboundary=\"_000_B209CC1FB920EE4AB75F588373E9DB873EBD46C44DEMV61UKRDdoma_\"\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "Subject: NEW\n" +
                "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
                "Accept-Language: en-US, en-GB\n" +
                "Content-Language: en-US\n" +
                "X-MS-Has-Attach:\n" +
                "X-MS-TNEF-Correlator: <B209CC1FB920EE4AB75F588373E9DB873EBD46C44D@ripe.net>\n" +
                "acceptlanguage: en-US, en-GB\n" +
                "MIME-Version: 1.0\n" +
                "\n" +
                "--_000_B209CC1FB920EE4AB75F588373E9DB873EBD46C44DEMV61UKRDdoma_\n" +
                "Content-Type: text/plain; charset=\"us-ascii\"\n" +
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
                "password: owner\n" +
                "\n" +
                "--_000_B209CC1FB920EE4AB75F588373E9DB873EBD46C44DEMV61UKRDdoma_\n" +
                "Content-Disposition: attachment; filename=\"winmail.dat\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Type: application/ms-tnef; name=\"winmail.dat\"\n" +
                "\n" +
                "eJ8+IiIzAQaQCAAEAAAAAAABAAEAAQeQBgAIAAAA5AQAAAAAAADoAAEJgAEAIQAAADY5NzY1QTQ5\n" +
                "NTkzRThBNDY5NzM3QjEwQUNDMzc1ODk1ABEHAQ2ABAACAAAAAgACAAEFgAMADgAAANwHCAAUAAoA\n" +
                "MgA6AAEAdgEBIIADAA4AAADcBwgAFAAKADIAOgABAHYBAQiABwAYAAAASVBNLk1pY3Jvc29mdCBN\n" +
                "AC0AVQBTACwAIABlAG4ALQBHAEIAAAAAAB8AAICGAwIAAAAAAMAAAAAAAABGAQAAACAAAAB4AC0A\n" +
                "bQBzAC0AaABhAHMALQBhAHQAdABhAGMAaAAAAAEAAAACAAAAAAAAAJ17\n" +
                "\n" +
                "--_000_B209CC1FB920EE4AB75F588373E9DB873EBD46C44DEMV61UKRDdoma_--"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
    }

    def "encrypted messages are not supported"() {
      when:
        def message = send "Content-Type: multipart/encrypted;\n" +
                "\tboundary=\"Apple-Mail=_FBB8DA0A-2B6E-4F0C-956B-3EB265E0655D\";\n" +
                "\tprotocol=\"application/pgp-encrypted\";\n" +
                "Subject: encrypted\n" +
                "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
                "From: noreply@ripe.net\n" +
                "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Description: OpenPGP encrypted message\n" +
                "To: test-dbm@ripe.net\n" +
                "\n" +
                "This is an OpenPGP/MIME encrypted message (RFC 2440 and 3156)\n" +
                "--Apple-Mail=_FBB8DA0A-2B6E-4F0C-956B-3EB265E0655D\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: application/pgp-encrypted\n" +
                "Content-Description: PGP/MIME Versions Identification\n" +
                "\n" +
                "Version: 1\n" +
                "\n" +
                "--Apple-Mail=_FBB8DA0A-2B6E-4F0C-956B-3EB265E0655D\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: inline;\n" +
                "\tfilename=encrypted.asc\n" +
                "Content-Type: application/octet-stream;\n" +
                "\tname=encrypted.asc\n" +
                "Content-Description: OpenPGP encrypted message\n" +
                "\n" +
                "-----BEGIN PGP MESSAGE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "\n" +
                "hQEMA4YgPa5BOutSAQgAyxc9cRKThX67m1ZuamtNt/5UEayWXhUg3CttJyBIiosf\n" +
                "YFUBP/yKKpybmzwpZoOvxSGXywUhWuFArf5WCKp3T+xEv+tKir+48PHDtK9wrnYj\n" +
                "q46eN+QbTA8YYBZc1CyExnvALKvXY9A9ioaodLKYOnetQTvpaQMlkEXsbVvoZPdx\n" +
                "A3QuuvjnI20YXzsPS2AbhoXuUvKHxp7HAIndFgF9gYXV/a96bC/w/ym04PuvIKpX\n" +
                "7MXZruM7FBnCBb9gjKu1s0gbW3U4p3+IAkjNzPuXmJKKPJTIwp2zCJ+PyTzLZ5FV\n" +
                "kgGNJbSGenjYe7KQ8EaYhtT5jsdjuVjWDPtzXMrlf9LpAQiAekIsa4s4JPv6LRuV\n" +
                "pgLPHrqIlBdlaTo/BaVgGy9KZrJGy8sqmVX5u+nccXRSb3jVhtEXFZutOtKkUEl+\n" +
                "I7Y9Taz1N3uvrbgCdMd5wICImb980v1Vvlnngr/uHnwj7tKaV/3gICw9Fv0tjwg6\n" +
                "L2RpVzybuz6N7PGm0t6qMXP5Ez0DJucn9jFAc0QUxGDaVQPF/0tDgkCN82o/gtrh\n" +
                "LIqhXrp5BcKDiIyJYJHhIPvcHO/NupoGmZt/kSmabScLUBKf+fw+IPuQ5nIWbK9i\n" +
                "Q0rIgrmhRNkFSciSwUTZgBhVqzxuy+M62rmsSXqMPivjBymlYowubM4ZvoCMJ+08\n" +
                "Da6ByQiBGCccmn2aJWlY0zNj84M7UdpFzfqz2u852dT1iTFk+Nk81L9/TouGUqYB\n" +
                "a7fbE9yNh61Jx0Jwla929Ge8gvxZNijramz9AZcrs7VrGXIDeq71UX40Lj0btT5t\n" +
                "zakF2gDBwzOmtMQRU8loTcqtCmpPAUxiAaEeoyhTteqI+WqJpJf6l3StlZHnH0r8\n" +
                "HKycQWu96FAUmqULYwtQTBsrCzZLAIMrswoGOypRzQLVkibjFJWibzbCcg/+kQbS\n" +
                "WJMbKTPzNaHkfJG3aM/B9x+As4OQoeIQQmp/Z+XuYsVirZAf6P0FbZmJ4SFXavbA\n" +
                "7n4+c61mAuOhdGE2FQYK57bANC7nY1vVAfsldRm2W+fbq7ygb80eFBiivVoIUeTa\n" +
                "DkCkjkXNVcgt9Vsck0eKoydYVOoVqL5nXgSBn5qxNhdk8x7thRf4NIR/t4VREcz2\n" +
                "vYIpVmtIjsgzFoAafpZwi+/05+ZlO/O92fK4uC9YHYxofilJmj2DZOiKWKECMn9Z\n" +
                "+sIpQCAP29G3wZomKix45XWS5o7dolhJqJW+aOGFI/F5rHwI2em76PKPeetZ3+yd\n" +
                "m/YxHkHF7LkBKwUfR+Bb5N8m4Z9DrcQmXWaOi0cUdUaOBgLg6Nx4ZDN7HoFAbV9p\n" +
                "X9Ngu0AiVpY7ZVGLKdb+lhia5vp9qOc=\n" +
                "=bYlz\n" +
                "-----END PGP MESSAGE-----\n" +
                "\n" +
                "--Apple-Mail=_FBB8DA0A-2B6E-4F0C-956B-3EB265E0655D--"
      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "unrecognised content type is not supported"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "Content-Type: unrecognised; charset=us-ascii\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Subject: NEW\n" +
                "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
                "Message-Id: <90563E66-2415-4A49-B8DF-3BD1CBB8868C@ripe.net>\n" +
                "To: test-dbm@ripe.net\n" +
                "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
                "\n" +
                "This is a test.\n\n"
      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "non-break space is substituted with regular space"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: UPDATE\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First\u00a0Person\n" +
                "address: \u00a0St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: \u00a0owner\n"
      then:
        def ack = ackFor message

        ack.success
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
    }

    def "unknown-8bit charset is handled"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=unknown-8bit\n" +
                "Content-Disposition: inline\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "Mime-Version: 1.0\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
    }

    def "text/plain with utf-8 charset is handled"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
    }

    @Ignore // TODO fix delete functionality then enable test
    def "delete mntner with sso auth is handled"() {
        when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: \n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "mntner:    SSO-MNT\n" +
                "admin-c:   TP1-TEST\n" +
                "upd-to:    person@net.net\n" +
                "auth:      SSO person@net.net\n" +
                "mnt-by:    SSO-MNT\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n" +
                "password: owner\n" +
                "delete: test\n\n"
        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[mntner] SSO-MNT" }
    }

    def "text/html is not handled"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/html\n" +
                "Mime-Version: 1.0\n" +
                "\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
                "<title>RIPE</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<H1>This is a test.</H1>\n" +
                "</body>"
      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "multipart/mixed with only an attachment is not supported"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "Content-Type: multipart/mixed; boundary=\"Apple-Mail=_923629C7-88C8-4CDE-B30B-C639C8E76279\"\n" +
                "Subject: NEW\n" +
                "Message-Id: <8A29B2AC-732C-45E3-B62E-BBF2B5B751A1@ripe.net>\n" +
                "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "To: test-dbm@ripe.net\n" +
                "Mime-Version: 1.0 (Mac OS X Mail 6.2 \\(1499\\))\n" +
                "X-Mailer: Apple Mail (2.1499)\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_923629C7-88C8-4CDE-B30B-C639C8E76279\n" +
                "Content-Disposition: attachment;\n" +
                "\tfilename=update.txt\n" +
                "Content-Type: application/octet-stream;\n" +
                "\tname=\"update.txt\"\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_923629C7-88C8-4CDE-B30B-C639C8E76279--"
      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "application/octet-stream is not supported"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "Subject: DIFF\n" +
                "Message-ID: <5099e7ae.IPDjtNMRZVKw40wN%anandb@ripe.net>\n" +
                "User-Agent: Heirloom mailx 12.4 7/29/08\n" +
                "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "To: test-dbm@ripe.net\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/mixed;\n" +
                " boundary=\"=_5099e7ae.v8T+K5hR49s+hq/QzIXugeMLQFMlLaBH46OwULpo9oP734NV\"\n" +
                "\n" +
                "This is a multi-part message in MIME format.\n" +
                "\n" +
                "--=_5099e7ae.v8T+K5hR49s+hq/QzIXugeMLQFMlLaBH46OwULpo9oP734NV\n" +
                "Content-Type: application/octet-stream\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "Content-Disposition: attachment;\n" +
                " filename=\"mkautnum.eml.asc\"\n" +
                "\n" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "aut-num: AS25152=0D\n" +
                "as-name: K-ROOT-SERVER=0D\n" +
                "descr: Reseaux IP Europeens Network Coordination Centre (RIPE NCC)=0D\n" +
                "descr: Originates prefixes of k.root-servers.net (root name server)=0D\n" +
                "org: ORG-RIEN1-RIPE=0D\n" +
                "remarks:=0D\n" +
                "import: from AS1221=0D\n" +
                "         203.14.8.50=0D\n" +
                "         action pref=3D100;=0D\n" +
                "         accept ANY=0D\n" +
                "export: to AS1221=0D\n" +
                "         203.14.8.50=0D\n" +
                "         action pref=3D100; community =3D { NO_EXPORT };=0D\n" +
                "         announce RS-KROOT-APNIC=0D\n" +
                "admin-c: GII-RIPE=0D\n" +
                "tech-c: GII-RIPE=0D\n" +
                "mnt-by: RIPE-NCC-END-MNT=0D\n" +
                "mnt-by: RIPE-GII-MNT=0D\n" +
                "mnt-routes: RIPE-GII-MNT=0D\n" +
                "notify: gii-people@ripe.net=0D\n" +
                "source: RIPE=0D\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG/MacGPG2 v2.0.18 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iEYEARECAAYFAlCZ5jkACgkQi+U8Q0SwlCuvygCgitflIY1d5XWqGBVxNPhV6R9Y\n" +
                "YtYAn2OZ6A5lN6NebbmtTfZPu6NhtxvN\n" +
                "=3DXThn\n" +
                "-----END PGP SIGNATURE-----\n" +
                "\n" +
                "--=_5099e7ae.v8T+K5hR49s+hq/QzIXugeMLQFMlLaBH46OwULpo9oP734NV--"
      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "exception parsing body part content type"() {
      when:
        def message = send "From: \"John Doe\" <John.Doe@company.biz>\n" +
                "To: auto-dbm@ripe.net\n" +
                "Date: Mon, 12 Nov 2012 12:20:01 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-type: Multipart/Mixed; boundary=Message-Boundary-11583\n" +
                "Subject: (Fwd) Update AS on Network\n" +
                "Message-ID: <50A0DB61.21413.E284EF4@John.Doe.Company.biz>\n" +
                "\n" +
                "--Message-Boundary-11583\n" +
                "Content-type: Multipart/Alternative; boundary=\"Alt-Boundary-13147.237522676\"\n" +
                "\n" +
                "--Alt-Boundary-13147.237522676\n" +
                "Content-type: text/plain; charset=US-ASCII\n" +
                "Content-transfer-encoding: 7BIT\n" +
                "Content-description: Mail message body\n" +
                "\n" +
                "\n" +
                "------- Forwarded message follows -------\n" +
                "Date sent:\tMon, 12 Nov 2012 12:18:49 +0100\n" +
                "To:\tNOC@company.net\n" +
                "Subject:\tUpdate AS on Network\n" +
                "From:\tNOC@company.net\n" +
                "\n" +
                "Send this email to auto-dbm@ripe.net IN PLAIN TEXT\n" +
                "\n" +
                "aut-num: AS31449\n" +
                "\n" +
                "\n" +
                "-----\n" +
                "Aucun virus trouve dans ce message.\n" +
                "Analyse effectuee par AVG - www.avg.fr\n" +
                "Version: 2012.0.2221 / Base de donnees virale: 2441/5389 - Date:\n" +
                "11/11/2012\n" +
                "\n" +
                "------- End of forwarded message -------\n" +
                "\n" +
                "--Alt-Boundary-13147.237522676\n" +
                "Content-type: text/html; charset=US-ASCII\n" +
                "Content-transfer-encoding: 7BIT\n" +
                "Content-description: Mail message body\n" +
                "\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "          \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html  xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"><head>\n" +
                "<title></title>\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"/>\n" +
                "<meta http-equiv=\"Content-Style-Type\" content=\"text/css\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n" +
                "--Alt-Boundary-13147.237522676--\n" +
                "\n" +
                "--Message-Boundary-11583\n" +
                "Content-type: Application/Octet-stream; name=\"WPM\$1XJ2.PM\$\"; type=Plain text\n" +
                "Content-description: Mail message body\n" +
                "Content-disposition: attachment; filename=\"WPM\$1XJ2.PM\$\"\n" +
                "Content-transfer-encoding: BASE64\n" +
                "\n" +
                "DQpTZW5kIHRoaXMgZW1haWwgdG8gYXV0by1kYm1AcmlwZS5uZXQgSU4gUExBSU4gVEVYVA0K\n" +
                "ZTogMTEvMTEvMjAxMg0KDQo=\n" +
                "\n" +
                "--Message-Boundary-11583--"

      then:
        def ack = ackFor message

        ack.failed
        ack.contents.contains("***Error:   No valid update found")
    }

    def "diff keyword in subject line adds a warning"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: DIFF\n" +
                "Message-ID: <50A0DB61.21413.E284EF4@ripe.net>\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-type: text/plain\n" +
                "\n"
      then:
        def ack = ackFor message

        ack.contents =~ /Number of objects found:\s*0/
        ack.contents.contains("***Warning: The DIFF keyword is not supported")
    }

    def "diff keyword in subject line of update mail adds a warning but continues"() {
        when:
        def message = send new Message(subject: "DIFF", body: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       OWNER-MNT
            mnt-ref:      OWNER-MNT
            source:       TEST

            password:     owner
            """.stripIndent(true))

        def ack = ackFor message

        then:
        def response = ack.contents
        response.contains("Create SUCCEEDED: [organisation] ORG-RNO1-TEST")
        response.contains("Warning: The DIFF keyword is not supported")
    }

    def "invalid keyword in subject line"() {
      when:
        def message = send "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: update\n" +
                "Message-ID: <50A0DB61.21413.E284EF4@ripe.net>\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-type: text/plain\n" +
                "\n"
      then:
        def ack = ackFor message

        ack.contents =~ /\*\*\*Warning: Invalid keyword\(s\) found: update/
        ack.contents =~ /\*\*\*Warning: All keywords were ignored/
    }

    def "non latin-1 characters are substituted"() {
      when:
        def create = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First Person\n" +
                "address: Тверская улица,москва\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def createAck = ackFor create

        createAck.success
        createAck.summary.nrFound == 1
        createAck.summary.assertSuccess(1, 1, 0, 0, 0)
        createAck.summary.assertErrors(0, 0, 0, 0)

        createAck.countErrorWarnInfo(0, 3, 0)
        createAck.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        createAck.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Value changed due to conversion into the ISO-8859-1 (Latin-1) character set"]

        queryMatches("-r FP1-TEST", "address:\\s+\\?\\?\\?\\?\\?\\?\\?\\? \\?\\?\\?\\?\\?,\\?\\?\\?\\?\\?\\?")

      then:
        def update = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: UPDATE\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First Person\n" +
                "address: Тверская улица,москва\n" +
                "remarks: Updated\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "remarks: updated\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def updateAck = ackFor update

        updateAck.success
        updateAck.summary.nrFound == 1
        updateAck.summary.assertSuccess(1, 0, 1, 0, 0)
        updateAck.summary.assertErrors(0, 0, 0, 0)

        updateAck.countErrorWarnInfo(0, 5, 0)
        updateAck.successes.any { it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person" }
        updateAck.warningSuccessMessagesFor("Modify", "[person] FP1-TEST   First Person") == [
                "Value changed due to conversion into the ISO-8859-1 (Latin-1) character set"]

        queryMatches("-r FP1-TEST", "address:\\s+\\?\\?\\?\\?\\?\\?\\?\\? \\?\\?\\?\\?\\?,\\?\\?\\?\\?\\?\\?")
    }

    def "latin-1 control characters are substituted"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First Person\n" +
                "address: Test\u000b\u000c\u007F\u008f Address\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Invalid character(s) were substituted in attribute \"address\" value"]

        queryMatches("-r FP1-TEST", "address:\\s+Test\\?\\?\\?\\? Address")
    }

    def "latin-1 extended ASCII characters are preserved"() {
        when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: ISO-8859-1\n" +
                "\n" +
                "person:  First Person\n" +
                "address:  ÖÜëñ\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1

        queryMatches("-r FP1-TEST", "address:\\s+ÖÜëñ")
    }

    def "IDN email address converted to Punycode"() {
      when:
        def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                "person:  First Person\n" +
                "address: Moscow\n" +
                "e-mail:  example@москва.ru\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "password: owner\n\n"
      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.warningSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Value changed due to conversion of IDN email address(es) into Punycode"]

        queryMatches("-Br FP1-TEST", "e-mail:         example@xn--80adxhks.ru")
    }

    def "blank lines are replaced by plus continuation character"() {
        when:
          def message = send "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                  "From: noreply@ripe.net\n" +
                  "To: test-dbm@ripe.net\n" +
                  "Subject: NEW\n" +
                  "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                  "\n" +
                  "password: owner\n" +
                  "person: First Person\n" +
                  "\t\t\t\n" +
                  "address:   St James Street\n" +
                  "\t\t\t\n" +
                  "address: Burnley\n" +
                  "\t\t\t\n" +
                  "address: UK\n" +
                  "\t\t\t\n" +
                  "phone: +44 282 420469\n" +
                  "\t\t\t\n" +
                  "nic-hdl: FP1-TEST\n" +
                  "\t\t\t\n" +
                  "mnt-by:  OWNER-MNT\n" +
                  "\t\t\t\n" +
                  "source:  TEST\n" +
                  "\t\t\t\n" +
                  "\n"
        then:
          def ack = ackFor message

          ack.success
          ack.summary.nrFound == 1
        then:
          queryObject("-r FP1-TEST", "person", "First Person")
    }

}
