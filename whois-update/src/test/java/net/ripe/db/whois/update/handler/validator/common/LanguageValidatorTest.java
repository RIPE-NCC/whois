package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dao.LanguageCodeRepository;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LanguageValidatorTest {
    @Mock
    private LanguageCodeRepository repository;
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;

    @InjectMocks private LanguageValidator subject;

    @Test
    public void valid_language() {
        when(repository.getLanguageCodes()).thenReturn(ciSet("DK", "UK"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\nlanguage:DK"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void invalid_language() {
        when(repository.getLanguageCodes()).thenReturn(ciSet("DK", "UK"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\nlanguage:AB"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.languageNotRecognised("AB"));
    }
}
