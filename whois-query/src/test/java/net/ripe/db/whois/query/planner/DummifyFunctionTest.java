package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DummifyFunctionTest {
    @Mock Dummifier dummifier;
    @Mock SourceContext sourceContext;
    @InjectMocks DummifyFunction subject;

    @Before
    public void setUp() throws Exception {
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST-GRS"));
    }

    @Test
    public void apply_message_object() {
        final ResponseObject messageObject = new MessageObject("test");
        final ResponseObject response = subject.apply(messageObject);
        assertThat(response, is(messageObject));
    }

    @Test
    public void apply_rpsl_object_not_allowed() {
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(false);
        final ResponseObject rpslObject = RpslObject.parse("person: Test person\nnic-hdl: TEST-PB");
        final ResponseObject response = subject.apply(rpslObject);
        assertNull(response);
    }

    @Test
    public void apply_rpsl_object_dummify() {
        final RpslObject dummifiedObject = RpslObject.parse(1, "mntner: DEV-MNT\nsource: TEST".getBytes());
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(true);
        when(dummifier.dummify(anyInt(), any(RpslObject.class))).thenReturn(dummifiedObject);

        final ResponseObject rpslObject = RpslObject.parse(1, "mntner: DEV-MNT\nadmin-c: TEST-PN\nsource: TEST".getBytes());
        final ResponseObject response = subject.apply(rpslObject);
        assertThat((RpslObject) response, is(RpslObject.parse("" +
                "mntner:         DEV-MNT\n" +
                "source:         TEST-GRS")));
    }
}
