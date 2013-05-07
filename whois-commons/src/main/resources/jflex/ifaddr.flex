package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: ifaddr.l

  description:
    Defines the tokenizer for an RPSL ifaddr attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, ifaddr.y.
*/

%%

%class IfaddrLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private IfaddrParser yyparser;

    /* constructor taking an additional parser object */
    public IfaddrLexer(java.io.Reader r, IfaddrParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

FLTRNAME       = FLTR-[a-zA-Z0-9_-]*[a-zA-Z0-9]
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RSNAME         = RS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
INT            =[0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO        = {INT}:{INT}
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }


OR    { return IfaddrParser.OP_OR; }
==    { return IfaddrParser.OP_COMPARE; }
=     { return IfaddrParser.OP_EQUAL; }
\.=   { return IfaddrParser.OP_APPEND; }


ACTION    { return IfaddrParser.KEYW_ACTION; }
IGP_COST  { return IfaddrParser.KEYW_IGP_COST; }
SELF      { return IfaddrParser.KEYW_SELF; }
PREPEND   { return IfaddrParser.KEYW_PREPEND; }
APPEND    { return IfaddrParser.KEYW_APPEND; }
DELETE    { return IfaddrParser.KEYW_DELETE; }
CONTAINS  { return IfaddrParser.KEYW_CONTAINS; }

INTERNET      { return IfaddrParser.KEYW_INTERNET; }
NO_EXPORT     { return IfaddrParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return IfaddrParser.KEYW_NO_ADVERTISE; }

MASKLEN     { return IfaddrParser.KEYW_MASKLEN; }

PREF        { return IfaddrParser.TKN_PREF; }
MED         { return IfaddrParser.TKN_MED; }
DPA         { return IfaddrParser.TKN_DPA; }
ASPATH      { return IfaddrParser.TKN_ASPATH; }
COMMUNITY   { return IfaddrParser.TKN_COMMUNITY; }
NEXT_HOP    { return IfaddrParser.TKN_NEXT_HOP; }
COST        { return IfaddrParser.TKN_COST; }


{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return IfaddrParser.TKN_ASNO;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return IfaddrParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return IfaddrParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return IfaddrParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return IfaddrParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}

