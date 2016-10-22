#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
    "parent": "block/cube_all",
    "textures": {
        "all": "blocks/$arg"
    }
}
EOF
done