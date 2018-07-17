package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservedWordValidatorTest {

    @Mock
    private PreparedUpdate update;
    @Mock
    private UpdateContext updateContext;

    private ReservedWordValidator subject = new ReservedWordValidator();

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void not_reserved_word() {
        mockUpdate("mntner: OWNER-MNT\nsource: TEST");

        subject.validate(update, updateContext);

        verifyOk();
    }

    @Test
    public void reserved_word_as_any_not_ok() {
        mockUpdate("as-set: AS-ANy\nsource: TEST");

        subject.validate(update, updateContext);

        verifyReservedName("as-any");
    }

    @Test
    public void reserved_prefix_as_not_ok() {
        mockUpdate("mntner: AS-TEST\nsource: TEST");

        subject.validate(update, updateContext);

        verifyReservedPrefixUsed("as-", ObjectType.AS_SET);
    }

    @Test
    public void reserved_prefix_org_not_ok() {
        mockUpdate("mntner: ORG-TEST\nsource: TEST");

        subject.validate(update, updateContext);

        verifyReservedPrefixUsed("org-", ObjectType.ORGANISATION);
    }

    @Test
    public void reserved_prefix_as_ok() {
        mockUpdate("as-set: AS-TEST\nsource: TEST");

        subject.validate(update, updateContext);

        verifyOk();
    }

    // helper methods

    private void mockUpdate(final String objectString) {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse(objectString));
    }

    private void verifyReservedName(final CharSequence name) {
        verify(updateContext).addMessage(update, UpdateMessages.reservedNameUsed(name));
        verifyNoMoreInteractions(updateContext);
    }

    private void verifyReservedPrefixUsed(final CharSequence name, final ObjectType type) {
        verify(updateContext).addMessage(update, UpdateMessages.reservedPrefixUsed(name, type));
        verifyNoMoreInteractions(updateContext);
    }

    private void verifyOk() {
        verifyNoMoreInteractions(updateContext);
    }
}
