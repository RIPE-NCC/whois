package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: mp_default.flex

  description:
    Defines the tokenizer for an RPSLng mp-default attribute. Derived
   from default.l.

  notes:
    Tokens are defined in the associated grammar, mp_default.y.
*/

%%

%public
%class MpDefaultLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private MpDefaultParser yyparser;

    /* constructor taking an additional parser object */
    public MpDefaultLexer(java.io.Reader r, MpDefaultParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

/* macro definitions */

ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
FLTRNAME       = FLTR-[a-zA-Z0-9_-]*[a-zA-Z0-9]
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RSNAME         = RS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
INT            = [0-9]+
QUAD           = [0-9a-fA-F]{1,4}
IPV4           = {INT}(\.{INT}){3}
IPV6           = {QUAD}(:{QUAD}){7}
IPV6DC         = (({QUAD}:){0,6}{QUAD})?::({QUAD}(:{QUAD}){0,6})?
PRFXV4         = {IPV4}\/{INT}
PRFXV6         = {IPV6}\/{INT}
PRFXV6DC       = {IPV6DC}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PRFXV6RNG      = {PRFXV6}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PRFXV6DCRNG    = {PRFXV6DC}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO        = {INT}:{INT}
AFI            = AFI
AFIVALUE_V4    = IPV4|IPV4\.UNICAST|IPV4\.MULTICAST
AFIVALUE_V6    = IPV6|IPV6\.UNICAST|IPV6\.MULTICAST
AFIVALUE_ANY   = ANY|ANY\.UNICAST|ANY\.MULTICAST
ALNUM          = [0-9a-zA-Z]
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

/* keywords */

[ \t\n]+    { ; }

OR    { return MpDefaultParser.OP_OR; }
AND   { return MpDefaultParser.OP_AND; }
NOT   { return MpDefaultParser.OP_NOT; }
==    { return MpDefaultParser.OP_COMPARE; }
=     { return MpDefaultParser.OP_EQUAL; }
\.=   { return MpDefaultParser.OP_APPEND; }

\^-   { return MpDefaultParser.OP_MS; }
\^\+  { return MpDefaultParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return MpDefaultParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return MpDefaultParser.OP_MS;
}

ANY     { return MpDefaultParser.KEYW_ANY; }
PEERAS  { return MpDefaultParser.KEYW_PEERAS; }

TO        { return MpDefaultParser.KEYW_TO; }
ACTION    { return MpDefaultParser.KEYW_ACTION; }
NETWORKS  { return MpDefaultParser.KEYW_NETWORKS; }
IGP_COST  { return MpDefaultParser.KEYW_IGP_COST; }
SELF      { return MpDefaultParser.KEYW_SELF; }
PREPEND   { return MpDefaultParser.KEYW_PREPEND; }
APPEND    { return MpDefaultParser.KEYW_APPEND; }
DELETE    { return MpDefaultParser.KEYW_DELETE; }
CONTAINS  { return MpDefaultParser.KEYW_CONTAINS; }

INTERNET      { return MpDefaultParser.KEYW_INTERNET; }
NO_EXPORT     { return MpDefaultParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return MpDefaultParser.KEYW_NO_ADVERTISE; }

AT          { return MpDefaultParser.KEYW_AT; }

{AFI}       { return MpDefaultParser.KEYW_AFI; }

{AFIVALUE_V4}  { return MpDefaultParser.KEYW_AFI_VALUE_V4; }
{AFIVALUE_V6}  { return MpDefaultParser.KEYW_AFI_VALUE_V6; }

PREF        { return MpDefaultParser.TKN_PREF; }
MED         { return MpDefaultParser.TKN_MED; }
DPA         { return MpDefaultParser.TKN_DPA; }
ASPATH      { return MpDefaultParser.TKN_ASPATH; }
COMMUNITY   { return MpDefaultParser.TKN_COMMUNITY; }
NEXT_HOP    { return MpDefaultParser.TKN_NEXT_HOP; }
COST        { return MpDefaultParser.TKN_COST; }

\~\*            { return MpDefaultParser.ASPATH_POSTFIX; }
\~\+            { return MpDefaultParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return MpDefaultParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return MpDefaultParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return MpDefaultParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpDefaultParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return MpDefaultParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return MpDefaultParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return MpDefaultParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return MpDefaultParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return MpDefaultParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return MpDefaultParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpDefaultParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpDefaultParser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return MpDefaultParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpDefaultParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpDefaultParser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpDefaultParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpDefaultParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpDefaultParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return MpDefaultParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return MpDefaultParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return MpDefaultParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
