package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: aggr_bndry.flex

  description:
    Defines the tokenizer for an RPSL aggr-bndry attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Tokens are defined in the associated grammar, aggr_bndry.y.
*/
%%

%public
%class AggrBndryLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private AggrBndryParser yyparser;

    /* constructor taking an additional parser object */
    public AggrBndryLexer(java.io.Reader r, AggrBndryParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

ASNO           = AS([0-9]|[1-9][0-9]{1,8}|[1-3][0-9]{9}|4[0-1][0-9]{8}|42[0-8][0-9]{7}|429[0-3][0-9]{6}|4294[0-8][0-9]{5}|42949[0-5][0-9]{4}|429496[0-6][0-9]{3}|4294967[0-1][0-9]{2}|42949672[0-8][0-9]|429496729[0-5])
ASNAME         = AS-[a-zA-Z0-9_-]*[a-zA-Z0-9]

%%

[ \t\n]+    { ; }

EXCEPT     { return AggrBndryParser.KEYW_EXCEPT; }

OR    { return AggrBndryParser.OP_OR; }
AND   { return AggrBndryParser.OP_AND; }

{ASNO} {
    ParserHelper.validateAsNumber(yytext());
    return AggrBndryParser.TKN_ASNO;
}

(({ASNO}|peeras|{ASNAME}):)*{ASNAME}(:({ASNO}|peeras|{ASNAME}))* {
    return AggrBndryParser.TKN_ASNAME;
}

. {
    return yytext().charAt(0);
}

