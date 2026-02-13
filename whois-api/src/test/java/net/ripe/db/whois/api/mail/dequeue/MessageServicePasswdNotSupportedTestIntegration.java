package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.collect.Lists;
import jakarta.mail.MessagingException;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
public class MessageServicePasswdNotSupportedTestIntegration extends AbstractMailMessageIntegrationTest {

    @Autowired
    private MailSenderStub mailSenderStub;


    @BeforeAll
    public static void setUp() {
        System.setProperty("md5.password.supported", "false");
    }

    @AfterAll
    public static void clearProperties() {
        System.clearProperty("md5.password.supported");
    }

    @Test
    public void test_upd_single_object_with_password_then_error() throws MessagingException, IOException {

        final String incomingMessage = """
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST
                password: test
                """;

        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();
        assertThat(acknowledgement, containsString("""
                ***Error:   Authorisation for [role] DR1-TEST failed
                            using "mnt-by:"
                            not authenticated by: OWNER-MNT
                """));
        assertThat(acknowledgement, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void test_upd_single_object_with_pgp() throws MessagingException, IOException {
        setTime(LocalDateTime.parse("2026-02-09T13:06:50"));
        databaseHelper.addObjects(Lists.newArrayList(RpslObject.parse("""
                key-cert:       PGPKEY-5763950D
                method:         PGP
                owner:          noreply@ripe.net <noreply@ripe.net>
                fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.12 (Darwin)
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
                """),
                RpslObject.parse("""
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          PGPKEY-5763950D
                mnt-by:        OWNER1-MNT
                source:        TEST
                """)));

        final String incomingMessage = """
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA256
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                -----BEGIN PGP SIGNATURE-----
                
                iQEzBAEBCAAdFiEEiE+OI2nl5vGfs2P0u8y7LVdjlQ0FAmmJzqcACgkQu8y7LVdj
                lQ3iZggAnVljlhKRQDSKjU0DfbT9pgPI7d6Jbv2eQGWEy39IbtDL3iMEE7mC4FeV
                kqeUCxI6PcTyKVhp43Ltlkf2QT2jKPWtRUuncS8xdPlA4cnxZzy+lZTEyskjA4qK
                C6BeAobW1sT+1jZZvKkEqeGiqeSiWQz8jFl4Zr8Qh20QNtqEENahKow2MCy46fWU
                Nso0TCNoL2afv6dzazc6vE/Ai117VtBVMPTIQgiCJjNF4dR/n6N9fxTOBseRDViW
                wZRrvxJJK4W//gAl6UsA3lL8Q4/cl+tjevkvMlYpZfCV6HCpAMzQolFOXGLCsrEy
                sB2qMZD3ngpfhbgt0AP6zUr+LK90wA==
                =WANB
                -----END PGP SIGNATURE-----
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, is(containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role")));
    }

    @Test
    public void test_upd_multiple_objects_with_without_password_then_error() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString("""
                ***Error:   Authorisation for [mntner] OWNER1-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER1-MNT
                """));
        assertThat(acknowledgement, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }

    @Test
    public void test_upd_multiple_objects_with_password_then_error() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                
                role:        dummy role 1
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR2-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString("""
                ***Error:   Authorisation for [mntner] OWNER1-MNT failed
                            using "mnt-by:"
                            not authenticated by: OWNER1-MNT
                """));
        assertThat(acknowledgement, containsString("""
            ***Warning: MD5 hashed password authentication has been ignored because is not
                        longer supported."""));
    }
}
