package net.ripe.db.whois.smtp.request;

import io.netty.handler.codec.smtp.DefaultSmtpRequest;
import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;
import net.ripe.db.whois.smtp.SmtpException;
import net.ripe.db.whois.smtp.SmtpResponses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmtpRequestBuilder {

    private static Pattern COMMAND_PATTERN = Pattern.compile("(?i)([A-Z]+)\\s*+(.*)");

    private SmtpCommand command;
    private CharSequence parameters;

    public SmtpRequestBuilder(final String value) {
        final Matcher matcher = COMMAND_PATTERN.matcher(value);
        if (!matcher.find()) {
            throw new SmtpException(SmtpResponses.unrecognisedCommand());
        } else {
            this.command = SmtpCommand.valueOf(matcher.group(1));
            this.parameters = matcher.group(2);
        }
    }

    public void setCommand(final SmtpCommand command) {
        this.command = command;
    }

    public void setParameters(final CharSequence parameters) {
        this.parameters = parameters;
    }

    public SmtpRequest build() {
        if (SmtpCommand.HELO.equals(command)) {
            return new HelloSmtpRequest(command.name() + " " + parameters);
        } else if (SmtpCommand.EHLO.equals(command)) {
            return new ExtendedHelloSmtpRequest(command.name() + " " + parameters);
        } else if (SmtpCommand.MAIL.equals(command)) {
            return new MailSmtpRequest(command.name() + " " + parameters);
        } else if (SmtpCommand.RCPT.equals(command)) {
            return new RecipientSmtpRequest(command.name() + " " + parameters);
        } else if (SmtpCommand.DATA.equals(command)) {
            return new DataSmtpRequest();
        } else if (SmtpCommand.NOOP.equals(command)) {
            return new NoopSmtpRequest();
        } else if (SmtpCommand.HELP.equals(command)) {
            return new HelpSmtpRequest();
        } else if (SmtpCommand.RSET.equals(command)) {
            return new ResetSmtpRequest();
        } else if (SmtpCommand.QUIT.equals(command)) {
            return new QuitSmtpRequest();
        } else {
            return new DefaultSmtpRequest(this.command, this.parameters);
        }
    }
}
