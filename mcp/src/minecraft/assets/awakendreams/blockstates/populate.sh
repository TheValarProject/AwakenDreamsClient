#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
    "variants": {
        "normal": { "model": "awakendreams:$arg" }
    }
}
EOF
done
