#include <stdlib.h>
#include <assert.h>
#include <ctype.h>
#include <stdio.h>

/*  machine-dependent definitions			*/
/*  the following definitions are for the Tahoe		*/
/*  they might have to be changed for other machines	*/

/*  MAXCHAR is the largest unsigned character value	*/
/*  MAXSHORT is the largest value of a C short		*/
/*  MINSHORT is the most negative value of a C short	*/
/*  MAXTABLE is the maximum table size			*/
/*  BITS_PER_WORD is the number of bits in a C unsigned	*/
/*  WORDSIZE computes the number of words needed to	*/
/*	store n bits					*/
/*  BIT returns the value of the n-th bit starting	*/
/*	from r (0-indexed)				*/
/*  SETBIT sets the n-th bit starting from r		*/

#define	MAXCHAR		255
#define	MAXSHORT	32767
#define MINSHORT	-32768
#define MAXTABLE	32500
#define BITS_PER_WORD	32
#define	WORDSIZE(n)	(((n)+(BITS_PER_WORD-1))/BITS_PER_WORD)
#define	BIT(r, n)	((((r)[(n)>>5])>>((n)&31))&1)
#define	SETBIT(r, n)	((r)[(n)>>5]|=((unsigned)1<<((n)&31)))


/* yio 20020304: for boolean parameters */
#ifndef TRUE
  #define TRUE 1
  #define FALSE 0
#endif




/*  character names  */

#define	NUL		'\0'    /*  the null character  */
#define	NEWLINE		'\n'    /*  line feed  */
#define	SP		' '     /*  space  */
#define	BS		'\b'    /*  backspace  */
#define	HT		'\t'    /*  horizontal tab  */
#define	VT		'\013'  /*  vertical tab  */
#define	CR		'\r'    /*  carriage return  */
#define	FF		'\f'    /*  form feed  */
#define	QUOTE		'\''    /*  single quote  */
#define	DOUBLE_QUOTE	'\"'    /*  double quote  */
#define	BACKSLASH	'\\'    /*  backslash  */


/* defines for constructing filenames */

#define CODE_SUFFIX	".code.c"
#define	DEFINES_SUFFIX	".tab.h"
#define	OUTPUT_SUFFIX	".tab.c"
#define	JAVA_OUTPUT_SUFFIX	".java"  /*rwj*/
#define	JAVA_INTERFACE_SUFFIX	"Tokens"  /*rwj*/
#define	VERBOSE_SUFFIX	".output"


/* keyword codes */

#define TOKEN 0
#define LEFT 1
#define RIGHT 2
#define NONASSOC 3
#define MARK 4
#define TEXT 5
#define TYPE 6
#define START 7
#define UNION 8
#define IDENT 9
#define JAVA_ARG 10 /* yio 20020304: added for specifying java arguments in grammar */


/*  symbol classes  */

#define UNKNOWN 0
#define TERM 1
#define NONTERM 2


/*  the undefined value  */

#define UNDEFINED (-1)


/*  action codes  */

#define SHIFT 1
#define REDUCE 2


/*  character macros  */

#define IS_IDENT(c)	(isalnum(c) || (c) == '_' || (c) == '.' || (c) == '$')
#define	IS_OCTAL(c)	((c) >= '0' && (c) <= '7')
#define	NUMERIC_VALUE(c)	((c) - '0')


/*  symbol macros  */

#define ISTOKEN(s)	((s) < start_symbol)
#define ISVAR(s)	((s) >= start_symbol)


/*  storage allocation macros  */

#define CALLOC(k,n)	(calloc((unsigned)(k),(unsigned)(n)))
#define	FREE(x)		(free((char*)(x)))
#define MALLOC(n)	(malloc((unsigned)(n)))
#define	NEW(t)		((t*)allocate(sizeof(t)))
#define	NEW2(n,t)	((t*)allocate((unsigned)((n)*sizeof(t))))
#define REALLOC(p,n)	(realloc((char*)(p),(unsigned)(n)))


/*  the structure of a symbol table entry  */

typedef struct bucket bucket;
struct bucket
{
    struct bucket *link;
    struct bucket *next;
    char *name;
    char *tag;
    short value;
    short index;
    short prec;
    char class;
    char assoc;
};


/*  the structure of the LR(0) state machine  */

typedef struct core core;
struct core
{
    struct core *next;
    struct core *link;
    short number;
    short accessing_symbol;
    short nitems;
    short items[1];
};


/*  the structure used to record shifts  */

typedef struct shifts shifts;
struct shifts
{
    struct shifts *next;
    short number;
    short nshifts;
    short shift[1];
};


