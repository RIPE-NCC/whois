package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: mp_import.flex

  description:
    Defines the tokenizer for an RPSLng mp-import attribute. Derived from
    import.l.

  notes:
    Tokens are defined in the associated grammar, mp_import.y.
*/

%%

%public
%class MpImportLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private MpImportParser yyparser;

    /* constructor taking an additional parser object */
    public MpImportLexer(java.io.Reader r, MpImportParser yyparser) {
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
PROTOCOL_NAME  = BGP4|MPBGP|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
AFI            = AFI
AFIVALUE_V4    = IPV4|IPV4\.UNICAST|IPV4\.MULTICAST
AFIVALUE_V6    = IPV6|IPV6\.UNICAST|IPV6\.MULTICAST
AFIVALUE_ANY   = ANY\.UNICAST|ANY\.MULTICAST
ALNUM          = [0-9a-zA-Z]
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

/* keywords */

[ \t\n]+    { ; }

OR    { return MpImportParser.OP_OR; }
AND   { return MpImportParser.OP_AND; }
NOT   { return MpImportParser.OP_NOT; }
==    { return MpImportParser.OP_COMPARE; }
=     { return MpImportParser.OP_EQUAL; }
\.=   { return MpImportParser.OP_APPEND; }

\^-   { return MpImportParser.OP_MS; }
\^\+  { return MpImportParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return MpImportParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return MpImportParser.OP_MS;
}

ANY     { return MpImportParser.KEYW_ANY; }
PEERAS  { return MpImportParser.KEYW_PEERAS; }

FROM      { return MpImportParser.KEYW_FROM; }
ACTION    { return MpImportParser.KEYW_ACTION; }
IGP_COST  { return MpImportParser.KEYW_IGP_COST; }
SELF      { return MpImportParser.KEYW_SELF; }
PREPEND   { return MpImportParser.KEYW_PREPEND; }
APPEND    { return MpImportParser.KEYW_APPEND; }
DELETE    { return MpImportParser.KEYW_DELETE; }
CONTAINS  { return MpImportParser.KEYW_CONTAINS; }
ACCEPT    { return MpImportParser.KEYW_ACCEPT; }

INTERNET      { return MpImportParser.KEYW_INTERNET; }
NO_EXPORT     { return MpImportParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return MpImportParser.KEYW_NO_ADVERTISE; }

AT          { return MpImportParser.KEYW_AT; }
PROTOCOL    { return MpImportParser.KEYW_PROTOCOL; }
INTO        { return MpImportParser.KEYW_INTO; }
REFINE      { return MpImportParser.KEYW_REFINE; }
EXCEPT      { return MpImportParser.KEYW_EXCEPT; }

{PROTOCOL_NAME} { return MpImportParser.TKN_PROTOCOL; }

{AFI}       { return MpImportParser.KEYW_AFI; }

{AFIVALUE_V4}  { return MpImportParser.KEYW_AFI_VALUE_V4; }
{AFIVALUE_V6}  { return MpImportParser.KEYW_AFI_VALUE_V6; }
{AFIVALUE_ANY} { return MpImportParser.KEYW_AFI_VALUE_ANY; }

PREF        { return MpImportParser.TKN_PREF; }
MED         { return MpImportParser.TKN_MED; }
DPA         { return MpImportParser.TKN_DPA; }
ASPATH      { return MpImportParser.TKN_ASPATH; }
COMMUNITY   { return MpImportParser.TKN_COMMUNITY; }
NEXT_HOP    { return MpImportParser.TKN_NEXT_HOP; }
COST        { return MpImportParser.TKN_COST; }

\~\*            { return MpImportParser.ASPATH_POSTFIX; }
\~\+            { return MpImportParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return MpImportParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return MpImportParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return MpImportParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpImportParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return MpImportParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return MpImportParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return MpImportParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return MpImportParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return MpImportParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return MpImportParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpImportParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpImportParser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return MpImportParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpImportParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpImportParser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpImportParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpImportParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpImportParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return MpImportParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return MpImportParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return MpImportParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
