#!/bin/bash

#
# Run jams-worldwind
#
# run-demo.bash
#

echo Running $1
java -Xmx1g -Dsun.java2d.noddraw=true -jar jams-worldwind.jar jams.worldwind.Starter
