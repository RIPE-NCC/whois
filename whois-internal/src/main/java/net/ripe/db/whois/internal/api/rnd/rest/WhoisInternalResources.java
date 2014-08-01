package net.ripe.db.whois.internal.api.rnd.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.ErrorMessages;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@SuppressWarnings("UnusedDeclaration")
@JsonInclude(NON_EMPTY)
public class WhoisInternalResources {
    public static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/db/support/db-terms-conditions.pdf";

    private WhoisVersionInternal version;
    private WhoisObject object;
    private List<WhoisVersionInternal> incoming;
    private List<WhoisVersionInternal> outgoing;
    private ErrorMessages errorMessages;
    private List<WhoisVersionInternal> versions;
    private Link termsAndConditions;

    public void setErrorMessages(final List<ErrorMessage> errorMessages) {
        if (errorMessages.size() > 1) {
            Collections.sort(errorMessages);
        }
        this.errorMessages = new ErrorMessages(errorMessages);
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages != null ? errorMessages.getErrorMessages() : Collections.<ErrorMessage>emptyList();
    }

    public Link getTermsAndConditions() {
        return termsAndConditions;
    }

    public void includeTermsAndConditions() {
        this.termsAndConditions = new Link("locator", TERMS_AND_CONDITIONS);
    }

    public WhoisVersionInternal getVersion() {
        return version;
    }

    public void setVersion(final WhoisVersionInternal version) {
        this.version = version;
    }

    public WhoisObject getObject() {
        return object;
    }

    public void setObject(final WhoisObject object) {
        this.object = object;
    }

    public List<WhoisVersionInternal> getIncoming() {
        return incoming;
    }

    public void setIncoming(final List<WhoisVersionInternal> incoming) {
        this.incoming = incoming;
    }

    public List<WhoisVersionInternal> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(final List<WhoisVersionInternal> outgoing) {
        this.outgoing = outgoing;
    }

    public List<WhoisVersionInternal> getVersions() {
        return versions;
    }

    public void setVersions(final List<WhoisVersionInternal> versions) {
        this.versions = versions;
    }
}
