package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;

import java.util.Collections;
import java.util.Map;

public class ObjectMessages {
    private final Messages messages = new Messages();
    private final Map<RpslAttribute, Messages> attributeMessages = Maps.newLinkedHashMap();

    public Messages getMessages() {
        return messages;
    }

    public Map<RpslAttribute, Messages> getAttributeMessages() {
        return Collections.unmodifiableMap(attributeMessages);
    }

    public boolean contains(final Message message) {
        return messages.getAllMessages().contains(message);
    }

    public void addMessage(final Message message) {
        messages.add(message);
    }

    public void addMessage(final RpslAttribute attribute, final Message message) {
        getMessages(attribute).add(message);
    }

    public Messages getMessages(final RpslAttribute attribute) {
        Messages attributeMessages = this.attributeMessages.get(attribute);
        if (attributeMessages == null) {
            attributeMessages = new Messages();
            this.attributeMessages.put(attribute, attributeMessages);
        }

        return attributeMessages;
    }

    public boolean hasErrors() {
        if (hasErrors(messages)) {
            return true;
        }

        for (final Messages msgs : attributeMessages.values()) {
            if (hasErrors(msgs)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasMessages() {
        if (messages.hasMessages()) {
            return true;
        }

        for (final Messages msgs : attributeMessages.values()) {
            if (msgs.hasMessages()) {
                return true;
            }
        }

        return false;
    }

    public int getErrorCount() {
        int count = messages.getErrors().size();

        for (final Messages msgs : attributeMessages.values()) {
            count += msgs.getErrors().size();
        }

        return count;
    }

    private boolean hasErrors(final Messages messages) {
        return !messages.getMessages(Messages.Type.ERROR).isEmpty();
    }

    public void addAll(final ObjectMessages objectMessages) {
        messages.addAll(objectMessages.messages);

        for (final Map.Entry<RpslAttribute, Messages> entry : objectMessages.attributeMessages.entrySet()) {
            getMessages(entry.getKey()).addAll(entry.getValue());
        }
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (Message message : messages.getMessages(Messages.Type.ERROR)) {
            ret.append(", ").append(message.toString());
        }

        for (Map.Entry<RpslAttribute, Messages> entry : attributeMessages.entrySet()) {
            for (Message message : entry.getValue().getMessages(Messages.Type.ERROR)) {
                ret.append(", ").append(entry.getKey().getType().getName()).append(": ").append(message.toString());
            }
        }

        if (ret.length() == 0) {
            return "";
        }

        return ret.substring(1);
    }
}
