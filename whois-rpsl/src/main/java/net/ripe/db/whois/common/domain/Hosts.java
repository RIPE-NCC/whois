package net.ripe.db.whois.common.domain;

import org.apache.commons.lang.StringUtils;

public class Hosts {
    private static final String instanceName;

    static {
        instanceName = System.getProperty("instance.name");
        if (StringUtils.isBlank(instanceName)) {
            System.err.println("Instance name is not defined");
            System.exit(1);
        }
    }

    public static String getInstanceName() {
        return instanceName;
    }
}
