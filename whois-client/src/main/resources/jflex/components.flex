package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: components.flex

  description:
    Defines the tokenizer for an RPSL components attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, components.y.
*/
%%

%class ComponentsLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private ComponentsParser yyparser;

    /* constructor taking an additional parser object */
    public ComponentsLexer(java.io.Reader r, ComponentsParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}



ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASRANGE        = {ASNO}[ ]*[-][ ]*{ASNO}
FLTRNAME       = FLTR-[A-Za-z0-9_-]*[A-Za-z0-9]
ASNAME         = AS-[A-Za-z0-9_-]*[A-Za-z0-9]
RSNAME         = RS-[A-Za-z0-9_-]*[A-Za-z0-9]
INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
COMM_NO        = {INT}:{INT}


%%

[ \t\n]+    { ; }

OR    { return ComponentsParser.OP_OR; }
AND   { return ComponentsParser.OP_AND; }
NOT   { return ComponentsParser.OP_NOT; }
=     { return ComponentsParser.OP_EQUAL; }
==    { return ComponentsParser.OP_COMPARE; }
\.=   { return ComponentsParser.OP_APPEND; }

\^-         { return ComponentsParser.OP_MS; }
\^\+        { return ComponentsParser.OP_MS; }
\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ComponentsParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ComponentsParser.OP_MS;
}


ATOMIC  { return ComponentsParser.KEYW_ATOMIC; }
ANY     { return ComponentsParser.KEYW_ANY; }
PEERAS  { return ComponentsParser.KEYW_PEERAS; }

IGP_COST  { return ComponentsParser.KEYW_IGP_COST; }
SELF      { return ComponentsParser.KEYW_SELF; }
PREPEND   { return ComponentsParser.KEYW_PREPEND; }
APPEND    { return ComponentsParser.KEYW_APPEND; }
DELETE    { return ComponentsParser.KEYW_DELETE; }
CONTAINS  { return ComponentsParser.KEYW_CONTAINS; }

INTERNET      { return ComponentsParser.KEYW_INTERNET; }
NO_EXPORT     { return ComponentsParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ComponentsParser.KEYW_NO_ADVERTISE; }

PREF        { return ComponentsParser.TKN_PREF; }
MED         { return ComponentsParser.TKN_MED; }
DPA         { return ComponentsParser.TKN_DPA; }
ASPATH      { return ComponentsParser.TKN_ASPATH; }
COMMUNITY   { return ComponentsParser.TKN_COMMUNITY; }
NEXT_HOP    { return ComponentsParser.TKN_NEXT_HOP; }
COST        { return ComponentsParser.TKN_COST; }

\~\*            { return ComponentsParser.ASPATH_POSTFIX; }
\~\+            { return ComponentsParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ComponentsParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ComponentsParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ComponentsParser.ASPATH_POSTFIX; }

PROTOCOL         { return ComponentsParser.KEYW_PROTOCOL; }
{PROTOCOL_NAME}  { return ComponentsParser.TKN_PROTOCOL; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ComponentsParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ComponentsParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ComponentsParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ComponentsParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ComponentsParser.TKN_RSNAME;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ComponentsParser.TKN_PRFXV4;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ComponentsParser.TKN_PRFXV4RNG;
}


{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ComponentsParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ComponentsParser.TKN_INT;
}

. {
    return yytext().charAt(0);
}

