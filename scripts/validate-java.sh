#!/bin/bash

SOURCE_NO_LICENSE=$(find ../src/main/java/info/bonjean/ -name "*.java" -exec grep -L "This file is part of Beluga." {} \;)

if [ ! -z "$SOURCE_NO_LICENSE" ]
then
	echo "File with no license found (first match): $SOURCE_NO_LICENSE"
	exit -1
fi
