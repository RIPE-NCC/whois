%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: peering.y

  description:
    Defines the grammar for an RPSL peering attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Defines tokens for the associated lexer, peering.l.
*/
%}

%token OP_OR OP_AND
%token TKN_ASNO TKN_ASNAME
%token TKN_IPV4 TKN_DNS TKN_RTRSNAME TKN_PRNGNAME
%token KEYW_EXCEPT
%token KEYW_AT
%token <sval> TKN_DNS
%type <sval> domain_name


%%

peering: as_expr opt_router_expr opt_router_expr_with_at
| TKN_PRNGNAME
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
| domain_name {
    ParserHelper.validateDomainName($1);
}
| TKN_RTRSNAME
;

domain_name: TKN_DNS
| domain_name '.' TKN_DNS
;

%%

private PeeringLexer lexer;

private int yylex () {
	int yyl_return = -1;
	try {
		yyl_return = lexer.yylex();
	}
	catch (java.io.IOException e) {
		ParserHelper.log(e);
	}
	return yyl_return;
}

public void yyerror (final String error) {
    ParserHelper.parserError(error);
}

@Override
public Void parse(final String attributeValue) {
	lexer = new PeeringLexer(new java.io.StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}
