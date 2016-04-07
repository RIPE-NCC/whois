package net.ripe.db.whois.update.handler.transformpipeline;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransformPipelineTest {

    LatinTransformer latinTransformer;
    TransformPipeline subject;

    @Before
    public void setUp() throws Exception {
        latinTransformer = new LatinTransformer();
        subject = new TransformPipeline(new PipelineTransformer[]{latinTransformer});
    }


    @Test
    public void should_convert_into_latin_plus_verify_and_assert() throws Exception {

        UpdateContext updateContext = mock(UpdateContext.class);

        Update update = mock(Update.class);
        when(update.getParagraph()).thenReturn(new Paragraph(""));
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
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

        Update transformedUpdate = subject.transform(update, updateContext);

        // Verify_interation
        verify(update).getParagraph();
        verify(update).getOperation();
        verify(update).getSubmittedObject();
        verify(update).getDeleteReasons();
        verify(updateContext).addMessage(update, UpdateMessages.valueChangedDueToLatin1Conversion("address"));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);

        // Validate result
        assertNotNull(transformedUpdate);
        RpslObject transformedPerson = transformedUpdate.getSubmittedObject();
        assertNotNull(transformedPerson);
        assertThat(transformedPerson.getAttributes().size(), is(7));
        assertThat(transformedPerson.getValueForAttribute(AttributeType.ADDRESS), is("???????? ?????,??????"));
    }


    @Test
    public void should_validate_loop_only_and_verify_access() throws Exception {
        UpdateContext updateContext = mock(UpdateContext.class);

        Update update = mock(Update.class);
        when(update.getParagraph()).thenReturn(new Paragraph(""));
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        RpslObject organisation = RpslObject.parse("organisation: FOOBAR");
        when(update.getSubmittedObject()).thenReturn(organisation);

        DummyTransformer dummyTransformer = mock(DummyTransformer.class);
        when(dummyTransformer.transform(organisation, update, updateContext)).thenReturn(organisation);

        TransformPipeline subject = new TransformPipeline(new  PipelineTransformer[]{dummyTransformer, dummyTransformer, dummyTransformer});
        subject.transform(update, updateContext);

        verify(dummyTransformer, times(3)).transform(organisation, update, updateContext);
        verify(update).getParagraph();
        verify(update).getOperation();
        verify(update).getSubmittedObject();
        verify(update).getDeleteReasons();
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);

    }
}
