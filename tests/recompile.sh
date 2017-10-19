#!/bin/bash

# Change current directory to the main directory
cd "$(dirname "${BASH_SOURCE[0]}")"
cd ../mcp/

./recompile.sh
exit $?
