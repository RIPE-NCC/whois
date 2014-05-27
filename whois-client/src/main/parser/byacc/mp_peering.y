%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
/*
  filename: mp_peering.y

  description:
    Defines the grammar for an RPSLng mp-peering attribute. Derived
    from peering.l.

  notes:
    Defines tokens for the associated lexer, mp_peering.l.
*/
%}

%token OP_OR OP_AND
%token TKN_ASNO TKN_ASNAME
%token TKN_IPV4 TKN_IPV6 TKN_IPV6DC TKN_DNS TKN_RTRSNAME TKN_PRNGNAME
%token KEYW_EXCEPT
%token KEYW_AT
%token <sval> TKN_DNS
%type <sval> domain_name


%%


mp_peering: as_expr opt_router_expr opt_router_expr_with_at
| TKN_PRNGNAME
;

as_expr: as_expr OP_OR as_expr_term
| as_expr_term
;

as_expr_term: as_expr_term OP_AND as_expr_factor
| as_expr_term KEYW_EXCEPT as_expr_factor
| as_expr_term OP_AND OP_NOT as_expr_factor
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
    ParserHelper.validateDomainName($1);
}
| TKN_RTRSNAME
;

domain_name: TKN_DNS
| domain_name '.' TKN_DNS
;

%%

private MpPeeringLexer lexer;

private int yylex() {
	int yyl_return = -1;
	try {
		yyl_return = lexer.yylex();
	}
	catch (java.io.IOException e) {
		ParserHelper.log("IO error :" + e);
	}
	return yyl_return;
}

public void yyerror(final String error) {
    ParserHelper.parserError(error);
}

@Override
public Void parse(final String attributeValue) {
	lexer = new MpPeeringLexer(new java.io.StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}
