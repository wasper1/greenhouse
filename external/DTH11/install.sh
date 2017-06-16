#!/bin/bash
FILE_PATH=/usr/local/bin/readDTH11
cd `dirname "$0"`
gcc readDTH11.c -o $FILE_PATH -L/usr/local/lib -lwiringPi
chown root $FILE_PATH
chmod u+s $FILE_PATH
