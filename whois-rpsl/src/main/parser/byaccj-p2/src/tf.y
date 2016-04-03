%{
import java.lang.Math;
import java.io.*;
import java.util.StringTokenizer;
%}
     
/* YACC Declarations */
%token NUM
%left '-' '+'
%left '*' '/'
%left NEG     /* negation--unary minus */
%right '^'    /* exponentiation        */
     
/* Grammar follows */
%%
input:    /* empty string */
             | input line
     ;
     
line:     '\n'
          | exp '\n'  { System.out.println(" " + $1 + " "); }
     ;
     
exp:      NUM                { $$ = $1;         }
             | exp '+' exp        { $$ = $1 + $3;    }
             | exp '-' exp        { $$ = $1 - $3;    }
             | exp '*' exp        { $$ = $1 * $3;    }
             | exp '/' exp        { $$ = $1 / $3;    }
             | '-' exp  %prec NEG { $$ = -$2;        }
             | exp '^' exp        { $$ = Math.pow($1, $3); }
             | '(' exp ')'        { $$ = $2;         }
     ;
%%

String ins;
StringTokenizer st;

void yyerror(String s)
{
  System.out.println("par:"+s);
}

boolean newline;
int yylex()
{
String s;
int tok;
Double d;
  //System.out.print("yylex ");
  if (!st.hasMoreTokens())
    if (!newline)
      {
      newline=true;
      return '\n';  //So we look like classic YACC example
      }
    else
      return 0;
  s = st.nextToken();
  //System.out.println("tok:"+s);
  try
    {
    d = Double.valueOf(s);/*this may fail*/
    yylval = d.doubleValue();
    tok = NUM;
    }
  catch (Exception e)
    {
    tok = s.charAt(0);/*if not float, return char*/
    }
  return tok;
}

void dotest()
{
BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
  System.out.println("BYACC/Java Calculator Demo");
  System.out.println("Note: Since this example uses the StringTokenizer");
  System.out.println("for simplicity, you will need to separate the items");
  System.out.println("with spaces, i.e.:  '( 3 + 5 ) * 2'");
  while (true)
    {
    System.out.print("expression:");
    try
      {
      ins = in.readLine();
      }
    catch (Exception e)
      {
      }
    st = new StringTokenizer(ins);
    newline=false;
    yyparse();
    }
}

public static void main(String args[])
{
  parser par = new parser(false);
  par.dotest();
}
