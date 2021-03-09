package net.ripe.db.whois.common.domain;

import org.apache.commons.lang.StringUtils;

public class Hosts {

    public static String getInstanceName() {
        final String instanceName = System.getProperty("instance.name");
        final String  hostName = System.getenv("HOSTNAME");
        if (StringUtils.isBlank(instanceName) && StringUtils.isBlank(hostName)) {
            throw new IllegalStateException("Instance name and host name is not defined");
        }
        return StringUtils.isBlank(instanceName) ? hostName : instanceName;
    }
}
