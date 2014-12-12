package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AckTest {
    private List<UpdateResult> updateResults;
    private List<Paragraph> ignoredParagraphs;

    @Before
    public void setUp() throws Exception {
        updateResults = Lists.newArrayList();
        ignoredParagraphs = Lists.newArrayList();
    }

    @Test
    public void getUpdateStatus_empty() {
        final Ack subject = new Ack(updateResults, ignoredParagraphs);

        assertThat(subject.getSucceededUpdates(), hasSize(0));
        assertThat(subject.getFailedUpdates(), hasSize(0));
        assertThat(subject.getUpdateStatus(), is(UpdateStatus.FAILED));
        assertThat(subject.getNrFound(), is(0));
    }

    @Test
    public void getUpdateStatus_success() {
        final RpslObject rpslObject = RpslObject.parse("mntner: DEV-MNT");
        final UpdateResult updateResult1 = new UpdateResult(rpslObject, rpslObject, Action.DELETE, UpdateStatus.SUCCESS, new ObjectMessages(), 0, false);

        final UpdateResult updateResult2 = new UpdateResult(rpslObject, rpslObject, Action.MODIFY, UpdateStatus.SUCCESS, new ObjectMessages(), 0, false);

        updateResults.add(updateResult1);
        updateResults.add(updateResult2);

        final Ack subject = new Ack(updateResults, ignoredParagraphs);

        assertThat(subject.getSucceededUpdates(), contains(updateResult1, updateResult2));
        assertThat(subject.getFailedUpdates(), hasSize(0));
        assertThat(subject.getUpdateStatus(), is(UpdateStatus.SUCCESS));
        assertThat(subject.getIgnoredParagraphs(), hasSize(0));
        assertThat(subject.getNrFound(), is(2));
        assertThat(subject.getNrProcessedsuccessfully(), is(2));
        assertThat(subject.getNrDelete(), is(1));
        assertThat(subject.getNrProcessedErrrors(), is(0));
        assertThat(subject.getNrDeleteErrors(), is(0));
    }

    @Test
    public void getUpdateStatus_failed() {
        final RpslObject rpslObject = RpslObject.parse("mntner: DEV-MNT");
        final UpdateResult updateResult1 = new UpdateResult(rpslObject, rpslObject, Action.DELETE, UpdateStatus.FAILED, new ObjectMessages(), 0, false);

        final UpdateResult updateResult2 = new UpdateResult(rpslObject, rpslObject, Action.MODIFY, UpdateStatus.FAILED, new ObjectMessages(), 0, false);
        updateResults.add(updateResult1);
        updateResults.add(updateResult2);

        final Ack subject = new Ack(updateResults, ignoredParagraphs);

        assertThat(subject.getSucceededUpdates(), hasSize(0));
        assertThat(subject.getFailedUpdates(), contains(updateResult1, updateResult2));
        assertThat(subject.getIgnoredParagraphs(), hasSize(0));
        assertThat(subject.getUpdateStatus(), is(UpdateStatus.FAILED));
        assertThat(subject.getNrFound(), is(2));
        assertThat(subject.getNrProcessedsuccessfully(), is(0));
        assertThat(subject.getNrDelete(), is(0));
        assertThat(subject.getNrProcessedErrrors(), is(2));
        assertThat(subject.getNrDeleteErrors(), is(1));
    }
}
