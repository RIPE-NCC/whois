package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
        assertThat(subject.getTypes(), contains(ObjectType.AUT_NUM, ObjectType.INET6NUM, ObjectType.INETNUM, ObjectType.ROUTE, ObjectType.ROUTE6));
    }

    @Test
    public void validate_single_valid_any() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT any\n" +
                "source:      TEST\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_single_valid_range() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "route:       20.13.0.0/16\n" +
                "origin:      AS3000\n" +
                "mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}\n" +
                "source:      TEST\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
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

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.MNT_ROUTES), new Message(Messages.Type.ERROR, "Syntax error in EXACT-MR-MNT {any, 20.13.0.0/16^+} (ANY can only occur as a single value)"));
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

        verifyZeroInteractions(updateContext);
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

        verifyZeroInteractions(updateContext);
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

        verifyZeroInteractions(updateContext);
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
        verify(updateContext).addMessage(update, attributes.get(0), new Message(Messages.Type.ERROR, "Syntax error in EXACT-MR-MNT {20.13.0.0/16^+} (ANY can only occur as a single value)"));
        verify(updateContext).addMessage(update, attributes.get(1), new Message(Messages.Type.ERROR, "Syntax error in EXACT-MR-MNT any (ANY can only occur as a single value)"));
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

        verifyZeroInteractions(updateContext);
    }
}
