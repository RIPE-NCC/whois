package net.ripe.db.whois.smtp.request;


import com.google.common.collect.Lists;
import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;
import net.ripe.db.whois.smtp.SmtpException;
import net.ripe.db.whois.smtp.SmtpResponses;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloSmtpRequest implements SmtpRequest {

    private static final Pattern HELO_PATTERN = Pattern.compile("(?i)HELO (.+)");

    final List<CharSequence> parameters;

    public HelloSmtpRequest(final CharSequence parameters) {
        final Matcher matcher = HELO_PATTERN.matcher(parameters);
        if (!matcher.find()) {
            throw new SmtpException(SmtpResponses.invalidHello());
        } else {
            this.parameters = Lists.newArrayList(matcher.group(1));
        }
    }

    @Override
    public List<CharSequence> parameters() {
        return parameters;
    }

    @Override
    public SmtpCommand command() {
        return SmtpCommand.HELO;
    }
}
