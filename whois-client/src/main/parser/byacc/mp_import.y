%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
/*
  filename: mp_import.y

  description:
    Defines the grammar for an RPSLng mp-import attribute. Derived from
    import.y.

  notes:
    Defines tokens for the associated lexer, mp_import.l.
*/
%}


%token OP_OR OP_AND OP_NOT OP_MS OP_EQUAL OP_APPEND OP_COMPARE
%token KEYW_ANY KEYW_PEERAS
%token ASPATH_POSTFIX
%token TKN_FLTRNAME TKN_ASNO TKN_ASRANGE TKN_RSNAME TKN_ASNAME TKN_PRFXV4 TKN_PRFXV4RNG
%token TKN_IPV4 TKN_RTRSNAME TKN_PRNGNAME
%token TKN_IPV6 TKN_IPV6DC TKN_PRFXV6 TKN_PRFXV6DC TKN_PRFXV6RNG TKN_PRFXV6DCRNG
%token KEYW_ACTION KEYW_EXCEPT
%token KEYW_AFI
%token KEYW_IPV4_TXT KEYW_IPV6_TXT KEYW_ANY_TXT
%token KEYW_UNICAST KEYW_MULTICAST
%token TKN_PREF TKN_MED TKN_DPA TKN_ASPATH TKN_COMMUNITY TKN_NEXT_HOP TKN_COST
%token TKN_COMM_NO
%token KEYW_IGP_COST KEYW_SELF KEYW_PREPEND
%token KEYW_APPEND KEYW_DELETE KEYW_CONTAINS KEYW_AT
%token KEYW_INTERNET KEYW_NO_EXPORT KEYW_NO_ADVERTISE
%token KEYW_PROTOCOL TKN_PROTOCOL
%token KEYW_INTO KEYW_REFINE KEYW_ACCEPT KEYW_FROM
%token <sval> TKN_INT TKN_DNAME
%type <sval> domain_name


%%

mp_import_attribute: opt_protocol_from opt_protocol_into afi_import_expr
| opt_protocol_from opt_protocol_into import_factor option_semicolon
| opt_protocol_from opt_protocol_into KEYW_AFI afi_list import_factor option_semicolon
;

option_semicolon: /* empty */
| ';'
;

opt_protocol_from:
| KEYW_PROTOCOL TKN_PROTOCOL
;

opt_protocol_into:
| KEYW_INTO TKN_PROTOCOL
;

afi_import_expr: import_expr
| KEYW_AFI afi_list import_expr
;

import_expr: import_term
| import_term KEYW_REFINE afi_import_expr
| import_term KEYW_EXCEPT afi_import_expr
;

afi_list: afi_list ',' afi_value_v4
| afi_list ',' afi_value_v6
| afi_list ',' afi_value_any
| afi_value_v4
| afi_value_v6
| afi_value_any
;

afi_value_v4: KEYW_IPV4_TXT
| KEYW_IPV4_TXT '.' KEYW_MULTICAST
|  KEYW_IPV4_TXT '.' KEYW_UNICAST
;

afi_value_v6: KEYW_IPV6_TXT
| KEYW_IPV6_TXT '.' KEYW_MULTICAST
|  KEYW_IPV6_TXT '.' KEYW_UNICAST
;

afi_value_any: KEYW_ANY_TXT '.' KEYW_MULTICAST
|  KEYW_ANY_TXT '.' KEYW_UNICAST
;

import_term: import_factor ';'
| '{' import_factor_list '}'
;

import_factor_list: import_factor ';'
| import_factor_list import_factor ';'
;

import_factor: import_peering_action_list KEYW_ACCEPT filter
;

import_peering_action_list: KEYW_FROM peering opt_action
| import_peering_action_list KEYW_FROM peering opt_action
;

peering: as_expr opt_router_expr opt_router_expr_with_at
| TKN_PRNGNAME
;

opt_action:
| KEYW_ACTION action
;

as_expr: as_expr OP_OR as_expr_term
| as_expr_term
;

as_expr_term: as_expr_term OP_AND as_expr_factor
| as_expr_term KEYW_EXCEPT as_expr_factor
| as_expr_factor
;

as_expr_factor: '(' as_expr ')'
| as_expr_operand
;

as_expr_operand: TKN_ASNO
| TKN_ASNAME
;

opt_router_expr:
| router_expr
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
| TKN_IPV6
| TKN_IPV6DC
| domain_name {
	ParserHelper.checkStringLength($1, 255);
}
| TKN_RTRSNAME
;

domain_name: TKN_DNAME
| domain_name '.' TKN_DNAME
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
| TKN_NEXT_HOP OP_EQUAL TKN_IPV6
| TKN_NEXT_HOP OP_EQUAL TKN_IPV6DC
| TKN_NEXT_HOP OP_EQUAL KEYW_SELF
;

cost: TKN_COST OP_EQUAL TKN_INT {
	ParserHelper.check16bit($3);
}
;

filter: filter OP_OR filter_term
| filter filter_term %prec OP_OR
| filter_term
;

filter_term : filter_term OP_AND filter_factor
| filter_factor
;

filter_factor :  OP_NOT filter_factor
| '(' filter ')'
| filter_operand
;

filter_operand: KEYW_ANY
| '<' filter_aspath '>'
| rp_attribute
| TKN_FLTRNAME
| filter_prefix
;

filter_prefix: filter_prefix_operand OP_MS
|  filter_prefix_operand
;

filter_prefix_operand: TKN_ASNO
| KEYW_PEERAS
| TKN_ASNAME
| TKN_RSNAME
| '{' opt_filter_prefix_list '}'
;

opt_filter_prefix_list:
| filter_prefix_list
;

filter_prefix_list: filter_prefix_list_prefix
| filter_prefix_list ',' filter_prefix_list_prefix
;

filter_prefix_list_prefix: TKN_PRFXV4
| TKN_PRFXV6
| TKN_PRFXV6DC
| TKN_PRFXV4RNG
| TKN_PRFXV6RNG
| TKN_PRFXV6DCRNG
;

filter_aspath: filter_aspath '|' filter_aspath_term
| filter_aspath_term
;

filter_aspath_term: filter_aspath_term filter_aspath_closure
| filter_aspath_closure
;

filter_aspath_closure: filter_aspath_closure '*'
| filter_aspath_closure '?'
| filter_aspath_closure '+'
| filter_aspath_closure ASPATH_POSTFIX
| filter_aspath_factor
;

filter_aspath_factor: '^'
| '$'
| '(' filter_aspath ')'
| filter_aspath_no
;

filter_aspath_no: TKN_ASNO
| KEYW_PEERAS
| TKN_ASNAME
| '.'
| '[' filter_aspath_range ']'
| '[' '^' filter_aspath_range ']'
;

filter_aspath_range:
| filter_aspath_range TKN_ASNO
| filter_aspath_range KEYW_PEERAS
| filter_aspath_range '.'
| filter_aspath_range TKN_ASRANGE
| filter_aspath_range TKN_ASNAME
;

%%

protected final Logger LOGGER = LoggerFactory.getLogger(MpImportParser.class);

private MpImportLexer lexer;

private int yylex () {
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

@Override
public Void parse(final String attributeValue) {
	lexer = new MpImportLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}

