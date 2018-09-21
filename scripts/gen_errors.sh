#!/bin/bash -e -x

ERRORS_REPO=https://code.hq.twilio.com/client/twilio-video-errors.git
ERROR_DIR="temp/gen_errors"
INSTALL_PATH="../../../library/src/main/java/com/twilio/video"

rm -rf "${ERROR_DIR}"

git clone ${ERRORS_REPO} ${ERROR_DIR}

pushd ${ERROR_DIR}
npm install

OPTIMAL_WIDTH=`./bin/create-twilioerrors android twilioerrors.hbs | grep ' = ' | sed -e's#///.*#///#g' | awk '{ print length, $0 }' | sort -n | cut -d" " -f2- | tail -1 | wc -c | tr -d '[:space:]'`

./bin/create-twilioerrors android  twilioerrors.hbs | awk -F'///' "NF==2{printf \"%-${OPTIMAL_WIDTH}s///%s\n\", \$1, \$2} NF<2{print}" > ${INSTALL_PATH}/TwilioException.java

./bin/create-twilioerrors android twilioerrors.hbs > ${INSTALL_PATH}/TwilioException.java
popd

rm -rf "${ERROR_DIR}"
