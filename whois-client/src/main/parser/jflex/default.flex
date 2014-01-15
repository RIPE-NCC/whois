package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: default.flex

  description:
    Defines the tokenizer for an RPSL default attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary.

  notes:
    Tokens are defined in the associated grammar, default.y.
*/

%%

%class DefaultLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private DefaultParser yyparser;

    /* constructor taking an additional parser object */
    public DefaultLexer(java.io.Reader r, DefaultParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

/* macro definitions */

ALNUM          = [0-9a-zA-Z]
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
FLTRNAME       = FLTR-[a-zA-Z0-9_-]*{ALNUM}
ASNAME         = AS-[a-zA-Z0-9_-]*{ALNUM}
RSNAME         = RS-[a-zA-Z0-9_-]*{ALNUM}
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*{ALNUM}
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*{ALNUM}
INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO        = {INT}:{INT}
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*{ALNUM})?

%%

/* keywords */

[ \t\n]+    { ; }

OR    { return DefaultParser.OP_OR; }
AND   { return DefaultParser.OP_AND; }
NOT   { return DefaultParser.OP_NOT; }
==    { return DefaultParser.OP_COMPARE; }
=     { return DefaultParser.OP_EQUAL; }
\.=   { return DefaultParser.OP_APPEND; }

\^-   { return DefaultParser.OP_MS; }
\^\+  { return DefaultParser.OP_MS; }

\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return DefaultParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return DefaultParser.OP_MS;
}

ANY     { return DefaultParser.KEYW_ANY; }
PEERAS  { return DefaultParser.KEYW_PEERAS; }

TO        { return DefaultParser.KEYW_TO; }
ACTION    { return DefaultParser.KEYW_ACTION; }
NETWORKS  { return DefaultParser.KEYW_NETWORKS; }
IGP_COST  { return DefaultParser.KEYW_IGP_COST; }
SELF      { return DefaultParser.KEYW_SELF; }
PREPEND   { return DefaultParser.KEYW_PREPEND; }
APPEND    { return DefaultParser.KEYW_APPEND; }
DELETE    { return DefaultParser.KEYW_DELETE; }
CONTAINS  { return DefaultParser.KEYW_CONTAINS; }

INTERNET      { return DefaultParser.KEYW_INTERNET; }
NO_EXPORT     { return DefaultParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return DefaultParser.KEYW_NO_ADVERTISE; }

AT          { return DefaultParser.KEYW_AT; }

PREF        { return DefaultParser.TKN_PREF; }
MED         { return DefaultParser.TKN_MED; }
DPA         { return DefaultParser.TKN_DPA; }
ASPATH      { return DefaultParser.TKN_ASPATH; }
COMMUNITY   { return DefaultParser.TKN_COMMUNITY; }
NEXT_HOP    { return DefaultParser.TKN_NEXT_HOP; }
COST        { return DefaultParser.TKN_COST; }

\~\*            { return DefaultParser.ASPATH_POSTFIX; }
\~\+            { return DefaultParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return DefaultParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return DefaultParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return DefaultParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return DefaultParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return DefaultParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return DefaultParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return DefaultParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return DefaultParser.TKN_RSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return DefaultParser.TKN_PRNGNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return DefaultParser.TKN_PRFXV4RNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return DefaultParser.TKN_PRFXV4;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return DefaultParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return DefaultParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return DefaultParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return DefaultParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
