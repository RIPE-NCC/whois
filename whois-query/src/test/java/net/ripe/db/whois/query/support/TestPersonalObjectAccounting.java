package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.acl.PersonalObjectAccounting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Profile({WhoisProfile.TEST})
@Component
public class TestPersonalObjectAccounting implements PersonalObjectAccounting {
    private Map<InetAddress, Integer> queriedPersonalObjects = new HashMap<>();
    private Map<String, Integer> ssoQueriedPersonalObjects = new HashMap<>();

    @Override
    public int getQueriedPersonalObjects(final InetAddress remoteAddress) {
        final Integer count = queriedPersonalObjects.get(remoteAddress);
        return count == null ? 0 : count;
    }

    @Override
    public int getQueriedPersonalObjects(final String ssoId) {
        final Integer count = ssoQueriedPersonalObjects.get(ssoId);
        return count == null ? 0 : count;
    }

    @Override
    public int accountPersonalObject(final InetAddress remoteAddress, final int amount) {
        Integer count = queriedPersonalObjects.get(remoteAddress);
        count = (count == null) ? amount : count + amount;

        queriedPersonalObjects.put(remoteAddress, count);
        return count;
    }

    @Override
    public int accountPersonalObject(final String ssoId, final int amount) {
        Integer count = ssoQueriedPersonalObjects.get(ssoId);
        count = (count == null) ? amount : count + amount;

        ssoQueriedPersonalObjects.put(ssoId, count);
        return count;
    }

    @Override
    public void resetAccounting() {
        queriedPersonalObjects = new HashMap<>();
        ssoQueriedPersonalObjects = new HashMap<>();
    }
}
