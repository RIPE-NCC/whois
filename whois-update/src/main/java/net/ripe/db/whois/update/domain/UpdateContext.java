package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;
import net.ripe.db.whois.update.dns.DnsCheckResponse;
import net.ripe.db.whois.update.log.LoggerContext;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateContext {
    private static final AtomicInteger NEXT_NR_SINCE_RESTART = new AtomicInteger();

    private int nrSinceRestart;
    private final List<Paragraph> ignored = Lists.newArrayList();
    private final Messages globalMessages = new Messages();
    private final Map<Update, CIString> placeHolderForUpdate = Maps.newHashMap();
    private final Map<CIString, GeneratedKey> generatedKeys = Maps.newHashMap();
    private final Map<Update, Context> contexts = Maps.newLinkedHashMap();
    private final Map<DnsCheckRequest, DnsCheckResponse> dnsCheckResponses = Maps.newHashMap();
    private final LoggerContext loggerContext;

    public UpdateContext(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        this.nrSinceRestart = NEXT_NR_SINCE_RESTART.incrementAndGet();
    }

    public int getNrSinceRestart() {
        return nrSinceRestart;
    }

    public void addDnsCheckResponse(final DnsCheckRequest request, final DnsCheckResponse response) {
        final DnsCheckResponse previous = dnsCheckResponses.put(request, response);
        if (previous != null) {
            throw new IllegalStateException("Existing response for request: " + request);
        }
    }

    @CheckForNull
    public DnsCheckResponse getCachedDnsCheckResponse(final DnsCheckRequest dnsCheckRequest) {
        return dnsCheckResponses.get(dnsCheckRequest);
    }

    public void addMessages(final UpdateContainer updateContainer, final ObjectMessages objectMessages) {
        getOrCreateContext(updateContainer).objectMessages.addAll(objectMessages);
        loggerContext.logMessages(updateContainer, objectMessages);
    }

    public void addMessage(final UpdateContainer updateContainer, final Message message) {
        getOrCreateContext(updateContainer).objectMessages.addMessage(message);
        loggerContext.logMessage(updateContainer, message);
    }

    public void addMessage(final UpdateContainer updateContainer, final RpslAttribute attribute, final Message message) {
        getOrCreateContext(updateContainer).objectMessages.addMessage(attribute, message);
        loggerContext.logMessage(updateContainer, attribute, message);
    }

    public ObjectMessages getMessages(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).objectMessages;
    }

    public void setAction(final UpdateContainer updateContainer, final Action action) {
        getOrCreateContext(updateContainer).action = action;
        loggerContext.logAction(updateContainer, action);
    }

    public PreparedUpdate getPreparedUpdate(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).preparedUpdate;
    }

    public boolean hasErrors(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).objectMessages.hasErrors();
    }

    public int getErrorCount(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).objectMessages.getErrorCount();
    }

    public void status(final UpdateContainer updateContainer, final UpdateStatus status) {
        getOrCreateContext(updateContainer).status = status;
        loggerContext.logStatus(updateContainer, status);
    }

    public UpdateStatus getStatus(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).getStatus();
    }

    public void subject(final UpdateContainer updateContainer, final Subject subject) {
        getOrCreateContext(updateContainer).subject = subject;
    }

    public Subject getSubject(final UpdateContainer updateContainer) {
        return getOrCreateContext(updateContainer).subject;
    }

    public void setPreparedUpdate(final PreparedUpdate preparedUpdate) {
        final Context context = getOrCreateContext(preparedUpdate);
        context.preparedUpdate = preparedUpdate;
    }

    public void ignore(final Paragraph paragraph) {
        if (!paragraph.getContent().isEmpty()) {
            ignored.add(paragraph);
        }

        if (!paragraph.getCredentials().ofType(OverrideCredential.class).isEmpty()) {
            addGlobalMessage(UpdateMessages.overrideIgnored());
        }
    }

    public void addGlobalMessage(final Message message) {
        globalMessages.add(message);
    }

    List<Paragraph> getIgnoredParagraphs() {
        return ignored;
    }

    // TODO [AK] Used by velocity templates, add test cases!
    @SuppressWarnings("UnusedDeclaration")
    Set<Update> getUpdates() {
        final Set<Update> updates = contexts.keySet();
        final Set<Update> result = Sets.newLinkedHashSetWithExpectedSize(updates.size());
        for (final Update update : result) {
            if (contexts.get(update).preparedUpdate != null) {
                result.add(update);
            }
        }

        return result;
    }

    public Messages getGlobalMessages() {
        return globalMessages;
    }

    public void addGeneratedKey(final UpdateContainer updateContainer, final CIString keyPlaceholder, final GeneratedKey generatedKey) {
        final Update update = updateContainer.getUpdate();
        if (placeHolderForUpdate.put(update, keyPlaceholder) != null) {
            throw new IllegalStateException("Multiple place holders for update: " + update.getSubmittedObject().getFormattedKey());
        }

        generatedKeys.put(keyPlaceholder, generatedKey);
    }

    public void failedUpdate(final UpdateContainer updateContainer, final Message... messages) {
        final CIString placeHolder = placeHolderForUpdate.remove(updateContainer.getUpdate());
        if (placeHolder != null) {
            generatedKeys.remove(placeHolder);
        }

        for (Message message : messages) {
            addMessage(updateContainer, message);
        }
    }

    @CheckForNull
    public GeneratedKey getGeneratedKey(final CIString keyPlaceholder) {
        return generatedKeys.get(keyPlaceholder);
    }

    public Ack createAck() {
        final List<UpdateResult> updateResults = Lists.newArrayList();

        for (final Update update : contexts.keySet()) {
            updateResults.add(createUpdateResult(update));
        }

        final List<Paragraph> ignoredParagraphs = getIgnoredParagraphs();
        return new Ack(updateResults, ignoredParagraphs);
    }

    public UpdateResult createUpdateResult(final UpdateContainer updateContainer) {
        final Context context = getOrCreateContext(updateContainer);
        final Update update = updateContainer.getUpdate();

        final RpslObject updatedObject;
        if (context.preparedUpdate != null) {
            updatedObject = context.preparedUpdate.getUpdatedObject();
        } else {
            updatedObject = update.getSubmittedObject();
        }

        return new UpdateResult(update, updatedObject, context.action, context.getStatus(), context.objectMessages, context.retryCount);
    }

    public void prepareForReattempt(final Update update) {
        final Context context = contexts.remove(update);
        getOrCreateContext(update).retryCount = context.retryCount + 1;
    }

    private Context getOrCreateContext(final UpdateContainer updateContainer) {
        final Update update = updateContainer.getUpdate();

        Context context = contexts.get(update);
        if (context == null) {
            context = new Context();
            contexts.put(update, context);
        }

        return context;
    }

    private static class Context {
        private final ObjectMessages objectMessages = new ObjectMessages();
        private Action action;
        private PreparedUpdate preparedUpdate;
        private Subject subject;
        private UpdateStatus status;
        private int retryCount;

        public UpdateStatus getStatus() {
            if (status != null) {
                return status;
            }

            if (objectMessages.hasErrors()) {
                return UpdateStatus.FAILED;
            }

            return UpdateStatus.SUCCESS;
        }
    }
}
