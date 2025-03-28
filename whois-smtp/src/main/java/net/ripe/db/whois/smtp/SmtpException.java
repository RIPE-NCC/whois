package net.ripe.db.whois.smtp;

import io.netty.handler.codec.smtp.SmtpResponse;

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
        for (CharSequence detail : response.details()) {
            builder.append(response.code()).append(" ").append(detail).append("\n");
        }
        return builder.toString();
    }


}
