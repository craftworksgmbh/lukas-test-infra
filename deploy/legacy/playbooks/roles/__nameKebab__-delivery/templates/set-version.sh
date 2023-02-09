#!/bin/bash

if [ -z $1 ]
then
    echo "no argument given - version will not be changed"
else
    replaceString=$1 
        sed -i "s/\(IMAGE_VERSION=\).*\$/\1${replaceString}/" .env
    echo "version changed to $1"
fi