/*  the structure used to store reductions  */

typedef struct reductions reductions;
struct reductions
{
    struct reductions *next;
    short number;
    short nreds;
    short rules[1];
};


/*  the structure used to represent parser actions  */

typedef struct action action;
struct action
{
    struct action *next;
    short symbol;
    short number;
    short prec;
    char action_code;
    char assoc;
    char suppressed;
};


/* global variables */

extern char dflag;
extern char lflag;
extern char rflag;
extern char tflag;
extern char vflag;

extern char *myname;
extern char *cptr;
extern char *line;
extern int lineno;
extern int outline;

extern char *banner[];
extern char *tables[];
extern char *header[];
extern char *body[];
extern char *trailer[];

/*#### Java Things ####*/
extern char jflag;
extern char *jbanner[];
extern char *jtables[];
extern char *jheader[];
extern char *jbody_a[]; /* yio 20020304: split to allow 'jthrows' parameter */
extern char *jbody_b[];
extern char *jtrailer[];
extern char jrun;
extern char jconstruct;

extern char *jclass_name;
extern char *jpackage_name;
extern char *jextend_name;
extern char *jimplement_name;
extern char *jsemantic_type;
extern int  jstack_size;

/* yio 20020304 */
extern char *jbody_nodebug_a[];
extern char *jbody_nodebug_b[];
extern char *jtrailer_nodebug[];
extern char *jyyparse_throws;
extern int  jdebug;
extern int  jfinal_class;

extern char *action_file_name;
extern char *code_file_name;
extern char *defines_file_name;
extern char *input_file_name;
extern char *output_file_name;
extern char *text_file_name;
extern char *union_file_name;
extern char *verbose_file_name;

extern FILE *action_file;
extern FILE *code_file;
extern FILE *defines_file;
extern FILE *input_file;
extern FILE *output_file;
extern FILE *text_file;
extern FILE *union_file;
extern FILE *verbose_file;

extern int nitems;
extern int nrules;
extern int nsyms;
extern int ntokens;
extern int nvars;
extern int ntags;

extern char unionized;
extern char line_format[];
extern char jline_format[];/*rwj*/

extern int   start_symbol;
extern char  **symbol_name;
extern short *symbol_value;
extern short *symbol_prec;
extern char  *symbol_assoc;

extern short *ritem;
extern short *rlhs;
extern short *rrhs;
extern short *rprec;
extern char  *rassoc;

extern short **derives;
extern char *nullable;

extern bucket *first_symbol;
extern bucket *last_symbol;

extern int nstates;
extern core *first_state;
extern shifts *first_shift;
extern reductions *first_reduction;
extern short *accessing_symbol;
extern core **state_table;
extern shifts **shift_table;
extern reductions **reduction_table;
extern unsigned *LA;
extern short *LAruleno;
extern short *lookaheads;
extern short *goto_map;
extern short *from_state;
extern short *to_state;

extern action **parser;
extern int SRtotal;
extern int RRtotal;
extern short *SRconflicts;
extern short *RRconflicts;
extern short *defred;
extern short *rules_used;
extern short nunused;
extern short final_state;


/* system variables */

extern int errno;


