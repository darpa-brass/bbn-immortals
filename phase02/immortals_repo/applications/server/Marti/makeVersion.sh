#!/bin/sh

VER=`svnversion`
DATE=`date -u`

echo "Build Number: 1.2.2.$VER Build Date: $DATE " > Version.txt
