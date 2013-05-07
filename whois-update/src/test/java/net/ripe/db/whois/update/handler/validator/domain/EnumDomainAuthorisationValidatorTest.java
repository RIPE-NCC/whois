package net.ripe.db.whois.update.handler.validator.domain;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnumDomainAuthorisationValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject authSubject;
    @InjectMocks EnumDomainAuthorisationValidator subject;

    @Before
    public void setUp() throws Exception {
        when(updateContext.getSubject(update)).thenReturn(authSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.DOMAIN));
    }

    @Test
    public void validate_non_enum() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "domain: 200.193.193.in-addr.arpa"));

        subject.validate(update, updateContext);
        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("domain: 2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));

        when(authSubject.hasPrincipal(Principal.ENUM_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);
        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_enum_no_enum_maintainer() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "domain: 2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));

        when(authSubject.hasPrincipal(Principal.ENUM_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);

        verify(authSubject).hasPrincipal(Principal.ENUM_MAINTAINER);
        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForEnumDomain());
    }

    @Test
    public void validate_enum_with_enum_maintainer() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "domain: 2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));

        when(authSubject.hasPrincipal(Principal.ENUM_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(authSubject).hasPrincipal(Principal.ENUM_MAINTAINER);
        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForEnumDomain());
    }
}
