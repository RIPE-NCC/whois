package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: mp_peer.flex

  description:
    Defines the tokenizer for an RPSLng mp-peer attribute.
    Derived from peer.l.

  notes:
    Tokens are defined in the associated grammar, mp_peer.y.
*/

%%

%public
%class MpPeerLexer
%implements net.ripe.db.whois.common.rpsl.AttributeLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private MpPeerParser yyparser;

    /* constructor taking an additional parser object */
    public MpPeerLexer(java.io.Reader r, MpPeerParser yyparser) {
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

/* macro definitions */

INT            = [0-9]+
QUAD           = [0-9a-fA-F]{1,4}
IPV4           = {INT}(\.{INT}){3}
IPV6           = {QUAD}(:{QUAD}){7}
IPV6DC         = (({QUAD}:){0,6}{QUAD})?::({QUAD}(:{QUAD}){0,6})?
DNAME          = [a-zA-Z]([-a-zA-Z0-9]*[a-zA-Z0-9])?
PRNGNAME       = PRNG-[a-zA-Z0-9_-]*[a-zA-Z0-9]
RTRSNAME       = RTRS-[a-zA-Z0-9_-]*[a-zA-Z0-9]
ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]

%%

/* keywords */

[ \t\n]+    { ; }

ASNO {
    return MpPeerParser.KEYW_ASNO;
}

FLAP_DAMP {
    return MpPeerParser.KEYW_FLAP_DAMP;
}

PEERAS  { return MpPeerParser.KEYW_PEERAS; }

OSPF|RIP|IGRP|IS-IS|STATIC|RIPng|DVMRP|PIM-DM|PIM-SM|CBT|MOSPF {
    return MpPeerParser.TKN_SIMPLE_PROTOCOL;
}

BGP4|MPBGP {
    return MpPeerParser.TKN_NON_SIMPLE_PROTOCOL;
}

(({ASNO}|peeras|{RTRSNAME}):)*{RTRSNAME}(:({ASNO}|peeras|{RTRSNAME}))* {
    return MpPeerParser.TKN_RTRSNAME;
}

(({ASNO}|peeras|{PRNGNAME}):)*{PRNGNAME}(:({ASNO}|peeras|{PRNGNAME}))* {
    return MpPeerParser.TKN_PRNGNAME;
}

{IPV4} {
    ParserHelper.validateIpv4(yytext());
    return MpPeerParser.TKN_IPV4;
}

{IPV6} {
    ParserHelper.validateIpv6(yytext());
    return MpPeerParser.TKN_IPV6;
}

{IPV6DC} {
    ParserHelper.validateIpv6(yytext());
    return MpPeerParser.TKN_IPV6DC;
}

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return MpPeerParser.TKN_ASNO;
}

{INT} {
    /* check port is in range */
    ParserHelper.validateSmallInt(yytext());
    return MpPeerParser.TKN_SMALLINT;
}

{DNAME} {
    ParserHelper.validateDomainNameLabel(yytext());
    storeTokenValue();
    return MpPeerParser.TKN_DNS;
}

. {
    return yytext().charAt(0);
}
