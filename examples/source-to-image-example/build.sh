#!/bin/sh

# build the maven project
mvn clean install 

# trigger the acutal build
oc start-build openshift-example --from-dir=./target --follow
