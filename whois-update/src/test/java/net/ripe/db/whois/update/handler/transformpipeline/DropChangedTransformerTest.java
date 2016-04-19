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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DropChangedTransformerTest {

    @Mock
    Update update;
    @Mock
    UpdateContext updateContext;

    private DropChangedTransformer subject;

    private RpslObject testPerson;

    @Before
    public void setUp() throws Exception {
        update = mock(Update.class);
        updateContext = mock(UpdateContext.class);
        subject = new DropChangedTransformer();

        testPerson = new RpslObjectBuilder()
                .append(new RpslAttribute(AttributeType.PERSON, "Test Person"))
                .append(new RpslAttribute(AttributeType.ADDRESS, "Street, City, Country"))
                .append(new RpslAttribute(AttributeType.PHONE, "+30 123 411141"))
                .append(new RpslAttribute(AttributeType.FAX_NO, "+30 123 411140"))
                .append(new RpslAttribute(AttributeType.NIC_HDL, "TP1-TEST"))
                .append(new RpslAttribute(AttributeType.MNT_BY, "UPD-MNT"))
                .append(new RpslAttribute(AttributeType.SOURCE, "TEST"))
                .sort()
                .get();
    }

    @Test
    public void should_be_equal() throws Exception {
        when(update.getSubmittedObject()).thenReturn(testPerson);
        RpslObject transformedPerson = subject.transform(testPerson, update, updateContext, Action.NOOP);

        assertNotNull(transformedPerson);
        assertThat(transformedPerson, is(testPerson));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void should_drop_changed() throws Exception {
        final RpslObject testPersonChanged = new RpslObjectBuilder()
                .append(testPerson.getAttributes())
                .append(new RpslAttribute(AttributeType.CHANGED, "hostmaster@ripe.net 20160419"))
                .sort()
                .get();
        when(update.getSubmittedObject()).thenReturn(testPersonChanged);

        RpslObject transformedPerson = subject.transform(testPersonChanged, update, updateContext, Action.NOOP);

        assertNotNull(transformedPerson);
        assertThat(transformedPerson.getAttributes().size(), is(7));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }
}
