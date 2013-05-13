package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: mp_peering.l

  description:
    Defines the tokenizer for an RPSLng mp-peering attribute. Derived
    from peering.l.

  notes:
    Tokens are defined in the associated grammar, mp_peering.y.
*/

%%

%class MpPeeringLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private MpPeeringParser yyparser;

    /* constructor taking an additional parser object */
    public MpPeeringLexer(java.io.Reader r, MpPeeringParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

FLTRNAME       = FLTR-[A-Za-z0-9_-]*[A-Za-z0-9]
ASNAME         = AS-[A-Za-z0-9_-]*[A-Za-z0-9]
RSNAME         = RS-[A-Za-z0-9_-]*[A-Za-z0-9]
PRNGNAME       = PRNG-[A-Za-z0-9_-]*[A-Za-z0-9]
RTRSNAME       = RTRS-[A-Za-z0-9_-]*[A-Za-z0-9]
INT            = [0-9]+
QUAD           = [0-9A-Fa-f]{1,4}
IPV4           = {INT}(\.{INT}){3}
IPV6           = {QUAD}(:{QUAD}){7}
IPV6DC         = (({QUAD}:){0,6}{QUAD})?::({QUAD}(:{QUAD}){0,6})?
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }

OR    { return MpPeeringParser.OP_OR; }
AND   { return MpPeeringParser.OP_AND; }

AT          { return MpPeeringParser.KEYW_AT; }
EXCEPT      { return MpPeeringParser.KEYW_EXCEPT; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpPeeringParser.TKN_ASNO;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return MpPeeringParser.TKN_ASNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return MpPeeringParser.TKN_PRNGNAME;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpPeeringParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpPeeringParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpPeeringParser.TKN_IPV6DC;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return MpPeeringParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}


