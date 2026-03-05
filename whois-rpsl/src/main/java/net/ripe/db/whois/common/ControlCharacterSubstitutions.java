package net.ripe.db.whois.common;

import java.util.List;

public class ControlCharacterSubstitutions {

    private static final List<Character> SUBSTITUTIONS = List.of(
            '\u000b',
            '\u000c',
            '\u007F',
            '\u008f',
            '\u0008'
    );
    public static final char CHARACTER_REPLACEMENT = '?';

    public static char substitute(final Character input) {
        return SUBSTITUTIONS.contains(input) ? CHARACTER_REPLACEMENT : input;
    }
}
