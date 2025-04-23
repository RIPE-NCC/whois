package net.ripe.db.whois.update.domain;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.ripe.db.whois.common.FormatHelper.prettyPrint;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;

public final class Notification {

    public enum Type {
        SUCCESS, SUCCESS_REFERENCE, FAILED_AUTHENTICATION
    }

    private final String email;
    private final Map<Type, Set<Update>> updates;

    public Notification(final String email) {
        this.email = email;
        this.updates = Maps.newEnumMap(Type.class);

        for (final Type notificationType : Type.values()) {
            updates.put(notificationType, Sets.<Update>newLinkedHashSet());
        }
    }

    public void add(final Type type, final PreparedUpdate update, UpdateContext updateContext) {
        updates.get(type).add(new Update(update, updateContext));
    }

    public String getEmail() {
        return email;
    }

    public Set<Update> getUpdates(final Type type) {
        return updates.get(type);
    }

    public boolean has(final Type type) {
        return !updates.get(type).isEmpty();
    }

    /**
     * Was override used (for at least 1 update)?
     * @return boolean
     */
    public boolean isOverrideUsed() {
        return updates.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(update -> update.getPreparedUpdate().isOverride());
    }

    @SuppressWarnings("UnusedDeclaration")
    @Immutable
    public static class Update {
        private static final Map<Action, String> RESULT_MAP = Maps.newEnumMap(Action.class);
        private static final FilterAuthFunction filterAuthFunction = new FilterAuthFunction();
        private static final FilterChangedFunction filterChangedFunction = new FilterChangedFunction();

        static {
            RESULT_MAP.put(Action.CREATE, "CREATED");
            RESULT_MAP.put(Action.MODIFY, "MODIFIED");
            RESULT_MAP.put(Action.DELETE, "DELETED");
            RESULT_MAP.put(Action.NOOP, "UNCHANGED");
        }

        private final RpslObject referenceObject;
        private final RpslObject updatedObject;
        private final String action;
        private final String result;
        private final String reason;
        private final PreparedUpdate preparedUpdate;
        private final int versionId;

        public Update(final PreparedUpdate update, final UpdateContext updateContext) {
            this.referenceObject = filterChangedFunction.apply(filterAuthFunction.apply(update.getReferenceObject()));
            this.updatedObject = filterChangedFunction.apply(filterAuthFunction.apply(update.getUpdatedObject()));
            this.action = update.getAction().name();
            this.result = RESULT_MAP.get(update.getAction());
            this.preparedUpdate = update;

            String updateReason = StringUtils.join(update.getUpdate().getDeleteReasons(), ", ");
            if (StringUtils.isNotEmpty(updateReason)) {
                updateReason = prettyPrint(String.format("***%s: ", Messages.Type.INFO), updateReason, 12, 80);
            }

            versionId = updateContext.getVersionId(update);

            this.reason = updateReason;
        }

        public RpslObject getReferenceObject() {
            return referenceObject;
        }

        public RpslObject getUpdatedObject() {
            return updatedObject;
        }

        public boolean isReplacement() {
            return !referenceObject.equals(updatedObject);
        }

        public String getAction() {
            return action;
        }

        public String getResult() {
            return result;
        }

        public String getReason() {
            return reason;
        }

        public String getDiff() {
            return RpslObjectFilter.diff(referenceObject, updatedObject);
        }

        public int getVersionId() {
            return versionId;
        }

        public String getPKey() {
            return updatedObject.getKey().toString();
        }

        public PreparedUpdate getPreparedUpdate() {
            return preparedUpdate;
        }

        public boolean isShowVersionInstruction() {
            return !(updatedObject.getType() == PERSON || updatedObject.getType() == ROLE);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            final Update that = (Update) obj;

            return Objects.equals(preparedUpdate, that.preparedUpdate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(preparedUpdate);
        }
    }
}
