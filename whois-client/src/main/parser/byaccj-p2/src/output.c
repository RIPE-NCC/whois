#include <stdio.h>
#include <string.h>

#include "defs.h"

#define MAX_ELEMENTS_IN_JAVA_METHODS 1000
#define MAX_ELEMENTS_ON_A_LINE 30

static int nvectors;
static int nentries;
static short **froms;
static short **tos;
static short *tally;
static short *width;
static short *state_count;
static short *order;
static short *base;
static short *pos;
static int maxtable;
static short *table;
static short *check;
static int lowzero;
static int high;


void output(void)
{
    free_itemsets();
    free_shifts();
    free_reductions();
    output_stored_text();
    if (jflag)    /*rwj*/
      {
      write_section(jheader);
      output_stype();
      }
    output_defines();
    output_rule_data();
    output_yydefred();
    output_actions();
    free_parser();
    output_debug();
    if (!jflag)    /*rwj*/
      output_stype();
    if (rflag) write_section(tables);
    if (!jflag)    /*rwj*/
      write_section(header);
    output_trailing_text();
    if (jflag)  { /*rwj*/
		/* yio 20020304: nodebug and throws options */
		if (jdebug == TRUE) {
			write_section(jbody_a);
			if (strlen(jyyparse_throws)>0)
				fprintf(code_file,"throws %s\n",jyyparse_throws);
			write_section(jbody_b);
		}
		else {
			write_section(jbody_nodebug_a);
			if (strlen(jyyparse_throws)>0)
				fprintf(code_file,"throws %s\n",jyyparse_throws);
			write_section(jbody_nodebug_b);
		}
	}
    else
		write_section(body);

    output_semantic_actions();
    if (jflag) {  /*rwj*/
		/* yio 20020304: nodebug option */
		if (jdebug == TRUE)
			write_section(jtrailer);
		else
			write_section(jtrailer_nodebug);
	}
    else
		write_section(trailer);
}


void output_rule_data(void)
{
int i;
int j;

    if (jflag)  /*rwj*/
      fprintf(output_file, "final static short yylhs[] = {%29d,",
	    symbol_value[start_symbol]);
    else
      fprintf(output_file, "short yylhs[] = {%42d,",
	    symbol_value[start_symbol]);

    j = 10;
    for (i = 3; i < nrules; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
        else
	    ++j;

        fprintf(output_file, "%5d,", symbol_value[rlhs[i]]);
    }
    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");

    if (jflag) /*rwj*/
      fprintf(output_file, "final static short yylen[] = {%29d,",2);
    else
      fprintf(output_file, "short yylen[] = {%42d,", 2);

    j = 10;
    for (i = 3; i < nrules; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
	else
	  j++;

        fprintf(output_file, "%5d,", rrhs[i + 1] - rrhs[i] - 1);
    }
    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");
}


void output_yydefred(void)
{
int i, j;

    if (jflag)
       fprintf(output_file, "final static short yydefred[] = {%26d,",
	    (defred[0] ? defred[0] - 2 : 0));
    else
       fprintf(output_file, "short yydefred[] = {%39d,",
	    (defred[0] ? defred[0] - 2 : 0));

    j = 10;
    for (i = 1; i < nstates; i++)
    {
	if (j < 10)
	    ++j;
	else
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}

	fprintf(output_file, "%5d,", (defred[i] ? defred[i] - 2 : 0));
    }

    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");
}


void output_actions(void)
{
    nvectors = 2*nstates + nvars;

    froms = NEW2(nvectors, short *);
    tos = NEW2(nvectors, short *);
    tally = NEW2(nvectors, short);
    width = NEW2(nvectors, short);

    token_actions();
    FREE(lookaheads);
    FREE(LA);
    FREE(LAruleno);
    FREE(accessing_symbol);

    goto_actions();
    FREE(goto_map + ntokens);
    FREE(from_state);
    FREE(to_state);

    sort_actions();
    pack_table();
    output_base();
    output_table();
    output_check();
}


