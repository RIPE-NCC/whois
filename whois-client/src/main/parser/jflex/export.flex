package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: export.flex

  description:
    Defines the tokenizer for an RPSL export attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary.

  to generate ExportLexer.Java class:
    bin/jflex -d ~/git/Whois/whois-update/src/main/java/net/ripe/db/whois/update/parser ~/git/Whois/whois-update/src/main/resources/jflex/export.flex

  notes:
    Tokens are defined in the associated grammar, export.y.
*/

%%

%public
%class ExportLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private ExportParser yyparser;

    /* constructor taking an additional parser object */
    public ExportLexer(java.io.Reader r, ExportParser yyparser) {
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
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?

%%

/* keywords */

[ \t\n]+    { ; }

OR    { return ExportParser.OP_OR; }
AND   { return ExportParser.OP_AND; }
NOT   { return ExportParser.OP_NOT; }
==    { return ExportParser.OP_COMPARE; }
=     { return ExportParser.OP_EQUAL; }
\.=   { return ExportParser.OP_APPEND; }

\^-   { return ExportParser.OP_MS; }
\^\+  { return ExportParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ExportParser.OP_MS;
}

\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ExportParser.OP_MS;
}

ANY     { return ExportParser.KEYW_ANY; }
PEERAS  { return ExportParser.KEYW_PEERAS; }

TO        { return ExportParser.KEYW_TO; }
ACTION    { return ExportParser.KEYW_ACTION; }
IGP_COST  { return ExportParser.KEYW_IGP_COST; }
SELF      { return ExportParser.KEYW_SELF; }
APPEND    { return ExportParser.KEYW_APPEND; }
DELETE    { return ExportParser.KEYW_DELETE; }
CONTAINS  { return ExportParser.KEYW_CONTAINS; }
PREPEND   { return ExportParser.KEYW_PREPEND; }

INTERNET      { return ExportParser.KEYW_INTERNET; }
NO_EXPORT     { return ExportParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ExportParser.KEYW_NO_ADVERTISE; }

AT          { return ExportParser.KEYW_AT; }
PROTOCOL    { return ExportParser.KEYW_PROTOCOL; }
INTO        { return ExportParser.KEYW_INTO; }
REFINE      { return ExportParser.KEYW_REFINE; }
ANNOUNCE    { return ExportParser.KEYW_ANNOUNCE; }

BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|PIM-DM|PIM-SM|CBT|MOSPF {
    return ExportParser.TKN_PROTOCOL;
}

PREF        { return ExportParser.TKN_PREF; }
MED         { return ExportParser.TKN_MED; }
DPA         { return ExportParser.TKN_DPA; }
ASPATH      { return ExportParser.TKN_ASPATH; }
COMMUNITY   { return ExportParser.TKN_COMMUNITY; }
NEXT_HOP    { return ExportParser.TKN_NEXT_HOP; }
COST        { return ExportParser.TKN_COST; }

\~\*            { return ExportParser.ASPATH_POSTFIX; }
\~\+            { return ExportParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ExportParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ExportParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ExportParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ExportParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ExportParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ExportParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ExportParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ExportParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return ExportParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ExportParser.TKN_PRFXV4RNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ExportParser.TKN_PRFXV4;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return ExportParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ExportParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ExportParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return ExportParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}

