package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class UpdateResultTest {
    @Mock Update update;

    @Test
    public void string_representation() {
        final RpslObject updatedObject = RpslObject.parse("mntner: DEV-ROOT-MNT\nsource: RIPE #Filtered\ninvalid: invalid\nmnt-by: MNT2");

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
