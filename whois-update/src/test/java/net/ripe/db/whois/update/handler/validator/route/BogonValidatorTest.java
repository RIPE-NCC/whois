package net.ripe.db.whois.update.handler.validator.route;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BogonValidatorTest {

    @Mock
    private PreparedUpdate update;
    @Mock
    private UpdateContext updateContext;

    private BogonValidator subject;

    @Before
    public void setUp() {
        subject = new BogonValidator("2001:2::/48", "192.0.2.0/24");
    }

    @Test
    public void ipv6_exact_match_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6: 2001:2::/48\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.bogonPrefixNotAllowed("2001:2::/48"));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void ipv4_exact_match_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 192.0.2.0/24\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.bogonPrefixNotAllowed("192.0.2.0/24"));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void ipv6_more_specific_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6: 2001:2:0:1::/64\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.bogonPrefixNotAllowed("2001:2:0:1::/64"));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void ipv4_more_specific_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 192.0.2.1/32\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.bogonPrefixNotAllowed("192.0.2.1/32"));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void ipv6_not_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6: 2002:1:2:3::/64\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void ipv4_not_bogon() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.201.1.0/24\norigin: AS3333\nsource: TEST"));

        subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }
}
