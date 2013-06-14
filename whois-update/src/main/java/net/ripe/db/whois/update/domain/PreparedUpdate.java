package net.ripe.db.whois.update.domain;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class PreparedUpdate implements UpdateContainer {
    private final Update update;
    private final RpslObject originalObject;
    private final RpslObject updatedObject;
    private final Action action;
    private final OverrideOptions overrideOptions;

    public PreparedUpdate(final Update update, @Nullable final RpslObject originalObject, final RpslObject updatedObject, final Action action) {
        this(update, originalObject, updatedObject, action, OverrideOptions.NONE);
    }

    public PreparedUpdate(final Update update, @Nullable final RpslObject originalObject, final RpslObject updatedObject, final Action action, final OverrideOptions overrideOptions) {
        this.update = update;
        this.originalObject = originalObject;
        this.updatedObject = updatedObject;
        this.action = action;
        this.overrideOptions = overrideOptions;
    }

    @Override
    public Update getUpdate() {
        return update;
    }

    public Paragraph getParagraph() {
        return update.getParagraph();
    }

    public boolean hasOriginalObject() {
        return originalObject != null;
    }

    public RpslObject getReferenceObject() {
        if (originalObject != null) {
            return originalObject;
        }

        return updatedObject;
    }

    public RpslObject getUpdatedObject() {
        return updatedObject;
    }

    public Action getAction() {
        return action;
    }

    public Credentials getCredentials() {
        return update.getCredentials();
    }

    public boolean isOverride() {
        return update.isOverride();
    }

    public OverrideOptions getOverrideOptions() {
        return overrideOptions;
    }

    public ObjectType getType() {
        return update.getType();
    }

    public String getKey() {
        return updatedObject.getKey().toString();
    }

    public String getFormattedKey() {
        return updatedObject.getFormattedKey();
    }

    public Set<CIString> getNewValues(final AttributeType attributeType) {
        final Set<CIString> newValues = updatedObject.getValuesForAttribute(attributeType);
        if (originalObject != null) {
            newValues.removeAll(originalObject.getValuesForAttribute(attributeType));
        }

        return newValues;
    }

    public Set<CIString> getDifferences(final AttributeType attributeType) {
        Set<CIString> differences = updatedObject.getValuesForAttribute(attributeType);
        if (originalObject != null && !Action.DELETE.equals(action)) {
            differences = Sets.symmetricDifference(differences, originalObject.getValuesForAttribute(attributeType));
        }

        return differences;
    }

    @Override
    public String toString() {
        return "PreparedUpdate{" + action + " " + updatedObject.getTypeAttribute() + '}';
    }
}