void token_actions(void)
{
int i, j;
int shiftcount, reducecount;
int max, min;
short *actionrow, *r, *s;
action *p;

    actionrow = NEW2(2*ntokens, short);
    for (i = 0; i < nstates; ++i)
    {
	if (parser[i])
	{
	    for (j = 0; j < 2*ntokens; ++j)
	    actionrow[j] = 0;

	    shiftcount = 0;
	    reducecount = 0;
	    for (p = parser[i]; p; p = p->next)
	    {
		if (p->suppressed == 0)
		{
		    if (p->action_code == SHIFT)
		    {
			++shiftcount;
			actionrow[p->symbol] = p->number;
		    }
		    else if (p->action_code == REDUCE && p->number != defred[i])
		    {
			++reducecount;
			actionrow[p->symbol + ntokens] = p->number;
		    }
		}
	    }

	    tally[i] = shiftcount;
	    tally[nstates+i] = reducecount;
	    width[i] = 0;
	    width[nstates+i] = 0;
	    if (shiftcount > 0)
	    {
		froms[i] = r = NEW2(shiftcount, short);
		tos[i] = s = NEW2(shiftcount, short);
		min = MAXSHORT;
		max = 0;
		for (j = 0; j < ntokens; ++j)
		{
		    if (actionrow[j])
		    {
			if (min > symbol_value[j])
			    min = symbol_value[j];
			if (max < symbol_value[j])
			    max = symbol_value[j];
			*r++ = symbol_value[j];
			*s++ = actionrow[j];
		    }
		}
		width[i] = max - min + 1;
	    }
	    if (reducecount > 0)
	    {
		froms[nstates+i] = r = NEW2(reducecount, short);
		tos[nstates+i] = s = NEW2(reducecount, short);
		min = MAXSHORT;
		max = 0;
		for (j = 0; j < ntokens; ++j)
		{
		    if (actionrow[ntokens+j])
		    {
			if (min > symbol_value[j])
			    min = symbol_value[j];
			if (max < symbol_value[j])
			    max = symbol_value[j];
			*r++ = symbol_value[j];
			*s++ = actionrow[ntokens+j] - 2;
		    }
		}
		width[nstates+i] = max - min + 1;
	    }
	}
    }
    FREE(actionrow);
}

void goto_actions(void)
{
    register int i, j, k;

    state_count = NEW2(nstates, short);

    k = default_goto(start_symbol + 1);
    if (jflag)  /*rwj*/
      fprintf(output_file, "final static short yydgoto[] = {%27d,",k);
    else
      fprintf(output_file, "short yydgoto[] = {%40d,", k);
    save_column(start_symbol + 1, k);

    j = 10;
    for (i = start_symbol + 2; i < nsyms; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
	else
	    ++j;

	k = default_goto(i);
	fprintf(output_file, "%5d,", k);
	save_column(i, k);
    }

    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");
    FREE(state_count);
}

int default_goto(int symbol)
{
int i;
int m;
int n;
int default_state;
int max;

    m = goto_map[symbol];
    n = goto_map[symbol + 1];

    if (m == n) return (0);

    for (i = 0; i < nstates; i++)
	state_count[i] = 0;

    for (i = m; i < n; i++)
	state_count[to_state[i]]++;

    max = 0;
    default_state = 0;
    for (i = 0; i < nstates; i++)
    {
	if (state_count[i] > max)
	{
	    max = state_count[i];
	    default_state = i;
	}
    }

    return (default_state);
}



void save_column(int symbol,int default_state)
{
int i;
int m;
int n;
short *sp;
short *sp1;
short *sp2;
int count;
int symno;

    m = goto_map[symbol];
    n = goto_map[symbol + 1];

    count = 0;
    for (i = m; i < n; i++)
    {
	if (to_state[i] != default_state)
	    ++count;
    }
    if (count == 0) return;

    symno = symbol_value[symbol] + 2*nstates;

    froms[symno] = sp1 = sp = NEW2(count, short);
    tos[symno] = sp2 = NEW2(count, short);

    for (i = m; i < n; i++)
    {
	if (to_state[i] != default_state)
	{
	    *sp1++ = from_state[i];
	    *sp2++ = to_state[i];
	}
    }

    tally[symno] = count;
    width[symno] = sp1[-1] - sp[0] + 1;
}

void sort_actions(void)
{
int i;
int j;
int k;
int t;
int w;

  order = NEW2(nvectors, short);
  nentries = 0;

  for (i = 0; i < nvectors; i++)
    {
      if (tally[i] > 0)
	{
	  t = tally[i];
	  w = width[i];
	  j = nentries - 1;

	  while (j >= 0 && (width[order[j]] < w))
	    j--;

	  while (j >= 0 && (width[order[j]] == w) && (tally[order[j]] < t))
	    j--;

	  for (k = nentries - 1; k > j; k--)
	    order[k + 1] = order[k];

	  order[j + 1] = i;
	  nentries++;
	}
    }
}


void pack_table(void)
{
int i;
int place;
int state;

    base = NEW2(nvectors, short);
    pos = NEW2(nentries, short);

    maxtable = 1000;
    table = NEW2(maxtable, short);
    check = NEW2(maxtable, short);

    lowzero = 0;
    high = 0;

    for (i = 0; i < maxtable; i++)
	check[i] = -1;

    for (i = 0; i < nentries; i++)
    {
	state = matching_vector(i);

	if (state < 0)
	    place = pack_vector(i);
	else
	    place = base[state];

	pos[i] = place;
	base[order[i]] = place;
    }

    for (i = 0; i < nvectors; i++)
    {
	if (froms[i])
	    FREE(froms[i]);
	if (tos[i])
	    FREE(tos[i]);
    }

    FREE(froms);
    FREE(tos);
    FREE(pos);
}


