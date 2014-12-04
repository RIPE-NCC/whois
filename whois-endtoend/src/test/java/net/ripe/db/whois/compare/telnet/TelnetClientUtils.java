package net.ripe.db.whois.compare.telnet;

import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelnetClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetClientUtils.class);

    private static final Pattern SERIAL_PATTERN = Pattern.compile("(?m)^\\w+:\\d+:X:\\d+-(\\d+)$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("(?m)^% whois-server-(.*)$");

    public static long getLatestSerialId(final QueryExecutorConfiguration configuration) throws IOException {
        final TelnetWhoisClient client = new TelnetWhoisClient(configuration.getHost(), configuration.getNrtmPort());
        final String response = client.sendQuery("-q sources");

        final Matcher matcher = SERIAL_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new RuntimeException("serial string not found in whois nrtm response of " + configuration.getIdentifier());
        }

        final Long serial = Long.parseLong(matcher.group(1));
        LOGGER.warn(" ***** Server {} is running on DB with serial {} ***** ", configuration.getIdentifier(), serial);
        return serial;
    }

    public static void getVersion(final QueryExecutorConfiguration configuration) throws IOException {
        final TelnetWhoisClient client = new TelnetWhoisClient(configuration.getHost(), configuration.getQueryPort());
        final String response = client.sendQuery("-q version");
        final Matcher matcher = VERSION_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new RuntimeException("version string not found in whois query response of " + configuration.getIdentifier());
        }
        LOGGER.warn(" ***** Server {} is running version {} ***** ", configuration.getIdentifier(), matcher.group(1));
    }
}
