#!/bin/bash

# Update Environment
export ONOS_ROOT=~/onos
source $ONOS_ROOT/tools/dev/bash_profile

# Build
mvn clean install

# Deploy
onos-app 192.168.123.1 reinstall! target/onos-acl-firewall*.oar
