package net.ripe.db.whois.update.handler.validator.route;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.ReservedResources;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BogonValidatorTest {

    @Mock
    private PreparedUpdate update;
    @Mock
    private UpdateContext updateContext;

    private BogonValidator subject;

    @BeforeEach
    public void setUp() {
        ReservedResources reservedResources = new ReservedResources("0,64496-131071,4200000000-4294967295","","2001:2::/48", "192.0.2.0/24");
        subject = new BogonValidator(reservedResources);
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
