package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: peer.flex

  description:
    Defines the tokenizer for an RPSL peer attribute.

  notes:
    Tokens are defined in the associated grammar, peer.y.
*/

%%

%public
%class PeerLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private PeerParser yyparser;

    /* constructor taking an additional parser object */
    public PeerLexer(java.io.Reader r, PeerParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

/* macro definitions */

INT            = [0-9]+
IPV4           = {INT}(\.{INT}){3}
DNAME          = [a-zA-Z]([-a-zA-Z0-9]*[a-zA-Z0-9])?
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]

%%

/* keywords */

[ \t\n]+    { ; }

ASNO {
    return PeerParser.KEYW_ASNO;
}

FLAP_DAMP {
    return PeerParser.KEYW_FLAP_DAMP;
}

PEERAS  { return PeerParser.KEYW_PEERAS; }

OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF {
    return PeerParser.TKN_SIMPLE_PROTOCOL;
}

BGP4 {
    return PeerParser.TKN_BGP4;
}

(({ASNO}|peeras|{RTRSNAME}):)*{RTRSNAME}(:({ASNO}|peeras|{RTRSNAME}))* {
    return PeerParser.TKN_RTRSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return PeerParser.TKN_PRNGNAME;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return PeerParser.TKN_IPV4;
}

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return PeerParser.TKN_ASNO;
}

{INT} {
    /* check port is in range */
    ParserHelper.validateSmallInt(yytext());
    return PeerParser.TKN_SMALLINT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    yyparser.yylval.sval = yytext();
    return PeerParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
