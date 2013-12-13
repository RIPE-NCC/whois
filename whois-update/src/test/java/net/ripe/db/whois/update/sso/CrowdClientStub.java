package net.ripe.db.whois.update.sso;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.profiles.TestingProfile;
import org.springframework.stereotype.Component;

import java.util.Map;

@TestingProfile
@Component
public class CrowdClientStub implements CrowdClient {

    private Map<String, String> usermap;

    public CrowdClientStub() {
        usermap = Maps.newHashMap();
        usermap.put("db-test@ripe.net", "ed7cd420-6402-11e3-949a-0800200c9a66");
        usermap.put("random@ripe.net", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503");
        usermap.put("test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5");
        usermap.put("person@net.net", "906635c2-0405-429a-800b-0602bd716124");

        usermap.put("ed7cd420-6402-11e3-949a-0800200c9a66", "db-test@ripe.net");
        usermap.put("017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", "random@ripe.net");
        usermap.put("8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "test@ripe.net");
        usermap.put("906635c2-0405-429a-800b-0602bd716124", "person@net.net");
    }

    @Override
    public String getUuid(String username) {
        return get(username);
    }

    @Override
    public String getUsername(String uuid) {
        return get(uuid);
    }

    private String get(final String userOrUuid) {
        final String result = usermap.get(userOrUuid);
        if (result == null) {
            throw new IllegalArgumentException("Unknown RIPE Access user: " + userOrUuid);
        }
        return result;
    }
}
