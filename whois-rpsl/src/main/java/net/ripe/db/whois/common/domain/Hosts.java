package net.ripe.db.whois.common.domain;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Hosts {

    public static String getInstanceName() {
        final String instanceName = System.getProperty("instance.name");
        if (!StringUtils.isBlank(instanceName)) {
            return instanceName;
        }
        final String  hostName = System.getenv("HOSTNAME");
        if (!StringUtils.isBlank(hostName)) {
            return hostName;
        }
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
