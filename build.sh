# !/bin/bash

# Get location of the script itself .. thanks SO ! http://stackoverflow.com/a/246128
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
    DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
PROJECT_ROOT="$( cd -P "$( dirname "$SOURCE" )" && pwd )"


export DEPOTTOOLS="/vagrant/depot_tools"

# Installs the required dependencies on the machine
install_dependencies() {
    sudo apt-get -y install wget git gnupg flex bison gperf build-essential zip curl subversion pkg-config
    #Download the latest script to install the android dependencies for ubuntu
    curl -o install-build-deps-android.sh https://src.chromium.org/svn/trunk/src/build/install-build-deps-android.sh
    #use bash (not dash which is default) to run the script
    sudo /bin/bash ./install-build-deps-android.sh
    #delete the file we just downloaded... not needed anymore
    rm install-build-deps-android.sh

    sudo apt-get -y install libglib2.0-dev libgtk2.0-dev libxtst-dev libxss-dev libnss3-dev libdbus-1-dev libdrm-dev libgconf2-dev libgnome-keyring-dev libgcrypt11-dev
    sudo apt-get -y install libpci-dev libudev-dev 
    install_ruby
   # sudo apt-get install ruby2.0 ruby2.0-dev
    

}

install_ruby() {
    sudo apt-add-repository -y ppa:brightbox/ruby-ng
    apt-get update
    apt-get install -y ruby2.2 ruby2.2-dev
    sudo gem install -r aws-sdk-v1 plist
}

# Update/Get/Ensure the Gclient Depot Tools
# Also will add to your environment
pull_depot_tools() {
	WORKING_DIR=`pwd`

    # Either clone or get latest depot tools
	if [ ! -d "$DEPOTTOOLS" ]
	then
	    echo Make directory for gclient called Depot Tools
	    mkdir -p $DEPOTTOOLS

	    echo Pull the depo tools project from chromium source into the depot tools directory
	    git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git $DEPOTTOOLS

	else
		echo Change directory into the depot tools
		cd $DEPOTTOOLS

		echo Pull the depot tools down to the latest
		git pull
	fi
	PATH="$PATH:$DEPOTTOOLS"

    # Navigate back
	cd $WORKING_DIR
}

