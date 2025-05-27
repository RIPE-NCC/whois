package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;

public class NrtmMessages {

    private NrtmMessages() {
        // do not instantiate
    }

    public static Message termsAndConditions() {
        return new Message(Messages.Type.INFO,
            "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See https://docs.db.ripe.net/terms-conditions.html");
    }

    public static Message version(final String applicationVersion) {
        return new Message(Messages.Type.INFO, "%% nrtm-server-%s", applicationVersion);
    }

    public static Message end(final String source) {
        return new Message(Messages.Type.INFO, "%%END %s", source);
    }

    public static Message end(final int begin, final int end) {
        return new Message(Messages.Type.INFO, "%%END %d - %d", begin, end);
    }

    public static Message deprecatedVersion(final int version) {
        return new Message(Messages.Type.WARNING, "%%WARNING: NRTM version %d is deprecated, please consider migrating to version %d!", version, NrtmServer.NRTM_VERSION);
    }

    public static Message internalError() {
        return new Message(Messages.Type.ERROR, "internal error occurred.");
    }

    public static Message notAuthorised(final String remoteAddress) {
        return new Message(Messages.Type.ERROR, "%%ERROR:402: not authorised to mirror the database from IP address %s\n", remoteAddress);
    }

    public static Message connectionsExceeded(final int connectionLimit) {
        return new Message(Messages.Type.ERROR, ""
                + "ERROR:306: connections exceeded\n"
                + "\n"
                + "Number of connections from a single IP address\n"
                + "has exceeded the maximum number allowed (%d).", connectionLimit);
    }

}
