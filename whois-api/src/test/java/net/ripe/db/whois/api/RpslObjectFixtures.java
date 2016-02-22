package net.ripe.db.whois.api;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class RpslObjectFixtures {

    public static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    public static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "mnt-nfy:     mnt-nfy@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    public static final RpslObject PASSWORD_ONLY_MNT = RpslObject.parse("" +
            "mntner:      PASSWORD-ONLY-MNT\n" +
            "descr:       Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      PASSWORD-ONLY-MNT\n" +
            "source:      TEST");

    public static final RpslObject SSO_ONLY_MNT = RpslObject.parse("" +
            "mntner:         SSO-ONLY-MNT\n" +
            "descr:          Maintainer\n" +
            "admin-c:        TP1-TEST\n" +
            "auth:           SSO person@net.net\n" +
            "mnt-by:         SSO-ONLY-MNT\n" +
            "upd-to:         noreply@ripe.net\n" +
            "source:         TEST");

    public static final RpslObject SSO_AND_PASSWORD_MNT = RpslObject.parse("" +
            "mntner:         SSO-PASSWORD-MNT\n" +
            "descr:          Maintainer\n" +
            "admin-c:        TP1-TEST\n" +
            "auth:           SSO person@net.net\n" +
            "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:         SSO-PASSWORD-MNT\n" +
            "upd-to:         noreply@ripe.net\n" +
            "source:         TEST");

    public static final RpslObject SECOND_MNT = RpslObject.parse("" +
            "mntner:        SECOND-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$1ZnhrEYU$h8QUAsDPLZYOYVjm3uGQr1 #secondmnt\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    public static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    public static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    public static final RpslObject TEST_IRT = RpslObject.parse("" +
            "irt:          irt-test\n" +
            "address:      RIPE NCC\n" +
            "e-mail:       noreply@ripe.net\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "auth:         MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:       OWNER-MNT\n" +
            "source:       TEST\n");


    public static final RpslObject INET_NUM = RpslObject.parse("" +
            "inetnum:         193.0.0.0 - 193.0.0.255\n"+
            "netname:         RIPE-NCC\n"+
            "descr:           description\n"+
            "country:         DK\n"+
            "admin-c:         TP1-TEST\n"+
            "tech-c:          TP1-TEST\n"+
            "status:          SUB-ALLOCATED PA\n"+
            "mnt-by:          OWNER-MNT\n"+
            "source:          TEST\n" );


    public static final RpslObject ABUSE_CONTACT_ROLE = RpslObject.parse("" +
            "role:          Abuse Contact\n" +
            "nic-hdl:       AC1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "source:        TEST");

    public static final RpslObject ABUSE_CONTACT_ORGANISATION = RpslObject.parse("" +
            "organisation:  ORG-RN1-TEST\n" +
            "org-name:      Ripe NCC\n" +
            "org-type:      OTHER\n" +
            "address:       Amsterdam\n" +
            "abuse-c:       AC1-TEST\n" +
            "e-mail:        some@email.net\n" +
            "mnt-ref:       OWNER-MNT\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    public static final RpslObject ABUSE_CONTACT_INETNUM = RpslObject.parse("" +
            "inetnum:       193.0.0.0 - 193.0.0.255\n" +
            "netname:       RIPE-NCC\n" +
            "descr:         some description\n" +
            "org:           ORG-RN1-TEST\n" +
            "country:       NL\n" +
            "admin-c:       TP1-TEST\n" +
            "tech-c:        TP1-TEST\n" +
            "status:        SUB-ALLOCATED PA\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");
}
