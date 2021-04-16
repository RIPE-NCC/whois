package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@DeployedProfile
public class JvmSecurityManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmSecurityManager.class);

    @Autowired
    public JvmSecurityManager() {
        java.security.Security.setProperty("networkaddress.cache.ttl", "30");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "10");
    }
}
