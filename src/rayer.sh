#!/bin/bash

# cleanup
if [ "$1" == "clean" ]; then
	rm *.class;
	rm rayer/*.class;
	rm rayermath/*.class;
fi

javac -d . **/*.java \
&& javac -d . *.java \
&& java Rayer

