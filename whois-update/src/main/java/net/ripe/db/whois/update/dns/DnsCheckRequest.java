package net.ripe.db.whois.update.dns;

import net.ripe.db.whois.update.domain.Update;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class DnsCheckRequest {
    private final Update update;
    private final String domain;
    private final String glue;

    public DnsCheckRequest(final Update update, final String domain, final String glue) {
        this.update = update;
        this.domain = domain;
        this.glue = glue;
    }

    public Update getUpdate() {
        return update;
    }

    public String getDomain() {
        return domain;
    }

    public String getGlue() {
        return glue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DnsCheckRequest that = (DnsCheckRequest) o;

        return Objects.equals(domain, that.domain) &&
            Objects.equals(glue, that.glue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(update, domain, glue);
    }

    @Override
    public String toString() {
        return domain + " " + glue;
    }
}
