package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: filter.flex

  description:
    Defines the tokenizer for an RPSL filter attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, filter.y.
*/

%%

%public
%class FilterLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private FilterParser yyparser;

    /* constructor taking an additional parser object */
    public FilterLexer(java.io.Reader r, FilterParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }

    /* assign value associated with current token to the external parser variable yylval. */
    private void storeTokenValue() {
        if ((this.yyparser != null) && (this.yyparser.yylval != null)) {
            yyparser.yylval.sval = yytext();
        }
    }
%}

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

[ \t\n]+    { ; }

OR    { return FilterParser.OP_OR; }
AND   { return FilterParser.OP_AND; }
NOT   { return FilterParser.OP_NOT; }
=     { return FilterParser.OP_EQUAL; }
==    { return FilterParser.OP_COMPARE; }
\.=   { return FilterParser.OP_APPEND; }


\^-         { return FilterParser.OP_MS; }
\^\+        { return FilterParser.OP_MS; }
\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return FilterParser.OP_MS;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return FilterParser.OP_MS;
}

ANY     { return FilterParser.KEYW_ANY; }
PEERAS  { return FilterParser.KEYW_PEERAS; }

IGP_COST  { return FilterParser.KEYW_IGP_COST; }
SELF      { return FilterParser.KEYW_SELF; }
PREPEND   { return FilterParser.KEYW_PREPEND; }
APPEND    { return FilterParser.KEYW_APPEND; }
DELETE    { return FilterParser.KEYW_DELETE; }
CONTAINS  { return FilterParser.KEYW_CONTAINS; }

INTERNET      { return FilterParser.KEYW_INTERNET; }
NO_EXPORT     { return FilterParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return FilterParser.KEYW_NO_ADVERTISE; }

PREF        { return FilterParser.TKN_PREF; }
MED         { return FilterParser.TKN_MED; }
DPA         { return FilterParser.TKN_DPA; }
ASPATH      { return FilterParser.TKN_ASPATH; }
COMMUNITY   { return FilterParser.TKN_COMMUNITY; }
NEXT_HOP    { return FilterParser.TKN_NEXT_HOP; }
COST        { return FilterParser.TKN_COST; }

\~\*            { return FilterParser.ASPATH_POSTFIX; }
\~\+            { return FilterParser.ASPATH_POSTFIX; }
\~?\{INT\}      { return FilterParser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return FilterParser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return FilterParser.ASPATH_POSTFIX; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return FilterParser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return FilterParser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return FilterParser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return FilterParser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return FilterParser.TKN_RSNAME;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return FilterParser.TKN_PRFXV4;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return FilterParser.TKN_PRFXV4RNG;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return FilterParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return FilterParser.TKN_COMM_NO;
}

{INT} {
    storeTokenValue();
    return FilterParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    storeTokenValue();
    return FilterParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}

