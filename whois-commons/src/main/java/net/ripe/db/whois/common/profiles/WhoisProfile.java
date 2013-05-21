package net.ripe.db.whois.common.profiles;

public class WhoisProfile {
    public static final String ENDTOEND = "ENDTOEND";
    public static final String TEST = "TEST";
    public static final String DEPLOYED = "DEPLOYED";
    private static boolean deployed, endtoend;

    public static boolean isDeployed() {
        return deployed;
    }

    public static void setDeployed() {
        WhoisProfile.deployed = true;
        WhoisProfile.endtoend = false;
        System.setProperty("spring.profiles.active", DEPLOYED);
    }

    public static boolean isEndtoend() {
        return endtoend;
    }

    public static void setEndtoend() {
        WhoisProfile.deployed = false;
        WhoisProfile.endtoend = true;
        System.setProperty("spring.profiles.active", ENDTOEND);
    }
}
