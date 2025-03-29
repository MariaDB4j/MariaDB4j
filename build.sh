#!/usr/bin/env bash

#this does what .travis.yml does

set -euxo pipefail

#before_install:
#  - mvn -f DBs/pom.xml clean install
#
#script: mvn package -B -V

#if machine is behind a proxy don't forget to set it so that mvn/ant can work
#export MAVEN_OPTS="-Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128"

#./mvnw -f DBs/pom.xml clean install
./mvnw package -B -V
