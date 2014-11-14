%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
/*
  filename: inject.y

  description:
    Defines the grammar for an RPSL inject attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Defines tokens for the associated lexer, inject.l.
*/
%}


%token OP_OR OP_EQUAL OP_APPEND OP_COMPARE OP_AND
%token TKN_ASNO
%token TKN_IPV4
%token KEYW_ACTION
%token TKN_PREF TKN_MED TKN_DPA TKN_ASPATH TKN_COMMUNITY TKN_NEXT_HOP TKN_COST
%token TKN_COMM_NO TKN_RTRSNAME TKN_PRFXV4 TKN_PRFXV4RNG
%token KEYW_IGP_COST KEYW_SELF KEYW_PREPEND
%token KEYW_APPEND KEYW_DELETE KEYW_CONTAINS KEYW_AT KEYW_EXCEPT KEYW_UPON
%token KEYW_STATIC KEYW_HAVE_COMPONENTS KEYW_EXCLUDE
%token KEYW_INTERNET KEYW_NO_EXPORT KEYW_NO_ADVERTISE KEYW_MASKLEN
%token <sval> TKN_INT TKN_DNS
%type <sval> domain_name


%%

inject: opt_router_expr_with_at opt_action opt_inject_expr
;

opt_router_expr_with_at:
| KEYW_AT router_expr
;

router_expr: router_expr OP_OR router_expr_term
| router_expr_term
;

router_expr_term: router_expr_term OP_AND router_expr_factor
| router_expr_term KEYW_EXCEPT router_expr_factor
| router_expr_factor
;

router_expr_factor: '(' router_expr ')'
| router_expr_operand
;

router_expr_operand: TKN_IPV4
| domain_name {
    ParserHelper.checkStringLength($1, 255);
}
| TKN_RTRSNAME
;

domain_name: TKN_DNS
| domain_name '.' TKN_DNS
;

opt_action:
| KEYW_ACTION action
;

opt_inject_expr:
| KEYW_UPON inject_expr
;

inject_expr: inject_expr OP_OR inject_expr_term
| inject_expr_term
;

inject_expr_term: inject_expr_term OP_AND inject_expr_factor
| inject_expr_factor
;

inject_expr_factor: '(' inject_expr ')'
| inject_expr_operand
;

inject_expr_operand: KEYW_STATIC
| KEYW_HAVE_COMPONENTS '{' opt_filter_prefix_list '}'
| KEYW_EXCLUDE '{' opt_filter_prefix_list '}'
;

opt_filter_prefix_list:
| filter_prefix_list
;

filter_prefix_list: filter_prefix_list_prefix
| filter_prefix_list ',' filter_prefix_list_prefix
;

filter_prefix_list_prefix: TKN_PRFXV4
| TKN_PRFXV4RNG
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

protected final Logger LOGGER = LoggerFactory.getLogger(InjectParser.class);

private InjectLexer lexer;

private int yylex () {
	int yyl_return = -1;
	try {
		yyl_return = lexer.yylex();
	}
	catch (IOException e) {
		 LOGGER.error("IO error :" + e);
	}
	return yyl_return;
}

public void yyerror(final String error) {
    String errorMessage = (yylval.sval == null ? error : yylval.sval);
    ParserHelper.parserError(errorMessage);
}

@Override
public Void parse(final String attributeValue) {
	lexer = new InjectLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}