/*  The function matching_vector determines if the vector specified by	*/
/*  the input parameter matches a previously considered	vector.  The	*/
/*  test at the start of the function checks if the vector represents	*/
/*  a row of shifts over terminal symbols or a row of reductions, or a	*/
/*  column of shifts over a nonterminal symbol.  Berkeley Yacc does not	*/
/*  check if a column of shifts over a nonterminal symbols matches a	*/
/*  previously considered vector.  Because of the nature of LR parsing	*/
/*  tables, no two columns can match.  Therefore, the only possible	*/
/*  match would be between a row and a column.  Such matches are	*/
/*  unlikely.  Therefore, to save time, no attempt is made to see if a	*/
/*  column matches a previously considered vector.			*/
/*									*/
/*  Matching_vector is poorly designed.  The test could easily be made	*/
/*  faster.  Also, it depends on the vectors being in a specific	*/
/*  order.								*/

int matching_vector(int vector)
{
int i;
int j;
int k;
int t;
int w;
int match;
int prev;

    i = order[vector];
    if (i >= 2*nstates)
	return (-1);

    t = tally[i];
    w = width[i];

    for (prev = vector - 1; prev >= 0; prev--)
    {
	j = order[prev];
	if (width[j] != w || tally[j] != t)
	    return (-1);

	match = 1;
	for (k = 0; match && k < t; k++)
	{
	    if (tos[j][k] != tos[i][k] || froms[j][k] != froms[i][k])
		match = 0;
	}

	if (match)
	    return (j);
    }

    return (-1);
}



int pack_vector(int vector)
{
int i, j, k, l;
int t;
int loc;
int ok;
short *from;
short *to;
int newmax;

    i = order[vector];
    t = tally[i];
    assert(t);

    from = froms[i];
    to = tos[i];

    j = lowzero - from[0];
    for (k = 1; k < t; ++k)
	if (lowzero - from[k] > j)
	    j = lowzero - from[k];
    for (;; ++j)
    {
	if (j == 0)
	    continue;
	ok = 1;
	for (k = 0; ok && k < t; k++)
	{
	    loc = j + from[k];
	    if (loc >= maxtable)
	    {
		if (loc >= MAXTABLE)
		    fatal("maximum table size exceeded");

		newmax = maxtable;
		do { newmax += 200; } while (newmax <= loc);
		table = (short *) REALLOC(table, newmax*sizeof(short));
		if (table == 0) no_space();
		check = (short *) REALLOC(check, newmax*sizeof(short));
		if (check == 0) no_space();
		for (l  = maxtable; l < newmax; ++l)
		{
		    table[l] = 0;
		    check[l] = -1;
		}
		maxtable = newmax;
	    }

	    if (check[loc] != -1)
		ok = 0;
	}
	for (k = 0; ok && k < vector; k++)
	{
	    if (pos[k] == j)
		ok = 0;
	}
	if (ok)
	{
	    for (k = 0; k < t; k++)
	    {
		loc = j + from[k];
		table[loc] = to[k];
		check[loc] = from[k];
		if (loc > high) high = loc;
	    }

	    while (check[lowzero] != -1)
		++lowzero;

	    return (j);
	}
    }
}



void output_base(void)
{
int i, j;
    
    if (jflag)  /*rwj*/
      fprintf(output_file, "final static short yysindex[] = {%26d,",base[0]);
    else
      fprintf(output_file, "short yysindex[] = {%39d,", base[0]);

    j = 10;
    for (i = 1; i < nstates; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
	else
	    ++j;

	fprintf(output_file, "%5d,", base[i]);
    }

    if (!rflag) outline += 2;
    
    if (jflag) /*rwj*/
      fprintf(output_file, "\n};\nfinal static short yyrindex[] = {%26d,",
	    base[nstates]);
    else
      fprintf(output_file, "\n};\nshort yyrindex[] = {%39d,",
	    base[nstates]);

    j = 10;
    for (i = nstates + 1; i < 2*nstates; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
	else
	    ++j;

	fprintf(output_file, "%5d,", base[i]);
    }

    if (!rflag) outline += 2;
    if (jflag)/*rwj*/
      fprintf(output_file, "\n};\nfinal static short yygindex[] = {%26d,",
	    base[2*nstates]);
    else
      fprintf(output_file, "\n};\nshort yygindex[] = {%39d,",
	    base[2*nstates]);

    j = 10;
    for (i = 2*nstates + 1; i < nvectors - 1; i++)
    {
	if (j >= 10)
	{
	    if (!rflag) ++outline;
	    putc('\n', output_file);
	    j = 1;
	}
	else
	    ++j;

	fprintf(output_file, "%5d,", base[i]);
    }

    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");
    FREE(base);
}

