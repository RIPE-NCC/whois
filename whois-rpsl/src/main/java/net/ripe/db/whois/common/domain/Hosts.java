package net.ripe.db.whois.common.domain;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Hosts {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hosts.class);
    private static final String localHostName;

    static {
        String hostName = System.getenv("HOSTNAME");
        if (StringUtils.isBlank(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {
                // ignored
                LOGGER.debug("UnknownHostException {} in {} class", ignored.getMessage(), ignored.getClass().getName());
            }

            if (StringUtils.isBlank(hostName)) {
                System.err.println("Acquiring HOSTNAME failed! Try\nexport HOSTNAME=$(hostname -s)\n");
                System.exit(1);
            }
        }

        localHostName = StringUtils.substringBefore(hostName, ".").toUpperCase();
    }

    public static String getLocalHostName() {
        return localHostName;
    }
}
