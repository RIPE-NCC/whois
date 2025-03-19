package net.ripe.db.whois.smtp.request;


import com.google.common.collect.Lists;
import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;
import net.ripe.db.whois.smtp.SmtpException;
import net.ripe.db.whois.smtp.SmtpResponses;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailSmtpRequest implements SmtpRequest {

    private static final Pattern MAIL_PATTERN = Pattern.compile("(?i)MAIL FROM:.*<(.*)>\\s?(?:SIZE=)?(\\d+)?");

    final List<CharSequence> parameters;

    public MailSmtpRequest(final CharSequence parameters) {
        final Matcher matcher = MAIL_PATTERN.matcher(parameters);
        if (!matcher.find()) {
            throw new SmtpException(SmtpResponses.unrecognisedCommand());
        } else {
            this.parameters = Lists.newArrayList(matcher.group(1), matcher.group(2));
        }
    }

    @Override
    public List<CharSequence> parameters() {
        return parameters;
    }

    @Override
    public SmtpCommand command() {
        return SmtpCommand.MAIL;
    }
}
