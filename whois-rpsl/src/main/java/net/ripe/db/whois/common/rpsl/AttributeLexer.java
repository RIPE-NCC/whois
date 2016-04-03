package net.ripe.db.whois.common.rpsl;

public interface AttributeLexer {

    public void yyclose() throws java.io.IOException;

    public void yyreset(java.io.Reader reader);

    public int yystate();

    public void yybegin(int newState);

    public String yytext();

    public char yycharat(int pos);

    public int yylength();

    public void yypushback(int number);

    public int yylex() throws java.io.IOException;
}
