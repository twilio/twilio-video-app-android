#!/bin/bash

set -ex

ANDROID_API=19

function main {
    mydir=$(dirname $0)
    if [ "${mydir}" = "." ]; then
        mydir=$(pwd)
    elif [ "${mydir:0:1}" != "/" ]; then
        mydir="$(pwd)/${mydir}"
    fi

    #. ${mydir}/functions.sh
    #check_android_tiools $ANDROID_API

    twsdkroot="${mydir}/.."
    buildroot="${twsdkroot}/output"
    echo "twsdkroot: " ${twsdkroot}  
    echo "buildRoot: " ${buildroot}
    build_project "/TestCore"
}

function build_project {
    PROJECT_DIR=$1

    pushd $PROJECT_DIR
    echo "Building project at ${PROJECT_DIR}..."

    rm -f build.xml
    android update project -p ${PROJECT_DIR}

    ant clean
    ant debug

    echo "Project built succesffully."
    popd
}

main "$@"
