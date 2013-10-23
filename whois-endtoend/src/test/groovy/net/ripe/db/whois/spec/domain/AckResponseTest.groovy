package net.ripe.db.whois.spec.domain

import spock.lang.Specification

class AckResponseTest extends Specification {
    AckResponse ack = new AckResponse("", """"
>  From:       0be472f7-0ed8-40e6-9b83-936892cdd0f2@ripe.net
>  Subject:    create with notify
>  Date:       Wed Aug 29 11:28:34 CEST 2012
>  Reply-To:   0be472f7-0ed8-40e6-9b83-936892cdd0f2@ripe.net
>  Message-ID: <1637889341.0.1346232514441.JavaMail.andre@guest201.guestnet.ripe.net>

SUMMARY OF UPDATE:

Number of objects found:                   36
Number of objects processed successfully:  10
  Create:         1
  Modify:         2
  Delete:         3
  No Operation:   4
Number of objects processed with errors:   26
  Create:         5
  Modify:         6
  Delete:         7

DETAILED EXPLANATION:

***Warning: Invalid keyword(s) found: create with notify
***Warning: All keywords were ignored

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The following object(s) were found to have ERRORS:

---
Delete FAILED: [mntner] DEL-MNT

mntner:      DEL-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST
***Error:   Unknown source

***Error:   Object [mntner] DEL-MNT does not exist in the database

---
Delete FAILED: [mntner] DEL2-MNT

mntner:      DEL2-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST

***Error:   Object [mntner] DEL2-MNT does not exist in the database

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The following object(s) were processed SUCCESSFULLY:

---
Create SUCCEEDED: [mntner] MOD-MNT
***Warning: Some warning

---
Create SUCCEEDED: [mntner] MOD2-MNT

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The RIPE Database is subject to Terms and Conditions:
http://www.ripe.net/db/support/db-terms-conditions.pdf

For assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>

This update was processed by RIPE Database Update Service version 1.21-SNAPSHOT (UNDEFINED)
""")

    def quote() {
      when:
        def quote = ack.quote
      then:
        quote.from == "0be472f7-0ed8-40e6-9b83-936892cdd0f2@ripe.net"
        quote.subject == "create with notify"
        quote.date == "Wed Aug 29 11:28:34 CEST 2012"
        quote.replyTo == "0be472f7-0ed8-40e6-9b83-936892cdd0f2@ripe.net"
        quote.messageId == "<1637889341.0.1346232514441.JavaMail.andre@guest201.guestnet.ripe.net>"
    }

    def summary() {
      when:
        def summary = ack.summary
      then:
        summary.nrFound == 36

        summary.nrSuccess == 10
        summary.nrSuccessCreate == 1
        summary.nrSuccessModify == 2
        summary.nrSuccessDelete == 3
        summary.nrSuccessNoop == 4

        summary.nrErrors == 26
        summary.nrErrorsCreate == 5
        summary.nrErrorsModify == 6
        summary.nrErrorsDelete == 7
    }

    def warnings() {
      when:
        def warnings = ack.allWarnings
      then:
        warnings.size() == 3
        warnings[0] == "Invalid keyword(s) found: create with notify"
        warnings[1] == "All keywords were ignored"
        warnings[2] == "Some warning"
    }

    def errorSection() {
      when:
        def errorSection = ack.errorSection
      then:
        errorSection == """---
Delete FAILED: [mntner] DEL-MNT

mntner:      DEL-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST
***Error:   Unknown source

***Error:   Object [mntner] DEL-MNT does not exist in the database

---
Delete FAILED: [mntner] DEL2-MNT

mntner:      DEL2-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST

***Error:   Object [mntner] DEL2-MNT does not exist in the database

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"""
    }

    def errors() {
      when:
        def errors = ack.errors
      then:
        errors.size() == 2

        errors[0].operation == "Delete"
        errors[0].key == "[mntner] DEL-MNT"
        errors[0].errors.size() == 2
        errors[0].errors[1] == "Object [mntner] DEL-MNT does not exist in the database"
        errors[0].object == """\
mntner:      DEL-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST
***Error:   Unknown source

***Error:   Object [mntner] DEL-MNT does not exist in the database"""


        errors[1].operation == "Delete"
        errors[1].key == "[mntner] DEL2-MNT"
        errors[1].errors.size() == 1
        errors[1].errors[0] == "Object [mntner] DEL2-MNT does not exist in the database"
        errors[1].object == """mntner:      DEL2-MNT
descr:       MNTNER for test
admin-c:     TP1-TEST
upd-to:      updto_test@ripe.net
mnt-nfy:     mntnfy_test@ripe.net
notify:      notif_test@ripe.net
auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
mnt-by:      DEL-MNT
referral-by: DEL-MNT
changed:     dbtest@ripe.net
source:      TEST

***Error:   Object [mntner] DEL2-MNT does not exist in the database

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"""
    }

    def successSection() {
      when:
        def successSection = ack.successSection
      then:
        successSection == "---\nCreate SUCCEEDED: [mntner] MOD-MNT\n***Warning: Some warning\n\n---\nCreate SUCCEEDED: [mntner] MOD2-MNT\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    }

    def successes() {
      when:
        def successes = ack.successes
      then:
        successes.size() == 2

        successes[0].operation == "Create"
        successes[0].key == "[mntner] MOD-MNT"
        successes[0].warnings.any { it == "Some warning" }

        successes[1].operation == "Create"
        successes[1].key == "[mntner] MOD2-MNT"
    }
}
