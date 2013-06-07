package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.PendingUpdate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdenticalPendingUpdateFinderTest {
    @Mock private PendingUpdateDao pendingDao;
    @InjectMocks private IdenticalPendingUpdateFinder subject;

    @Test
    public void find_non_identical_object() {
        final RpslObject object = RpslObject.parse("route6: 2345:2345::/48\norigin: AS1234\nmnt-by: TEST-MNT,OTHER-MNT");
        final RpslObjectBase objectBase = RpslObjectBase.parse("route6: 2345:2345::/48\norigin: AS1234\nmnt-by: OTHER-MNT,TEST-MNT");

        when(pendingDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.newArrayList(new PendingUpdate("InetnumAuthentication", objectBase)));

        final PendingUpdate pendingUpdate = subject.find(object);
        assertThat(pendingUpdate, is(nullValue()));
    }

    @Test
    public void find_identical_object() {
        final RpslObject object = RpslObject.parse("route6:   2345:2345::/48\norigin: AS1234\nmnt-by: TEST-MNT,OTHER-MNT");
        final RpslObjectBase objectBase = RpslObjectBase.parse("route6: 2345:2345::/48\norigin:  AS1234\nmnt-by: TEST-MNT,OTHER-MNT");

        when(pendingDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.newArrayList(new PendingUpdate("InetnumAuthentication", objectBase)));

        final PendingUpdate pendingUpdate = subject.find(object);
        assertThat(pendingUpdate, is(not(nullValue())));
    }

    @Test
    public void find_no_database_results() {
        final RpslObject object = RpslObject.parse("route6:   2345:2345::/48\norigin: AS1234\nmnt-by: TEST-MNT,OTHER-MNT");

        when(pendingDao.findByTypeAndKey(object.getType(), object.getKey().toString())).thenReturn(Lists.<PendingUpdate>newArrayList());

        final PendingUpdate pendingUpdate = subject.find(object);
        assertThat(pendingUpdate, is(nullValue()));
    }
}
