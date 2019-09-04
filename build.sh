#!/usr/bin/env bash

#this does what .travis.yml does

set -v
set -e

#before_install:
#  - mvn -f DBs/pom.xml clean install
#
#script: mvn package -B -V

#if machine is behind a proxy don't forget to set it so that mvn/ant can work
#export MAVEN_OPTS="-Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128"

mvn -f DBs/pom.xml clean install
mvn package -B -V
