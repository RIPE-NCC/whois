package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: v6_filter.flex

  description:
    Defines the tokenizer for an RPSLng mp-filter attribute. Derived
    from mp_filter.flex.

  notes:
    Tokens are defined in the associated grammar, v6_filter.y.
*/

%%

%public
%class V6FilterLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private V6FilterParser yyparser;

    /* constructor taking an additional parser object */
    public V6FilterLexer(java.io.Reader r, V6FilterParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
FLTRNAME       = FLTR-[A-Za-z0-9_-]*[A-Za-z0-9]
ASNAME         = AS-[A-Za-z0-9_-]*[A-Za-z0-9]
RSNAME         = RS-[A-Za-z0-9_-]*[A-Za-z0-9]
PRNGNAME       = PRNG-[A-Za-z0-9_-]*[A-Za-z0-9]
RTRSNAME       = RTRS-[A-Za-z0-9_-]*[A-Za-z0-9]
INT            = [0-9]+
QUAD           = [0-9A-Fa-f]{1,4}
IPV4           = {INT}(\.{INT}){3}
IPV6           = {QUAD}(:{QUAD}){7}
IPV6DC         = (({QUAD}:){0,6}{QUAD})?::({QUAD}(:{QUAD}){0,6})?
PRFXV4         = {IPV4}\/{INT}
PRFXV6         = {IPV6}\/{INT}
PRFXV6DC       = {IPV6DC}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PRFXV6RNG      = {PRFXV6}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PRFXV6DCRNG    = {PRFXV6DC}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO32      = {INT}.{INT}:{INT}
COMM_NO16      = {INT}:{INT}
COMM_NO        = {COMM_NO32}|{COMM_NO16}
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }

OR    { return V6FilterParser.OP_OR; }
AND   { return V6FilterParser.OP_AND; }
NOT   { return V6FilterParser.OP_NOT; }
=     { return V6FilterParser.OP_EQUAL; }
==    { return V6FilterParser.OP_COMPARE; }
\.=   { return V6FilterParser.OP_APPEND; }

\^-         { return V6FilterParser.OP_MS; }
\^\+        { return V6FilterParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return V6FilterParser.OP_MS_V6;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return V6FilterParser.OP_MS_V6;
}

ANY     { return V6FilterParser.KEYW_ANY; }
PEERAS  { return V6FilterParser.KEYW_PEERAS; }

IGP_COST  { return V6FilterParser.KEYW_IGP_COST; }
SELF      { return V6FilterParser.KEYW_SELF; }
PREPEND   { return V6FilterParser.KEYW_PREPEND; }
APPEND    { return V6FilterParser.KEYW_APPEND; }
DELETE    { return V6FilterParser.KEYW_DELETE; }
CONTAINS  { return V6FilterParser.KEYW_CONTAINS; }

INTERNET      { return V6FilterParser.KEYW_INTERNET; }
NO_EXPORT     { return V6FilterParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return V6FilterParser.KEYW_NO_ADVERTISE; }

PREF        { return V6FilterParser.TKN_PREF; }
MED         { return V6FilterParser.TKN_MED; }
DPA         { return V6FilterParser.TKN_DPA; }
ASPATH      { return V6FilterParser.TKN_ASPATH; }
COMMUNITY   { return V6FilterParser.TKN_COMMUNITY; }
NEXT_HOP    { return V6FilterParser.TKN_NEXT_HOP; }
COST        { return V6FilterParser.TKN_COST; }

\~\*            { return V6FilterParser.ASPATH_POSTFIX; }
\~\+            { return V6FilterParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return V6FilterParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return V6FilterParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return V6FilterParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return V6FilterParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return V6FilterParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return V6FilterParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return V6FilterParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return V6FilterParser.TKN_RSNAME;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return V6FilterParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return V6FilterParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return V6FilterParser.TKN_PRFXV6DC;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return V6FilterParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return V6FilterParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return V6FilterParser.TKN_PRFXV6DCRNG;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return V6FilterParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return V6FilterParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return V6FilterParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return V6FilterParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return V6FilterParser.TKN_INT;
}

. {
    return yytext().charAt(0);
}