/************************
## PROTOTYPES
************************/
/*in warshall.c*/
void transitive_closure(unsigned *R, int n);
void reflexive_transitive_closure(unsigned *R, int n);
/*in verbose.c*/
void verbose(void);
void log_unused(void);
void log_conflicts(void);
void print_state(int state);
void print_conflicts(int state);
void print_core(int state);
void print_nulls(int state);
void print_actions(int stateno);
void print_shifts(action *p);
void print_reductions(action *p,int defred);
void print_gotos(int stateno);
/*in closure.c*/
void set_EFF(void);
void set_first_derives(void);
void closure(short *nucleus,int n);
void finalize_closure(void);
void print_closure(int n);
void print_EFF(void);
void print_first_derives(void);
/*in error.c*/
void fatal(char *msg);
void no_space(void);
void open_error(char *filename);
void unexpected_EOF(void);
void print_pos(char *st_line,char *st_cptr);
void syntax_error(int st_lineno,char *st_line,char *st_cptr);
void unterminated_comment(int c_lineno,char *c_line,char *c_cptr);
void unterminated_string(int s_lineno,char *s_line,char *s_cptr);
void unterminated_text(int t_lineno,char *t_line,char *t_cptr);
void unterminated_union(int u_lineno,char *u_line,char *u_cptr);
void over_unionized(char *u_cptr);
void illegal_tag(int t_lineno,char *t_line,char *t_cptr);
void illegal_character(char *c_cptr);
void used_reserved(char *s);
void tokenized_start(char *s);
void retyped_warning(char *s);
void reprec_warning(char *s);
void revalued_warning(char *s);
void terminal_start(char *s);
void restarted_warning(void);
void no_grammar(void);
void terminal_lhs(int s_lineno);
void prec_redeclared(void);
void unterminated_action(int a_lineno,char *a_line,char *a_cptr);
void dollar_warning(int a_lineno,int i);
void dollar_error(int a_lineno,char *a_line,char *a_cptr);
void untyped_lhs(void);
void untyped_rhs(int i,char *s);
void unknown_rhs(int i);
void default_action_warning(void);
void undefined_goal(char *s);
void undefined_symbol_warning(char *s);
/*in skeleton.c*/
void write_section(char *section[]);
/*in symtab.c*/
int hash(char *name);
bucket *make_bucket(char *name);
bucket *lookup(char *name);
void create_symbol_table(void);
void free_symbol_table(void);
void free_symbols(void);
/*in reader.c*/
void cachec(int c);
void get_line(void);
char *dup_line(void);
void skip_comment(void);
int nextc(void);
int keyword(void);
void copy_ident(void);
void copy_text(void);
void copy_union(void);
int hexval(int c);
bucket *get_literal(void);
int is_reserved(char *name);
bucket *get_name(void);
int get_number(void);
char *get_tag(void);
void declare_tokens(int assoc);
void declare_types(void);
void declare_start(void);
void read_declarations(void);
void initialize_grammar(void);
void expand_items(void);
void expand_rules(void);
void advance_to_start(void);
void start_rule(bucket *bp,int s_lineno);
void end_rule(void);
void insert_empty_rule(void);
void add_symbol(void);
void copy_action(void);
int mark_symbol(void);
void read_grammar(void);
void free_tags(void);
void pack_names(void);
void check_symbols(void);
void pack_symbols(void);
void pack_grammar(void);
void print_grammar(void);
void reader(void);
/*in output.c*/
void output(void);
void output_rule_data(void) ;
void output_yydefred(void);
void output_actions(void);
void token_actions(void);
void goto_actions(void);
int default_goto(int symbol);
void save_column(int symbol,int default_state);
void sort_actions(void);
void pack_table(void);
int matching_vector(int vector);
int pack_vector(int vector);
void output_base(void);
void output_table(void);
void output_check(void);
int is_C_identifier(char *name);
void output_defines(void);
void output_stored_text(void);
void output_debug(void);
void output_stype(void);
void output_trailing_text(void);
void output_semantic_actions(void);
void free_itemsets(void);
void free_shifts(void);
void free_reductions(void);
/*in mkpar.c*/
void make_parser(void);
action *parse_actions(int stateno);
action *get_shifts(int stateno);
action *add_reductions(int stateno,action *actions);
action *add_reduce(action *actions,int ruleno,int symbol);
void find_final_state(void);
void unused_rules(void);
void remove_conflicts(void);
void total_conflicts(void);
int sole_reduction(int stateno);
void defreds(void);
void free_action_row(action *p);
void free_parser(void);
/*in lr0.c*/
void allocate_itemsets(void);
void allocate_storage(void);
void append_states(void);
void free_storage(void);
void generate_states(void);
int get_state(int symbol);
void initialize_states(void);
void new_itemsets(void);
core *new_state(int symbol);
void show_cores(void);
void show_ritems(void);
void show_rrhs(void);
void show_shifts(void);
void save_shifts(void);
void save_reductions(void);
void set_derives(void);
void free_derives(void);
void print_derives(void);
void set_nullable(void);
void free_nullable(void);
void lr0(void);
/*in lalr.c*/
void lalr(void);
void set_state_table(void);
void set_accessing_symbol(void);
void set_shift_table(void);
void set_reduction_table(void);
void set_maxrhs(void);
void initialize_LA(void);
void set_goto_map(void);
int map_goto(int state,int symbol);
void initialize_F(void);
void build_relations(void);
void add_lookback_edge(int stateno,int ruleno,int gotono);
short **transpose(short **R,int n);
void compute_FOLLOWS(void);
void compute_lookaheads(void);
void digraph(short **relation);
void traverse(int i);
/*in main.c*/
void done(int k);
void onintr(int);
void set_signals(void);
void usage(void);
void getargs(int argc,char **argv);
char *allocate(unsigned n);
void create_file_names(void);
void open_files(void);
void getJavaArg(char *option); /* yio 20020304 */
int main(int argc,char **argv);





