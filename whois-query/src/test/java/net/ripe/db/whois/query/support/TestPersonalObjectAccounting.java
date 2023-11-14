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
    private Map<String, Integer> queriedPersonalObjects = new HashMap<>();

    @Override
    public int getQueriedPersonalObjects(final String identifier) {
        final Integer count = queriedPersonalObjects.get(identifier);
        if (count == null) {
            return 0;
        }

        return count;
    }

    @Override
    public int accountPersonalObject(final String identifier, final int amount) {
        Integer count = queriedPersonalObjects.get(identifier);
        if (count == null) {
            count = amount;
        } else {
            count += amount;
        }

        queriedPersonalObjects.put(identifier, count);
        return count;
    }

    @Override
    public void resetAccounting() {
        queriedPersonalObjects = new HashMap<>();
    }
}
