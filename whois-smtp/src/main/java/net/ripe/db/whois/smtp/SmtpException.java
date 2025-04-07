package net.ripe.db.whois.smtp;

import io.netty.handler.codec.smtp.SmtpResponse;

import java.util.Iterator;

public class SmtpException extends RuntimeException {

    private final SmtpResponse response;

    public SmtpException(final SmtpResponse response) {
        this.response = response;
    }

    public SmtpResponse getResponse() {
        return response;
    }

    @Override
    public String getMessage() {
        final StringBuilder builder = new StringBuilder();
        for (Iterator<CharSequence> iterator = response.details().iterator(); iterator.hasNext(); ) {
            final CharSequence detail = iterator.next();
            builder.append(response.code()).append(iterator.hasNext() ? "-" : " ").append(detail).append("\n");
        }
        return builder.toString();
    }


}
