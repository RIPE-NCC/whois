package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


@Component
public class WhoisService {
    private final AccessControlListManager accessControlListManager;

    @Autowired
    public WhoisService(final AccessControlListManager accessControlListManager) {
        this.accessControlListManager = accessControlListManager;
    }

    public WhoisResources createErrorEntity(final HttpServletRequest request, final Message... errorMessage) {
        return createErrorEntity(request, Arrays.asList(errorMessage));
    }

    public WhoisResources createErrorEntity(final HttpServletRequest request, final List<Message> errorMessages) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setErrorMessages(createErrorMessages(errorMessages));
        // TODO: [AH] the external URL should be configurable via properties
        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    public List<ErrorMessage> createErrorMessages(final List<Message> messages) {
        final List<ErrorMessage> errorMessages = Lists.newArrayList();
        for (Message message : messages) {
            errorMessages.add(new ErrorMessage(message));
        }
        return errorMessages;
    }

    public boolean isTrusted(final HttpServletRequest request) {
        return accessControlListManager.isTrusted(InetAddresses.forString(request.getRemoteAddr()));
    }
}
