%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
/*
  filename: aggr_bndry.y

  description:
    Defines the grammar for an RPSL aggr-bndry attribute.  It was mostly
    stolen from the IRRToolSet, simplified by removing ability to parse
    things defined by a dictionary (we use XML for extensibility rather
    than a dictionary).

  notes:
    Defines tokens for the associated lexer, aggr_bndry.l.
*/
%}

%token OP_OR OP_AND
%token KEYW_EXCEPT
%token TKN_ASNO TKN_ASNAME


%%

aggr_bndry: as_expr
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

%%


protected final Logger LOGGER = LoggerFactory.getLogger(AggrBndryParser.class);

private AggrBndryLexer lexer;

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
	lexer = new AggrBndryLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}


