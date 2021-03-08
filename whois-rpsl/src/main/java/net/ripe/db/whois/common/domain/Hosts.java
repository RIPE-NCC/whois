package net.ripe.db.whois.common.domain;

import org.apache.commons.lang.StringUtils;

public class Hosts {
    private static final String instanceName;

    static {
        instanceName = System.getProperty("instance.name");
        if (StringUtils.isBlank(instanceName)) {
            throw new IllegalStateException("Instance name is not defined");
        }
    }

    public static String getInstanceName() {
        return instanceName;
    }
}
