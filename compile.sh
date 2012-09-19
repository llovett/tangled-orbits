#!/bin/sh

# This script assumes you are running a mac!

TO_ROOT=`pwd`
GLDIR=$TO_ROOT/macgl:$TO_ROOT/opengl.jar:$TO_ROOT/opengl/com:$TO_ROOT/opengl/gluegen-rt.jar:$TO_ROOT/opengl/javax::$TO_ROOT/opengl/jogl.jar:$TO_ROOT/opengl/opengl.jar
CP=$TO_ROOT:$TO_ROOT/core.jar:$TO_ROOT/jsyn.jar:$TO_ROOT/traer:$TO_ROOT/oscP5.jar:$GL_DIR

for dir in {display,event}; do
    cd $dir
    rm -f *.class
    # If making clean, don't compile java
    [ "x$1" = "xclean" ] && { cd .. ; continue ; }
    javac -cp $CP *.java
    cd ..
done
