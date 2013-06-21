package net.ripe.db.whois.common.generated;

import net.ripe.db.whois.common.rpsl.ParserHelper;

/*
  filename: name.flex

  description:
    Defines the tokenizer for an RPSL object name attribute.

    Ref. RFC 2622, Section 2:

   <object-name>
      Many objects in RPSL have a name.  An <object-name> is made up of
      letters, digits, the character underscore "_", and the character
      hyphen "-"; the first character of a name must be a letter, and
      the last character of a name must be a letter or a digit.  The
      following words are reserved by RPSL, and they can not be used as
      names:

          any as-any rs-any peeras
          and or not
          atomic from to at action accept announce except refine
          networks into inbound outbound

      Names starting with certain prefixes are reserved for certain
      object types.  Names starting with "as-" are reserved for as set
      names.  Names starting with "rs-" are reserved for route set
      names.  Names starting with "rtrs-" are reserved for router set
      names.  Names starting with "fltr-" are reserved for filter set
      names.  Names starting with "prng-" are reserved for peering set
      names.

  notes:
    Tokens are defined in the associated grammar, name.y.
*/

%%

%class NameLexer

%byaccj

%unicode

%line
%column
%char

%ignorecase

%{
    /* store a reference to the parser object */
    private NameParser yyparser;

    /* constructor taking an additional parser object */
    public NameLexer(java.io.Reader r, NameParser yyparser) {
        this(r);
        this.yyparser = yyparser;
    }
%}

/* macro definitions */

NAME           = [a-zA-Z]{1}[a-zA-Z0-9_-]{0,78}[a-zA-Z0-9]{1}
RESERVED       = ANY|AS-ANY|RS-ANY|PEERAS|AND|OR|NOT|ATOMIC|FROM|TO|AT|ACTION|ACCEPT|ANNOUNCE|EXCEPT|REFINE|NETWORKS|INTO|INBOUND|OUTBOUND

%%

/* keywords */

[ \t\n]+    { ; }

{RESERVED} {
    return NameParser.TKN_RESERVED;
}

{NAME} {
    return NameParser.TKN_NAME;
}

. {
    return yytext().charAt(0);
}
