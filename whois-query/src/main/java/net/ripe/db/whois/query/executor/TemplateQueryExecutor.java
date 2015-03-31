package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectDocumentation;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TemplateQueryExecutor implements QueryExecutor {

    @Autowired
    private final TimestampsMode timestampsMode;

    //TODO [TP] remove when timestamps are always on
    @Autowired
    public TemplateQueryExecutor(final TimestampsMode timestampsMode) {
        this.timestampsMode = timestampsMode;
    }

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
        //TODO [TP] remove when timestamps are always on
        if (timestampsMode.isTimestampsOff()){
            executeTimestampsOff(query, responseHandler);
            return;
        }

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





    //TODO [TP] remove when timestamps are always on
    // This code below is horrible because it is almost the same as the code in object template.
    // I did not make it elegant, or refactor it because it needs to be removed as is, when the feature is always on.
    public void executeTimestampsOff(final Query query, final ResponseHandler responseHandler) {
        final String objectTypeString = query.isTemplate() ? query.getTemplateOption() : query.getVerboseOption();
        final ObjectType objectType = ObjectType.getByNameOrNull(objectTypeString);

        final MessageObject messageObject;
        if (objectType == null) {
            messageObject = new MessageObject(QueryMessages.invalidObjectType(objectTypeString));
        } else {
            messageObject = new MessageObject(query.isTemplate() ? printTemplateTimestampsOff(objectType) : printVerboseTemplateTimestampsOff(objectType));
        }

        responseHandler.handle(messageObject);
    }

    public String printTemplateTimestampsOff(final ObjectType objectType) {
        final ObjectTemplate template = ObjectTemplate.getTemplate(objectType);

        final StringBuilder result = new StringBuilder();

        for (final AttributeTemplate attributeTemplate : template.getAttributeTemplates()) {
            if (attributeTemplate.getAttributeType() == AttributeType.CREATED
                    || attributeTemplate.getAttributeType() == AttributeType.LAST_MODIFIED) {
                continue;
            }
            result.append(attributeTemplate).append('\n');
        }

        return result.toString();
    }

    public String printVerboseTemplateTimestampsOff(final ObjectType objectType) {
        final ObjectTemplate template = ObjectTemplate.getTemplate(objectType);

        final StringBuilder result = new StringBuilder();

        result.append("The ")
                .append(objectType.getName())
                .append(" class:\n\n")
                .append(ObjectDocumentation.getDocumentation(objectType))
                .append('\n')
                .append(printTemplateTimestampsOff(objectType))
                .append("\nThe content of the attributes of the ")
                .append(objectType.getName())
                .append(" class are defined below:\n\n");

        for (final AttributeTemplate attributeTemplate : template.getAttributeTemplates()) {

            if (attributeTemplate.getAttributeType() == AttributeType.CREATED
                    || attributeTemplate.getAttributeType() == AttributeType.LAST_MODIFIED) {
                continue;
            }

            final AttributeType attributeType = attributeTemplate.getAttributeType();

            String attributeDescription = attributeType.getDescription(objectType);
            if (attributeDescription.indexOf('\n') == -1) {
                attributeDescription = WordUtils.wrap(attributeDescription, 70);
            }

            if (attributeDescription.endsWith("\n")) {
                attributeDescription = attributeDescription.substring(0, attributeDescription.length() - 1);
            }

            String syntaxDescription = attributeType.getSyntax().getDescription(objectType);
            if (syntaxDescription.endsWith("\n")) {
                syntaxDescription = syntaxDescription.substring(0, syntaxDescription.length() - 1);
            }

            result.append(attributeType.getName())
                    .append("\n\n   ")
                    .append(attributeDescription.replaceAll("\n", "\n   "))
                    .append("\n\n     ")
                    .append(syntaxDescription.replaceAll("\n", "\n     "))
                    .append("\n\n");
        }

        return result.toString();
    }




}
