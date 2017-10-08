if [[ "$CIRCLE_BRANCH" == "master" && "$CIRCLE_TAG" == *"v"* ]]; then
    ./gradlew clean build bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
fi
exit 0