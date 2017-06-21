#!/bin/bash
SCRIPT_DIR=`dirname "$0"`
sudo java -jar ${SCRIPT_DIR}/../target/greenhouse-1.0.0-jar-with-dependencies.jar ${SCRIPT_DIR}/greenhouse.properties