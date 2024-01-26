#!/usr/bin/env bash

# Fail if any command fails or if there are unbound variables and check syntax
set -euxo pipefail
bash -n "$0"

./mvnw versions:update-properties versions:use-releases versions:use-latest-releases -Dexcludes=com.github.ben-manes.caffeine:caffeine:jar:\*,org.slf4j:\*,org.glassfish.jaxb:\*,com.sun.istack:\*,com.sun.xml.fastinfoset:\*,net.sf.jopt-simple:\*,ch.qos.logback:\*
