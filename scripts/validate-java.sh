#!/bin/bash

HEADER_MD5="49a2b7c9dd236b673e04127764875069"

find ../src/ -wholename "*/info/bonjean/beluga/*.java" | while read file ; do
	md5="$(head -n19 $file | md5sum | awk '{print $1}')"
	[ "$md5" != "$HEADER_MD5" ] && echo "invalid header for $file"
done

echo "all done"
