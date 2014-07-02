package net.ripe.db.whois.internal.api.rnd;


import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class VersionsResponseHandlerTest {
    VersionsResponseHandler subject;

    @Before
    public void setup() {
        subject = new VersionsResponseHandler();
    }

    @Test
    public void getApi() {
        assertThat(subject.getApi(), is("INTERNAL_API"));
    }

    @Test
    public void handleVersionResponseObject() {
        final VersionDateTime versionDateTime = new VersionDateTime(new LocalDateTime());
        final VersionResponseObject versionResponseObject = new VersionResponseObject(versionDateTime, Operation.UPDATE, ObjectType.PERSON, "AA1-TEST");
        subject.handle(versionResponseObject);

        assertThat(subject.getErrors(), hasSize(0));
        assertThat(subject.getVersions(), hasSize(1));
        assertThat(subject.getVersions().get(0), is(versionResponseObject));
    }

    @Test
    public void handleWarningMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.uselessIpFlagPassed());

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(1));
        assertThat(subject.getErrors().get(0), is(messageObject.getMessage()));
        assertThat(subject.getVersions(), hasSize(0));
    }

    @Test
    public void handleInfoMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.primaryKeysOnlyNotice());

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(0));
        assertThat(subject.getVersions(), hasSize(0));
    }

    @Test
    public void handleErrorMessage() {
        final MessageObject messageObject = new MessageObject(QueryMessages.attributeNotSearchable("str"));

        subject.handle(messageObject);

        assertThat(subject.getErrors(), hasSize(1));
        assertThat(subject.getErrors().get(0), is(messageObject.getMessage()));
        assertThat(subject.getVersions(), hasSize(0));
    }
}