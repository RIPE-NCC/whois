package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: peering.l

  description:
    Defines the tokenizer for an RPSL peering attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, peering.y.
*/

%%

%public
%class PeeringLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private PeeringParser yyparser;

    /* constructor taking an additional parser object */
    public PeeringLexer(java.io.Reader r, PeeringParser yyparser) {
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

FLTRNAME       = FLTR-[A-Za-z0-9_-]*[A-Za-z0-9]
ASNAME         = AS-[A-Za-z0-9_-]*[A-Za-z0-9]
RSNAME         = RS-[A-Za-z0-9_-]*[A-Za-z0-9]
PRNGNAME       = PRNG-[A-Za-z0-9_-]*[A-Za-z0-9]
RTRSNAME       = RTRS-[A-Za-z0-9_-]*[A-Za-z0-9]
INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
PRFXV4         = {IPV4}\/{INT}
PRFXV4RNG      = {PRFXV4}("^+"|"^-"|"^"{INT}|"^"{INT}-{INT})
PROTOCOL_NAME  = BGP4|OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF
DNAME          = [a-zA-Z]([0-9a-zA-Z-]*[0-9a-zA-Z])?
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])

%%

[ \t\n]+    { ; }

OR    { return PeeringParser.OP_OR; }
AND   { return PeeringParser.OP_AND; }

AT          { return PeeringParser.KEYW_AT; }
EXCEPT      { return PeeringParser.KEYW_EXCEPT; }


{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return PeeringParser.TKN_ASNO;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return PeeringParser.TKN_ASNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return PeeringParser.TKN_PRNGNAME;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return PeeringParser.TKN_IPV4;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    storeTokenValue();
    return PeeringParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}

