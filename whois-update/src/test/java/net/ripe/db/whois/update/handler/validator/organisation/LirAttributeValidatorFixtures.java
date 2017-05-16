package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class LirAttributeValidatorFixtures {

    protected static final RpslObject NON_LIR_ORG = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject NON_LIR_ORG_CHANGED = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      different street and number\n" +
            "address:      different city \n" +
            "address:      different country\n" +
            "phone:        +31 111 1111111\n" +
            "fax-no:       +31 111 1111112\n" +
            "e-mail:       different@test.com\n");

    protected static final RpslObject NON_LIR_ORG_SINGLE_USER_MNTNER = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n" +
            "mnt-by:       MNT1-NOT-LIR\n");

    protected static final RpslObject NON_LIR_ORG_MULTIPLE_USER_MNTNER = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n" +
            "mnt-by:       MNT1-NOT-LIR\n" +
            "mnt-by:       MNT2-NOT-LIR\n");

    protected static final RpslObject LIR_ORG = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_SINGLE_USER_MNTNER = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n" +
            "mnt-by:       MNT1-LIR\n");

    protected static final RpslObject LIR_ORG_MULTIPLE_USER_MNTNER = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n" +
            "mnt-by:       MNT1-LIR\n" +
            "mnt-by:       MNT2-LIR\n");

    protected static final RpslObject LIR_ORG_ADDRESS = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      different street and number\n" +
            "address:      different city \n" +
            "address:      different country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_PHONE = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 111 1111111\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_FAX = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 111 1111111\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_EMAIL = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       different@test.com\n");

    protected static final RpslObject LIR_ORG_ORG_NAME = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd modified\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_MNT_BY = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "mnt-by:       TEST-MNT\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_ORG = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "org:          LIR-ORG-TST\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_ORG_TYPE = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     OTHER\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    protected static final RpslObject LIR_ORG_ABUSE_MAILBOX = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "abuse-mailbox: abuse@test.com\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");
}
