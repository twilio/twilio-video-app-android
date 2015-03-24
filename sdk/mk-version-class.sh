#!/bin/bash

set -e

. ../scripts/functions.sh

[ "$1" ] && sdk_version=$1 || sdk_version=$(get_sdk_version)
[ "$sdk_version" ]

mkdir -p gen/com/twilio/client

echo "New SDK version is ${sdk_version}"

cat >gen/com/twilio/client/Version.java <<EOF
package com.twilio.client;

public abstract class Version
{
    /**
     * The current version of the Twilio Client SDK.
     */
    public static final String SDK_VERSION = "${sdk_version}";
}
EOF

cat >external/signal-sdk-core/TwilioCoreSDK/TwilioCoreSDK/Sources/Core/TSCVersion.h <<EOF
/* this file is auto-generated; do not edit! */

#ifdef DEBUG
#define TSC_CORE_SDK_VERSION "${sdk_version}"
#else
#define TSC_CORE_SDK_VERSION "${sdk_version}"
#endif
EOF
