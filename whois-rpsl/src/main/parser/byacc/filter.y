%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
/*
  filename: filter.y

  description:
    Defines the grammar for an RPSL filter attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Defines tokens for the associated lexer, filter.l.
*/
%}


%token OP_OR OP_AND OP_NOT OP_MS OP_EQUAL OP_APPEND OP_COMPARE
%token KEYW_ANY KEYW_PEERAS
%token KEYW_IGP_COST KEYW_PREPEND KEYW_APPEND KEYW_DELETE KEYW_CONTAINS
%token KEYW_INTERNET KEYW_NO_EXPORT KEYW_NO_ADVERTISE KEYW_SELF
%token ASPATH_POSTFIX
%token TKN_FLTRNAME TKN_ASNO TKN_ASRANGE TKN_RSNAME TKN_ASNAME TKN_PRFXV4 TKN_PRFXV4RNG
%token TKN_PREF TKN_MED TKN_DPA TKN_ASPATH TKN_COMMUNITY
%token TKN_COMM_NO TKN_NEXT_HOP TKN_IPV4 TKN_COST
%token <sval> TKN_INT TKN_DNS
%type <sval> domain_name


%%

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
| TKN_PRFXV4RNG
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
| filter_aspath_range TKN_ASNO '-' TKN_ASNO
| filter_aspath_range TKN_ASRANGE
| filter_aspath_range TKN_ASNAME
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

domain_name: TKN_DNS
| domain_name '.' TKN_DNS
;


%%

protected final Logger LOGGER = LoggerFactory.getLogger(FilterParser.class);

private FilterLexer lexer;

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

public void yyerror (String error) {
    String errorMessage = (yylval.sval == null ? error : yylval.sval);
    ParserHelper.parserError(errorMessage);
}

@Override
public Void parse(final String attributeValue) {
	lexer = new FilterLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}