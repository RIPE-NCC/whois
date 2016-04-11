package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class LatinTransformerTest {

    @Mock
    Update update;
    @Mock
    UpdateContext updateContext;

    private LatinTransformer subject;

    @Before
    public void setUp() throws Exception {
        update = mock(Update.class);
        updateContext = mock(UpdateContext.class);
        subject = new LatinTransformer();
    }

    @Test
    public void should_be_already_latin() throws Exception {

        final RpslObject person = RpslObject.parse("" +
                "person:     Test Person\n" +
                "address:    street\n" +
                "phone:      +30 123 411141\n" +
                "fax-no:     +30 123 411140\n" +
                "nic-hdl:    TP1-TEST\n" +
                "mnt-by:     UPD-MNT\n" +
                "source:     TEST\n");
        when(update.getSubmittedObject()).thenReturn(person);
        RpslObject transformedPerson = subject.transform(person, update, updateContext, Action.NOOP);

        assertNotNull(transformedPerson);
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void should_convert_into_latin() throws Exception {
        final RpslObject person = new RpslObjectBuilder()
                .append(new RpslAttribute(AttributeType.PERSON, "Test Person"))
                .append(new RpslAttribute(AttributeType.ADDRESS, "Тверская улица,москва"))
                .append(new RpslAttribute(AttributeType.PHONE, "+30 123 411141"))
                .append(new RpslAttribute(AttributeType.FAX_NO, "+30 123 411140"))
                .append(new RpslAttribute(AttributeType.NIC_HDL, "TP1-TEST"))
                .append(new RpslAttribute(AttributeType.MNT_BY, "UPD-MNT"))
                .append(new RpslAttribute(AttributeType.SOURCE, "TEST"))
                .get();
        when(update.getSubmittedObject()).thenReturn(person);

        RpslObject transformedPerson = subject.transform(person, update, updateContext, Action.NOOP);

        assertNotNull(transformedPerson);
        assertThat(transformedPerson.getAttributes().size(), is(7));
        assertThat(transformedPerson.getValueForAttribute(AttributeType.ADDRESS), is("???????? ?????,??????"));
        verify(updateContext).addMessage(update, UpdateMessages.valueChangedDueToLatin1Conversion("address"));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }
}
