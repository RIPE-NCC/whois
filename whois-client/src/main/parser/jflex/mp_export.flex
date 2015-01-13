package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: mp_export.flex

  description:
    Defines the tokenizer for an RPSLng mp-export attribute. Derived
    from export.l.

  notes:
    Tokens are defined in the associated grammar, mp_export.y.
*/

%%

%public
%class MpExportLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    private MpExportParser yyparser;

    /* constructor taking an additional parser object */
    public MpExportLexer(java.io.Reader r, MpExportParser yyparser) {
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

OR    { return MpExportParser.OP_OR; }
AND   { return MpExportParser.OP_AND; }
NOT   { return MpExportParser.OP_NOT; }
==    { return MpExportParser.OP_COMPARE; }
=     { return MpExportParser.OP_EQUAL; }
\.=   { return MpExportParser.OP_APPEND; }

\^-         { return MpExportParser.OP_MS; }
\^\+        { return MpExportParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return MpExportParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return MpExportParser.OP_MS;
}

ANY     { return MpExportParser.KEYW_ANY; }
PEERAS  { return MpExportParser.KEYW_PEERAS; }

TO        { return MpExportParser.KEYW_TO; }
ACTION    { return MpExportParser.KEYW_ACTION; }
IGP_COST  { return MpExportParser.KEYW_IGP_COST; }
SELF      { return MpExportParser.KEYW_SELF; }
APPEND    { return MpExportParser.KEYW_APPEND; }
DELETE    { return MpExportParser.KEYW_DELETE; }
CONTAINS  { return MpExportParser.KEYW_CONTAINS; }
PREPEND   { return MpExportParser.KEYW_PREPEND; }
ANNOUNCE    { return MpExportParser.KEYW_ANNOUNCE; }

INTERNET      { return MpExportParser.KEYW_INTERNET; }
NO_EXPORT     { return MpExportParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return MpExportParser.KEYW_NO_ADVERTISE; }

AT          { return MpExportParser.KEYW_AT; }
PROTOCOL    { return MpExportParser.KEYW_PROTOCOL; }
INTO        { return MpExportParser.KEYW_INTO; }
REFINE      { return MpExportParser.KEYW_REFINE; }
EXCEPT      { return MpExportParser.KEYW_EXCEPT; }

{PROTOCOL_NAME} { return MpExportParser.TKN_PROTOCOL; }

{AFI}       { return MpExportParser.KEYW_AFI; }

{AFIVALUE_V4}  { return MpExportParser.KEYW_AFI_VALUE_V4; }
{AFIVALUE_V6}  { return MpExportParser.KEYW_AFI_VALUE_V6; }
{AFIVALUE_ANY} { return MpExportParser.KEYW_AFI_VALUE_ANY; }

PREF        { return MpExportParser.TKN_PREF; }
MED         { return MpExportParser.TKN_MED; }
DPA         { return MpExportParser.TKN_DPA; }
ASPATH      { return MpExportParser.TKN_ASPATH; }
COMMUNITY   { return MpExportParser.TKN_COMMUNITY; }
NEXT_HOP    { return MpExportParser.TKN_NEXT_HOP; }
COST        { return MpExportParser.TKN_COST; }

\~\*            { return MpExportParser.ASPATH_POSTFIX; }
\~\+            { return MpExportParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return MpExportParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return MpExportParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return MpExportParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpExportParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return MpExportParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return MpExportParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return MpExportParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return MpExportParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return MpExportParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return MpExportParser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpExportParser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return MpExportParser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return MpExportParser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpExportParser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return MpExportParser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpExportParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpExportParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpExportParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return MpExportParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return MpExportParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return MpExportParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
