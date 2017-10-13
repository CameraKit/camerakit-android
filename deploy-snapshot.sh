#!/bin/bash

if [[ "$CIRCLE_BRANCH" == "master" ]]; then
    ./gradlew clean build bintrayUpload -Puser=$BINTRAY_USER -Pkey=$BINTRAY_KEY
fi
exit 0
