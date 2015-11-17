package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateResultTest {
    @Mock Update update;

    @Test
    public void string_representation() {
        final RpslObject updatedObject = RpslObject.parse("mntner: DEV-ROOT-MNT\nsource: RIPE #Filtered\ninvalid: invalid\nmnt-by: MNT2");
        when(update.getType()).thenReturn(ObjectType.MNTNER);
        when(update.getSubmittedObject()).thenReturn(updatedObject);

        final ObjectMessages objectMessages = new ObjectMessages();
        objectMessages.addMessage(UpdateMessages.filteredNotAllowed());
        objectMessages.addMessage(updatedObject.getAttributes().get(0), UpdateMessages.objectInUse(updatedObject));
        objectMessages.addMessage(updatedObject.getAttributes().get(2), ValidationMessages.unknownAttribute("invalid"));
        objectMessages.addMessage(updatedObject.getAttributes().get(3), UpdateMessages.referencedObjectMissingAttribute(ObjectType.MNTNER, "MNT2", AttributeType.DESCR));

        final UpdateResult subject = new UpdateResult(null, updatedObject, Action.MODIFY, UpdateStatus.FAILED, objectMessages, 0, false);

        final String string = subject.toString();
        assertThat(string, is("" +
                "mntner:         DEV-ROOT-MNT\n" +
                "***Error:   Object [mntner] DEV-ROOT-MNT is referenced from other objects\n" +
                "source:         RIPE #Filtered\n" +
                "invalid:        invalid\n" +
                "***Error:   \"invalid\" is not a known RPSL attribute\n" +
                "mnt-by:         MNT2\n" +
                "***Warning: Referenced mntner object MNT2 is missing mandatory attribute\n" +
                "            \"descr:\"\n" +
                "\n" +
                "***Error:   Cannot submit filtered whois output for updates\n" +
                "\n"));
    }
}
