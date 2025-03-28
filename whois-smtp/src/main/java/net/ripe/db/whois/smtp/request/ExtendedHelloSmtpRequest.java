package net.ripe.db.whois.smtp.request;

import com.google.common.collect.Lists;
import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;
import net.ripe.db.whois.smtp.SmtpException;
import net.ripe.db.whois.smtp.SmtpResponses;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedHelloSmtpRequest implements SmtpRequest {

    private static final Pattern EHLO_PATTERN = Pattern.compile("(?i)EHLO (.+)");

    final List<CharSequence> parameters;

    public ExtendedHelloSmtpRequest(final CharSequence parameters) {
        final Matcher matcher = EHLO_PATTERN.matcher(parameters);
        if (!matcher.find()) {
            throw new SmtpException(SmtpResponses.invalidEhlo());
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
        return SmtpCommand.EHLO;
    }
}
