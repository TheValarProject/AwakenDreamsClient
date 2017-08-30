#!/bin/bash

# Change current directory to the main directory
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../

# An array of allowable sizes (powers of 2 between 16 and 4096)
POW2=(16 32 64 128 256 512 1024 2048 4096)

# Get all image assets in the awakendreams resource domain
IMAGES=$(find mcp/src/minecraft/assets/awakendreams/textures -type f -name "*.png")

# A flag for identifing if any errors occurred during execution
ERROR=0

# An array of files that are allowed to be rectangular
RECTANGULAR=("mcp/src/minecraft/assets/awakendreams/textures/models/armor/" "mcp/src/minecraft/assets/awakendreams/textures/entity/")

# An array of files that are allowed to have dimensions that are not a power of 2
IRREGULAR=()

# A helper function to determine if a string starts with any element in an array
containsElement () {
  local e match="$1"
  shift
  for e; do [[ "$e" == $match ]] || [ "${match#$e}" != "${match}" ] && return 0; done
  return 1
}

# Loop through image assets in the awakendreams resource domain
for IMAGE in $IMAGES; do
  HEIGHT=$(identify -format "%h" "$IMAGE")
  WIDTH=$(identify -format "%w" "$IMAGE")

  # If the image is not square and if the image path is not in the rectangular array
	if [ $HEIGHT -ne $WIDTH ] && ! $(containsElement "$IMAGE" "${RECTANGULAR[@]}"); then
		if ! $(containsElement "$IMAGE" "${EXCEPTIONS[@]}"); then
			ERROR=1
			echo "Image at $IMAGE is not square" 1>&2
		fi
	fi

  # If the image height is irregular and the image path is not in the irregular array
  if ! $(containsElement $HEIGHT "${POW2[@]}") && ! $(containsElement "$IMAGE" "${IRREGULAR[@]}"); then
    ERROR=1
    echo "Image at $IMAGE has an invalid height of $HEIGHT" 1>&2
  fi

  # If the image width is irregular and the image path is not in the irregular array
  if ! $(containsElement $WIDTH "${POW2[@]}") && ! $(containsElement "$IMAGE" "${IRREGULAR[@]}"); then
    ERROR=1
    echo "Image at $IMAGE has an invalid width of $WIDTH" 1>&2
  fi
done

if [ $ERROR -ne 0 ]; then
	exit 1
fi