void output_java_short_array(short *stable, int len, char *basename)
{
    int i;
    int j;
    int functionIndex = 0;
    fprintf(output_file, "static short[] %s = create_%s();\n", basename, basename);
    for (i = 0; i < len; i++) {
        if (i % MAX_ELEMENTS_IN_JAVA_METHODS == 0) {
            if (i != 0) {
                fprintf(output_file, "};\n}\n");
            }
            fprintf(output_file, "private static short[] create_%s%d() {\n", basename, functionIndex);
            functionIndex++;
            fprintf(output_file, "return new short[] {\n");
        } else {
            fprintf(output_file, ", ");
            if (i % MAX_ELEMENTS_ON_A_LINE == MAX_ELEMENTS_ON_A_LINE - 1) {
                fprintf(output_file, "\n");
            }
        }
        fprintf(output_file, "%d", stable[i]);
    }
    fprintf(output_file, "};\n}\n");
    fprintf(output_file, "private static short[] create_%s() {\n", basename);
    for (i = 0; i < functionIndex; i++) {
        fprintf(output_file, "short[] %s%d = create_%s%d();\n", basename, i, basename, i);
    }
    fprintf(output_file, "short[] new_%s = new short[%d];\n", basename, len);
    fprintf(output_file, "int next = 0;\n");
    for (i = 0; i < functionIndex; i++) {
        fprintf(output_file, "System.arraycopy(%s%d, 0, new_%s, next, %s%d.length);\n", basename, i, basename, basename, i);
        fprintf(output_file, "next += %s%d.length;\n", basename, i);
    }
    fprintf(output_file, "return new_%s;\n}\n", basename);
}

void output_table(void)
{
int i;
int j;

    ++outline;
    if (jflag)  /*rwj*/
      {
          fprintf(code_file, "final static int YYTABLESIZE=%d;\n", high);
          output_java_short_array(table, high + 1, "yytable");
      }
    else
      {
          fprintf(code_file, "#define YYTABLESIZE %d\n", high);
          fprintf(output_file, "short yytable[] = {%40d,", table[0]);
          
    
        j = 10;
        for (i = 1; i <= high; i++)
        {
        if (j >= 10)
        {
            if (!rflag) ++outline;
            putc('\n', output_file);
            j = 1;
        }
        else
            ++j;
    
        fprintf(output_file, "%5d,", table[i]);
        }
    
        if (!rflag) outline += 2;
        fprintf(output_file, "\n};\n");
      }
    FREE(table);
}

void output_check(void)
{
    register int i;
    register int j;
    int functionIndex = 0;
    
    if (jflag) {
        output_java_short_array(check, high + 1, "yycheck");
    } else {
        fprintf(output_file, "short yycheck[] = {%40d,", check[0]);
    
        j = 10;
        for (i = 1; i <= high; i++)
        {
            if (j >= 10)
            {
                if (!rflag) ++outline;
                putc('\n', output_file);
                j = 1;
            }
            else
                ++j;
    
            fprintf(output_file, "%5d,", check[i]);
        }
    
        if (!rflag) outline += 2;
        fprintf(output_file, "\n};\n");
    }
    FREE(check);
}


int is_C_identifier(char *name)
{
char *s;
int c;

    s = name;
    c = *s;
    if (c == '"')
    {
	c = *++s;
	if (!isalpha(c) && c != '_' && c != '$')
	    return (0);
	while ((c = *++s) != '"')
	{
	    if (!isalnum(c) && c != '_' && c != '$')
		return (0);
	}
	return (1);
    }

    if (!isalpha(c) && c != '_' && c != '$')
	return (0);
    while ((c = *++s)!=0)
    {
	if (!isalnum(c) && c != '_' && c != '$')
	    return (0);
    }
    return (1);
}


void output_defines(void)
{
int c, i;
char *s;

    if (jflag && dflag)
    {
        if (jpackage_name && strlen(jpackage_name)>0)
            fprintf(defines_file,"package %s;\n",jpackage_name);
	fprintf(defines_file, "public interface %s%s {\n",jclass_name,JAVA_INTERFACE_SUFFIX);
    }
    for (i = 2; i < ntokens; ++i)
    {
	s = symbol_name[i];
	if (is_C_identifier(s))
	{
	    if (jflag)    /*rwj*/
                fprintf(dflag?defines_file:code_file, "public final static short ");
	    else
	    {
                fprintf(code_file, "#define ");
                if (dflag) fprintf(defines_file, "#define ");
            }
	    c = *s;
	    if (c == '"')
	    {
		while ((c = *++s) != '"')
		{
		    if (!jflag || (jflag && !dflag)) putc(c, code_file);
		    if (dflag) putc(c, defines_file);
		}
	    }
	    else
	    {
		do
		{
		    if (!jflag || (jflag && !dflag)) putc(c, code_file);
		    if (dflag) putc(c, defines_file);
		}
		while ((c = *++s)!=0);
	    }
	    ++outline;
	    if (jflag)  /*rwj*/
                fprintf(dflag?defines_file:code_file, "=%d;\n", symbol_value[i]);
	    else 
            {
                fprintf(code_file, " %d\n", symbol_value[i]);
                if (dflag) fprintf(defines_file, " %d\n", symbol_value[i]);
            }
	}
    }

    ++outline;
    if (jflag) /*rwj*/
      fprintf(code_file, "public final static short YYERRCODE=%d;\n", symbol_value[1]);
    else
      fprintf(code_file, "#define YYERRCODE %d\n", symbol_value[1]);

    if (dflag && unionized)
    {
	fclose(union_file);
	union_file = fopen(union_file_name, "r");
	if (union_file == NULL) open_error(union_file_name);
	while ((c = getc(union_file)) != EOF)
	    putc(c, defines_file);
	fprintf(defines_file, " YYSTYPE;\nextern YYSTYPE yylval;\n");
    }
    if (jflag && dflag)
	fprintf(defines_file, "}\n",jclass_name,JAVA_INTERFACE_SUFFIX);    
}


