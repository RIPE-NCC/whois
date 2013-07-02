package net.ripe.db.whois.query.executor;

import com.google.common.collect.Sets;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.ripe.db.whois.common.FormatHelper.prettyPrint;

@Component
public class HelpQueryExecutor implements QueryExecutor {
    private static final int MAX_LINE_LENGTH = 80;
    private static final int IND_1 = 6;
    private static final int IND_2 = 12;

    private static final MessageObject HELP_RESPONSE;

    static final Set<QueryFlag> SKIPPED = Sets.newHashSet();

    static {
        final StringBuilder help = new StringBuilder();

        help.append("% NAME\n");
        help.append(prettyPrint("%", "whois query server", IND_1, MAX_LINE_LENGTH, true));
        help.append("%\n");
        help.append("% DESCRIPTION\n");
        help.append(prettyPrint("%", "The following options are available:", IND_1, MAX_LINE_LENGTH, true));
        help.append("%\n");

        for (final QueryFlag queryFlag : QueryFlag.values()) {
            if (SKIPPED.contains(queryFlag)) {
                continue;
            }

            final StringBuilder optionLineBuilder = new StringBuilder();
            optionLineBuilder.append(queryFlag);
            if (queryFlag.getSearchKey() != null) {
                optionLineBuilder.append(' ').append(queryFlag.getSearchKey());
            }

            help.append(prettyPrint("%", optionLineBuilder.toString(), IND_1, Integer.MAX_VALUE, true));
            help.append(prettyPrint("%", queryFlag.getDescription(), IND_2, MAX_LINE_LENGTH, true));
            help.append("%\n");
        }

        help.append("% SEE ALSO\n");
        help.append(prettyPrint("%", "[REF] RIPE Database Reference Manual.", IND_1, MAX_LINE_LENGTH, true));
        help.append(prettyPrint("%", "http://www.ripe.net/data-tools/support/documentation", IND_2, MAX_LINE_LENGTH, true));
        help.append("%\n");

        HELP_RESPONSE = new MessageObject(help.toString());
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(final Query query) {
        return !query.hasOptions() && query.isHelp();
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        responseHandler.handle(HELP_RESPONSE);
    }
}
