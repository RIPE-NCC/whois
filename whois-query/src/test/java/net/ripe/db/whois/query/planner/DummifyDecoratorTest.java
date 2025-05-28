package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.executor.decorators.DummifyDecorator;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DummifyDecoratorTest {
    @Mock Dummifier dummifier;
    @Mock SourceContext sourceContext;
    @InjectMocks DummifyDecorator subject;
    @Mock Query query;

    @Test
    public void apply_message_object() {
        final ResponseObject message = new MessageObject("test");
        Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(message)).iterator();
        assertThat(response.next(), is(message));
        assertThat(response.hasNext(), is(false));
    }

    @Test
    public void apply_rpsl_object_not_allowed() {
        when(sourceContext.isDummificationRequired()).thenReturn(true);
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(false);

        final ResponseObject rpslObject = RpslObject.parse("person: Test person\nnic-hdl: TEST-PB");
        Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(rpslObject)).iterator();
        assertThat(response.hasNext(), is(false));
    }

    @Test
    public void apply_rpsl_object_dummify() {
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST-GRS"));
        when(sourceContext.isDummificationRequired()).thenReturn(true);

        final RpslObject dummifiedObject = RpslObject.parse("mntner: DEV-MNT\nsource: TEST".getBytes());
        when(dummifier.isAllowed(anyInt(), any(RpslObject.class))).thenReturn(true);
        when(dummifier.dummify(anyInt(), any(RpslObject.class))).thenReturn(dummifiedObject);

        final ResponseObject rpslObject = RpslObject.parse("mntner: DEV-MNT\nadmin-c: TEST-PN\nsource: TEST".getBytes());
        final Iterator<? extends ResponseObject> response = subject.decorate(query, Arrays.asList(rpslObject)).iterator();

        assertThat(response.next(), equalTo(RpslObject.parse("mntner:         DEV-MNT\nsource:         TEST-GRS")));
    }
}
