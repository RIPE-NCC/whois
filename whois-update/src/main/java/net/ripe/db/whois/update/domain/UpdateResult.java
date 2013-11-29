package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

public class UpdateResult {

    private final RpslObject originalObject;
    private final RpslObject updatedObject;
    private final Action action;
    private final UpdateStatus status;
    private final ObjectMessages objectMessages;
    private final int retryCount;
    private final boolean dryRun;

    public UpdateResult(@Nullable final RpslObject originalObject, final RpslObject updatedObject, final Action action, final UpdateStatus status, final ObjectMessages objectMessages, final int retryCount, final boolean dryRun) {
        this.originalObject = originalObject;
        this.updatedObject = updatedObject;
        this.action = action;
        this.status = status;
        this.objectMessages = objectMessages;
        this.retryCount = retryCount;
        this.dryRun = dryRun;
    }

    public RpslObject getUpdatedObject() {
        return updatedObject;
    }

    public String getKey() {
        return updatedObject.getFormattedKey();
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public Action getAction() {
        return action;
    }

    public String getActionString() {
        if (action != null) {
            return action.getDescription();
        }
        return "";
    }

    public Collection<Message> getErrors() {
        return objectMessages.getMessages().getMessages(Messages.Type.ERROR);
    }

    public Collection<Message> getWarnings() {
        return objectMessages.getMessages().getMessages(Messages.Type.WARNING);
    }

    public Collection<Message> getInfos() {
        return objectMessages.getMessages().getMessages(Messages.Type.INFO);
    }

    public boolean isPending() {
        return status.equals(UpdateStatus.PENDING_AUTHENTICATION);
    }

    public boolean isNoop() {
        return Action.NOOP.equals(action);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public String toString() {
        final Writer writer = new StringWriter();

        try {
            toString(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Should not occur", e);
        }
    }

    public void toString(final Writer writer) throws IOException {
        final boolean showAttributes = !UpdateStatus.SUCCESS.equals(status);

        if (!showAttributes && dryRun && Action.MODIFY.equals(action) && originalObject != null) {
            writer.write(updatedObject.diff(originalObject));
            writer.write('\n');
        }

        for (final RpslAttribute attribute : updatedObject.getAttributes()) {
            if (showAttributes) {
                attribute.writeTo(writer);
            }

            writeMessages(writer, objectMessages.getMessages(attribute));
        }

        final Messages messages = objectMessages.getMessages();
        if (!messages.getAllMessages().isEmpty()) {
            writer.write('\n');
            writeMessages(writer, messages, "\n");
        }
    }

    private void writeMessages(final Writer writer, final Messages messages) throws IOException {
        writeMessages(writer, messages, "");
    }

    private void writeMessages(final Writer writer, final Messages messages, final String separator) throws IOException {
        for (final Message message : messages.getAllMessages()) {
            Messages.Type type = message.getType();
            if (UpdateStatus.PENDING_AUTHENTICATION.equals(status) && Messages.Type.ERROR.equals(type)) {
                writer.write(UpdateMessages.print(new Message(Messages.Type.INFO, message.getFormattedText())));
            } else {
                writer.write(UpdateMessages.print(message));
            }

            writer.write(separator);
        }
    }
}
