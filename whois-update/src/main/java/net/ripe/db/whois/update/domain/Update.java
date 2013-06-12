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

    // TODO: [AH] these 3 should be moved to preparedupdate?
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

    public boolean isOverride() {
        return paragraph.getCredentials().has(OverrideCredential.class);
    }

    public boolean isDryRun() {
        return paragraph.isDryRun();
    }

    public Credentials getCredentials() {
        return paragraph.getCredentials();
    }
}
