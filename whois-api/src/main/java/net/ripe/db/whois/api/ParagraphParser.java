package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.keycert.PgpSignedMessage;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ParagraphParser {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?im)^password:\\s*(.*)\\s*");
    private static final Pattern OVERRIDE_PATTERN = Pattern.compile("(?im)^override:\\s*(.*)\\s*");
    private static final Pattern DRY_RUN_PATTERN = Pattern.compile("(?im)^dry-run:\\s*(.*)\\s*");

    private static final Splitter CONTENT_SPLITTER = Splitter.on(Pattern.compile("\\n[ \\t]*\\n")).omitEmptyStrings();

    public List<Paragraph> createParagraphs(final ContentWithCredentials contentWithCredentials) {
        final String content = contentWithCredentials.getContent().replaceAll("\\r\\n", "\n");

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
                cleanedParagraph = extractOverride(credentials, cleanedParagraph);

                final Matcher dryRunMatcher = DRY_RUN_PATTERN.matcher(cleanedParagraph);
                final boolean dryRun = dryRunMatcher.find();
                if (dryRun) {
                    cleanedParagraph = dryRunMatcher.reset().replaceAll("");
                }

                paragraphs.add(new Paragraph(cleanedParagraph.trim(), new Credentials(credentials), dryRun));
            }
        }
    }

    private String removePasswords(final String paragraph) {
        return PASSWORD_PATTERN.matcher(paragraph).replaceAll("");
    }

    private String extractOverride(final Set<Credential> credentials, final String paragraph) {
        final Matcher overrideMatcher = OVERRIDE_PATTERN.matcher(paragraph);
        while (overrideMatcher.find()) {
            credentials.add(OverrideCredential.parse(overrideMatcher.group(1).trim()));
        }

        return overrideMatcher.reset().replaceAll("");
    }
}
