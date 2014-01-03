package net.ripe.db.whois.spec.domain

import spock.lang.Specification

class NotificationResponseTest extends Specification {
    NotificationResponse deleteSuccess = new NotificationResponse("", """
Dear Colleague,

This is to notify you that some object(s) in RIPE Database that
you either maintain or you are listed in as to-be-notified have
been added, deleted or changed.

This message is auto-generated.
Please DO NOT reply to this message.

If you do not understand why we sent you this message,
or for assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>

The update causing these changes had the following address:

- From:      Andre Kampert <cac37ak@ripe.net>
- Date/Time: Thu, 14 Jun 2012 10:04:42 +0200

---
OBJECT BELOW DELETED:

mntner: DEV-ROOT-MNT


The RIPE Database is subject to Terms and Conditions:
http://www.ripe.net/db/support/db-terms-conditions.pdf

For assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>""")

    NotificationResponse updateSuccess = new NotificationResponse("", """
Dear Colleague,

This is to notify you that some object(s) in RIPE Database that
you either maintain or you are listed in as to-be-notified have
been added, deleted or changed.

This message is auto-generated.
Please DO NOT reply to this message.

If you do not understand why we sent you this message,
or for assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>

The update causing these changes had the following address:

- From:      Andre Kampert <cac37ak@ripe.net>
- Date/Time: Thu, 14 Jun 2012 10:04:42 +0200

---
OBJECT BELOW MODIFIED:

mntner: DEV-ROOT-MNT

REPLACED BY:

mntner: DEV-ROOT-MNT
remarks: changed


The RIPE Database is subject to Terms and Conditions:
http://www.ripe.net/db/support/db-terms-conditions.pdf

For assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>""")

    NotificationResponse authFailed = new NotificationResponse("", """
    Dear Maintainer,

This is to notify you that some objects in which you are referenced
as a maintainer were requested to be changed, but *failed* the
proper authorisation for any of the referenced maintainers.
Please contact the sender of this update about changes that need
to be made to the following objects.

This message is auto-generated.
Please DO NOT reply to this message.

If you do not understand why we sent you this message,
or for assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>

The update causing these changes had the following address:

- From:      Andre Kampert <cac37ak@ripe.net>
- Date/Time: Thu, 14 Jun 2012 10:04:42 +0200

---
DELETE REQUESTED FOR:

mtner: DEV-ROOT-MNT


The RIPE Database is subject to Terms and Conditions:
http://www.ripe.net/db/support/db-terms-conditions.pdf

For assistance or clarification please contact:
RIPE Database Administration <ripe-dbm@ripe.net>""")

    def "notification delete success"() {
      when:
        def notification = deleteSuccess
      then:
        notification.deleted.any { it.type == "mntner" && it.key == "DEV-ROOT-MNT" }
    }
}
