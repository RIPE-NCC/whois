package net.ripe.db.whois.common.profiles;

import net.ripe.db.whois.common.WhoisAWSPropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class WhoisProfile {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisAWSPropertyResolver.class);

    private WhoisProfile() {}

    public static final String TEST = "TEST";
    public static final String RIPE_DEPLOYED = "RIPE_DEPLOYED";
    public static final String AWS_DEPLOYED = "AWS_DEPLOYED";

    public static ClassPathXmlApplicationContext initContextWithProfile(String configLocation, String... defaultProfiles) {
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        if(env.getActiveProfiles() == null || env.getActiveProfiles().length == 0) {
            LOGGER.info("No active profile specified, using default one");
            env.setActiveProfiles(defaultProfiles);
        }

        applicationContext.setConfigLocation(configLocation);
        applicationContext.refresh();
        return applicationContext;
    }
}
