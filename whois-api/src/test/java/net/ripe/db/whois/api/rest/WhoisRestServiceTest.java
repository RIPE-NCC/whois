package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisRestServiceTest {

    @Mock HttpServletRequest request;
    @Mock WhoisResources whoisResources;
    @Mock UpdateContext updateContext;
    @Mock SourceContext sourceContext;
    @InjectMocks WhoisRestService subject;
    Source source;

    @Before
    public void setup() {
        source = Source.slave("TEST");
        when(sourceContext.getCurrentSource()).thenReturn(source);
        when(sourceContext.getAllSourceNames()).thenReturn(CIString.ciSet("TEST", "TEST-GRS"));
        when(whoisResources.getErrorMessages()).thenReturn(Lists.newArrayList(new ErrorMessage(new Message(Messages.Type.ERROR, "Disallowed search flag '%s'"))));
        when(request.getRequestURL()).thenReturn(new StringBuffer());
    }

    @Test
    public void dryRun_null() {
        subject.setDryRun(updateContext, null);

        verify(updateContext, never()).dryRun();
    }

    @Test
    public void dryRun_false() {
        subject.setDryRun(updateContext, "fAlsE");

        verify(updateContext, never()).dryRun();
    }

    @Test
    public void dryRun_emptyString() {
        subject.setDryRun(updateContext, "");

        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun_true() {
        subject.setDryRun(updateContext, "tRuE");

        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun_whatever() {
        subject.setDryRun(updateContext, "whatever");

        verify(updateContext, never()).dryRun();
    }
}
