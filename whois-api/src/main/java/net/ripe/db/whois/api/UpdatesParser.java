package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.PgpSignedMessage;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UpdatesParser {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?im)^password:(.*)(?:\\n|$)");
    private static final Pattern OVERRIDE_PATTERN = Pattern.compile("(?im)^override:(.*)(?:\\n|$)");
    private static final Pattern DRY_RUN_PATTERN = Pattern.compile("(?im)^dry-run:.*(?:\\n|$)");
    private static final Pattern DELETE_PATTERN = Pattern.compile("(?im)^delete:(.*)(?:\\n|$)");

    private static final Splitter CONTENT_SPLITTER = Splitter.on(Pattern.compile("(?m)^$")).trimResults().omitEmptyStrings();

    public List<Paragraph> createParagraphs(final ContentWithCredentials contentWithCredentials, final UpdateContext updateContext) {
        String content = StringUtils.remove(contentWithCredentials.getContent(), '\r');

        if (DRY_RUN_PATTERN.matcher(content).find()) {
            updateContext.dryRun();
        }

        final Set<Credential> baseCredentials = getPasswordCredentials(content);
        baseCredentials.addAll(contentWithCredentials.getCredentials());

        final List<Paragraph> paragraphs = Lists.newArrayList();

        int offset = 0;

        while (offset < content.length()) {
            final Matcher signedMessageMatcher = PgpSignedMessage.SIGNED_MESSAGE_PATTERN.matcher(content).region(offset, content.length());
            if (signedMessageMatcher.find(offset)) {
                addPlainTextContent(baseCredentials, paragraphs, content, offset, signedMessageMatcher.start());
                offset = addSignedContent(baseCredentials, paragraphs, content, signedMessageMatcher.start());
            } else {
                offset = addPlainTextContent(baseCredentials, paragraphs, content, offset, content.length());
            }
        }

        return paragraphs;
    }

    private int addPlainTextContent(final Set<Credential> baseCredentials, final List<Paragraph> paragraphs, final String content, final int beginIndex, final int endIndex) {
        if (endIndex > beginIndex) {
            final String substring = content.substring(beginIndex, endIndex);
            if (StringUtils.isNotBlank(substring)) {
                addParagraphs(paragraphs, substring, baseCredentials);
            }
        }
        return endIndex + 1;
    }

    private int addSignedContent(final Set<Credential> baseCredentials, final List<Paragraph> paragraphs, final String content, final int beginIndex) {
        final Set<Credential> credentials = Sets.newLinkedHashSet(baseCredentials);

        int endIdx = -1;
        String signedContent = content.substring(beginIndex);

        while (true) {
            final Matcher matcher = PgpSignedMessage.SIGNED_MESSAGE_PATTERN.matcher(signedContent);
            if (matcher.find()) {
                if (endIdx == -1) {
                    endIdx = beginIndex + matcher.end() + 1;
                }

                try {
                    final String clearText = matcher.group(0);
                    final PgpCredential credential = PgpCredential.createOfferedCredential(clearText);
                    credentials.add(credential);
                    signedContent = credential.getContent();
                    if (StringUtils.isBlank(signedContent)) {
                        addPlainTextContent(baseCredentials, paragraphs, clearText, 0, clearText.length());
                    }
                } catch (IllegalArgumentException e) {
                    addPlainTextContent(baseCredentials, paragraphs, signedContent, 0, signedContent.length());
                }
            } else {
                addParagraphs(paragraphs, signedContent, credentials);
                break;
            }
        }
        return endIdx == -1 ? content.length() : endIdx;
    }

    private Set<Credential> getPasswordCredentials(final String content) {
        final Set<Credential> result = Sets.newLinkedHashSet();

        final Matcher matcher = PASSWORD_PATTERN.matcher(content);
        while (matcher.find()) {
            result.add(new PasswordCredential(matcher.group(1).trim()));
        }

        return result;
    }

    private void addParagraphs(final List<Paragraph> paragraphs, final String content, final Set<Credential> baseCredentials) {
        for (final String paragraph : CONTENT_SPLITTER.split(content)) {
            if (StringUtils.isNotEmpty(paragraph)) {
                final Set<Credential> credentials = Sets.newLinkedHashSet(baseCredentials);

                String cleanedParagraph = paragraph;

                cleanedParagraph = removePasswords(cleanedParagraph);
                cleanedParagraph = removeDryRun(cleanedParagraph);
                cleanedParagraph = extractOverride(credentials, cleanedParagraph);
                cleanedParagraph = cleanedParagraph.trim();

                // Also add empty paragraphs to detect dangling credentials
                paragraphs.add(new Paragraph(cleanedParagraph, new Credentials(credentials)));
            }
        }
    }

    private String removePasswords(final String paragraph) {
        return PASSWORD_PATTERN.matcher(paragraph).replaceAll("");
    }

    private String removeDryRun(final String paragraph) {
        return DRY_RUN_PATTERN.matcher(paragraph).replaceAll("");
    }

    private String extractOverride(final Set<Credential> credentials, final String paragraph) {
        final Matcher overrideMatcher = OVERRIDE_PATTERN.matcher(paragraph);
        while (overrideMatcher.find()) {
            credentials.add(OverrideCredential.parse(overrideMatcher.group(1).trim()));
        }

        return overrideMatcher.reset().replaceAll("");
    }

    public List<Update> parse(final UpdateContext updateContext, final List<ContentWithCredentials> contentWithCredentials) {
        final List<Paragraph> paragraphs = Lists.newArrayList();
        for (final ContentWithCredentials next : contentWithCredentials) {
            paragraphs.addAll(createParagraphs(next, updateContext));
        }

        final List<Update> updates = Lists.newArrayList();
        for (final Paragraph paragraph : paragraphs) {
            String content = paragraph.getContent();
            Operation operation = Operation.UNSPECIFIED;
            List<String> deleteReasons = Lists.newArrayList();

            final Matcher matcher = DELETE_PATTERN.matcher(content);
            while (matcher.find()) {
                operation = Operation.DELETE;
                deleteReasons.add(matcher.group(1).trim());
            }
            if (operation == operation.DELETE) {
                content = matcher.reset().replaceAll("");
            }

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
