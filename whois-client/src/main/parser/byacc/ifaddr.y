%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
/*
  filename: ifaddr.y

  description:
    Defines the grammar for an RPSL ifaddr attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Defines tokens for the associated lexer, ifaddr.l.
*/

%}

%token OP_OR OP_EQUAL OP_APPEND OP_COMPARE
%token TKN_ASNO
%token TKN_IPV4
%token KEYW_ACTION
%token TKN_PREF TKN_MED TKN_DPA TKN_ASPATH TKN_COMMUNITY TKN_NEXT_HOP TKN_COST
%token TKN_COMM_NO
%token KEYW_IGP_COST KEYW_SELF KEYW_PREPEND
%token KEYW_APPEND KEYW_DELETE KEYW_CONTAINS
%token KEYW_INTERNET KEYW_NO_EXPORT KEYW_NO_ADVERTISE KEYW_MASKLEN
%token <sval> TKN_INT TKN_DNS


%%

ifaddr: TKN_IPV4 KEYW_MASKLEN masklen
| TKN_IPV4 KEYW_MASKLEN masklen KEYW_ACTION action
;

masklen: TKN_INT {
    ParserHelper.checkMaskLength($1);
}
;

action: rp_attribute ';'
| action rp_attribute ';'
;

rp_attribute: pref
| med
| dpa
| aspath
| community
| next_hop
| cost
;

pref: TKN_PREF OP_EQUAL TKN_INT {
    ParserHelper.check16bit($3);
}
;

med: TKN_MED OP_EQUAL TKN_INT {
    ParserHelper.check16bit($3);
}
| TKN_MED OP_EQUAL KEYW_IGP_COST
;

dpa: TKN_DPA OP_EQUAL TKN_INT {
    ParserHelper.check16bit($3);
}
;

aspath: TKN_ASPATH '.' KEYW_PREPEND '(' asno_list ')'
;

asno_list: TKN_ASNO
| asno_list ',' TKN_ASNO
;

community: TKN_COMMUNITY OP_EQUAL community_list
| TKN_COMMUNITY OP_APPEND community_list
| TKN_COMMUNITY '.' KEYW_APPEND '(' community_elm_list ')'
| TKN_COMMUNITY '.' KEYW_DELETE '(' community_elm_list ')'
| TKN_COMMUNITY '.' KEYW_CONTAINS '(' community_elm_list ')'
| TKN_COMMUNITY '(' community_elm_list ')'
| TKN_COMMUNITY OP_COMPARE community_list
;

community_list: '{' community_elm_list '}'
;

community_elm_list: community_elm
| community_elm_list ',' community_elm
;

community_elm: KEYW_INTERNET
| KEYW_NO_EXPORT
| KEYW_NO_ADVERTISE
| TKN_INT {
    ParserHelper.check32bit($1);
}
| TKN_COMM_NO
;

next_hop: TKN_NEXT_HOP OP_EQUAL TKN_IPV4
| TKN_NEXT_HOP OP_EQUAL KEYW_SELF
;

cost: TKN_COST OP_EQUAL TKN_INT {
    ParserHelper.check16bit($3);
}
;

%%

protected final Logger LOGGER = LoggerFactory.getLogger(IfaddrParser.class);

private IfaddrLexer lexer;

@Override
public Void parse(final String attributeValue) {
    lexer = new IfaddrLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
    if (result > 0) {
        LOGGER.error("can't parse " + attributeValue);
        throw new IllegalArgumentException("Unexpected parse result: " + result);
    }
    return null;
}

public int yylex () {
    int yyl_return = -1;
    try {
        yyl_return = lexer.yylex();
    }
    catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
    }
    return yyl_return;
}


public void yyerror (final String error) {
    String errorMessage = (yylval.sval == null ? error : yylval.sval);
    ParserHelper.parserError(errorMessage);
}