void output_stored_text(void)
{
int c;
FILE *in, *out;

    fclose(text_file);
    text_file = fopen(text_file_name, "r");
    if (text_file == NULL)
	open_error(text_file_name);
    in = text_file;
    if ((c = getc(in)) == EOF)
	return;
    out = code_file;
    if (c ==  '\n')
	++outline;
    putc(c, out);
    while ((c = getc(in)) != EOF)
    {
	if (c == '\n')
	    ++outline;
	putc(c, out);
    }
    if (!lflag)
      if (jflag)/*rwj*/
	fprintf(out, jline_format, ++outline + 1, code_file_name);
      else
	fprintf(out, line_format, ++outline + 1, code_file_name);
}


void output_debug(void)
{
int i, j, k, max;
char **symnam, *s;

    ++outline;
    if (jflag)  /*rwj*/
      fprintf(code_file, "final static short YYFINAL=%d;\n", final_state);
    else
      fprintf(code_file, "#define YYFINAL %d\n", final_state);
    outline += 3;
    if (!jflag)/*rwj*/
      fprintf(code_file, "#ifndef YYDEBUG\n#define YYDEBUG %d\n#endif\n",
	    tflag);
    if (rflag)
	fprintf(output_file, "#ifndef YYDEBUG\n#define YYDEBUG %d\n#endif\n",
		tflag);

    max = 0;
    for (i = 2; i < ntokens; ++i)
	if (symbol_value[i] > max)
	    max = symbol_value[i];
    ++outline;
    if (jflag) /*rjw*/
      fprintf(code_file, "final static short YYMAXTOKEN=%d;\n", max);
    else
      fprintf(code_file, "#define YYMAXTOKEN %d\n", max);

    symnam = (char **) MALLOC((max+1)*sizeof(char *));
    if (symnam == 0) no_space();

    /* Note that it is  not necessary to initialize the element		*/
    /* symnam[max].							*/
    for (i = 0; i < max; ++i)
	symnam[i] = 0;
    for (i = ntokens - 1; i >= 2; --i)
	symnam[symbol_value[i]] = symbol_name[i];
    symnam[0] = "end-of-file";

    if (!rflag) ++outline;
    if (jflag)/*rwj*/
      fprintf(output_file, "final static String yyname[] = {");
    else
      fprintf(output_file, "#if YYDEBUG\nchar *yyname[] = {");
    j = 80;
    for (i = 0; i <= max; ++i)
    {
	if ((s = symnam[i])!=0)
	{
	    if (s[0] == '"')
	    {
		k = 7;
		while (*++s != '"')
		{
		    ++k;
		    if (*s == '\\')
		    {
			k += 2;
			if (*++s == '\\')
			    ++k;
		    }
		}
		j += k;
		if (j > 80)
		{
		    if (!rflag) ++outline;
		    putc('\n', output_file);
		    j = k;
		}
		fprintf(output_file, "\"\\\"");
		s = symnam[i];
		while (*++s != '"')
		{
		    if (*s == '\\')
		    {
			fprintf(output_file, "\\\\");
			if (*++s == '\\')
			    fprintf(output_file, "\\\\");
			else
			    putc(*s, output_file);
		    }
		    else
			putc(*s, output_file);
		}
		fprintf(output_file, "\\\"\",");
	    }
	    else if (s[0] == '\'')
	    {
		if (s[1] == '"')
		{
		    j += 7;
		    if (j > 80)
		    {
			if (!rflag) ++outline;
			putc('\n', output_file);
			j = 7;
		    }
		    fprintf(output_file, "\"'\\\"'\",");
		}
		else
		{
		    k = 5;
		    while (*++s != '\'')
		    {
			++k;
			if (*s == '\\')
			{
			    k += 2;
			    if (*++s == '\\')
				++k;
			}
		    }
		    j += k;
		    if (j > 80)
		    {
			if (!rflag) ++outline;
			putc('\n', output_file);
			j = k;
		    }
		    fprintf(output_file, "\"'");
		    s = symnam[i];
		    while (*++s != '\'')
		    {
			if (*s == '\\')
			{
			    fprintf(output_file, "\\\\");
			    if (*++s == '\\')
				fprintf(output_file, "\\\\");
			    else
				putc(*s, output_file);
			}
			else
			    putc(*s, output_file);
		    }
		    fprintf(output_file, "'\",");
		}
	    }
	    else
	    {
		k = strlen(s) + 3;
		j += k;
		if (j > 80)
		{
		    if (!rflag) ++outline;
		    putc('\n', output_file);
		    j = k;
		}
		putc('"', output_file);
		do { putc(*s, output_file); } while (*++s);
		fprintf(output_file, "\",");
	    }
	}
	else
	{
	if (jflag)/*rwj -- null strings should be 'null'*/
	    {
	    j += 5;
	    if (j > 80)
	    {
		if (!rflag) ++outline;
		putc('\n', output_file);
		j = 5;
	    }
	    fprintf(output_file, "null,");
	    }
	  else /*rwj -- not jflag, output a 0*/
	    {
	    j += 2;
	    if (j > 80)
	    {
		if (!rflag) ++outline;
		putc('\n', output_file);
		j = 2;
	    }
	    fprintf(output_file, "0,");
	    }
	}
    }
    if (!rflag) outline += 2;
    fprintf(output_file, "\n};\n");
    FREE(symnam);

    if (!rflag) ++outline;
    if (jflag)/*rwj*/
      fprintf(output_file, "final static String yyrule[] = {\n");
    else
      fprintf(output_file, "char *yyrule[] = {\n");
    for (i = 2; i < nrules; ++i)
    {
	fprintf(output_file, "\"%s :", symbol_name[rlhs[i]]);
	for (j = rrhs[i]; ritem[j] > 0; ++j)
	{
	    s = symbol_name[ritem[j]];
	    if (s[0] == '"')
	    {
		fprintf(output_file, " \\\"");
		while (*++s != '"')
		{
		    if (*s == '\\')
		    {
			if (s[1] == '\\')
			    fprintf(output_file, "\\\\\\\\");
			else
			    fprintf(output_file, "\\\\%c", s[1]);
			++s;
		    }
		    else
			putc(*s, output_file);
		}
		fprintf(output_file, "\\\"");
	    }
	    else if (s[0] == '\'')
	    {
		if (s[1] == '"')
		    fprintf(output_file, " '\\\"'");
		else if (s[1] == '\\')
		{
		    if (s[2] == '\\')
			fprintf(output_file, " '\\\\\\\\");
		    else
			fprintf(output_file, " '\\\\%c", s[2]);
		    s += 2;
		    while (*++s != '\'')
			putc(*s, output_file);
		    putc('\'', output_file);
		}
		else
		    fprintf(output_file, " '%c'", s[1]);
	    }
	    else
		fprintf(output_file, " %s", s);
	}
	if (!rflag) ++outline;
	fprintf(output_file, "\",\n");
    }

    if (!rflag) outline += 2;
    if (jflag)/*rwj*/
      fprintf(output_file, "};\n\n");
    else
      fprintf(output_file, "};\n#endif\n");
}

