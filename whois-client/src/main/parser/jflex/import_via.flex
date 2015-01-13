package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: import_via.flex

  description:
    Defines the tokenizer for an import-via attribute. Derived from
    mp_import.l.

  notes:
    Tokens are defined in the associated grammar, import_via.y.
*/

%%

%public
%class ImportViaLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private ImportViaParser yyparser;

    /* constructor taking an additional parser object */
    public ImportViaLexer(java.io.Reader r, ImportViaParser yyparser) {
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

OR    { return ImportViaParser.OP_OR; }
AND   { return ImportViaParser.OP_AND; }
NOT   { return ImportViaParser.OP_NOT; }
==    { return ImportViaParser.OP_COMPARE; }
=     { return ImportViaParser.OP_EQUAL; }
\.=   { return ImportViaParser.OP_APPEND; }

\^-   { return ImportViaParser.OP_MS; }
\^\+  { return ImportViaParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ImportViaParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ImportViaParser.OP_MS;
}

ANY     { return ImportViaParser.KEYW_ANY; }
PEERAS  { return ImportViaParser.KEYW_PEERAS; }

FROM      { return ImportViaParser.KEYW_FROM; }
ACTION    { return ImportViaParser.KEYW_ACTION; }
IGP_COST  { return ImportViaParser.KEYW_IGP_COST; }
SELF      { return ImportViaParser.KEYW_SELF; }
PREPEND   { return ImportViaParser.KEYW_PREPEND; }
APPEND    { return ImportViaParser.KEYW_APPEND; }
DELETE    { return ImportViaParser.KEYW_DELETE; }
CONTAINS  { return ImportViaParser.KEYW_CONTAINS; }
ACCEPT    { return ImportViaParser.KEYW_ACCEPT; }

INTERNET      { return ImportViaParser.KEYW_INTERNET; }
NO_EXPORT     { return ImportViaParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ImportViaParser.KEYW_NO_ADVERTISE; }

AT          { return ImportViaParser.KEYW_AT; }
PROTOCOL    { return ImportViaParser.KEYW_PROTOCOL; }
INTO        { return ImportViaParser.KEYW_INTO; }
REFINE      { return ImportViaParser.KEYW_REFINE; }
EXCEPT      { return ImportViaParser.KEYW_EXCEPT; }

{PROTOCOL_NAME} { return ImportViaParser.TKN_PROTOCOL; }

{AFI}       { return ImportViaParser.KEYW_AFI; }

{AFIVALUE_V4}  { return ImportViaParser.KEYW_AFI_VALUE_V4; }
{AFIVALUE_V6}  { return ImportViaParser.KEYW_AFI_VALUE_V6; }
{AFIVALUE_ANY} { return ImportViaParser.KEYW_AFI_VALUE_ANY; }

PREF        { return ImportViaParser.TKN_PREF; }
MED         { return ImportViaParser.TKN_MED; }
DPA         { return ImportViaParser.TKN_DPA; }
ASPATH      { return ImportViaParser.TKN_ASPATH; }
COMMUNITY   { return ImportViaParser.TKN_COMMUNITY; }
NEXT_HOP    { return ImportViaParser.TKN_NEXT_HOP; }
COST        { return ImportViaParser.TKN_COST; }

\~\*            { return ImportViaParser.ASPATH_POSTFIX; }
\~\+            { return ImportViaParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ImportViaParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ImportViaParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ImportViaParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ImportViaParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ImportViaParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ImportViaParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ImportViaParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ImportViaParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return ImportViaParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ImportViaParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ImportViaParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ImportViaParser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ImportViaParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ImportViaParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ImportViaParser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return ImportViaParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return ImportViaParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return ImportViaParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ImportViaParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ImportViaParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return ImportViaParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
