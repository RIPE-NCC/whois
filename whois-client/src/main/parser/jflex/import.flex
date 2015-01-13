package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: import.flex

  description:
    Defines the tokenizer for an RPSL import attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary.

  notes:
    Tokens are defined in the associated grammar, import.y.
*/

%%

%public
%class ImportLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private ImportParser yyparser;

    /* constructor taking an additional parser object */
    public ImportLexer(java.io.Reader r, ImportParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

/* macro definitions */
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
FLTRNAME       = FLTR-[a-zA-Z0-9_-]*[a-zA-Z0-9]
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RSNAME         = RS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO        = {INT}:{INT}
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
ALNUM          = [0-9a-zA-Z]
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?

%%

/* keywords */

[ \t\n]+    { ; }

OR    { return ImportParser.OP_OR; }
AND   { return ImportParser.OP_AND; }
NOT   { return ImportParser.OP_NOT; }
==    { return ImportParser.OP_COMPARE; }
=     { return ImportParser.OP_EQUAL; }
\.=   { return ImportParser.OP_APPEND; }

\^-         { return ImportParser.OP_MS; }
\^\+        { return ImportParser.OP_MS; }
\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ImportParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ImportParser.OP_MS;
}

ANY     { return ImportParser.KEYW_ANY; }
PEERAS  { return ImportParser.KEYW_PEERAS; }

ACTION    { return ImportParser.KEYW_ACTION; }
IGP_COST  { return ImportParser.KEYW_IGP_COST; }
SELF      { return ImportParser.KEYW_SELF; }
PREPEND   { return ImportParser.KEYW_PREPEND; }
APPEND    { return ImportParser.KEYW_APPEND; }
DELETE    { return ImportParser.KEYW_DELETE; }
CONTAINS  { return ImportParser.KEYW_CONTAINS; }
ACCEPT    { return ImportParser.KEYW_ACCEPT; }

INTERNET      { return ImportParser.KEYW_INTERNET; }
NO_EXPORT     { return ImportParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ImportParser.KEYW_NO_ADVERTISE; }

AT          { return ImportParser.KEYW_AT; }
PROTOCOL    { return ImportParser.KEYW_PROTOCOL; }
INTO        { return ImportParser.KEYW_INTO; }
REFINE      { return ImportParser.KEYW_REFINE; }
EXCEPT      { return ImportParser.KEYW_EXCEPT; }
FROM        { return ImportParser.KEYW_FROM; }

BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|PIM-DM|PIM-SM|CBT|MOSPF {
    return ImportParser.TKN_PROTOCOL;
}

PREF        { return ImportParser.TKN_PREF; }
MED         { return ImportParser.TKN_MED; }
DPA         { return ImportParser.TKN_DPA; }
ASPATH      { return ImportParser.TKN_ASPATH; }
COMMUNITY   { return ImportParser.TKN_COMMUNITY; }
NEXT_HOP    { return ImportParser.TKN_NEXT_HOP; }
COST        { return ImportParser.TKN_COST; }

\~\*            { return ImportParser.ASPATH_POSTFIX; }
\~\+            { return ImportParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ImportParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ImportParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ImportParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ImportParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ImportParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ImportParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ImportParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ImportParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return ImportParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ImportParser.TKN_PRFXV4RNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ImportParser.TKN_PRFXV4;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return ImportParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ImportParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ImportParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return ImportParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
