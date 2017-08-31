#!/bin/bash

# Change current directory to the main directory
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../

# Get all json assets in the awakendreams resource domain
JSONS=$(find mcp/src/minecraft/assets/awakendreams/ -type f -name "*.json")

# A flag for identifing if any errors occurred during execution
ERROR=0

# Store the sha256 sums of files that have completely passed this test
CACHED=""

# Loop through json assets in the awakendreams resource domain
for JSON in $JSONS; do
  # Check if file is in cache and skip check if it is
  if command -v md5; then
    # Mac
    SUM="$(md5 -q "$JSON")"
  else
    # Linux
    SUM="$(md5sum "$JSON" | cut -d' ' -f1)"
  fi
  if grep -Fe "$SUM" "tests/cache/json_hashes.txt" >/dev/null; then
    echo "$JSON cached, skipping"
    CACHED="$CACHED$SUM\n"
    continue
  fi

	# Check if the json file is valid
	if PRETTYPRINT="$(jsonlint "$JSON")"; then
    echo "$JSON is valid"
    # JSON file is valid, write pretty-print version to file location
    if [ "$PRETTYPRINT" != "$(cat "$JSON")" ]; then
      printf "\nWarning: JSON file at $JSON is not properly formatted, attempting to fix...\n"
      echo "$PRETTYPRINT" > "$JSON"
    else
      CACHED="$CACHED$SUM\n"
    fi
  else
    # JSON file is not valid, print error
    ERROR=1
    echo "JSON file at $JSON contains errors" 1>&2
	fi
done

# Write out cache to the file at tests/cache/json_hashes.txt
printf "$CACHED" > "tests/cache/json_hashes.txt"

# Exit with an error if necessary
if [ $ERROR -ne 0 ]; then
	exit 1
fi
