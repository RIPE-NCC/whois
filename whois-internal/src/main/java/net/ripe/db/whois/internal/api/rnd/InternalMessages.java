package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;

public class InternalMessages {

    public static Message multipleVersionsForTimestamp(final int count) {
        return new Message(Messages.Type.WARNING, "There are actually %s versions on this interval.", count);
    }

    public static Message noVersion(final CharSequence key) {
        return new Message(Messages.Type.ERROR, "There is no entry for object %s for the supplied version.", key);
    }

    public static Message noVersions(final CharSequence key) {
        return new Message(Messages.Type.ERROR, "No entries found for object %s", key);
    }
}
