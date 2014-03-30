#!/bin/sh

input="$1"
shift
./bin/mysql --no-defaults -u root -D test  "$@" < "$input"
