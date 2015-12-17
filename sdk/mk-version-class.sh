#!/bin/bash

set -e

. ../scripts/functions.sh

[ "$1" ] && sdk_version=$1 || sdk_version=$(get_sdk_version)
[ "$sdk_version" ]

mkdir -p gen/com/twilio/signal

echo "New SDK version is ${sdk_version}"

cat >gen/com/twilio/conversations/Version.java <<EOF
package com.twilio.conversations;

public abstract class Version
{
    /**
     * The current version of the Twilio RTC Conversations SDK.
     */
    public static final String SDK_VERSION = "${sdk_version}";
}
EOF
