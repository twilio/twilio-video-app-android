#!/bin/bash
BASE_DIR=`dirname $0`
pushd "$BASE_DIR" >/dev/null

git submodule status --recursive

popd >/dev/null
