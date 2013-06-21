%{
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.ParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

/*
  filename: name.y

  description:
    Defines the grammar for an RPSL object name attribute.
    Ref. RFC 2622, Section 2.

  notes:
    Defines tokens for the associated lexer, name.l.
*/
%}

%token TKN_NAME
%token TKN_RESERVED

%%

/*
reserved: TKN_RESERVED {
    yyerror("reserved word");
};
*/

name: TKN_NAME {
    System.out.println("name");
};

%%

protected final Logger LOGGER = LoggerFactory.getLogger(NameParser.class);

private NameLexer lexer;

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

public void yyerror (final String error) {
    String errorMessage = (yylval.sval == null ? error : yylval.sval);
    ParserHelper.parserError(errorMessage);
}

@Override
public Void parse(final String attributeValue) {
	lexer = new NameLexer(new StringReader(attributeValue), this);
    final int result = yyparse();
	if (result > 0) {
	    throw new IllegalArgumentException("Unexpected parse result: " + result);
	}
	return null;
}