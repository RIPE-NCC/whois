package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.common.Credentials.OverrideCredential;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OverrideOptionsTest {
    @Mock Update update;
    @Mock UpdateContext updateContext;

    @Test
    public void no_override_specified() {
        when(update.getCredentials()).thenReturn(new Credentials());

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_specified_no_remarks() {
        useCredentialWithRemarks("");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_specified_remarks_without_options() {
        useCredentialWithRemarks("remarks");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_specified_single_valid_option() {
        useCredentialWithRemarks("{oid=1234}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(true));
        assertThat(overrideOptions.getObjectId(), is(1234));

        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_specified_multiple_valid_options() {
        useCredentialWithRemarks("{oid=1234,notify=false}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(true));
        assertThat(overrideOptions.getObjectId(), is(1234));

        assertThat(overrideOptions.isNotifyOverride(), is(true));
        assertThat(overrideOptions.isNotify(), is(false));

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_specified_option_without_value() {
        useCredentialWithRemarks("{oid}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verify(updateContext).addMessage(update, UpdateMessages.overrideOptionInvalid("oid"));
    }

    @Test
    public void override_specified_option_invalid_value() {
        useCredentialWithRemarks("{oid=abcd}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verify(updateContext).addMessage(update, UpdateMessages.overrideOptionInvalid("oid=abcd"));
    }

    @Test
    public void override_specified_option_unknown() {
        useCredentialWithRemarks("{unknown=123}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verify(updateContext).addMessage(update, UpdateMessages.overrideOptionInvalid("unknown=123"));
    }

    @Test
    public void override_last_modified_true() {
        useCredentialWithRemarks("{skip-last-modified=true}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isSkipLastModified(), is(true));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_last_modified_false() {
        useCredentialWithRemarks("{skip-last-modified=false}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isSkipLastModified(), is(false));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_last_modified_unknown() {
        useCredentialWithRemarks("{skip-last-modified=unknown}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        //!!!!!!!!!!!!!! check TO DO in OverrideOptions
        assertThat(overrideOptions.isSkipLastModified(), is(false));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_update_on_noop_true() {
        useCredentialWithRemarks("{update-on-noop=true}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isUpdateOnNoop(), is(true));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_update_on_noop_false() {
        useCredentialWithRemarks("{update-on-noop=false}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isUpdateOnNoop(), is(false));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_update_on_noop_unknown() {
        useCredentialWithRemarks("{update-on-noop=unknown}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isUpdateOnNoop(), is(false));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void override_last_modified_default() {
        useCredentialWithRemarks("");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        //!!!!!!!!!!!!!! check TO DO in OverrideOptions
        assertThat(overrideOptions.isSkipLastModified(), is(false));
        verifyNoMoreInteractions(updateContext);
    }

    private void useCredentialWithRemarks(final String remarks) {
        when(update.getCredentials()).thenReturn(new Credentials(Collections.<Credential>singleton(OverrideCredential.parse("user,pw," + remarks))));
    }
}
