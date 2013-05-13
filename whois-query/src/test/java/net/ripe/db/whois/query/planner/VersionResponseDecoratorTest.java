package net.ripe.db.whois.query.planner;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VersionResponseDecoratorTest {

    @Mock SourceContext sourceContext;
    @InjectMocks VersionResponseDecorator subject;

    @Before
    public void setup() {
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
    }

    @Test
    public void with_RpslObject() {
        final RpslObject mntner = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PW $1$1ZJHr26K\nnotify: mntner@email.com");
        final List<ResponseObject> responseObjects = Lists.newArrayList(mntner, new MessageObject("TEST-MESSAGE"));

        final Iterable<? extends ResponseObject> result = subject.getResponse(responseObjects);
        for (final ResponseObject object : result) {
            if (object instanceof RpslObject) {
                RpslObject casted = (RpslObject) object;
                assertThat(casted.findAttributes(AttributeType.NOTIFY), empty());
                assertThat(casted.findAttributes(AttributeType.AUTH), containsInAnyOrder(new RpslAttribute("auth", "MD5-PW #Filtered")));
            }
        }
        assertThat(Iterables.size(result), is(2));
    }

    @Test
    public void withoutRpslObject() {
        final MessageObject messageObject1 = new MessageObject("TEST-MESSAGE-BAR");
        final MessageObject messageObject2 = new MessageObject("TEST-MESSAGE-FOO");
        final List<? extends ResponseObject> responseObjects = Lists.newArrayList(messageObject2, messageObject1);

        final Iterable<? extends ResponseObject> result = subject.getResponse(responseObjects);

        assertThat(Iterables.contains(result, messageObject1), is(true));
        assertThat(Iterables.contains(result, messageObject2), is(true));
        assertThat(Iterables.size(result), is(2));
    }

    @Test
    public void noResults() {
        final Iterable<? extends ResponseObject> result = subject.getResponse(Lists.<ResponseObject>newArrayList());

        final ResponseObject errorMessage = Iterables.getFirst(result, null);
        assertThat(new String(errorMessage.toByteArray()), is(QueryMessages.noResults("TEST").toString()));
    }
}