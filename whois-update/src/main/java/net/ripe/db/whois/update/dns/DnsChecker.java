package net.ripe.db.whois.update.dns;

import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface DnsChecker {

    void checkAll(UpdateRequest updateRequest, UpdateContext updateContext);
}
