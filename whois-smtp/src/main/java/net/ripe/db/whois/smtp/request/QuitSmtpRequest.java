package net.ripe.db.whois.smtp.request;


import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;

import java.util.Collections;
import java.util.List;

public class QuitSmtpRequest implements SmtpRequest {
    @Override
    public SmtpCommand command() {
        return SmtpCommand.QUIT;
    }

    @Override
    public List<CharSequence> parameters() {
        return Collections.emptyList();
    }
}
