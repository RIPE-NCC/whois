package net.ripe.db.whois.common.profiles;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class WhoisProfile {

    private WhoisProfile() {}

    public static final String ENDTOEND = "ENDTOEND";
    public static final String TEST = "TEST";
    public static final String DEPLOYED = "DEPLOYED";

    public static ClassPathXmlApplicationContext initContextWithProfile(String configLocation, String... profiles) {
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        env.setActiveProfiles(profiles);
        applicationContext.setConfigLocation(configLocation);
        applicationContext.refresh();
        return applicationContext;
    }
}
