package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
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

    @Test
    public void should_validate_loop_only_and_verify_access() throws Exception {
        UpdateContext updateContext = mock(UpdateContext.class);

        Update update = mock(Update.class);
        when(update.getParagraph()).thenReturn(new Paragraph(""));
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        RpslObject organisation = RpslObject.parse("organisation: FOOBAR");
        when(update.getSubmittedObject()).thenReturn(organisation);

        DummyTransformer dummyTransformer = mock(DummyTransformer.class);
        when(dummyTransformer.transform(organisation, update, updateContext, Action.NOOP)).thenReturn(organisation);

        TransformPipeline subject = new TransformPipeline(new Transformer[]{dummyTransformer, dummyTransformer, dummyTransformer});
        subject.transform(organisation, update, updateContext, Action.NOOP);

        verify(dummyTransformer, times(3)).transform(organisation, update, updateContext, Action.NOOP);
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);

    }
}
