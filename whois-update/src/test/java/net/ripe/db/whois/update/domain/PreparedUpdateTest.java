package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PreparedUpdateTest {
    @Mock Update update;
    RpslObject originalObject;
    RpslObject updatedObject;
    PreparedUpdate subject;

    @Before
    public void setUp() throws Exception {
        originalObject = RpslObject.parse("mntner: DEV-TST-MNT");
        updatedObject = RpslObject.parse("mntner: DEV-TST-MNT");

        subject = new PreparedUpdate(update, originalObject, updatedObject, Action.MODIFY);
    }

    @Test
    public void getUpdate() {
        assertThat(subject.getUpdate(), is(update));
    }

    @Test
    public void getParagraph() {
        subject.getParagraph();

        verify(update).getParagraph();
    }

    @Test
    public void getOriginalObject() {
        assertThat(subject.getReferenceObject(), is(originalObject));
    }

    @Test
    public void getUpdatedObject() {
        assertThat(subject.getUpdatedObject(), is(updatedObject));
    }

    @Test
    public void getAction() {
        assertThat(subject.getAction(), is(Action.MODIFY));
    }

    @Test
    public void getCredentials() {
        subject.getCredentials();

        verify(update).getCredentials();
    }

    @Test
    public void getType() {
        subject.getType();

        verify(update).getType();
    }

    @Test
    public void getKey() {
        assertThat(subject.getFormattedKey(), is("[mntner] DEV-TST-MNT"));
    }

    @Test
    public void newValues_create() {
        subject = new PreparedUpdate(
                update,
                null,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.CREATE);

        final Set<CIString> newValues = subject.getNewValues(AttributeType.MNT_BY);
        assertThat(newValues, contains(ciString("DEV-MNT-1"), ciString("dev-MNT-2")));
    }

    @Test
    public void newValues_modify_added() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> newValues = subject.getNewValues(AttributeType.MNT_BY);
        assertThat(newValues, contains(ciString("dev-MNT-2")));
    }

    @Test
    public void newValues_modify_added_unavailable_attribute() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> newValues = subject.getNewValues(AttributeType.MNT_IRT);
        assertThat(newValues, hasSize(0));
    }

    @Test
    public void newValues_modify_removed() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> newValues = subject.getNewValues(AttributeType.MNT_BY);
        assertThat(newValues, hasSize(0));
    }

    @Test
    public void newValues_modify_delete() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                Action.DELETE);

        final Set<CIString> newValues = subject.getNewValues(AttributeType.MNT_BY);
        assertThat(newValues, hasSize(0));
    }

    @Test
    public void differentValues_create() {
        subject = new PreparedUpdate(
                update,
                null,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.CREATE);

        final Set<CIString> differences = subject.getDifferences(AttributeType.MNT_BY);
        assertThat(differences, contains(ciString("DEV-MNT-1"), ciString("dev-MNT-2")));
    }

    @Test
    public void differentValues_modify_added() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> differences = subject.getDifferences(AttributeType.MNT_BY);
        assertThat(differences, contains(ciString("dev-MNT-2")));
    }

    @Test
    public void differentValues_modify_added_unavailable_attribute() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> differences = subject.getDifferences(AttributeType.MNT_IRT);
        assertThat(differences, hasSize(0));
    }

    @Test
    public void differentValues_modify_removed() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.MODIFY);

        final Set<CIString> differences = subject.getDifferences(AttributeType.MNT_BY);
        assertThat(differences, contains(ciString("DEV-MNT-1")));
    }

    @Test
    public void differentValues_modify_delete() {
        subject = new PreparedUpdate(
                update,
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                RpslObject.parse("" +
                        "inetnum:  10.0.0.0\n" +
                        "mnt-by:   DEV-MNT-1\n" +
                        "mnt-by:   DEV-MNT-2\n"),
                Action.DELETE);

        final Set<CIString> differences = subject.getDifferences(AttributeType.MNT_BY);
        assertThat(differences, contains(ciString("DEV-MNT-1"), ciString("DEV-MNT-2")));
    }
}
