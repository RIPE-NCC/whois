package net.ripe.db.whois.common.collect;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class CollectionHelperTest {
    @Test
    public void uniqueResult_no_results() {
        final Object result = CollectionHelper.uniqueResult(Arrays.asList());
        assertNull(result);
    }

    @Test
    public void uniqueResult_single_result() {
        final Integer result = CollectionHelper.uniqueResult(Arrays.asList(1));
        assertThat(result, is(1));
    }

    @Test(expected = IllegalStateException.class)
    public void uniqueResult_multiple_results() {
        CollectionHelper.uniqueResult(Arrays.asList(1, 2));
    }

    @Test
    public void iterateProxy_empty() {
        ProxyLoader<Identifiable, RpslObject> proxyLoader = Mockito.mock(ProxyLoader.class);
        final Iterable<ResponseObject> responseObjects = CollectionHelper.iterateProxy(proxyLoader, Collections.<Identifiable>emptyList());

        verify(proxyLoader, never()).load(anyListOf(Identifiable.class), (List<RpslObject>) anyObject());

        final Iterator<ResponseObject> iterator = responseObjects.iterator();
        assertThat(iterator.hasNext(), is(false));

        verify(proxyLoader, never()).load(anyListOf(Identifiable.class), (List<RpslObject>) anyObject());
    }

    @Test
    public void iterateProxy() {
        final RpslObjectInfo info1 = new RpslObjectInfo(1, ObjectType.INETNUM, "1");
        final RpslObject object1 = RpslObject.parse("inetnum: 10.0.0.0");

        final RpslObjectInfo info2 = new RpslObjectInfo(2, ObjectType.INETNUM, "2");
        final RpslObject object2 = RpslObject.parse("inetnum: 10.0.0.1");

        ProxyLoader<Identifiable, RpslObject> proxyLoader = Mockito.mock(ProxyLoader.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                List<RpslObject> response = (List<RpslObject>) args[1];
                response.add(object1);
                response.add(object2);

                return null;
            }
        }).when(proxyLoader).load(any(List.class), any(List.class));


        final Iterable<ResponseObject> responseObjects = CollectionHelper.iterateProxy(proxyLoader, Arrays.asList(info1, info2));

        verify(proxyLoader).load(anyListOf(Identifiable.class), anyListOf(RpslObject.class));

        final Iterator<ResponseObject> iterator = responseObjects.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat((RpslObject) iterator.next(), is(object1));

        assertThat(iterator.hasNext(), is(true));
        assertThat((RpslObject) iterator.next(), is(object2));

        assertThat(iterator.hasNext(), is(false));

        verify(proxyLoader).load(anyListOf(Identifiable.class), anyListOf(RpslObject.class));
    }
}
