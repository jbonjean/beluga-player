#!/bin/sh

# based on initializr-verekia-4.0.zip

TEMPLATE="initializr"
TEMPLATE_SRC=""
SOURCE="index.md"
ROOT=".."

echo "initialize root"
mkdir -p "$ROOT"
rsync -a --delete --exclude "/src/" --exclude "/img-extra/" --exclude "/.git*" --exclude "/CNAME" \
	--exclude "/favicon" --exclude "apple-touch-icon-precomposed.png" --exclude "favicon.ico" \
	"$TEMPLATE/." "$ROOT/."

echo "generate index"
for area in article aside header footer; do
	data=
	[ -f "$area.md" ] && data="$(markdown $area.md)"
	[ -f "$area.html" ] && data="$(cat $area.html)"
	echo "populate $area area"
	awk -v data="$data" -v area="$area" '
		($0 ~ ".*") {if (in_area != 1) {print $0}}
		($0 ~ "<"area">" || $0 ~ "<"area" class=") {in_area=1}
		($0 ~ "</"area">") {if (in_area == 1) {in_area=0;print data;print $0;next}}
	' "$ROOT/index.html" > "$ROOT/index.html.1"
	mv "$ROOT/index.html.1" "$ROOT/index.html"
done

echo "install favicons"
mkdir -p "$ROOT/favicon"
rsync -a --delete "favicon/." "$ROOT/favicon/."

sed -i '/<title><.*/d' "$ROOT/index.html"
awk -v data="$(cat head.html)" '
	/<\/head>/ {print data}
	/.*/ {print $0}
	' "$ROOT/index.html" > "$ROOT/index.html.1"
mv "$ROOT/index.html.1" "$ROOT/index.html"

