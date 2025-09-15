package net.ripe.db.whois.update.dns;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DnsChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsChecker.class);
    private static final List<AttributeType> DNSCHECKER_ATTRIBUTES = ImmutableList.of(AttributeType.DOMAIN, AttributeType.NSERVER, AttributeType.DS_RDATA);

    private final DnsGateway dnsGateway;
    private final LoggerContext loggerContext;

    private final boolean dnsCheckEnabled;

    @Autowired
    public DnsChecker(final DnsGateway dnsGateway,
                      final LoggerContext loggerContext,
                      @Value("${whois.zonemaster.baseUrl:}") final String baseUrl) {
        this.dnsGateway = dnsGateway;
        this.loggerContext = loggerContext;
        dnsCheckEnabled = StringUtils.isNotBlank(baseUrl);

        if (!dnsCheckEnabled) {
            LOGGER.info("DNS check is disabled");
        }
    }

    public void checkAll(final UpdateRequest updateRequest, final UpdateContext updateContext) {

        if (!dnsCheckEnabled) {
            return;
        }

        Set<DnsCheckRequest> dnsCheckRequestSet = Sets.newLinkedHashSet();

        for (Update update : updateRequest.getUpdates()) {
            if (isDnsCheckRequired(update)) {
                dnsCheckRequestSet.add(createDnsCheckRequest(update));
            }
        }

        if (dnsCheckRequestSet.isEmpty()) {
            return;
        }

        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Map<DnsCheckRequest, DnsCheckResponse> dnsCheckResponseMap = dnsGateway.performDnsChecks(dnsCheckRequestSet);
        loggerContext.log(new Message(Messages.Type.INFO, "Called dnsGateway with %d requests and processed in %s", dnsCheckRequestSet.size(), stopwatch.stop().toString()));

        for (Map.Entry<DnsCheckRequest, DnsCheckResponse> entry : dnsCheckResponseMap.entrySet()) {
            final DnsCheckRequest dnsCheckRequest = entry.getKey();
            final DnsCheckResponse dnsCheckResponse = entry.getValue();

            updateContext.addDnsCheckResponse(dnsCheckRequest, dnsCheckResponse);

            for (final Message message : dnsCheckResponse.getMessages()) {
                updateContext.addMessage(dnsCheckRequest.getUpdate(), message);
            }
        }
    }

    private boolean isDnsCheckRequired(final Update update) {
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

        return true;
    }

    private boolean hasValidSyntax(Update update) {
        ObjectMessages objectMessages = new ObjectMessages();
        RpslObject rpslObject = update.getSubmittedObject();

        for (AttributeType attributeType: DNSCHECKER_ATTRIBUTES) {
            for (RpslAttribute rpslAttribute: rpslObject.findAttributes(attributeType)) {
                rpslAttribute.validateSyntax(ObjectType.DOMAIN, objectMessages);
                if (objectMessages.hasErrors()) {
                    return false;
                }
            }
        }

        return true;
    }

    private DnsCheckRequest createDnsCheckRequest(final Update update) {
        final RpslObject rpslObject = update.getSubmittedObject();
        final String domain = rpslObject.getKey().toString();
        final String glue = createGlue(rpslObject);
        return new DnsCheckRequest(update, domain, glue);
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
}
