#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
    "parent": "awakendreams:block/$arg"
}
EOF
done
