package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UpdatesParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesParser.class);

    private static final Splitter LINE_SPLITTER = Splitter.on('\n');
    private static final Pattern DELETE_PATTERN = Pattern.compile("(?i)^delete:\\s*(.*)\\s*$");
    private static final Pattern OBJECT_PATTERN = Pattern.compile("(?ms)^[a-zA-Z0-9-]+:.+");

    private final ParagraphParser paragraphParser;

    @Autowired
    public UpdatesParser(final ParagraphParser paragraphParser) {
        this.paragraphParser = paragraphParser;
    }

    public List<Update> parse(final UpdateContext updateContext, final List<ContentWithCredentials> contentWithCredentials) {
        final List<Paragraph> paragraphs = Lists.newArrayList();
        for (final ContentWithCredentials next : contentWithCredentials) {
            paragraphs.addAll(paragraphParser.createParagraphs(next, updateContext));
        }

        final List<Update> updates = Lists.newArrayList();
        for (final Paragraph paragraph : paragraphs) {
            String content = paragraph.getContent();
            Operation operation = Operation.UNSPECIFIED;
            List<String> deleteReasons = Lists.newArrayList();

            final StringBuilder contentWithoutDeleteBuilder = new StringBuilder();
            for (String line : LINE_SPLITTER.split(content)) {
                final Matcher matcher = DELETE_PATTERN.matcher(line);
                if (matcher.find()) {
                    operation = Operation.DELETE;
                    deleteReasons.add(matcher.group(1));
                } else {
                    contentWithoutDeleteBuilder.append(line).append('\n');
                }
            }
            content = contentWithoutDeleteBuilder.toString().trim();

            final RpslObject rpslObject = parseRpslObject(content);
            if (rpslObject == null || rpslObject.getValuesForAttribute(AttributeType.SOURCE).isEmpty()) {
                updateContext.ignore(paragraph);
                continue;
            }

            updates.add(new Update(paragraph, operation, deleteReasons, rpslObject));
        }

        return updates;
    }

    private RpslObject parseRpslObject(final String content) {
        final Matcher objectMatcher = OBJECT_PATTERN.matcher(content);
        if (objectMatcher.find()) {
            final String rpslObjectString = objectMatcher.group(0);
            try {
                return RpslObject.parseFully(rpslObjectString);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Unable to parse {}", rpslObjectString);
            }
        }

        return null;
    }
}
