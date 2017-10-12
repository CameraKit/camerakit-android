#!/bin/bash

if [[ "$CIRCLE_BRANCH" == "master" ]]; then
    ./gradlew clean build bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
fi
exit 0
