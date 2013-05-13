package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dao.CountryCodeRepository;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CountryValidatorTest {
    @Mock private CountryCodeRepository repository;
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;

    @InjectMocks private CountryValidator subject;

    @Test
    public void valid_country() {
        when(repository.getCountryCodes()).thenReturn(CIString.ciSet("DK", "UK"));
        final RpslObject rpslObject = RpslObject.parse("inetnum: 193.0/32\ncountry:DK");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void invalid_country() {
        when(repository.getCountryCodes()).thenReturn(CIString.ciSet("DK", "UK"));
        final RpslObject rpslObject = RpslObject.parse("inetnum: 193.0/32\ncountry:AB");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.COUNTRY), UpdateMessages.countryNotRecognised(ciString("AB")));
    }
}
