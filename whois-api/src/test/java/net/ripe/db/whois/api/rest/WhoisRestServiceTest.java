package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WhoisRestServiceTest {

    @Mock UpdateContext updateContext;
    @InjectMocks WhoisRestService subject;
    Source source;

    @BeforeEach
    public void setup() {
        source = Source.slave("TEST");
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
