package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class ShortFormatTransformerTest {


    @Mock
    private Update update;
    @Mock
    private UpdateContext updateContext;


    private ShortFormatTransformer subject;

    @BeforeEach
    public void setUp() {
        this.subject = new ShortFormatTransformer();
    }

    @Test
    public void dont_transform_no_short_format_attributes() {
        final RpslObject mntner = RpslObject.parse(
                "mntner:         MINE-MNT\n" +
                "admin-c:        AA1-TEST\n" +
                "auth:           MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword\n" +
                "mnt-by:         MINE-MNT\n" +
                "source:         TEST\n");

        final RpslObject updatedObject = subject.transform(mntner, update, updateContext, Action.MODIFY);

        assertThat(updatedObject, is(mntner));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void transform_short_format_attributes() {
        final RpslObject mntner = RpslObject.parse(
                "mntner:         MINE-MNT\n" +
                "admin-c:        AA1-TEST\n" +
                "auth:           MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword\n" +
                "mb:             MINE-MNT      # mb\n" +
                "*mb:            MINE-MNT     # star mb\n" +
                "mnt-by:         MINE-MNT\n" +
                "source:         TEST\n");

        final RpslObject updatedObject = subject.transform(mntner, update, updateContext, Action.MODIFY);

        assertThat(updatedObject.toString(), is(
                "mntner:         MINE-MNT\n" +
                "admin-c:        AA1-TEST\n" +
                "auth:           MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword\n" +
                "mnt-by:         MINE-MNT      # mb\n" +
                "mnt-by:         MINE-MNT     # star mb\n" +
                "mnt-by:         MINE-MNT\n" +
                "source:         TEST\n"));
        verify(updateContext).addMessage(update, UpdateMessages.shortFormatAttributeReplaced());
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

}
