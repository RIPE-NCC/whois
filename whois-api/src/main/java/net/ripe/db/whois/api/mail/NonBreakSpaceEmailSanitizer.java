package net.ripe.db.whois.api.mail;

import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class NonBreakSpaceEmailSanitizer implements EmailSanitizer {

    @Override
    public String sanitize(String content) {
        return content.replace('\u00A0',' ');
    }

    @Override
    public boolean isApplicable(Charset charset) {
        return charset.equals(Charset.forName("UTF-8"));
    }
}
