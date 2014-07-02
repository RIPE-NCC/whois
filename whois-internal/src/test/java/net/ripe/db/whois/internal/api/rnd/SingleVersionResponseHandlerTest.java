package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.planner.RpslAttributes;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SingleVersionResponseHandlerTest {
    @Mock
    RpslAttributes otherResponseObject;

    SingleVersionResponseHandler subject;


    @Before
    public void setup() {
        subject = new SingleVersionResponseHandler();
    }

    @Test
    public void getApi() {
        assertThat(subject.getApi(), is("INTERNAL_API"));
    }

    @Test
    public void handleRpslObjectWithTimestamp() {
        final RpslObject object = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "as-name: test-as\n" +
                "mnt-by: TEST-MNT");
        final VersionDateTime versionDateTime = new VersionDateTime(new LocalDateTime());
        final RpslObjectWithTimestamp rpslObjectWithTimestamp = new RpslObjectWithTimestamp(object, 0, versionDateTime);

        subject.handle(rpslObjectWithTimestamp);

        assertThat(subject.getErrors(), hasSize(0));
        assertThat(subject.getRpslObject(), is(object));
        assertThat(subject.getVersionDateTime(), is(versionDateTime));
    }

    @Test
    public void handleInfoMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.noPersonal());

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(0));
        assertThat(subject.getRpslObject(), is(nullValue()));
        assertThat(subject.getVersionDateTime(), is(nullValue()));
    }

    @Test
    public void handleErrorMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.invalidSearchKey());

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(1));
        assertThat(subject.getErrors().get(0), is(messageObject.getMessage()));
        assertThat(subject.getRpslObject(), is(nullValue()));
        assertThat(subject.getVersionDateTime(), is(nullValue()));
    }

    @Test
    public void handleWarningMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.uselessIpFlagPassed());

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(1));
        assertThat(subject.getErrors().get(0), is(messageObject.getMessage()));
        assertThat(subject.getRpslObject(), is(nullValue()));
        assertThat(subject.getVersionDateTime(), is(nullValue()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleNull() {
        subject.handle(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleOtherResponseObject() {
        subject.handle(otherResponseObject);
    }
}