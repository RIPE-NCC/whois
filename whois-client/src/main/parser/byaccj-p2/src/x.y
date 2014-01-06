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

/****
  put your Java field declarations here
****/


%}

%token FLOAT

%token VERSION LB RB QSTR DESCRIPTION TITLE DESC ABSTRACT CODE
%token LESSON EXERCISE FILE_W DIR FILENAME

%token CONFIG KEY STR INT PROGRAM
%token ICAT DATA SOUNDS HTML_TOOL AUTHOR_TOOL MODEL_TOOL

%%

crsfile: VERSION { /*VSTR(verbuf);*/ } coursedef
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
		title { /*VQSTR(titlebuf);*/ }
		desc { /*VQSTR(descbuf);*/ }
		code
		abstract { /*VASTR(abstract);*/ }
		RB
         {
         /*cs = crs_add_course(NULL,verbuf,titlebuf,descbuf,codebuf,abstract);
         par=cs;*/
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
        /*VQSTR(cf->icat_data);*/
        }
      |
      DATA QSTR
        {
        /*VQSTR(cf->icat_data);*/
        }
      ;

authorlist: /*Null*/
      |
      authorlist authoritem
      ;
authoritem:
      PROGRAM FILENAME
        {
        /*VQSTR(cf->author_tool);*/
        }
      |
      PROGRAM QSTR
        {
        /*VQSTR(cf->author_tool);*/
        }
      |
      FILE_W FILENAME
        {
        /*VQSTR(cf->author_file);*/
        }
      |
      FILE_W QSTR
        {
        /*VQSTR(cf->author_file);*/
        }
      ;
      
htmllist: /*Null*/
      |
      htmlitem htmllist
      ;
htmlitem: 
      PROGRAM FILENAME
        {
        /*VQSTR(cf->html_tool);*/
        }
      |
      PROGRAM QSTR
        {
        /*VQSTR(cf->html_tool);*/
        }
      |
      FILE_W FILENAME
        {
        /*VQSTR(cf->html_file);*/
        }
      |
      FILE_W QSTR
        {
        /*VQSTR(cf->html_file);*/
        }
      ;
modellist: /*Null*/
      |
      modelitem modellist
      ;
modelitem: 
      PROGRAM FILENAME
        {
        /*VQSTR(cf->model_tool);*/
        }
      |
      PROGRAM QSTR
        {
        /*VQSTR(cf->model_tool);*/
        }
      |
      FILE_W FILENAME
        {
        /*VQSTR(cf->model_file);*/
        }
      |
      FILE_W QSTR
        {
        /*VQSTR(cf->model_file);*/
        }
      ;
sounditem: 
      DIR FILENAME
        {
        /*VQSTR(cf->sound_dir);*/
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
		title      { }
		desc       {  }
		code       
		abstract
		{ }
		lessonlist
		exerlist
	        {
		 
		}
		RB
         {
         }
         ;

exercise: EXERCISE LB
		title     { }
		desc      {}
		code      {  }
		abstract  { }
		filename  {  }
		RB
         {
         }
         ;

/*Not recursive.  Zero or one*/
code: /*Nil*/
      {
      }
      |
      CODE QSTR
         {
         }
         ;

filename: FILE_W QSTR
         {
         }
         ;
   



%%

/****
  put your Java methods here
****/
int yylex()
{
  return 1;
}

/**** end of file ********************************************************/
