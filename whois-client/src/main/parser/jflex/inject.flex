package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: inject.flex

  description:
    Defines the tokenizer for an RPSL inject attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, inject.y.
*/
%%

%public
%class InjectLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private InjectParser yyparser;

    /* constructor taking an additional parser object */
    public InjectLexer(final java.io.Reader r, final InjectParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

ALNUM          = [0-9a-zA-Z]
FLTRNAME       = FLTR-[A-Za-z0-9_-]*{ALNUM}
ASNAME         = AS-[A-Za-z0-9_-]*{ALNUM}
RSNAME         = RS-[A-Za-z0-9_-]*{ALNUM}
PRNGNAME       = PRNG-[A-Za-z0-9_-]*{ALNUM}
RTRSNAME       = RTRS-[A-Za-z0-9_-]*{ALNUM}
INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
COMM_NO        = {INT}:{INT}
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*{ALNUM})?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }

AND   { return InjectParser.OP_AND; }
OR    { return InjectParser.OP_OR; }
==    { return InjectParser.OP_COMPARE; }
=     { return InjectParser.OP_EQUAL; }
\.=   { return InjectParser.OP_APPEND; }


ACTION    { return InjectParser.KEYW_ACTION; }
IGP_COST  { return InjectParser.KEYW_IGP_COST; }
SELF      { return InjectParser.KEYW_SELF; }
PREPEND   { return InjectParser.KEYW_PREPEND; }
APPEND    { return InjectParser.KEYW_APPEND; }
DELETE    { return InjectParser.KEYW_DELETE; }
CONTAINS  { return InjectParser.KEYW_CONTAINS; }

INTERNET      { return InjectParser.KEYW_INTERNET; }
NO_EXPORT     { return InjectParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return InjectParser.KEYW_NO_ADVERTISE; }

AT          { return InjectParser.KEYW_AT; }
EXCEPT      { return InjectParser.KEYW_EXCEPT; }
UPON        { return InjectParser.KEYW_UPON; }
STATIC      { return InjectParser.KEYW_STATIC; }
EXCLUDE     { return InjectParser.KEYW_EXCLUDE; }

HAVE-COMPONENTS   { return InjectParser.KEYW_HAVE_COMPONENTS; }

MASKLEN     { return InjectParser.KEYW_MASKLEN; }

PREF        { return InjectParser.TKN_PREF; }
MED         { return InjectParser.TKN_MED; }
DPA         { return InjectParser.TKN_DPA; }
ASPATH      { return InjectParser.TKN_ASPATH; }
COMMUNITY   { return InjectParser.TKN_COMMUNITY; }
NEXT_HOP    { return InjectParser.TKN_NEXT_HOP; }
COST        { return InjectParser.TKN_COST; }


{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return InjectParser.TKN_ASNO;
}

(({ASNO}|peeras|{RTRSNAME}):)*{RTRSNAME}(:({ASNO}|peeras|{RTRSNAME}))* {
    return InjectParser.TKN_RTRSNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return InjectParser.TKN_PRFXV4RNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return InjectParser.TKN_PRFXV4;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return InjectParser.TKN_IPV4;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return InjectParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return InjectParser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return InjectParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
