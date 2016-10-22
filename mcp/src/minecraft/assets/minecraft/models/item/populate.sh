#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
    "parent": "block/$arg"
}
EOF
done