package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;

import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public class Update implements UpdateContainer {
    private final Paragraph paragraph;
    private final Operation operation;

    private final List<String> deleteReasons;
    private final RpslObject submittedObject;
    private final RpslObjectUpdateInfo submittedObjectInfo;

    public Update(final Paragraph paragraph, final Operation operation, final List<String> deleteReasons, final RpslObject submittedObject, final RpslObjectUpdateInfo submittedObjectInfo) {
        Validate.notNull(paragraph, "paragraph cannot be null");
        Validate.notNull(operation, "operation cannot be null");
        Validate.notNull(submittedObject, "submittedObject cannot be null");
        Validate.notNull(submittedObject.getType(), "submittedObject type cannot be null");

        this.paragraph = paragraph;
        this.operation = operation;
        this.deleteReasons = deleteReasons;
        this.submittedObject = submittedObject;
        this.submittedObjectInfo = submittedObjectInfo;
    }

    public Update(final Paragraph paragraph, final Operation operation, final List<String> deleteReasons, final RpslObject submittedObject) {
        this(paragraph, operation, deleteReasons, submittedObject, null);
    }

    @Override
    public Update getUpdate() {
        return this;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<String> getDeleteReasons() {
        return deleteReasons;
    }

    public ObjectType getType() {
        return submittedObject.getType();
    }

    public RpslObject getSubmittedObject() {
        return submittedObject;
    }

    public RpslObjectUpdateInfo getSubmittedObjectInfo() {
        return submittedObjectInfo;
    }

    public boolean isSigned() {
        return paragraph.getCredentials().has(PgpCredential.class) || paragraph.getCredentials().has(X509Credential.class);
    }

    /** checks presence of credential only */
    public boolean isOverride() {
        return paragraph.getCredentials().has(OverrideCredential.class);
    }

    public Credentials getCredentials() {
        return paragraph.getCredentials();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("PARAGRAPH:\n\n").append(paragraph.getContent());
        if (!(builder.charAt(builder.length() - 1) == '\n')) {
            builder.append('\n');
        }
        builder.append("\nOBJECT:\n\n").append(submittedObject);
        if (!(builder.charAt(builder.length() - 1) == '\n')) {
            builder.append('\n');
        }
        if (operation == Operation.DELETE) {
            builder.append("\n\n");
            for (String reason : deleteReasons) {
                builder.append("REASON = ").append(reason).append('\n');
            }
        }
        if (submittedObjectInfo != null) {
            builder.append("\nOBJECT INFO:\n\n").append(submittedObjectInfo.toString()).append('\n');
        }

        return builder.toString();
    }
}
