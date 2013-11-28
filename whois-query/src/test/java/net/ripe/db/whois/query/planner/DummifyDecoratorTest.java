package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.executor.decorators.DummifyDecorator;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DummifyDecoratorTest {
    @Mock Dummifier dummifier;
    @Mock SourceContext sourceContext;
    @InjectMocks DummifyDecorator subject;
    @Mock Query query;

    @Before
    public void setUp() throws Exception {
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST-GRS"));
        when(sourceContext.isDummificationRequired()).thenReturn(true);
    }

    @Test
    public void apply_message_object() {
        final ResponseObject message = new MessageObject("test");
        Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(message)).iterator();
        assertThat(response.next(), is(message));
        assertThat(response.hasNext(), is(false));
    }

    @Test
    public void apply_rpsl_object_not_allowed() {
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(false);
        final ResponseObject rpslObject = RpslObject.parse("person: Test person\nnic-hdl: TEST-PB");
        Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(rpslObject)).iterator();
        assertThat(response.hasNext(), is(false));
    }

    @Test
    public void apply_rpsl_object_dummify() {
        final RpslObject dummifiedObject = RpslObject.parse("mntner: DEV-MNT\nsource: TEST".getBytes());
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(true);
        when(dummifier.dummify(anyInt(), any(RpslObject.class))).thenReturn(dummifiedObject);

        final ResponseObject rpslObject = RpslObject.parse("mntner: DEV-MNT\nadmin-c: TEST-PN\nsource: TEST".getBytes());
        Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(rpslObject)).iterator();
        assertEquals(response.next(), RpslObject.parse("mntner:         DEV-MNT\nsource:         TEST-GRS"));
    }
}
