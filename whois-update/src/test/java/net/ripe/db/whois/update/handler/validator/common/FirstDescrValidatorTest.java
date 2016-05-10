package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirstDescrValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock Subject subjectObject;
    @Mock Maintainers maintainers;
    @InjectMocks private FirstDescrValidator subject;

    @Before
    public void setup() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(maintainers.getPowerMaintainers()).thenReturn(Collections.singleton(CIString.ciString("RIPE-NCC-HM-MNT")));
    }

    @Test
    public void skip_when_override() {
        when(update.isOverride()).thenReturn(true);
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verifyZeroInteractions(update);
    }

    @Test
    public void skip_when_rs_maintainer() {
        when(subjectObject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verifyZeroInteractions(update);
    }

    @Test
    public void added_first_descr() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   added\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeAdded());
    }

    @Test
    public void added_second_descr() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   first\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   first\n" +
            "descr:   second\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.descrCannotBeAdded());
    }

    @Test
    public void changed_first_descr() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   original\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   changed\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeChanged());
    }

    @Test
    public void removed_first_descr() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "descr:   original\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(
            "inetnum: 193.0/32\n" +
            "mnt-by:  RIPE-NCC-HM-MNT\n" +
            "mnt-by:  USER-MNT\n" +
            "source:  TEST"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeRemoved());
    }
}