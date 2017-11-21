#!/bin/bash

./gradlew clean build bintrayUpload -Puser=$BINTRAY_USER -Pkey=$BINTRAY_KEY

exit 0
