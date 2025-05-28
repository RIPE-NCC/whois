package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrgNameFormatValidatorTest {

    @Mock
    private PreparedUpdate update;
    @Mock
    private UpdateContext updateContext;

    private OrgNameFormatValidator subject;

    @BeforeEach
    public void setup() {
        this.subject = new OrgNameFormatValidator();
    }

    @Test
    public void spaces() {
        ok("");
        ok("a");
        ok(" a");
        ok("a ");
        ok("a b c");
        ok("a b c");
        ok("  a b c  ");
        ok("  a b c");
        ok(" a b c  ");
        error("a  b c");
        error("a b  c");
        error("a  b  c");
    }

    @Test
    public void tabs() {
        ok("\ta\t");
        error("a\t\tb");
        error("a \tb");
        error("a\tb");
    }

    @Test
    public void newline() {
        error("a\n b");
        error("a\n+b");
        error("a\n\tb");
        error("a\n b\n c");
    }

    @Test
    public void comments() {
        ok("a      # comment");
        ok("a b    # comment");
        ok("a b\t# comment");
        ok("\ta b\t# comment");
        ok("   a b   # comment");
        error("a b    # comment\n c");
        error("a b    # comment\n+c");
        error("a b    # comment\n\tc");
    }


    // helper methods

    private void error(final String orgName) {
        when(update.getUpdatedObject()).thenReturn(createOrgObject(orgName));

       subject.validate(update, updateContext);

        verifyError();
        reset();
    }

    private void ok(final String orgName) {
        when(update.getUpdatedObject()).thenReturn(createOrgObject(orgName));

       subject.validate(update, updateContext);

        verifyOk();
        reset();
    }

    private RpslObject createOrgObject(final String orgName) {
        return RpslObject.parse(String.format("organisation: AUTO-1\norg-name: %s\nsource: TEST", orgName));
    }

    private void verifyError() {
        verify(updateContext).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    private void verifyOk() {
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    private void reset() {
        Mockito.reset(updateContext);

    }

}
