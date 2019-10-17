#!/bin/bash

# Generate private key file
echo $CIRCLECI_PRIVATE_KEY | sed -e 's/\\n/\'$'\n/g' > private.pem

# Generate hook payload
export AUTHOR_EMAIL=`git show -s --format='%ae' $CIRCLE_SHA1`
export CIRCLECI_HOOK_PAYLOAD="{\"tag\":\"$CIRCLE_TAG\",\
\"repo_owner_name\":\"$CIRCLE_PROJECT_USERNAME\",\
\"repo_name\":\"$CIRCLE_PROJECT_REPONAME\",\
\"author_email\":\"$AUTHOR_EMAIL\"}"

# Sign payload
echo -n $CIRCLECI_HOOK_PAYLOAD | openssl dgst -sha256 -sign private.pem -passin pass:"$CIRCLECI_KEY_PASSPHRASE" -out signed-message
openssl base64 -in signed-message -out payload-signature
export CIRCLECI_PAYLOAD_SIGNATURE=`cat payload-signature | tr -d '\n'`

# Send webhook
curl -d "payload=$CIRCLECI_HOOK_PAYLOAD" -H "Signature: $CIRCLECI_PAYLOAD_SIGNATURE" -X POST $CAPTAIN_CIRCLE_WEBHOOK_URL

# Cleanup files
rm private.pem
rm signed-message
rm payload-signature
