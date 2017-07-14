#!/bin/bash
if [ -z "$1" ]
then
    echo "USAGE: ./rebuild.sh /current/application/directory/path"
    exit
fi
cd "$1"
CURRENT_APP_PATH=`pwd`
TMP_PATH='/tmp/greenhouse'

CURRENT_VERSION=`git rev-parse HEAD`
REMOTE_VERSION=`git ls-remote https://github.com/wasper1/greenhouse.git | grep HEAD | awk '{print $1}'`
if [ "$CURRENT_VERSION" != "$REMOTE_VERSION" ]
then
    echo "New version detected"
    rm -rf ${TMP_PATH}
    mkdir ${TMP_PATH}
    cd ${TMP_PATH}
    git clone https://github.com/wasper1/greenhouse.git .
    mvn -B clean package
    if [ "$?" -ne 0 ]; then
        echo "Project build error"
        exit 1
    fi
    rm -rf ${CURRENT_APP_PATH}.old
    mv ${CURRENT_APP_PATH} ${CURRENT_APP_PATH}.old
    mv ${TMP_PATH} ${CURRENT_APP_PATH}
    echo "New version installed"
fi