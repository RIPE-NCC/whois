package net.ripe.db.whois.api.rest.domain;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ErrorMessageTest {

    @Test
    public void to_string_no_arguments() {
        assertThat(new ErrorMessage(new Message(Messages.Type.INFO, "message")).toString(), is("message"));
    }

    @Test
    public void to_string_with_single_argument() {
        assertThat(new ErrorMessage(new Message(Messages.Type.INFO, "message with %s", "argument")).toString(), is("message with argument"));
    }

    @Test
    public void to_string_with_multiple_arguments() {
        assertThat(new ErrorMessage(new Message(Messages.Type.INFO, "message with %s %s", "argument", "ending")).toString(), is("message with argument ending"));
    }

    @Test
    public void to_string_default_constructor() {
        assertThat(new ErrorMessage().toString(), is(nullValue()));
    }
}
