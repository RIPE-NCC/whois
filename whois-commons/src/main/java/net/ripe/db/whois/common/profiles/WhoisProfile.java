package net.ripe.db.whois.common.profiles;

public class WhoisProfile {

    private WhoisProfile() {}

    public static final String ENDTOEND = "ENDTOEND";
    public static final String TEST = "TEST";
    public static final String DEPLOYED = "DEPLOYED";

    private static boolean deployed;

    public static boolean isDeployed() {
        return deployed;
    }

    public static void setDeployed() {
        WhoisProfile.deployed = true;
        System.setProperty("spring.profiles.active", DEPLOYED);
    }

    public static void setTest() {
        WhoisProfile.deployed = false;
        System.setProperty("spring.profiles.active", TEST);
    }
}
