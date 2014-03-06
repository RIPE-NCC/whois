package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

    @Test
    public void equals() {
        final ErrorMessage errorMessage1 = new ErrorMessage("Error", new Attribute("name", "value"), "text", Lists.newArrayList(new Arg("value")));
        assertThat(errorMessage1.equals(null), is(false));
        assertThat(errorMessage1.equals("String"), is(false));
        assertThat(errorMessage1.equals(errorMessage1), is(true));
        assertThat(errorMessage1.equals(new ErrorMessage("Error", new Attribute("name", "value"), "text", Lists.newArrayList(new Arg("value")))), is(true));

        final ErrorMessage errorMessage2 = new ErrorMessage(new Message(Messages.Type.ERROR, "text"));
        assertThat(errorMessage2.equals(null), is(false));
        assertThat(errorMessage2.equals("String"), is(false));
        assertThat(errorMessage2.equals(errorMessage2), is(true));
        assertThat(errorMessage2.equals(new ErrorMessage(new Message(Messages.Type.ERROR, "text"))), is(true));

        final ErrorMessage errorMessage3 = new ErrorMessage(new Message(Messages.Type.ERROR, "text"), new RpslAttribute("key", "value"));
        assertThat(errorMessage3.equals(null), is(false));
        assertThat(errorMessage3.equals("String"), is(false));
        assertThat(errorMessage3.equals(errorMessage3), is(true));
        assertThat(errorMessage3.equals(new ErrorMessage(new Message(Messages.Type.ERROR, "text"), new RpslAttribute("key", "value"))), is(true));

        assertThat(errorMessage1.equals(errorMessage2), is(false));
        assertThat(errorMessage1.equals(errorMessage3), is(false));
    }
}
