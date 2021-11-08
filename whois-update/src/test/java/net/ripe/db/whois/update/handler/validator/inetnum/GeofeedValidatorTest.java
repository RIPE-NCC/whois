package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeofeedValidatorTest {

    @Mock
    private UpdateContext updateContext;
    @Mock
    private PreparedUpdate update;
    @InjectMocks
    private GeofeedValidator subject;

    @Test
    public void updated_object_doesnt_contain_geofeed_attribute() {
        final RpslObject object = RpslObject.parse("inetnum: 1.1.1.1");
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verifyNoInteractions(updateContext);
    }

    @Test
    public void updated_inetnum_contains_valid_geofeed_attribute() {
        final RpslObject object = RpslObject.parse("inetnum: 1.1/16\ngeofeed: https://example.com");
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verifyNoInteractions(updateContext);
    }

    @Test
    public void updated_inetnum_contains_invalid_geofeed_attribute() {
        final RpslObject object = RpslObject.parse("inetnum: 1.1.1.1\ngeofeed: https://example.com");
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.geofeedTooSpecific(24));
    }

    @Test
    public void updated_inet6num_contains_valid_geofeed_attribute() {
        final RpslObject object = RpslObject.parse("inet6num: 2001:67c::/32\ngeofeed: https://example.com");
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verifyNoInteractions(updateContext);
    }

    @Test
    public void updated_inet6num_contains_invalid_geofeed_attribute() {
        final RpslObject object = RpslObject.parse("inet6num: 2001:67c:2e8::/48\ngeofeed: https://example.com");
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.geofeedTooSpecific(32));
    }
}
