#!/bin/bash

# Switch to directory where the deploy script is located
cd "$( dirname "${BASH_SOURCE[0]}" )"

# Minecraft version to deploy to
MC_VERSION="1.10"

# Deployed version name
VN="AD-0.5b-$(date "+%d%m%y")"

# Set platform specific options
case "$(uname -s)" in
    Darwin)
        MC_PATH=~/Library/Application\ Support/minecraft/versions/$MC_VERSION/$MC_VERSION.jar
    ;;
    Linux)
        MC_PATH=~/.minecraft/versions/$MC_VERSION/$MC_VERSION.jar
        ;;
    *)
        echo "Unsupported OS"
        exit 1
        ;;
esac

if [ ! -e "$MC_PATH" ]; then
    echo "Cannot find Minecraft 1.10 jar, make sure you have run an 1.10 and then try again."
    echo "Expected location: $MC_PATH"
    exit 1
fi

# Clean up any files and folder from previous deployments
rm -rf ./build $VN.zip $VN

# Build code
./recompile.sh

# Reobfuscate code
./reobfuscate.sh

# Make a clean build directory and move to it
mkdir build
cd build

# Extract the contents of the minecraft jar to the mcp/build folder
jar xf "$MC_PATH"

# Remove jar signatures
rm -rf META-INF

# Recursively copy the contents of mcp/reobf/minecraft to mcp/build, overwriting any existing files
cp -a ../reobf/minecraft/. .

# Add the mod's assets from mcp/src/minecraft/assets to the existing minecraft assets in mcp/build/assets
cp -aP ../src/minecraft/assets/awakendreams assets/awakendreams

# Make directory for future versions subfolder
mkdir ../$VN

# Pack classes back into a jar folder
# Must use JDK 1.8.0*
jar cf ../$VN/$VN.jar *

# Change back to mcp folder
cd ../

# Copy build_template.json
cp build_template.json $VN/$VN.json

# Replace %ID with the version name
sed -i '' "s/%ID/$VN/g" $VN/$VN.json

# Replace %TIME with an ISO-8061 time
sed -i '' "s/%TIME/$(date "+%Y-%m-%dT%H:%M:%SZ")/g" $VN/$VN.json

# Zip the version subfolder and don't include DS_Store files (Mac)
zip -r $VN.zip $VN -x "*.DS_Store"
