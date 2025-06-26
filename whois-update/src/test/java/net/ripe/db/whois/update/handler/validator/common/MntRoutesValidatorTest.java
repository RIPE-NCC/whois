package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.MessageWithAttribute;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MntRoutesValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @InjectMocks MntRoutesValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.INET6NUM, ObjectType.INETNUM, ObjectType.ROUTE, ObjectType.ROUTE6));
    }

    @Test
    public void validate_single_valid_any() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "source:      TEST\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_single_valid_range() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}\n" +
                "source:      TEST\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_single_invalid() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT {any, 20.13.0.0/16^+}\n" +
                "source:      TEST\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        final RpslAttribute mnt_routes = rpslObject.findAttribute(AttributeType.MNT_ROUTES);

        verify(updateContext).addMessage(update, mnt_routes, new MessageWithAttribute(Messages.Type.ERROR, mnt_routes, "Syntax error in EXACT-MR-MNT {any, 20.13.0.0/16^+} (ANY can only occur as a single value)"));
    }

    @Test
    public void validate_multiple_valid_any() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "source:      TEST\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_lots_valid_any() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "source:      TEST\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_multiple_valid_range() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}\n" +
                "mnt-routes:  EXACT-MR-MNT {20.13.0.0/16}\n" +
                "source:      TEST\n"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_multiple_invalid_range() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "source:      TEST\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        final List<RpslAttribute> attributes = rpslObject.findAttributes(AttributeType.MNT_ROUTES);
        verify(updateContext).addMessage(update, attributes.get(0), new MessageWithAttribute(Messages.Type.ERROR, attributes.get(0), "Syntax error in EXACT-MR-MNT {20.13.0.0/16^+} (ANY can only occur as a single value)"));
        verify(updateContext).addMessage(update, attributes.get(1), new MessageWithAttribute(Messages.Type.ERROR,attributes.get(1),  "Syntax error in EXACT-MR-MNT any (ANY can only occur as a single value)"));
    }

    @Test
    public void validate_multiple_different_maintainers() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT1 {20.13.0.0/16^+}\n" +
                "mnt-routes:  EXACT-MR-MNT2 any\n" +
                "source:      TEST\n");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }
}
