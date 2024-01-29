package net.ripe.db.whois.common.sso;


import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

public class SsoTranslation {

    private final Map<String, String> usernameToUuidCache = Maps.newHashMap();

    public void put(final String username, final String uuid) {
        usernameToUuidCache.put(username, uuid);
    }

    public boolean containsUsername(final String username) {
        return usernameToUuidCache.containsKey(username);
    }

    public boolean containsUuid(final String uuid) {
        return usernameToUuidCache.containsValue(uuid);
    }

    @Nullable
    public String getUsername(final String uuid) {
        return getKey(uuid);
    }

    @Nullable
    public String getUuid(final String username) {
        return getValue(username);
    }

    @Nullable
    private String getKey(final String value) {
        for (Map.Entry<String, String> entry : usernameToUuidCache.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    private String getValue(final String key) {
        return usernameToUuidCache.get(key);
    }

}
