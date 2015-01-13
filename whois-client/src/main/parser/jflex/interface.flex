package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: interface.flex

  description:
    Defines the tokenizer for an RPSLng interface attribute.  Derived
    from ifaddr.l.

  notes:
    Tokens are defined in the associated grammar, interface.y.
*/

%%

%public
%class InterfaceLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private InterfaceParser yyparser;

    /* constructor taking an additional parser object */
    public InterfaceLexer(java.io.Reader r, InterfaceParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

FLTRNAME       = FLTR-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RSNAME         = RS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
INT            = [0-9]+
QUAD           = [0-9A-Fa-f]{1,4}
IPV4           = {INT}(\.{INT}){3}
IPV6           = {QUAD}(:{QUAD}){7}
IPV6DC         = (({QUAD}:){0,6}{QUAD})?::({QUAD}(:{QUAD}){0,6})?
COMM_NO        = {INT}:{INT}
ENCAPSULATION  = GRE|IPINIP
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASNAME         = AS-[A-Za-z0-9_-]*[A-Za-z0-9]


%%

[ \t\n]+    { ; }


OR    { return InterfaceParser.OP_OR; }
==    { return InterfaceParser.OP_COMPARE; }
=     { return InterfaceParser.OP_EQUAL; }
\.=   { return InterfaceParser.OP_APPEND; }


ACTION    { return InterfaceParser.KEYW_ACTION; }
IGP_COST  { return InterfaceParser.KEYW_IGP_COST; }
SELF      { return InterfaceParser.KEYW_SELF; }
PREPEND   { return InterfaceParser.KEYW_PREPEND; }
APPEND    { return InterfaceParser.KEYW_APPEND; }
DELETE    { return InterfaceParser.KEYW_DELETE; }
CONTAINS  { return InterfaceParser.KEYW_CONTAINS; }

INTERNET      { return InterfaceParser.KEYW_INTERNET; }
NO_EXPORT     { return InterfaceParser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return InterfaceParser.KEYW_NO_ADVERTISE; }

MASKLEN     { return InterfaceParser.KEYW_MASKLEN; }

TUNNEL      { return InterfaceParser.KEYW_TUNNEL; }
GRE|IPV6INIPV4|IPINIP|DVMRP { return InterfaceParser.KEYW_ENCAPSULATION; }

PREF        { return InterfaceParser.TKN_PREF; }
MED         { return InterfaceParser.TKN_MED; }
DPA         { return InterfaceParser.TKN_DPA; }
ASPATH      { return InterfaceParser.TKN_ASPATH; }
COMMUNITY   { return InterfaceParser.TKN_COMMUNITY; }
NEXT_HOP    { return InterfaceParser.TKN_NEXT_HOP; }
COST        { return InterfaceParser.TKN_COST; }


{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return InterfaceParser.TKN_ASNO;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return InterfaceParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return InterfaceParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return InterfaceParser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return InterfaceParser.TKN_COMM_NO;
}

{INT} {
    yyparser.yylval.sval = yytext();
    return InterfaceParser.TKN_INT;
}

. {
    return yytext().charAt(0);
}
