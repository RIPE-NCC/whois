package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: components_r6.flex

  description:
    Defines the tokenizer for an RPSLng route6 components attribute.  It is
    derived from components.l.

  notes:
    Tokens are defined in the associated grammar, components.l.
*/
%%

%public
%class ComponentsR6Lexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private ComponentsR6Parser yyparser;

    /* constructor taking an additional parser object */
    public ComponentsR6Lexer(final java.io.Reader r, final ComponentsR6Parser yyparser) {
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
PROTOCOL_NAME  = BGP4|MPBGP|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
COMM_NO        = {INT}:{INT}

%%

[ \t\n]+    { ; }

OR    { return ComponentsR6Parser.OP_OR; }
AND   { return ComponentsR6Parser.OP_AND; }
NOT   { return ComponentsR6Parser.OP_NOT; }
=     { return ComponentsR6Parser.OP_EQUAL; }
==    { return ComponentsR6Parser.OP_COMPARE; }
\.=   { return ComponentsR6Parser.OP_APPEND; }

\^-         { return ComponentsR6Parser.OP_MS; }
\^\+        { return ComponentsR6Parser.OP_MS; }
\^[0-9]+ {
    ParserHelper.validateMoreSpecificsOperator(yytext());
    return ComponentsR6Parser.OP_MS_V6;
}
\^[0-9]+-[0-9]+ {
    ParserHelper.validateRangeMoreSpecificsOperators(yytext());
    return ComponentsR6Parser.OP_MS_V6;
}


ATOMIC  { return ComponentsR6Parser.KEYW_ATOMIC; }
ANY     { return ComponentsR6Parser.KEYW_ANY; }
PEERAS  { return ComponentsR6Parser.KEYW_PEERAS; }

IGP_COST  { return ComponentsR6Parser.KEYW_IGP_COST; }
SELF      { return ComponentsR6Parser.KEYW_SELF; }
PREPEND   { return ComponentsR6Parser.KEYW_PREPEND; }
APPEND    { return ComponentsR6Parser.KEYW_APPEND; }
DELETE    { return ComponentsR6Parser.KEYW_DELETE; }
CONTAINS  { return ComponentsR6Parser.KEYW_CONTAINS; }

INTERNET      { return ComponentsR6Parser.KEYW_INTERNET; }
NO_EXPORT     { return ComponentsR6Parser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return ComponentsR6Parser.KEYW_NO_ADVERTISE; }

PREF        { return ComponentsR6Parser.TKN_PREF; }
MED         { return ComponentsR6Parser.TKN_MED; }
DPA         { return ComponentsR6Parser.TKN_DPA; }
ASPATH      { return ComponentsR6Parser.TKN_ASPATH; }
COMMUNITY   { return ComponentsR6Parser.TKN_COMMUNITY; }
NEXT_HOP    { return ComponentsR6Parser.TKN_NEXT_HOP; }
COST        { return ComponentsR6Parser.TKN_COST; }

\~\*            { return ComponentsR6Parser.ASPATH_POSTFIX; }
\~\+            { return ComponentsR6Parser.ASPATH_POSTFIX; }
\~?\{INT\}      { return ComponentsR6Parser.ASPATH_POSTFIX; }
\~?\{INT,INT\}  { return ComponentsR6Parser.ASPATH_POSTFIX; }
\~?\{INT,\}     { return ComponentsR6Parser.ASPATH_POSTFIX; }

PROTOCOL         { return ComponentsR6Parser.KEYW_PROTOCOL; }
{PROTOCOL_NAME}  { return ComponentsR6Parser.TKN_PROTOCOL; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return ComponentsR6Parser.TKN_ASNO;
}

{ASRANGE} {
    ParserHelper.validateAsRange(yytext());
    return ComponentsR6Parser.TKN_ASRANGE;
}

(({ASNO}|peeras|{FLTRNAME}):)*{FLTRNAME}(:({ASNO}|peeras|{FLTRNAME}))* {
    return ComponentsR6Parser.TKN_FLTRNAME;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return ComponentsR6Parser.TKN_ASNAME;
}

(({ASNO}|peeras|{RSNAME}):)*{RSNAME}(:({ASNO}|peeras|{RSNAME}))* {
    return ComponentsR6Parser.TKN_RSNAME;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return ComponentsR6Parser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ComponentsR6Parser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return ComponentsR6Parser.TKN_PRFXV6DC;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return ComponentsR6Parser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ComponentsR6Parser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return ComponentsR6Parser.TKN_PRFXV6DCRNG;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return ComponentsR6Parser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return ComponentsR6Parser.TKN_INT;
}

. {
    return yytext().charAt(0);
}

