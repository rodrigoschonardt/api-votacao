#!/bin/bash
set -e

USERNAME="rodrigoschonardt"

echo "Building native image for ARM64..."
./gradlew bootBuildImage --imageName=${USERNAME}/votingapi-arm64

echo "Building native image for AMD64..."
./gradlew  bootBuildImage --imageName=${USERNAME}/votingapi-amd64 --imagePlatform linux/amd64

echo "Pushing images..."
docker push ${USERNAME}/votingapi-arm64
docker push ${USERNAME}/votingapi-amd64

echo "Done!"
echo "ARM64: ${USERNAME}/votingapi-arm64"
echo "AMD64: ${USERNAME}/votingapi-amd64"