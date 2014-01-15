%{  /* Do not delete this line! */
/**************************************************************************
** file: course.y
**
** does: Profides course file ".crs" file parsing for ICAT
** 
** date: 950715
**
** author: R. Jamison
**
***************************************************************************
** notes:
**
**
***************************************************************************
** history:
**
** date   engr              remarks
** ------ ----------------- -----------------------------
** ...... .                 .
**
**************************************************************************/
#include <stdio.h>                            /*Standard C i/o           */
#include <string.h>
#include <stdlib.h>                           /*Added C functions        */
#include "course.h"                           /*Our include              */
#include "config.h"

#define TRUE  1
#define FALSE 0

course *par,*lsn,*exer,*sav;
course *cs;

config *cf;

char verbuf[CRSSTRLEN+1];
char titlebuf[CRSSTRLEN+1];
char descbuf[CRSSTRLEN+1];
char codebuf[CRSSTRLEN+1];
char *abstract;
char filebuf[CRSSTRLEN+1];

extern char yytext[];
extern long crs_line;
extern char *crs_text;

#define MAX_CSTACK 20
course *c_stack[MAX_CSTACK];
int c_depth;
int c_push(course *c)
{
  if (c_depth>=MAX_CSTACK)
    return 0;
  c_stack[c_depth++]=c;
  return 1;
}

course *c_pop(void)
{
  if (c_depth<=0)
    return NULL;
  return c_stack[--c_depth];
}

#define VCPY(a,b) strncpy(a,b,CRSSTRLEN)
#define VSTR(a)   strncpy(a,yytext,CRSSTRLEN)
#define VASTR(a)  if (a) free(a); a=strdup(crs_text)
#define VQSTR(a)  strncpy(a,crs_text,CRSSTRLEN)
%}

%token FLOAT

%token VERSION LB RB QSTR DESCRIPTION TITLE DESC ABSTRACT CODE
%token LESSON EXERCISE FILE_W DIR FILENAME

%token CONFIG KEY STR INT PROGRAM
%token ICAT DATA SOUNDS HTML_TOOL AUTHOR_TOOL MODEL_TOOL

%%

crsfile: VERSION { VSTR(verbuf); } coursedef
         {
         }
         ;

coursedef: description lessonlist exerlist
         {
         }
         |
         description config lessonlist exerlist
         {
         }
         ;

/*+++DESCRIPTION+++*/
description : DESCRIPTION LB
		title { VQSTR(titlebuf); }
		desc { VQSTR(descbuf); }
		code
		abstract { VASTR(abstract); }
		RB
         {
         cs = crs_add_course(NULL,verbuf,titlebuf,descbuf,codebuf,abstract);
         par=cs;
         /*
         printf(":%s:%s:%s:%s:%s:\n",verbuf,titlebuf,descbuf,codebuf,abstract);
         */
         }
         ;

title:   TITLE QSTR
         {
         }
         ;

desc:    DESC QSTR
         {
         }
         ;

abstract: ABSTRACT QSTR
         {
          }
         ;

/*+++CONFIG+++*/
config: CONFIG LB cfglist RB
        {
        }
        ;

cfglist: /*Nil*/
        |
        cfgitem cfglist
        {
        }
        ;

cfgitem:  ICAT LB icatlist RB
      |
      AUTHOR_TOOL LB authorlist RB
      |
      SOUNDS LB sounditem RB
      |
      HTML_TOOL LB htmllist RB
      |
      MODEL_TOOL LB modellist RB
      ;
      
   
icatlist: /*Null*/
      |
      icatlist icatitem
      ;
icatitem:
      DATA FILENAME
        {
        VQSTR(cf->icat_data);
        }
      |
      DATA QSTR
        {
        VQSTR(cf->icat_data);
        }
      ;

authorlist: /*Null*/
      |
      authorlist authoritem
      ;
authoritem:
      PROGRAM FILENAME
        {
        VQSTR(cf->author_tool);
        }
      |
      PROGRAM QSTR
        {
        VQSTR(cf->author_tool);
        }
      |
      FILE_W FILENAME
        {
        VQSTR(cf->author_file);
        }
      |
      FILE_W QSTR
        {
        VQSTR(cf->author_file);
        }
      ;
      
htmllist: /*Null*/
      |
      htmlitem htmllist
      ;
htmlitem: 
      PROGRAM FILENAME
        {
        VQSTR(cf->html_tool);
        }
      |
      PROGRAM QSTR
        {
        VQSTR(cf->html_tool);
        }
      |
      FILE_W FILENAME
        {
        VQSTR(cf->html_file);
        }
      |
      FILE_W QSTR
        {
        VQSTR(cf->html_file);
        }
      ;
modellist: /*Null*/
      |
      modelitem modellist
      ;
modelitem: 
      PROGRAM FILENAME
        {
        VQSTR(cf->model_tool);
        }
      |
      PROGRAM QSTR
        {
        VQSTR(cf->model_tool);
        }
      |
      FILE_W FILENAME
        {
        VQSTR(cf->model_file);
        }
      |
      FILE_W QSTR
        {
        VQSTR(cf->model_file);
        }
      ;
