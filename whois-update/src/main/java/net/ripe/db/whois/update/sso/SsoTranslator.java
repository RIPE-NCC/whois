package net.ripe.db.whois.update.sso;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class SsoTranslator {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    private final CrowdClient crowdClient;

    @Autowired
    public SsoTranslator(final CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }

    public String getUuidForUsername(final String username) {
        return crowdClient.getUuid(username);
    }

    public String getUsernameForUuid(final String uuid) {
        return crowdClient.getUsername(uuid);
    }

    public void populate(final Update update, final UpdateContext updateContext) {
        final RpslObject submittedObject = update.getSubmittedObject();
        if (!ObjectType.MNTNER.equals(submittedObject.getType())) {
            return;
        }

        for (final RpslAttribute auth : submittedObject.findAttributes(AttributeType.AUTH)) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(auth.getCleanValue()).iterator();
            final String passwordType = authIterator.next();
            if (passwordType.equalsIgnoreCase("SSO")) {
                String username = authIterator.next();
                if (!updateContext.hasSsoTranslationResult(username)) {
                    updateContext.addSsoTranslationResult(username, getUuidForUsername(username));
                }
            }
        }
    }

    public RpslObject translateAuthToUuid(UpdateContext updateContext, RpslObject rpslObject) {
        return translateAuth(updateContext, rpslObject);
    }

    public RpslObject translateAuthToUsername(final UpdateContext updateContext, final RpslObject rpslObject) {
        return translateAuth(updateContext, rpslObject);
    }

    private RpslObject translateAuth(final UpdateContext updateContext, final RpslObject rpslObject) {
        if (!ObjectType.MNTNER.equals(rpslObject.getType())) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(auth.getCleanValue()).iterator();
            final String passwordType = authIterator.next().toUpperCase();
            if (passwordType.equals("SSO")) {
                String token = authIterator.next();
                String authValue = "SSO " + updateContext.getSsoTranslationResult(token);
                replace.put(auth, new RpslAttribute(auth.getKey(), authValue));
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
        }
    }
}
