package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.query.acl.PersonalObjectAccounting;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class InternalPersonalObjectAccounting implements PersonalObjectAccounting {
    @Override
    public int getQueriedPersonalObjects(final InetAddress remoteAddress) {
        return 0;
    }

    @Override
    public int accountPersonalObject(final InetAddress remoteAddress, final int amount) {
        return 0;
    }

    @Override
    public void resetAccounting() {

    }
}
