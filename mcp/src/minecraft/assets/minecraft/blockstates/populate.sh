#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
    "variants": {
        "normal": { "model": "$arg" }
    }
}
EOF
done