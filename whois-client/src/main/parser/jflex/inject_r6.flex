package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: inject_r6.flex

  description:
    Defines the tokenizer for an RPSLng route6 inject attribute.  It is
    derived from inject.l.

  notes:
    Tokens are defined in the associated grammar, inject_r6.y.
*/
%%

%public
%class InjectR6Lexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private InjectR6Parser yyparser;

    /* constructor taking an additional parser object */
    public InjectR6Lexer(final java.io.Reader r, final InjectR6Parser yyparser) {
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
AFI            = AFI
AFIVALUE       = IPV4|IPV4\.UNICAST|IPV4\.MULTICAST|IPV6|IPV6\.UNICAST|IPV6\.MULTICAST
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*{ALNUM})?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])


%%

[ \t\n]+    { ; }

AND   { return InjectR6Parser.OP_AND; }
OR    { return InjectR6Parser.OP_OR; }
==    { return InjectR6Parser.OP_COMPARE; }
=     { return InjectR6Parser.OP_EQUAL; }
\.=   { return InjectR6Parser.OP_APPEND; }


ACTION    { return InjectR6Parser.KEYW_ACTION; }
IGP_COST  { return InjectR6Parser.KEYW_IGP_COST; }
SELF      { return InjectR6Parser.KEYW_SELF; }
PREPEND   { return InjectR6Parser.KEYW_PREPEND; }
APPEND    { return InjectR6Parser.KEYW_APPEND; }
DELETE    { return InjectR6Parser.KEYW_DELETE; }
CONTAINS  { return InjectR6Parser.KEYW_CONTAINS; }

INTERNET      { return InjectR6Parser.KEYW_INTERNET; }
NO_EXPORT     { return InjectR6Parser.KEYW_NO_EXPORT; }
NO_ADVERTISE  { return InjectR6Parser.KEYW_NO_ADVERTISE; }

AT          { return InjectR6Parser.KEYW_AT; }
EXCEPT      { return InjectR6Parser.KEYW_EXCEPT; }
UPON        { return InjectR6Parser.KEYW_UPON; }
STATIC      { return InjectR6Parser.KEYW_STATIC; }
EXCLUDE     { return InjectR6Parser.KEYW_EXCLUDE; }

HAVE-COMPONENTS   { return InjectR6Parser.KEYW_HAVE_COMPONENTS; }

MASKLEN     { return InjectR6Parser.KEYW_MASKLEN; }

{AFI}       { return InjectR6Parser.KEYW_AFI; }

{AFIVALUE}  { return InjectR6Parser.KEYW_AFI_VALUE; }

PREF        { return InjectR6Parser.TKN_PREF; }
MED         { return InjectR6Parser.TKN_MED; }
DPA         { return InjectR6Parser.TKN_DPA; }
ASPATH      { return InjectR6Parser.TKN_ASPATH; }
COMMUNITY   { return InjectR6Parser.TKN_COMMUNITY; }
NEXT_HOP    { return InjectR6Parser.TKN_NEXT_HOP; }
COST        { return InjectR6Parser.TKN_COST; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return InjectR6Parser.TKN_ASNO;
}

(({ASNO}|peeras|{RTRSNAME}):)*{RTRSNAME}(:({ASNO}|peeras|{RTRSNAME}))* {
    return InjectR6Parser.TKN_RTRSNAME;
}

{PRFXV4RNG} {
    ParserHelper.validateIpv4PrefixRange(yytext());
    return InjectR6Parser.TKN_PRFXV4RNG;
}

{PRFXV6RNG} {
    ParserHelper.validateIpv6PrefixRange(yytext());
    return InjectR6Parser.TKN_PRFXV6RNG;
}

{PRFXV6DCRNG} {
     ParserHelper.validateIpv6PrefixRange(yytext());
    return InjectR6Parser.TKN_PRFXV6DCRNG;
}

{PRFXV4} {
    ParserHelper.validateIpv4Prefix(yytext());
    return InjectR6Parser.TKN_PRFXV4;
}

{PRFXV6} {
    ParserHelper.validateIpv6Prefix(yytext());
    return InjectR6Parser.TKN_PRFXV6;
}

{PRFXV6DC} {
    ParserHelper.validateIpv6Prefix(yytext());
    return InjectR6Parser.TKN_PRFXV6DC;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return InjectR6Parser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return InjectR6Parser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return InjectR6Parser.TKN_IPV6DC;
}

{COMM_NO} {
    ParserHelper.validateCommunity(yytext());
    return InjectR6Parser.TKN_COMM_NO;
}

{INT} {
    storeTokenValue();
    return InjectR6Parser.TKN_INT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    storeTokenValue();
    return InjectR6Parser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
