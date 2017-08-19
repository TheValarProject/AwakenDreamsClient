#!/bin/bash
for arg in "$@"
do
	cat > $arg.json <<EOF
{
	"parent": "item/generated",
	"textures": {
	  "layer0": "awakendreams:items/$arg"
  }
}
EOF
done
