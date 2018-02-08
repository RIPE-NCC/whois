package net.ripe.db.whois.update.handler.validator.common;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExcludedEmailValidatorTest {

    @Mock
    private PreparedUpdate preparedUpdate;
    @Mock
    private UpdateContext updateContext;
    @Mock
    private Subject subject;

    private ExcludedEmailValidator excludedEmailValidator;

    @Before
    public void setup() {
        this.excludedEmailValidator = new ExcludedEmailValidator(Lists.newArrayList("ripe-dbm@ripe.net"));

        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.hasPrincipal(Matchers.eq(Principal.RS_MAINTAINER))).thenReturn(false);
        when(subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
    }

    @Test
    public void validate_allowed_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  user@host.org\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void validate_excluded_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  ripe-dbm@ripe.net\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void validate_excluded_address_is_case_insensitive() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  RIPE-DbM@ripe.net\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void validate_excluded_name_and_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  RIPE DBM <ripe-dbm@ripe.net>\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }
}
