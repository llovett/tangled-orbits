#!/bin/sh

# This script assumes you are running a mac!

TO_ROOT=`pwd`
GLDIR=$TO_ROOT/macgl:$TO_ROOT/opengl/gluegen-rt.jar:$TO_ROOT/opengl/jogl.jar:$TO_ROOT/opengl/opengl.jar:$TO_ROOT/processing/opengl/javax:$TO_ROOT/processing/opengl:$TO_ROOT/processing/opengl/com
CP=$TO_ROOT:$TO_ROOT/core.jar:$TO_ROOT/jsyn.jar:$TO_ROOT/oscP5.jar:$TO_ROOT/traer:$GLDIR

echo $GLDIR

java -Xmx2g -cp "$CP" -Djava.library.path="$GLDIR" display.HMusicWrapper
