package net.ripe.db.whois.update.domain;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import org.apache.commons.lang.StringUtils;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.FormatHelper.prettyPrint;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;

public final class Notification {

    public static enum Type {
        SUCCESS, SUCCESS_REFERENCE, FAILED_AUTHENTICATION, PENDING_UPDATE
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

    @Immutable
    public static class Update {
        private static final Map<Action, String> RESULT_MAP = Maps.newEnumMap(Action.class);
        private static final FilterAuthFunction filterAuthFunction = new FilterAuthFunction();

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
        private final PreparedUpdate update;
        private final int versionId;

        public Update(final PreparedUpdate update, UpdateContext updateContext) {
            this.referenceObject = filterAuthFunction.apply(update.getReferenceObject());
            this.updatedObject = filterAuthFunction.apply(update.getUpdatedObject());
            this.action = update.getAction().name();
            this.result = RESULT_MAP.get(update.getAction());
            this.update = update;

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

        public boolean isShowVersionInstruction() {
            return !(updatedObject.getType() == PERSON || updatedObject.getType() == ROLE);
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (that == null || getClass() != that.getClass()) return false;
            return ((Update) that).update == update;
        }

        @Override
        public int hashCode() {
            return update.hashCode();
        }
    }
}
