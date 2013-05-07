package net.ripe.db.whois.update.domain;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.Nonnull;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public enum Keyword {
    NEW("new", "NEW", true),
    DIFF("diff", "DIFF", true),
    HELP("help", "HELP", false),
    HOWTO("howto", "HELP", false),
    NONE(null, "", true);

    private static final Map<CIString, Keyword> OPERATIONS_BY_KEYWORD = Maps.newHashMapWithExpectedSize(Keyword.values().length);

    static {
        for (final Keyword keyword : Keyword.values()) {
            if (keyword.getKeyword() != null) {
                OPERATIONS_BY_KEYWORD.put(ciString(keyword.getKeyword()), keyword);
            }
        }
    }

    public static Keyword getByKeyword(@Nonnull final String keyword) {
        return OPERATIONS_BY_KEYWORD.get(ciString(keyword));
    }

    private final String keyword;
    private final String action;
    private boolean contentExpected;

    private Keyword(final String keyword, final String action, final boolean contentExpected) {
        this.keyword = keyword;
        this.action = action;
        this.contentExpected = contentExpected;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getAction() {
        return action;
    }

    public boolean isContentExpected() {
        return contentExpected;
    }
}