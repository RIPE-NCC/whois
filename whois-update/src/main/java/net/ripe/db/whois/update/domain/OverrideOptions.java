package net.ripe.db.whois.update.domain;

import com.google.common.base.Splitter;

import javax.annotation.concurrent.Immutable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
public class OverrideOptions {
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("\\{(.*)\\}");
    private static final Splitter OPTION_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Pattern OPTION_PATTERN = Pattern.compile("(.+)=(.+)");

    public static final OverrideOptions NONE = new OverrideOptions(null, null);

    private final Integer objectId;
    private final Boolean notify;

    OverrideOptions(final Integer objectId, final Boolean notify) {
        this.objectId = objectId;
        this.notify = notify;
    }

    public boolean isObjectIdOverride() {
        return objectId != null;
    }

    public int getObjectId() {
        return objectId;
    }

    public boolean isNotifyOverride() {
        return notify != null;
    }

    public boolean isNotify() {
        return notify;
    }

    public static OverrideOptions parse(final Update update, final UpdateContext updateContext) {
        final Set<OverrideCredential> overrideCredentials = update.getCredentials().ofType(OverrideCredential.class);
        if (overrideCredentials.isEmpty()) {
            return NONE;
        }

        if (overrideCredentials.size() > 1) {
            updateContext.addMessage(update, UpdateMessages.multipleOverridePasswords());
        }

        final OverrideCredential overrideCredential = overrideCredentials.iterator().next();
        final String remarks = overrideCredential.getRemarks();

        final Matcher optionMatcher = OPTIONS_PATTERN.matcher(remarks);
        if (!optionMatcher.find()) {
            return NONE;
        }

        final String options = optionMatcher.group(1);
        Integer objectId = null;
        Boolean notify = null;

        for (final String option : OPTION_SPLITTER.split(options)) {
            final Matcher matcher = OPTION_PATTERN.matcher(option);
            if (!matcher.matches()) {
                updateContext.addMessage(update, UpdateMessages.overrideOptionInvalid(option));
            } else {
                final String key = matcher.group(1).toLowerCase();
                final String value = matcher.group(2);

                try {
                    if ("oid".equals(key)) {
                        objectId = Integer.parseInt(value);
                    } else if ("notify".equals(key)) {
                        notify = Boolean.parseBoolean(value);
                    } else {
                        updateContext.addMessage(update, UpdateMessages.overrideOptionInvalid(option));
                    }
                } catch (RuntimeException e) {
                    updateContext.addMessage(update, UpdateMessages.overrideOptionInvalid(option));
                }
            }
        }

        return new OverrideOptions(objectId, notify);
    }
}
