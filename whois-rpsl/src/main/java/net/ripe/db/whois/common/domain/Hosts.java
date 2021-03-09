package net.ripe.db.whois.common.domain;

import org.apache.commons.lang.StringUtils;

public class Hosts {

    public static String getInstanceName() {
        final String instanceName = System.getProperty("instance.name");
        if (StringUtils.isBlank(instanceName)) {
            throw new IllegalStateException("Instance name is not defined");
        }
        return instanceName;
    }
}
