package net.ripe.db.whois.common;

import com.google.common.base.Splitter;

import javax.annotation.concurrent.Immutable;

/** Specialization of Messages to:
 * - add port43 comment signs
 * - add ending newline to formatted message (NB: this should not be there, it is an overlook during the first implementation of REST API) */
@Immutable
public class QueryMessage extends Message {
    private static final Splitter LINE_SPLITTER = Splitter.on('\n');

    public QueryMessage(final Messages.Type type, final String text, final Object... args) {
        this.type = type;
        // TODO: [AH] Drop ending newlines in query messages from REST API for simplicity and consistency with update/rest/etc... messages
        this.text = text + "\n";
        this.args = args;
        this.formattedText = formatMessage(text, args);
    }

    @Override
    protected String formatMessage(final String text, final Object[] args) {
        final String formattedMessage = super.formatMessage(text, args);
        final StringBuilder result = new StringBuilder(256);

        for (String line : LINE_SPLITTER.split(formattedMessage)) {
            if (line.length() == 0) {
                result.append("%\n");
            } else if (line.startsWith("ERROR:") || line.startsWith("WARNING:")) {
                result.append('%').append(line).append('\n');
            } else {
                result.append("% ").append(line).append('\n');
            }
        }

        return result.toString();
    }
}
