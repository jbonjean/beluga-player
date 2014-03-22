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

I18N_FILE="../src/main/resources/i18n/messages.properties"

find ../src -name "*.java" -or -name "*.bxml" | while read file
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
done
