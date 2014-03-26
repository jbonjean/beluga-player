#!/bin/sh
# Copyright (C) 2014 Julien Bonjean <julien@bonjean.info>

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

set -e

BASEDIR="$(dirname $0)/.."
I18N_FILE="$BASEDIR/src/main/resources/i18n/messages.properties"
CONV_CLASS_PATH="$BASEDIR/target/test-classes/info/bonjean/beluga/misc/ResourceBundleToResources.class"
CONV_CLASS="info.bonjean.beluga.misc.ResourceBundleToResources"

find "$BASEDIR/src" -name "*.java" -or -name "*.bxml" | while read file
do
	extension=${file##*.}

	[ "$extension" = "java" ] &&
	{
		if [ "$(basename $file)" = "PandoraError.java" ]; then
			# enum values
			perl -lne 'print for m/"(\w+)"/g' "$file"
		else
			# log.*
			grep -v "log\.debug" "$file" | perl -lne 'print for m/log.[a-z]+\("(\w+)"\)/g'

			# resources.get
			perl -lne 'print for m/resources.get\("(\w+)"\)/g' "$file"

			# exceptions
			perl -lne 'print for m/Exception\("(\w+)", /g' "$file"
		fi
	}

	[ "$extension" = "bxml" ] &&
		perl -lne 'print for m/="%(\w+)"/g' "$file"
done | sort | uniq | while read key
do
	grep "^$key=" $I18N_FILE || echo "$key="
done > /tmp/messages.properties

diff -u "$I18N_FILE" /tmp/messages.properties |
	awk 'BEGIN{del=0;add=0} /^-[^-]/ {del++} /^+[^+]/ {add++} END{print "+"add" -"del}'

mv /tmp/messages.properties "$I18N_FILE"

if [ -f "$CONV_CLASS_PATH" ]; then
	 java -cp "$BASEDIR/target/test-classes/" "$CONV_CLASS" "$(dirname $I18N_FILE)/"
else
	echo "conv class not compiled"
fi
