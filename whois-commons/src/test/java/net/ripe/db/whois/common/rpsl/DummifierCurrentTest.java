package net.ripe.db.whois.common.rpsl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class DummifierCurrentTest {
    @Mock RpslObject object;

    private DummifierCurrent subject;

    @Before
    public void setup() {
        subject = new DummifierCurrent();
    }

    @Test
    public void allowed() {
        assertThat(subject.isAllowed(3, object), is(true));

        assertThat(subject.isAllowed(2, object), is(false));
        assertThat(subject.isAllowed(1, object), is(false));
    }

    @Test
    public void person() {
        final RpslObject person = RpslObject.parse(15, "" +
                "person: Fred Blogs\n" +
                "address: RIPE Network Coordination Centre (NCC)\n" +
                "address: Singel 258\n" +
                "address: 1016 AB Amsterdam\n" +
                "address: The Netherlands\n" +
                "phone: +31 20 535 4444\n" +
                "fax-no: +31 20 535 4445\n" +
                "e-mail: guy@ripe.net\n" +
                "nic-hdl: FB99999-RIPE\n" +
                "mnt-by: AARDVARK-MNT\n" +
                "notify: guy@ripe.net\n" +
                "changed: guy@ripe.net 20040225\n" +
                "source: RIPE");

        final RpslObject dummified = subject.dummify(4, person);

        assertThat(dummified.getAttributes(), contains(
                new RpslAttribute("person", "Name Removed"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "The Netherlands"),
                new RpslAttribute("phone", "+31 20 5.. ...."),
                new RpslAttribute("fax-no", "+31 20 5.. ...."),
                new RpslAttribute("e-mail", "* * *@ripe.net"),
                new RpslAttribute("nic-hdl", "FB99999-RIPE"),
                new RpslAttribute("mnt-by", "AARDVARK-MNT"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("changed", "* * *@ripe.net 20040225"),
                new RpslAttribute("source", "RIPE")));
    }

    @Test
    public void inetnum() {
        final RpslObject inetnum = RpslObject.parse(13, "" +
                "inetnum: 193.0.0.0 - 193.0.7.255\n" +
                "netname: RIPE-NCC\n" +
                "descr: RIPE Network Coordination Centre\n" +
                "descr: Amsterdam, Netherlands\n" +
                "remarks: Used for RIPE NCC infrastructure.\n" +
                "country: NL\n" +
                "admin-c: JDR-RIPE\n" +
                "admin-c: BRD-RIPE\n" +
                "tech-c: OPS4-RIPE\n" +
                "notify: ncc@ripe.net\n" +
                "status: ASSIGNED PI\n" +
                "source: RIPE\n" +
                "mnt-by: RIPE-NCC-MNT\n" +
                "mnt-lower: RIPE-NCC-MNT\n" +
                "changed: bit-bucket@ripe.net 20110217");

        final RpslObject dummified = subject.dummify(4, inetnum);

        assertThat(dummified.getAttributes(), contains(
                new RpslAttribute("inetnum", "193.0.0.0 - 193.0.7.255"),
                new RpslAttribute("netname", "RIPE-NCC"),
                new RpslAttribute("descr", "RIPE Network Coordination Centre"),
                new RpslAttribute("descr", "Amsterdam, Netherlands"),
                new RpslAttribute("remarks", "Used for RIPE NCC infrastructure."),
                new RpslAttribute("country", "NL"),
                new RpslAttribute("admin-c", "JDR-RIPE"),
                new RpslAttribute("admin-c", "BRD-RIPE"),
                new RpslAttribute("tech-c", "OPS4-RIPE"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("status", "ASSIGNED PI"),
                new RpslAttribute("source", "RIPE"),
                new RpslAttribute("mnt-by", "RIPE-NCC-MNT"),
                new RpslAttribute("mnt-lower", "RIPE-NCC-MNT"),
                new RpslAttribute("changed", "* * *@ripe.net 20110217")
        ));
    }

    @Test
    public void organisation() {
        final RpslObject organisation = RpslObject.parse(12, "" +
                "organisation: ORG-NCC1-RIPE\n" +
                "org-name: RIPE Network Coordination Centre\n" +
                "org-type: RIR\n" +
                "address: RIPE NCC\n" +
                " Singel 258\n" +
                " 1016 AB Amsterdam\n" +
                " Netherlands\n" +
                "phone: +31 20 535 4444\n" +
                "fax-no: +31 20 535 4445\n" +
                "e-mail: ncc@ripe.net\n" +
                "admin-c: AP110-RIPE\n" +
                "admin-c: CREW-RIPE\n" +
                "tech-c: CREW-RIPE\n" +
                "ref-nfy: hm-dbm-msgs@ripe.net\n" +
                "mnt-ref: RIPE-NCC-RIS-MNT\n" +
                "mnt-ref: RIPE-NCC-HM-MNT\n" +
                "notify: hm-dbm-msgs@ripe.net\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "changed: bitbucket@ripe.net 20121217\n" +
                "source: RIPE");

        final RpslObject dummified = subject.dummify(4, organisation);

        assertThat(dummified.getAttributes(), containsInAnyOrder(
                new RpslAttribute("organisation", "ORG-NCC1-RIPE"),
                new RpslAttribute("org-name", "RIPE Network Coordination Centre"),
                new RpslAttribute("org-type", "RIR"),
                new RpslAttribute("address", "RIPE NCC\n" +
                        "        Singel 258\n" +
                        "        1016 AB Amsterdam\n" +
                        "        Netherlands"),
                new RpslAttribute("e-mail", "* * *@ripe.net"),
                new RpslAttribute("mnt-ref", "RIPE-NCC-RIS-MNT"),
                new RpslAttribute("mnt-ref", "RIPE-NCC-HM-MNT"),
                new RpslAttribute("mnt-by", "RIPE-NCC-HM-MNT"),
                new RpslAttribute("changed", "* * *@ripe.net 20121217"),
                new RpslAttribute("phone", "+31 20 5.. ...."),
                new RpslAttribute("fax-no", "+31 20 5.. ...."),
                new RpslAttribute("admin-c", "AP110-RIPE"),
                new RpslAttribute("admin-c", "CREW-RIPE"),
                new RpslAttribute("tech-c", "CREW-RIPE"),
                new RpslAttribute("ref-nfy", "* * *@ripe.net"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("source", "RIPE")
        ));
    }

    @Test
    public void mntner() {
        final RpslObject mntner = RpslObject.parse(11, "" +
                "mntner: AARDVARK-MNT\n" +
                "descr: Mntner for guy's objects\n" +
                "admin-c: FB99999-RIPE\n" +
                "tech-c: FB99999-RIPE\n" +
                "upd-to: guy@ripe.net\n" +
                "auth: X509-1\n" +
                "auth: X509-1689\n" +
                "auth: MD5-PW # Filtered\n" +
                "notify: guy@ripe.net\n" +
                "mnt-by: AARDVARK-MNT\n" +
                "referral-by: AARDVARK-MNT\n" +
                "changed: guy@ripe.net 20120510\n" +
                "source: RIPE # Filtered");

        final RpslObject dummified = subject.dummify(4, mntner);

        assertThat(dummified.getAttributes(), containsInAnyOrder(
                new RpslAttribute("mntner", "AARDVARK-MNT"),
                new RpslAttribute("descr", "Mntner for guy's objects"),
                new RpslAttribute("admin-c", "FB99999-RIPE"),
                new RpslAttribute("tech-c", "FB99999-RIPE"),
                new RpslAttribute("upd-to", "* * *@ripe.net"),
                new RpslAttribute("auth", "X509-1"),
                new RpslAttribute("auth", "X509-1689"),
                new RpslAttribute("auth", "MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("mnt-by", "AARDVARK-MNT"),
                new RpslAttribute("referral-by", "AARDVARK-MNT"),
                new RpslAttribute("changed", "* * *@ripe.net 20120510"),
                new RpslAttribute("source", "RIPE")
        ));
    }

    @Test
    public void role_without_abuse_mailbox() {
        final RpslObject role = RpslObject.parse(10, "" +
                "role: RIPE NCC tech contact\n" +
                "address: RIPE Network Coordination Centre (NCC)\n" +
                "address: Singel 258\n" +
                "address: 1016 AB Amsterdam\n" +
                "address: The Netherlands\n" +
                "phone: +31 20 535 4444\n" +
                "fax-no: +31 20 535 4445\n" +
                "e-mail: ncc@ripe.net\n" +
                "nic-hdl: RNTC-RIPE\n" +
                "mnt-by: RIPE-DBM-MNT\n" +
                "notify: ripe-dbm@ripe.net\n" +
                "changed: ripe-dbm@ripe.net 20040225\n" +
                "source: RIPE");

        final RpslObject dummified = subject.dummify(4, role);

        assertThat(dummified.getAttributes(), contains(
                new RpslAttribute("role", "RIPE NCC tech contact"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "* * *"),
                new RpslAttribute("address", "The Netherlands"),
                new RpslAttribute("phone", "+31 20 5.. ...."),
                new RpslAttribute("fax-no", "+31 20 5.. ...."),
                new RpslAttribute("e-mail", "* * *@ripe.net"),
                new RpslAttribute("nic-hdl", "RNTC-RIPE"),
                new RpslAttribute("mnt-by", "RIPE-DBM-MNT"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("changed", "* * *@ripe.net 20040225"),
                new RpslAttribute("source", "RIPE")
        ));
    }

    @Test
    public void role_with_abuse_mailbox() {
        final RpslObject role = RpslObject.parse(10, "" +
                "role: RIPE NCC tech contact\n" +
                "address: RIPE Network Coordination Centre (NCC)\n" +
                "address: Singel 258\n" +
                "address: 1016 AB Amsterdam\n" +
                "address: The Netherlands\n" +
                "phone: +31 20 535 4444\n" +
                "fax-no: +31 20 535 4445\n" +
                "e-mail: ncc@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "nic-hdl: RNTC-RIPE\n" +
                "mnt-by: RIPE-DBM-MNT\n" +
                "notify: ripe-dbm@ripe.net\n" +
                "changed: ripe-dbm@ripe.net 20040225\n" +
                "source: RIPE");

        final RpslObject dummified = subject.dummify(4, role);

        assertThat(dummified.getAttributes(), contains(
                new RpslAttribute("role", "RIPE NCC tech contact"),
                new RpslAttribute("address", "RIPE Network Coordination Centre (NCC)"),
                new RpslAttribute("address", "Singel 258"),
                new RpslAttribute("address", "1016 AB Amsterdam"),
                new RpslAttribute("address", "The Netherlands"),
                new RpslAttribute("phone", "+31 20 535 4444"),
                new RpslAttribute("fax-no", "+31 20 535 4445"),
                new RpslAttribute("e-mail", "* * *@ripe.net"),
                new RpslAttribute("abuse-mailbox", "abuse@ripe.net"),
                new RpslAttribute("nic-hdl", "RNTC-RIPE"),
                new RpslAttribute("mnt-by", "RIPE-DBM-MNT"),
                new RpslAttribute("notify", "* * *@ripe.net"),
                new RpslAttribute("changed", "* * *@ripe.net 20040225"),
                new RpslAttribute("source", "RIPE")
        ));
    }
}

