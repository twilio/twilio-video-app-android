#!/bin/bash
BASE_DIR=`dirname $0`
pushd "$BASE_DIR" >/dev/null

git submodule foreach 'rm -rf * .git*'

popd >/dev/null
