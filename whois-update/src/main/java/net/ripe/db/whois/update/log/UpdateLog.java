package net.ripe.db.whois.update.log;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Profile(WhoisProfile.DEPLOYED)
public class UpdateLog {
    private static final Map<Class<? extends Credential>, String> CREDENTIAL_NAME_MAP = Maps.newHashMap();

    static {
        CREDENTIAL_NAME_MAP.put(PasswordCredential.class, "PWD");
        CREDENTIAL_NAME_MAP.put(PgpCredential.class, "PGP");
        CREDENTIAL_NAME_MAP.put(X509Credential.class, "X509");
        CREDENTIAL_NAME_MAP.put(OverrideCredential.class, "OVERRIDE");
    }

    private final Logger logger;

    public UpdateLog() {
        this(LoggerFactory.getLogger(UpdateLog.class));
    }

    public UpdateLog(final Logger logger) {
        this.logger = logger;
    }

    public void logUpdateResult(final UpdateRequest updateRequest, final UpdateContext updateContext, final Update update, final Stopwatch stopwatch) {
        logger.info(formatMessage(updateRequest, updateContext, update, stopwatch));
    }

    protected String formatMessage(final UpdateRequest updateRequest, final UpdateContext updateContext, final Update update, final Stopwatch stopwatch) {
        final UpdateResult updateResult = updateContext.createUpdateResult(update);
        final RpslObject updatedObject = updateResult.getUpdatedObject();

        return String.format("[%5d] %-10s %s %-6s %-12s %-30s (%d) %-22s: <E%d,W%d,I%d> AUTH %s - %s",
                updateContext.getNrSinceRestart(),
                stopwatch.toString(),
                updateResult.isDryRun() ? "DRY" : "UPD",
                updateResult.getAction(),
                updatedObject.getType().getName(),
                updatedObject.getKey(),
                updateResult.getRetryCount() + 1,
                updateResult.getStatus(),
                updateResult.getErrors().size(),
                updateResult.getWarnings().size(),
                updateResult.getInfos().size(),
                StringUtils.join(getCredentialTypes(update), ','),
                updateRequest.getOrigin()
        );
    }

    private Set<String> getCredentialTypes(final Update update) {
        final Credentials credentials = update.getCredentials();
        final Set<String> result = Sets.newTreeSet();
        for (final Map.Entry<Class<? extends Credential>, String> entry : CREDENTIAL_NAME_MAP.entrySet()) {
            if (credentials.has(entry.getKey())) {
                result.add(entry.getValue());
            }
        }

        return result;
    }
}
