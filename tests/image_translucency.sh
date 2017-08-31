#!/bin/bash

# Change current directory to the main directory
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../

# Get all image assets in the awakendreams resource domain
IMAGES=$(find mcp/src/minecraft/assets/awakendreams/textures -type f -name "*.png")

# A flag for identifing if any errors occurred during execution
ERROR=0

# An array of files that it is acceptable to have translucency in
EXCEPTIONS=("mcp/src/minecraft/assets/awakendreams/textures/misc/telescopeblur.png")

# A helper function to determine if a string starts with any element in an array
containsElement () {
  local e match="$1"
  shift
  for e; do [[ "$e" == $match ]] || [ "${match#$e}" != "${match}" ] && return 0; done
  return 1
}

# Loop through image assets in the awakendreams resource domain
for IMAGE in $IMAGES; do
	# If the image's alpha channel contains more than 2 colors and if the image path is not in the exception array
	if [ $(identify -alpha extract -format "%k" "$IMAGE") -gt 2 ] && ! $(containsElement "$IMAGE" "${EXCEPTIONS[@]}"); then
		ERROR=1
		echo "Image at $IMAGE contains translucent components" 1>&2
	fi
done

# Exit with an error if necessary
if [ $ERROR -ne 0 ]; then
	exit 1
fi
