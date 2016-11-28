#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
	"parent": "item/generated",
	"textures": {
	  "layer0": "items/$arg"
  }
}
EOF
done
