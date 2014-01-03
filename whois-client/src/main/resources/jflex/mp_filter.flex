package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: mp_filter.l

  description:
    Defines the tokenizer for an RPSLng mp-filter attribute. Derived
    from filter.l.

  notes:
    Tokens are defined in the associated grammar, mp_filter.y.

  $Id: mp_filter.l,v 1.1.14.1 2006/09/29 12:32:29 katie Exp $
*/

%%

%class MpFilterLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private MpFilterParser yyparser;

    /* constructor taking an additional parser object */
    public MpFilterLexer(java.io.Reader r, MpFilterParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
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
COMM_NO        = {INT}:{INT}


%%

[ \t\n]+    { ; }

OR    { return MpFilterParser.OP_OR; }
AND   { return MpFilterParser.OP_AND; }
NOT   { return MpFilterParser.OP_NOT; }
=     { return MpFilterParser.OP_EQUAL; }
==    { return MpFilterParser.OP_COMPARE; }
\.=   { return MpFilterParser.OP_APPEND; }


\^-         { return MpFilterParser.OP_MS; }
\^\+        { return MpFilterParser.OP_MS; }
\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return MpFilterParser.OP_MS_V6;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return MpFilterParser.OP_MS_V6;
}


ANY     { return MpFilterParser.KEYW_ANY; }
PEERAS  { return MpFilterParser.KEYW_PEERAS; }

IGP_COST  { return MpFilterParser.KEYW_IGP_COST; }
SELF      { return MpFilterParser.KEYW_SELF; }
PREPEND   { return MpFilterParser.KEYW_PREPEND; }
APPEND    { return MpFilterParser.KEYW_APPEND; }
DELETE    { return MpFilterParser.KEYW_DELETE; }
CONTAINS  { return MpFilterParser.KEYW_CONTAINS; }

INTERNET      { return MpFilterParser.KEYW_INTERNET; }
NO_EXPORT     { return MpFilterParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return MpFilterParser.KEYW_NO_ADVERTISE; }

PREF        { return MpFilterParser.TKN_PREF; }
MED         { return MpFilterParser.TKN_MED; }
DPA         { return MpFilterParser.TKN_DPA; }
ASPATH      { return MpFilterParser.TKN_ASPATH; }
COMMUNITY   { return MpFilterParser.TKN_COMMUNITY; }
NEXT_HOP    { return MpFilterParser.TKN_NEXT_HOP; }
COST        { return MpFilterParser.TKN_COST; }

\~\*            { return MpFilterParser.ASPATH_POSTFIX; }
\~\+            { return MpFilterParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return MpFilterParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return MpFilterParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return MpFilterParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpFilterParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return MpFilterParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return MpFilterParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return MpFilterParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return MpFilterParser.TKN_RSNAME;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return MpFilterParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpFilterParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpFilterParser.TKN_PRFXV6DC;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return MpFilterParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpFilterParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpFilterParser.TKN_PRFXV6DCRNG;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpFilterParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpFilterParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpFilterParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return MpFilterParser.TKN_COMM_NO;
}

{INT} {
    return MpFilterParser.TKN_INT;
}

. {
    return yytext().charAt(0);
}

