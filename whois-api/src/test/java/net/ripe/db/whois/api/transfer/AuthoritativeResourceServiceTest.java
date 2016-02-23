package net.ripe.db.whois.api.transfer;

import net.ripe.commons.ip.*;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceDao;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceServiceTest {

    @Mock
    private AuthoritativeResourceDao authoritativeResourceDao;
    @Mock
    private ResourceDataDao resourceDataDao;

    private AuthoritativeResourceService subject;

    @Before
    public void setup() {
        this.subject = new AuthoritativeResourceService(authoritativeResourceDao, resourceDataDao, "test");
    }

    @Test
    public void transfer_in_ipv4_parent_contains_child() {
        when(resourceDataDao.load("test")).thenReturn(createIpv4AuthoritativeResource("193.0.0.0 - 193.255.255.255"));

        subject.transferInIpv4Block("193.10.0.0 - 193.10.255.255");

        verify(authoritativeResourceDao, never()).create("test", "193.10.0.0 - 193.10.255.255");
    }

    @Test
    public void transfer_in_ipv4_parent_overlaps_child() {
        when(resourceDataDao.load("test")).thenReturn(createIpv4AuthoritativeResource("193.0.0.0 - 193.255.255.255"));

        subject.transferInIpv4Block("193.255.255.0 - 194.10.255.255");

        verify(authoritativeResourceDao, never()).create("test", "193.10.0.0 - 193.10.255.255");
    }

    @Test
    public void transfer_in_ipv4_no_overlap() {
        when(resourceDataDao.load("test")).thenReturn(createIpv4AuthoritativeResource());

        subject.transferInIpv4Block("193.255.255.0 - 194.10.255.255");

        verify(authoritativeResourceDao, never()).create("test", "193.10.0.0 - 193.10.255.255");
    }

    @Test
    public void transfer_out_ipv4_parent_contains_child() {
        when(resourceDataDao.load("test")).thenReturn(createIpv4AuthoritativeResource("193.0.0.0 - 193.255.255.255"));

        subject.transferOutIpv4Block("193.10.0.0 - 193.10.255.255");

        verify(authoritativeResourceDao).delete("test", "193.0.0.0-193.255.255.255");
        verify(authoritativeResourceDao).create("test", "193.0.0.0-193.9.255.255");
        verify(authoritativeResourceDao).create("test", "193.11.0.0-193.255.255.255");
        verifyNoMoreInteractions(authoritativeResourceDao);
    }

    @Test
    public void transfer_out_ipv4_parent_overlaps_child() {
        when(resourceDataDao.load("test")).thenReturn(createIpv4AuthoritativeResource("193.0.0.0 - 193.255.255.255"));

        subject.transferOutIpv4Block("193.255.255.0 - 194.10.255.255");

        verify(authoritativeResourceDao).delete("test", "193.0.0.0-193.255.255.255");
        verify(authoritativeResourceDao).create("test", "193.0.0.0-193.255.254.255");
    }

    // helper methods

    private AuthoritativeResource createIpv4AuthoritativeResource(final String... ranges) {
        final SortedRangeSet<Ipv4, Ipv4Range> ipv4RangeSet = new SortedRangeSet<>();
        for (String range : ranges) {
            ipv4RangeSet.add(Ipv4Range.parse(range));
        }
        return new AuthoritativeResource(new SortedRangeSet<Asn, AsnRange>(), ipv4RangeSet, new SortedRangeSet<Ipv6, Ipv6Range>());
    }

}
