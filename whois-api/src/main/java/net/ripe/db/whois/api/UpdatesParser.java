package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
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

            RpslObject rpslObject = null;
            try {
                 rpslObject = RpslObject.parse(content);
                updates.add(new Update(paragraph, operation, deleteReasons, rpslObject));
            } catch (IllegalArgumentException e) {
                updateContext.ignore(paragraph);
            }
        }

        return updates;
    }
}
