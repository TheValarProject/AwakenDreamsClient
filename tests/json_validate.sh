#!/bin/bash

# Change current directory to the main directory
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../

# Get all json assets in the awakendreams resource domain
JSONS=$(find mcp/src/minecraft/assets/awakendreams/ -type f -name "*.json")

# A flag for identifing if any errors occurred during execution
ERROR=0

# Variables for tracking progress
COUNTER=1
TOTAL=$(echo "$JSONS" | wc -l)

# Store the sha256 sums of files that have completely passed this test
CACHED=""

# Loop through json assets in the awakendreams resource domain
for JSON in $JSONS; do
  # Print message if verbose output enabled
  if [[ $1 == "-v" ]]; then
    printf "\r%s/%s" "$COUNTER" "$TOTAL"
    COUNTER=$(bc <<< "$COUNTER + 1")
  fi

  # Check if file is in cache and skip check if it is
  SUM="$(shasum -a 256 "$JSON")"
  if grep "$SUM" "tests/cache/json_hashes.txt" >/dev/null; then
    CACHED="$CACHED$SUM\n"
    continue
  fi

	# Check if the json file is valid
	if PRETTYPRINT="$(jsonlint "$JSON")"; then
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

# Remove progress message
if [[ $1 == "-v" ]]; then
  printf "\r"
fi

# Write out cache to the file at tests/cache/json_hashes.txt
printf "$CACHED" > "tests/cache/json_hashes.txt"

# Exit with an error if necessary
if [ $ERROR -ne 0 ]; then
	exit 1
fi