sounditem: 
      DIR FILENAME
        {
        VQSTR(cf->sound_dir);
        }

/*+++LESSONS+++*/
/*Following recursive definition means 'zero to many'*/
lessonlist: /*nil*/
            |
            lesson lessonlist
         {
         }
         ;

exerlist: /*nil*/
            |
            exercise exerlist
         {
         }
         ;

lesson: LESSON LB
		title      { VQSTR(titlebuf); }
		desc       { VQSTR(descbuf); }
		code       
		abstract
		{ VASTR(abstract);
		  /*printf("lesson\n");*/
                  lsn = crs_add_lesson(par,titlebuf,
                          descbuf,codebuf,abstract);
                  c_push(par);
		  par=lsn;
		/*allow a recurse*/}
		lessonlist
		exerlist
	        {
		  par=c_pop();
		}
		RB
         {
         }
         ;

exercise: EXERCISE LB
		title     { VQSTR(titlebuf); }
		desc      { VQSTR(descbuf); }
		code      { VQSTR(codebuf); }
		abstract  { VASTR(abstract); }
		filename  { VQSTR(filebuf); }
		RB
         {
                /*printf("exercise\n");*/
                exer = crs_add_exercise(par,titlebuf,
                          descbuf,codebuf,abstract,filebuf);
         }
         ;

/*Not recursive.  Zero or one*/
code: /*Nil*/
      {
      codebuf[0]=0;
      }
      |
      CODE QSTR
         {
         VQSTR(codebuf);
         }
         ;

filename: FILE_W QSTR
         {
         }
         ;
   



%%
/**************************************************************************
* function: yyerror
* does    : Reports a YACC error
* inputs  : String to report error
* outputs : TRUE if successful, else FALSE
**************************************************************************/
int yyerror(char *str)                            /**/
{
  printf("YACC:%ld:%s\n",crs_line,str);
  return FALSE;                               /*Return success           */
}
/*** end of func *********************************************************/

/**************************************************************************
* function: crs_parse
* does    : Parses a file
* inputs  : File pointer
* outputs : course * if successful, else NULL
**************************************************************************/
course *crs_parse(FILE *f,config *cfg)                  /*//*/
{
  cs=NULL;
  if (!f)                                     /*Bad file?(Just in case)  */
    {
    return FALSE;
    }   
  crsin(f);
  cf=cfg;
  c_depth=0;
  abstract=0;
  if (yyparse()!=0)
    return NULL;
  return cs;                                  /*Return success           */
}
/*** end of func *********************************************************/

/**************************************************************************
* function: crs_parsefile
* does    : Parses a named file
* inputs  : File name pointer
* outputs : course * if successful, else NULL
**************************************************************************/
course *crs_parsefile(char *fname,config *cfg)                  /*//*/
{
FILE *f;
course *ret;
  f=fopen(fname,"r");                         /*Open the file?           */
  if (!f)
    {
    /*printf("Could not open %s for reading\n",fname);*/
    return FALSE;
    }
  ret=crs_parse(f,cfg);
  fclose(f);                                  /*Close the file           */
  return ret;                                 /*Return success           */
}
/*** end of func *********************************************************/

/**************************************************************************
* function: crs_load
* does    : Loads the course description file
* inputs  : None
* outputs : course * if successful, else NULL
**************************************************************************/
course *crs_load(config *cfg)                               /*//*/
{
char path[256];
char *e;
course *ret;
  if (!cfg_defaults(cfg))
    return NULL;
  e=getenv(CRS_PATH1);
  if (e)
    {
    sprintf(path,"%s%s%s",e,CRS_SEP,CRSNAME);
    ret=crs_parsefile(path,cfg);
    if (ret)
      return ret;
    }
  e=getenv(CRS_PATH2);
  if (e)
    {
    sprintf(path,"%s%s%s",e,CRS_SEP,CRSNAME);
    ret=crs_parsefile(path,cfg);
    if (ret)
      return ret;
    }
  sprintf(path,".%s%s",CRS_SEP,CRSNAME);
  ret=crs_parsefile(path,cfg);
  return ret;                                 /*Return failure           */
}
/*** end of func *********************************************************/

#ifdef TEST
/**************************************************************************
* function: main
* does    :
* inputs  :
* outputs :
**************************************************************************/
int main(int argc,char **argv)                    /**/
{
FILE *f;
int ret;
config cfg;
  if (argc!=2)
    {
    printf("usage:%s filename\n",argv[0]);
    return FALSE;
    }
  if (!crs_parsefile(argv[1],&cfg))                         
    {
    printf("Parse of file failed\n");
    return FALSE;
    }
  return TRUE;                                /*Return success           */
}
/*** end of func *********************************************************/

#endif /*TEST*/

/**** end of file ********************************************************/
