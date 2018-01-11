package net.ripe.db.whois.api.mail;

import java.nio.charset.Charset;

public interface EmailSanitizer {

    String sanitize(String content);

    boolean isApplicable(Charset charset);
}
