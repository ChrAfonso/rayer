#!/bin/bash

cat Rayer.java | awk 'BEGIN { file="Rayer_new.java"; }; { if($1 == "class") { file=$2; system("echo '' > "file); }; system("echo "$0" > "file); }'
