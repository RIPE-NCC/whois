package net.ripe.db.whois.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class Messages {
    private Map<Type, Set<Message>> messages = Maps.newEnumMap(Type.class);

    public Messages() {
    }

    public Messages(final Message... messages) {
        for (Message message : messages) {
            add(message);
        }
    }

    public void add(final Message message) {
        Set<Message> messageSet = messages.get(message.getType());
        if (messageSet == null) {
            messageSet = Sets.newLinkedHashSet();
            messages.put(message.getType(), messageSet);
        }

        messageSet.add(message);
    }

    public void remove(final Message message) {
        final Set<Message> messageSet = messages.get(message.getType());
        if (messageSet == null) {
            return;
        }

        messageSet.remove(message);
    }

    public void addAll(final Messages otherMessages) {
        for (final Map.Entry<Type, Set<Message>> otherEntry : otherMessages.messages.entrySet()) {
            final Type type = otherEntry.getKey();
            final Set<Message> messageSet = messages.get(type);
            final Set<Message> otherMessageSet = otherEntry.getValue();
            if (messageSet == null) {
                messages.put(type, otherMessageSet);
            } else {
                messageSet.addAll(otherMessageSet);
            }
        }
    }

    public Collection<Message> getAllMessages() {
        final Set<Message> result = Sets.newLinkedHashSet();

        for (final Map.Entry<Type, Set<Message>> messageEntry : messages.entrySet()) {
            result.addAll(messageEntry.getValue());
        }

        return result;
    }

    public Collection<Message> getMessages(final Type type) {
        final Set<Message> messageSet = messages.get(type);
        if (messageSet == null) {
            return Collections.emptyList();
        }

        return messageSet;
    }

    public boolean hasMessages() {
        for (final Set<Message> messageSet : messages.values()) {
            if (!messageSet.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public Collection<Message> getInfos() {
        return getMessages(Type.INFO);
    }

    public Collection<Message> getWarnings() {
        return getMessages(Type.WARNING);
    }

    public Collection<Message> getErrors() {
        return getMessages(Type.ERROR);
    }

    public enum Type {
        ERROR("Error"),
        WARNING("Warning"),
        INFO("Info");

        private final String string;

        private Type(final String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
