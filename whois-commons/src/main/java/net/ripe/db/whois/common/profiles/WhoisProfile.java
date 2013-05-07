package net.ripe.db.whois.common.profiles;

public class WhoisProfile {
    public static final String ENDTOEND = "ENDTOEND";
    public static final String TEST = "TEST";
    public static final String DEPLOYED = "DEPLOYED";

    public static void setActive(final String profile) {
        System.setProperty("spring.profiles.active", profile);
    }
}
