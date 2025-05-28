package net.ripe.db.whois.update.handler.validator.common;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExcludedEmailValidatorTest {

    @Mock
    private PreparedUpdate preparedUpdate;
    @Mock
    private UpdateContext updateContext;
    @Mock
    private Subject subject;

    private ExcludedEmailValidator excludedEmailValidator;

    @BeforeEach
    public void setup() {
        this.excludedEmailValidator = new ExcludedEmailValidator(Lists.newArrayList("ripe-dbm@ripe.net"));

        when(updateContext.getSubject(preparedUpdate)).thenReturn(subject);
        when(subject.hasPrincipal(ArgumentMatchers.eq(Principal.RS_MAINTAINER))).thenReturn(false);
        when(subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
    }

    @Test
    public void validate_allowed_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  user@host.org\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void validate_excluded_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  ripe-dbm@ripe.net\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void validate_excluded_address_is_case_insensitive() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  RIPE-DbM@ripe.net\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void validate_excluded_name_and_address() {
        when(preparedUpdate.getUpdatedObject()).thenReturn(RpslObject.parse("mntner:    OWNER-MNT\nupd-to:  RIPE DBM <ripe-dbm@ripe.net>\nsource:    TEST"));

        excludedEmailValidator.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}
