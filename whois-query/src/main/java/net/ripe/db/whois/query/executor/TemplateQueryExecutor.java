package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.query.Query;
import org.springframework.stereotype.Component;

@Component
public class TemplateQueryExecutor implements QueryExecutor {

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(final Query query) {
        return (query.isTemplate() || query.isVerbose());
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        final String objectTypeString = query.isTemplate() ? query.getTemplateOption() : query.getVerboseOption();
        final ObjectType objectType = ObjectType.getByNameOrNull(objectTypeString);

        final MessageObject messageObject;
        if (objectType == null) {
            messageObject = new MessageObject(QueryMessages.invalidObjectType(objectTypeString));
        } else {
            messageObject = new MessageObject(query.isTemplate() ? getTemplate(objectType) : getVerbose(objectType));
        }

        responseHandler.handle(messageObject);
    }

    private String getTemplate(final ObjectType objectType) {
        return ObjectTemplate.getTemplate(objectType).toString();
    }

    private String getVerbose(final ObjectType objectType) {
        return ObjectTemplate.getTemplate(objectType).toVerboseString();
    }
}
