package net.ripe.db.whois.update.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OverrideOptionsTest {
    @Mock Update update;
    @Mock UpdateContext updateContext;

    @Test
    public void no_override_specified() {
        when(update.getCredentials()).thenReturn(new Credentials());

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void override_specified_no_remarks() {
        useCredentialWithRemarks("");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void override_specified_remarks_without_options() {
        useCredentialWithRemarks("remarks");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(false));
        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void override_specified_single_valid_option() {
        useCredentialWithRemarks("{oid=1234}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(true));
        assertThat(overrideOptions.getObjectId(), is(1234));

        assertThat(overrideOptions.isNotifyOverride(), is(false));

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void override_specified_multiple_valid_options() {
        useCredentialWithRemarks("{oid=1234,notify=false}");

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);

        assertThat(overrideOptions.isObjectIdOverride(), is(true));
        assertThat(overrideOptions.getObjectId(), is(1234));

        assertThat(overrideOptions.isNotifyOverride(), is(true));
        assertThat(overrideOptions.isNotify(), is(false));

        verifyZeroInteractions(updateContext);
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

    private void useCredentialWithRemarks(final String remarks) {
        when(update.getCredentials()).thenReturn(new Credentials(Collections.<Credential>singleton(OverrideCredential.parse("user,pw," + remarks))));
    }
}
