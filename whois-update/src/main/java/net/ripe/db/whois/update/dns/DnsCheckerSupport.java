package net.ripe.db.whois.update.dns;


import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class DnsCheckerSupport implements DnsChecker {

    protected boolean isDnsCheckRequired(final Update update) {
        if (Operation.DELETE.equals(update.getOperation())) {
            return false;
        }

        if (!ObjectType.DOMAIN.equals(update.getType())) {
            return false;
        }

        if (update.isOverride()) {
            return false;
        }

        if (!hasValidSyntax(update)) {
            return false;
        }

        LOGGER.debug("DNS check required for update: {}", update);
        return true;
    }

    protected DnsCheckRequest createDnsCheckRequest(final Update update) {
        final RpslObject rpslObject = update.getSubmittedObject();
        final String domain = rpslObject.getKey().toString();
        final String glue = createGlue(rpslObject);
        return new DnsCheckRequest(update, domain, glue);
    }

    private boolean hasValidSyntax(Update update) {
        ObjectMessages objectMessages = new ObjectMessages();
        RpslObject rpslObject = update.getSubmittedObject();

        for (AttributeType attributeType : DNSCHECKER_ATTRIBUTES) {
            for (RpslAttribute rpslAttribute : rpslObject.findAttributes(attributeType)) {
                rpslAttribute.validateSyntax(ObjectType.DOMAIN, objectMessages);
                if (objectMessages.hasErrors()) {
                    return false;
                }
            }
        }

        return true;
    }

    private String createGlue(final RpslObject rpslObject) {
        final StringBuilder glue = new StringBuilder();

        for (final CIString nserver : rpslObject.getValuesForAttribute(AttributeType.NSERVER)) {
            if (glue.length() > 0) {
                glue.append(' ');
            }

            glue.append(nserver.toString().replaceAll(" ", "/"));
        }

        final CIString domain = rpslObject.getKey();
        for (final CIString rdata : rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)) {
            glue.append(" ds:/").append(domain).append("_DS_").append(rdata.toString().replaceAll(" ", "_"));
        }

        return glue.toString();
    }

    private static final List<AttributeType> DNSCHECKER_ATTRIBUTES = ImmutableList.of(AttributeType.DOMAIN, AttributeType.NSERVER, AttributeType.DS_RDATA);
    private static final Logger LOGGER = LoggerFactory.getLogger(DnsCheckerSupport.class);

}
