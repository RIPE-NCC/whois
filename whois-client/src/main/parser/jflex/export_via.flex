package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: export_via.flex

  description:
    Defines the tokenizer for an export-via attribute. Derived
    from mp_export.l.

  notes:
    Tokens are defined in the associated grammar, export_via.y.
*/

%%

%public
%class ExportViaLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    private ExportViaParser yyparser;

    /* constructor taking an additional parser object */
    public ExportViaLexer(java.io.Reader r, ExportViaParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
ALNUM          = [0-9a-zA-Z]
FLTRNAME       = FLTR-[A-Za-z0-9_-]*{ALNUM}
ASNAME         = AS-[A-Za-z0-9_-]*{ALNUM}
RSNAME         = RS-[A-Za-z0-9_-]*{ALNUM}
PRNGNAME       = PRNG-[A-Za-z0-9_-]*{ALNUM}
RTRSNAME       = RTRS-[A-Za-z0-9_-]*{ALNUM}
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
PROTOCOL_NAME  = BGP4|MPBGP|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
AFI            = AFI
AFIVALUE_V4    = IPV4|IPV4\.UNICAST|IPV4\.MULTICAST
AFIVALUE_V6    = IPV6|IPV6\.UNICAST|IPV6\.MULTICAST
AFIVALUE_ANY   = ANY\.UNICAST|ANY\.MULTICAST
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*{ALNUM})?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }

OR    { return ExportViaParser.OP_OR; }
AND   { return ExportViaParser.OP_AND; }
NOT   { return ExportViaParser.OP_NOT; }
==    { return ExportViaParser.OP_COMPARE; }
=     { return ExportViaParser.OP_EQUAL; }
\.=   { return ExportViaParser.OP_APPEND; }

\^-         { return ExportViaParser.OP_MS; }
\^\+        { return ExportViaParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ExportViaParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ExportViaParser.OP_MS;
}

ANY     { return ExportViaParser.KEYW_ANY; }
PEERAS  { return ExportViaParser.KEYW_PEERAS; }

TO        { return ExportViaParser.KEYW_TO; }
ACTION    { return ExportViaParser.KEYW_ACTION; }
IGP_COST  { return ExportViaParser.KEYW_IGP_COST; }
SELF      { return ExportViaParser.KEYW_SELF; }
APPEND    { return ExportViaParser.KEYW_APPEND; }
DELETE    { return ExportViaParser.KEYW_DELETE; }
CONTAINS  { return ExportViaParser.KEYW_CONTAINS; }
PREPEND   { return ExportViaParser.KEYW_PREPEND; }
ANNOUNCE    { return ExportViaParser.KEYW_ANNOUNCE; }

INTERNET      { return ExportViaParser.KEYW_INTERNET; }
NO_EXPORT     { return ExportViaParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ExportViaParser.KEYW_NO_ADVERTISE; }

AT          { return ExportViaParser.KEYW_AT; }
PROTOCOL    { return ExportViaParser.KEYW_PROTOCOL; }
INTO        { return ExportViaParser.KEYW_INTO; }
REFINE      { return ExportViaParser.KEYW_REFINE; }
EXCEPT      { return ExportViaParser.KEYW_EXCEPT; }

{PROTOCOL_NAME} { return ExportViaParser.TKN_PROTOCOL; }

{AFI}       { return ExportViaParser.KEYW_AFI; }

{AFIVALUE_V4}  { return ExportViaParser.KEYW_AFI_VALUE_V4; }
{AFIVALUE_V6}  { return ExportViaParser.KEYW_AFI_VALUE_V6; }
{AFIVALUE_ANY} { return ExportViaParser.KEYW_AFI_VALUE_ANY; }

PREF        { return ExportViaParser.TKN_PREF; }
MED         { return ExportViaParser.TKN_MED; }
DPA         { return ExportViaParser.TKN_DPA; }
ASPATH      { return ExportViaParser.TKN_ASPATH; }
COMMUNITY   { return ExportViaParser.TKN_COMMUNITY; }
NEXT_HOP    { return ExportViaParser.TKN_NEXT_HOP; }
COST        { return ExportViaParser.TKN_COST; }

\~\*            { return ExportViaParser.ASPATH_POSTFIX; }
\~\+            { return ExportViaParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ExportViaParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ExportViaParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ExportViaParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ExportViaParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ExportViaParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ExportViaParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ExportViaParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ExportViaParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return ExportViaParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ExportViaParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ExportViaParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ExportViaParser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ExportViaParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ExportViaParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ExportViaParser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return ExportViaParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return ExportViaParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return ExportViaParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ExportViaParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ExportViaParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return ExportViaParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
