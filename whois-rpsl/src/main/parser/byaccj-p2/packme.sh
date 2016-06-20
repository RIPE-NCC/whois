#!/bin/sh
VER=byaccj1.15
/bin/rm -rf ${VER}
mkdir ${VER} 
mkdir ${VER}/src
cp src/*.c ${VER}/src
cp src/*.h ${VER}/src
cp src/Makefile ${VER}/src
cp src/Makefile.bcc ${VER}/src
cp src/README ${VER}
cp src/yacc.exe ${VER}
cp src/yacc.irix ${VER}
cp src/yacc.linux ${VER}
cp src/yacc.solaris ${VER}
chmod -R 777 ${VER}
tar cvf - ${VER} | gzip > ${VER}.tar.gz
jar cMvf ${VER}.zip ${VER}
chmod 777 ${VER}.tar.gz ${VER}.zip
 
