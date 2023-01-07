#!/bin/bash

## install beagle
## expect to be called by a container install script, eg Dockerfile


export TERM=dumb
export NO_COLOR=TRUE
export DEBIAN_FRONTEND=noninteractive


#apt-get -y --quiet install beagle beagle-doc 
# debiang baagle package is something else (phsing genotypes), not the gpu lib used by phylogeny tree sw.
# so for beast (and MrBayes), need to build from source., per URL
# beagle: https://github.com/beagle-dev/beagle-lib/wiki/LinuxInstallInstructions
# libs needed to build beagle:
apt-get -y --quiet install cmake build-essential autoconf automake libtool git pkg-config openjdk-11-jdk subversion

#### install beagle ####

echo "======================================================================"
echo "======================= installing beagle ====================="
echo "======================================================================"

git clone --depth=1 https://github.com/beagle-dev/beagle-lib.git
cd beagle-lib
mkdir build
cd build

#xxmkdir -p /opt/libbeagle
#cmake -DCMAKE_INSTALL_PREFIX:PATH=$HOME ..
#cmake -DCMAKE_INSTALL_PREFIX:PATH=/opt/libbeagle ..
cmake -DCMAKE_INSTALL_PREFIX:PATH=/usr/local -DBUILD_CUDA=ON -DBUILD_OPENCL=ON -DBUILD_JNI=ON ..
make install
echo $?

echo "======================================================================"
echo "==== running make test for beagle ====="
echo "======================================================================"
make test
echo $?

echo "======================================================================"
echo "==== running make check for beagle ====="
echo "======================================================================"
make check
echo $?
echo "======================================================================"
echo "==== running beast -beagle_info ====="
echo "==== path inside build env problem, dont seems to work ==============="
echo "======================================================================"
/opt/gitrepo/beast/bin/beast -beagle_info
echo $?

date

#echo "export LD_LIBRARY_PATH=/opt/libbeagle/lib:/lib64:$LD_LIBRARY_PATH"  	>  /etc/profile.d/libbeagle.sh
echo "export LD_LIBRARY_PATH=/usr/local/cuda-11.7/compat:$LD_LIBRARY_PATH" 	>  /etc/profile.d/libbeagle.sh
echo "export PKG_CONFIG_PATH=/usr/local/lib/pkgconfig:$PKG_CONFIG_PATH"  	>> /etc/profile.d/libbeagle.sh
echo "export BEAGLE_LIB=/usr/local/lib"										>> /etc/profile.d/libbeagle.sh
echo "export JAVA_HOME=/usr/bin"    										>> /etc/profile.d/libbeagle.sh

# pork barrel, at end trying to reduce image fetch time.  sysadmin performance benchmark
#apt-get -y --quiet install htop cpupower-gui 
apt-get -y --quiet install hwloc-nox ipmitool cpustat nuttcp nttcp
apt-get -y --quiet install python3-dracclient python-dracclient-doc

