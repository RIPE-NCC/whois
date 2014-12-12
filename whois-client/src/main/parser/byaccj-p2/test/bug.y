
%{
import java.io.*;
import java.util.StringTokenizer;
%}


%token          ITEM
%token <sval>   NAME
%token          REPLACE
%token          OVERRIDE
%token          TERMINATOR
%token          DONE

%type   <sval>   identifier 
%type   <ival>   mode 


%start defn
%%

defn             : ITEM identifier mode DONE '\n' 
                    {
                      System.out.println( "Item definition" );
                      System.out.println( "   Name = " + $2 );
                      System.out.println( "   mode = " + $3 );
                    }

identifier      : NAME  { $$ = $1; }

mode            : /* empty */    { $$ = OVERRIDE; }
                | REPLACE        { $$ = REPLACE; }
                | OVERRIDE       { $$ = OVERRIDE; }
                ;

%%

// Code cut from the byacc demo page and modified to take 
// first arg as input file name

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
 String tokstr;
 Double d;
   //System.out.print("yylex ");
   if (!st.hasMoreTokens())
     if (!newline)
       {
       newline=true;
       System.out.println("tok: \\n");
       return '\n';  //So we look like classic YACC example
       }
     else
       return 0;

   s = st.nextToken();

   try {
     if ( s.equalsIgnoreCase("ITEM") ) {
       tok = ITEM;
       System.out.println("tok: ITEM");
     } else if ( s.equalsIgnoreCase("REPLACE") ) {
       tok = REPLACE;
       System.out.println("tok: REPLACE");
     } else if ( s.equalsIgnoreCase("DONE") ) {
       tok = DONE;
       System.out.println("tok: DONE");
     } else if ( s.equalsIgnoreCase("OVERRIDE")) {
       tok = OVERRIDE;
       System.out.println("tok: OVERRIDE");
     } else {
       yylval = new parserval( s );
       tok = NAME;
       System.out.println("tok: NAME -> " + s);
     }
   } catch ( Throwable e ) {
     return 0;
   }


   return tok;
 }



 void dotest( String filepath ) throws java.io.FileNotFoundException

 {

   
   BufferedReader in = new BufferedReader(new FileReader(filepath));

   // AS per the calculator demo
   System.out.println("Note: you will need to separate the items");

   System.out.println("with spaces, i.e.:  'ITEM hello override");

   while (true)

     {
       
       System.out.print("example item:");
       
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



 public static void main(String args[]) throws
java.io.FileNotFoundException

 {

   parser par = new parser();
   par.yydebug = true;
   System.out.println( "parse input file: " + args[0] ); 
   par.dotest(args[0]);

 }

// Invoke this code with 
// java parser test.input
// where test.input is the file containing the input text