void output_stype(void)
{
int prim; /*is the Java semantic type a primitive?*/
char filenam[128];

char *jvalclass; /* either [Parser]Val or a user-defined class */

FILE *f;
  if (jflag)/*rwj*/
    {
	jvalclass = (char *) MALLOC(strlen(jclass_name) + 4);  /* Val\0 */
	sprintf(jvalclass,"%sVal",jclass_name);

    if (jsemantic_type && strlen(jsemantic_type)>0)/*specific type requested*/
      {
    char *raw_type=jsemantic_type;
    if (strcmp(jsemantic_type,"byte")==0 ||
        strcmp(jsemantic_type,"short")==0 ||
        strcmp(jsemantic_type,"char")==0 ||
        strcmp(jsemantic_type,"int")==0 ||
        strcmp(jsemantic_type,"long")==0 ||
        strcmp(jsemantic_type,"float")==0 ||
        strcmp(jsemantic_type,"double")==0) {
       prim=1;
    } else {
       prim=0;
       char *end=strchr(jsemantic_type,'<');
       if (end!=NULL) {
         raw_type=(char *)CALLOC(end-jsemantic_type+1,sizeof(char));
         strncpy(raw_type,jsemantic_type,end-jsemantic_type);
       }
    }
    fprintf(code_file,"\n\n//########## SEMANTIC VALUES ##########\n");
    fprintf(code_file,"//## **user defined:%s\n",jsemantic_type);
    fprintf(code_file,"String   yytext;//user variable to return contextual strings\n");
    fprintf(code_file,"%s yyval; //used to return semantic vals from action routines\n",
                          jsemantic_type);
    fprintf(code_file,"%s yylval;//the 'lval' (result) I got from yylex()\n",
                          jsemantic_type);
    fprintf(code_file,"%s valstk[] = new %s[YYSTACKSIZE];\n",
                          jsemantic_type,raw_type);
    fprintf(code_file,"int valptr;\n");
    fprintf(code_file,"//###############################################################\n");
    fprintf(code_file,"// methods: value stack push,pop,drop,peek.\n");
    fprintf(code_file,"//###############################################################\n");
    fprintf(code_file,"final void val_init()\n");
    fprintf(code_file,"{\n");
    if (prim)
      {
      fprintf(code_file,"  yyval=(%s)0;\n",jsemantic_type);
      fprintf(code_file,"  yylval=(%s)0;\n",jsemantic_type);
      }
    else
      {
      fprintf(code_file,"  yyval=new %s();\n",jsemantic_type);  /*fix 980108*/
      fprintf(code_file,"  yylval=new %s();\n",jsemantic_type); /* ditto */
      }
    fprintf(code_file,"  valptr=-1;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final void val_push(%s val)\n",jsemantic_type);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  try {\n");
    fprintf(code_file,"    valptr++;\n");
	fprintf(code_file,"    valstk[valptr]=val;\n");
	fprintf(code_file,"  }\n");
	fprintf(code_file,"  catch (ArrayIndexOutOfBoundsException e) {\n");
	fprintf(code_file,"    int oldsize = valstk.length;\n");
	fprintf(code_file,"    int newsize = oldsize*2;\n");
	fprintf(code_file,"    %s[] newstack = new %s[newsize];\n",jsemantic_type,raw_type);
	fprintf(code_file,"    System.arraycopy(valstk,0,newstack,0,oldsize);\n");
	fprintf(code_file,"    valstk = newstack;\n");
	fprintf(code_file,"    valstk[valptr]=val;\n");
	fprintf(code_file,"  }\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final %s val_pop()\n",jsemantic_type);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  return valstk[valptr--];\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final void val_drop(int cnt)\n");
    fprintf(code_file,"{\n");
	fprintf(code_file,"  valptr -= cnt;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final %s val_peek(int relative)\n",jsemantic_type);
    fprintf(code_file,"{\n");
	fprintf(code_file,"  return valstk[valptr-relative];\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final %s dup_yyval(%s val)\n",jsemantic_type,jsemantic_type);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  return val;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"//#### end semantic value section ####\n");
      }

    else /*no definition -- use our semantic class*/
      {
    fprintf(code_file,"\n\n//########## SEMANTIC VALUES ##########\n");

	fprintf(code_file,"//public class %s is defined in %s.java\n\n\n",
                    jvalclass,jvalclass);

	sprintf(filenam,"%s.java",jvalclass);
	f=fopen(filenam,"w");
	if (!f)
	  return;
	fprintf(f,"//#############################################\n");
	fprintf(f,"//## file: %s.java\n",jclass_name);
	fprintf(f,"//## Generated by Byacc/j\n");
	fprintf(f,"//#############################################\n");
	if (jpackage_name && strlen(jpackage_name)>0)
	  fprintf(f,"package %s;\n\n",jpackage_name);
	fprintf(f,"/**\n");
	fprintf(f," * BYACC/J Semantic Value for parser: %s\n",jclass_name);
	fprintf(f," * This class provides some of the functionality\n");
	fprintf(f," * of the yacc/C 'union' directive\n");
	fprintf(f," */\n");

	/* yio 20020304: option to make the class final */
	if (jfinal_class == TRUE)
		fprintf(f,"final ");

	fprintf(f,"public class %s\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"/**\n");
	fprintf(f," * integer value of this 'union'\n");
	fprintf(f," */\n");
	fprintf(f,"public int ival;\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * double value of this 'union'\n");
	fprintf(f," */\n");
	fprintf(f,"public double dval;\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * string value of this 'union'\n");
	fprintf(f," */\n");
	fprintf(f,"public String sval;\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * object value of this 'union'\n");
	fprintf(f," */\n");
	fprintf(f,"public Object obj;\n");
	fprintf(f,"\n");
	fprintf(f,"//#############################################\n");
	fprintf(f,"//## C O N S T R U C T O R S\n");
	fprintf(f,"//#############################################\n");
	fprintf(f,"/**\n");
	fprintf(f," * Initialize me without a value\n");
	fprintf(f," */\n");
	fprintf(f,"public %s()\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"}\n");
	fprintf(f,"/**\n");
	fprintf(f," * Initialize me as an int\n");
	fprintf(f," */\n");
	fprintf(f,"public %s(int val)\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"  ival=val;\n");
	fprintf(f,"}\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * Initialize me as a double\n");
	fprintf(f," */\n");
	fprintf(f,"public %s(double val)\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"  dval=val;\n");
	fprintf(f,"}\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * Initialize me as a string\n");
	fprintf(f," */\n");
	fprintf(f,"public %s(String val)\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"  sval=val;\n");
	fprintf(f,"}\n");
	fprintf(f,"\n");
	fprintf(f,"/**\n");
	fprintf(f," * Initialize me as an Object\n");
	fprintf(f," */\n");
	fprintf(f,"public %s(Object val)\n",jvalclass);
	fprintf(f,"{\n");
	fprintf(f,"  obj=val;\n");
	fprintf(f,"}\n");
	fprintf(f,"}//end class\n\n");
	fprintf(f,"//#############################################\n");
	fprintf(f,"//## E N D    O F    F I L E\n");
	fprintf(f,"//#############################################\n");
	fclose(f);
	

    fprintf(code_file,"String   yytext;//user variable to return contextual strings\n");
    fprintf(code_file,"%s yyval; //used to return semantic vals from action routines\n",jvalclass);
    fprintf(code_file,"%s yylval;//the 'lval' (result) I got from yylex()\n",jvalclass);
    fprintf(code_file,"%s valstk[];\n",jvalclass);
    fprintf(code_file,"int valptr;\n");
    fprintf(code_file,"//###############################################################\n");
    fprintf(code_file,"// methods: value stack push,pop,drop,peek.\n");
    fprintf(code_file,"//###############################################################\n");
    fprintf(code_file,"void val_init()\n");
    fprintf(code_file,"{\n");
    fprintf(code_file,"  valstk=new %s[YYSTACKSIZE];\n",jvalclass);
    fprintf(code_file,"  yyval=new %s();\n",jvalclass);
    fprintf(code_file,"  yylval=new %s();\n",jvalclass);
    fprintf(code_file,"  valptr=-1;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"void val_push(%s val)\n",jvalclass);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  if (valptr>=YYSTACKSIZE)\n");
    fprintf(code_file,"    return;\n");
    fprintf(code_file,"  valstk[++valptr]=val;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"%s val_pop()\n",jvalclass);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  if (valptr<0)\n");
    fprintf(code_file,"    return new %s();\n",jvalclass);
    fprintf(code_file,"  return valstk[valptr--];\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"void val_drop(int cnt)\n");
    fprintf(code_file,"{\n");
    fprintf(code_file,"int ptr;\n");
    fprintf(code_file,"  ptr=valptr-cnt;\n");
    fprintf(code_file,"  if (ptr<0)\n");
    fprintf(code_file,"    return;\n");
    fprintf(code_file,"  valptr = ptr;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"%s val_peek(int relative)\n",jvalclass);
    fprintf(code_file,"{\n");
    fprintf(code_file,"int ptr;\n");
    fprintf(code_file,"  ptr=valptr-relative;\n");
    fprintf(code_file,"  if (ptr<0)\n");
    fprintf(code_file,"    return new %s();\n",jvalclass);
    fprintf(code_file,"  return valstk[ptr];\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"final %s dup_yyval(%s val)\n",jvalclass,jvalclass);
    fprintf(code_file,"{\n");
    fprintf(code_file,"  %s dup = new %s();\n",jvalclass,jvalclass);
    fprintf(code_file,"  dup.ival = val.ival;\n");
    fprintf(code_file,"  dup.dval = val.dval;\n");
    fprintf(code_file,"  dup.sval = val.sval;\n");
    fprintf(code_file,"  dup.obj = val.obj;\n");
    fprintf(code_file,"  return dup;\n");
    fprintf(code_file,"}\n");
    fprintf(code_file,"//#### end semantic value section ####\n");
      }
    }
  else  /*normal stuff  -- rwj*/
    {
      if (!unionized && ntags == 0)
      {
	outline += 3;
	fprintf(code_file, "#ifndef YYSTYPE\ntypedef int YYSTYPE;\n#endif\n");
      }
    }
}


void output_trailing_text(void)
{
int c, last;
FILE *in, *out;

    if (line == 0)
	return;

    in = input_file;
    out = code_file;
    c = *cptr;
    if (c == '\n')
    {
	++lineno;
	if ((c = getc(in)) == EOF)
	    return;
	if (!lflag)
	{
	    ++outline;
	    if (jflag)
	      fprintf(out, jline_format, lineno, input_file_name);
	    else
	      fprintf(out, line_format, lineno, input_file_name);
	}
	if (c == '\n')
	    ++outline;
	putc(c, out);
	last = c;
    }
    else
    {
	if (!lflag)
	{
	    ++outline;
	    if (jflag)/*rwj*/
	      fprintf(out, jline_format, lineno, input_file_name);
	    else
	      fprintf(out, line_format, lineno, input_file_name);
	}
	do { putc(c, out); } while ((c = *++cptr) != '\n');
	++outline;
	putc('\n', out);
	last = '\n';
    }

    while ((c = getc(in)) != EOF)
    {
	if (c == '\n')
	    ++outline;
	putc(c, out);
	last = c;
    }

    if (last != '\n')
    {
	++outline;
	putc('\n', out);
    }
    if (!lflag)
        if (jflag)
	  fprintf(out, jline_format, ++outline + 1, code_file_name);
	else
	  fprintf(out, line_format, ++outline + 1, code_file_name);
}


void output_semantic_actions(void)
{
int c, last;
FILE *out;

    fclose(action_file);
    action_file = fopen(action_file_name, "r");
    if (action_file == NULL)
	open_error(action_file_name);

    if ((c = getc(action_file)) == EOF)
	return;

    out = code_file;
    last = c;
    if (c == '\n')
	++outline;
    putc(c, out);
    while ((c = getc(action_file)) != EOF)
    {
	if (c == '\n')
	    ++outline;
	putc(c, out);
	last = c;
    }

    if (last != '\n')
    {
	++outline;
	putc('\n', out);
    }

    if (!lflag)
        if (jflag)/*rwj*/
	  fprintf(out, jline_format, ++outline + 1, code_file_name);
	else
	  fprintf(out, line_format, ++outline + 1, code_file_name);
}

void free_itemsets(void)
{
core *cp, *next;

  FREE(state_table);
  for (cp = first_state; cp; cp = next)
    {
	 next = cp->next;
	 FREE(cp);
    }
}


void free_shifts(void)
{
shifts *sp, *next;

  FREE(shift_table);
  for (sp = first_shift; sp; sp = next)
    {
	 next = sp->next;
	 FREE(sp);
    }
}



void free_reductions(void)
{
reductions *rp, *next;

  FREE(reduction_table);
  for (rp = first_reduction; rp; rp = next)
    {
	 next = rp->next;
	 FREE(rp);
    }
}
