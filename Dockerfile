# Dockerfile for creating container to host BEAST
# manual build, mostly:
# docker build -f Dockerfile .  | tee LOG.Dockerfile.txt

## FROM debian:bullseye ## used by r* for OS package without beagle lib for gpu
## FROM nvidia/cuda:11.7.1-devel-ubuntu22.04  # hung A5000 with cuda 11.4/centos 7.9 (b15)
## FROM nvidia/cuda:11.2.1-devel-ubuntu18.04  # n0005 CUDA 11.2 >>  wrong opencl-icd
## FROM nvidia/cuda:11.4.2-devel-ubuntu18.04  # n0259 CUDA 11.4
FROM nvidia/cuda:11.4.2-devel-ubuntu20.04
#?? FROM nvidia/cuda:11.4.0-devel-centos7
# default aka :latest no longer supported.  https://hub.docker.com/r/nvidia/cuda


LABEL Source1="https://github.com/CompEvol/beast2"
LABEL Source2="https://github.com/beagle-dev/beagle-lib/wiki/LinuxInstallInstructions"
LABEL Source3="https://hub.docker.com/r/nvidia/cuda/tags?page=1&name=11.4.2-devel-ubuntu"
LABEL description="this is a containerazation of the beast2 software, \
build with beagle-lib with GPU support, utilizing nvidia CUDA docker images as substrate"


MAINTAINER Tin_at_berkeley.edu
ARG DEBIAN_FRONTEND=noninteractive
#ARG TERM=vt100
ARG TERM=dumb
ARG TZ=PST8PDT
ARG NO_COLOR=1


# will use stand alone script to do most of the installation
# so that it can be used on singlarity (if not building from docker image)
# or perhaps installed on a cloud instance directly

RUN echo  ''  ;\
    touch _TOP_DIR_OF_CONTAINER_  ;\
    echo "This container build as os, then add additional package via standalone shell script " | tee -a _TOP_DIR_OF_CONTAINER_  ;\
    export TERM=dumb      ;\
    export NO_COLOR=TRUE  ;\
    apt-get update ;\
    apt-get -y --quiet install git git-all file wget curl gzip bash zsh fish tcsh less vim procps screen tmux ;\
    apt-get -y --quiet install apt-file ;\
    cd /    ;\
    echo ""

RUN echo ''  ;\
    echo '==================================================================' ;\
    test -d /opt/gitrepo            || mkdir -p /opt/gitrepo             ;\
    test -d /opt/gitrepo/container  || mkdir -p /opt/gitrepo/container   ;\
    #the git command dont produce output, thought container run on the dir squatting on the git files.  COPY works... oh well
    #git branch |tee /opt/gitrepo/container/git.branch.out.txt            ;\
    git log --oneline --graph --decorate | tee /opt/gitrepo/container/git.lol.out.txt       ;\
    #--echo "--------" | tee -a _TOP_DIR_OF_CONTAINER_           ;\
    #--echo "git cloning the repo for reference/tracking" | tee -a _TOP_DIR_OF_CONTAINER_ ;\
    cd /     ;\
    echo ""

# add some marker of how Docker was build.
COPY .              /opt/gitrepo/container/
#COPY Dockerfile*   /opt/gitrepo/container/


RUN echo  ''  ;\
    touch _TOP_DIR_OF_CONTAINER_  ;\
    date | tee -a       _TOP_DIR_OF_CONTAINER_ ;\
    export TERM=dumb      ;\
    export NO_COLOR=TRUE  ;\
    cd /     ;\
    echo ""  ;\
    echo '==================================================================' ;\
    echo '==== install beagle gpu lib ======================================' ;\
    echo '==================================================================' ;\
    echo " calling external shell script..." | tee -a _TOP_DIR_OF_CONTAINER_  ;\
    echo " cd to /opt/gitrepo/container/"    | tee -a _TOP_DIR_OF_CONTAINER_  ;\
    date | tee -a      _TOP_DIR_OF_CONTAINER_                                 ;\
    echo '==================================================================' ;\
    cd /opt/gitrepo/container     ;\
    git branch |tee /opt/gitrepo/container/git.branch.out.txt                 ;\
    # the install from source repo create dir, so cd /opt/gitrepo             ;\
    cd /opt/gitrepo                                                           ;\
    ln -s /opt/gitrepo/container/install_beagle_src.sh .                      ;\
    bash -x install_beagle_src.sh 2>&1 | tee install_beagle_src.log           ;\
    cd /    ;\
    echo ""


RUN echo  ''  ;\
    touch _TOP_DIR_OF_CONTAINER_  ;\
    date | tee -a       _TOP_DIR_OF_CONTAINER_ ;\
    export TERM=dumb      ;\
    export NO_COLOR=TRUE  ;\
    cd /     ;\
    echo ""  ;\
    echo '==================================================================' ;\
    echo '==== install beast phylo sw ======================================' ;\
    echo '==================================================================' ;\
    echo " calling external shell script..." | tee -a _TOP_DIR_OF_CONTAINER_  ;\
    echo " cd to /opt/gitrepo/container/"    | tee -a _TOP_DIR_OF_CONTAINER_  ;\
    date | tee -a      _TOP_DIR_OF_CONTAINER_                                 ;\
    echo '==================================================================' ;\
    cd /opt/gitrepo/container     ;\
    # the install from source repo create dir, so cd /opt/gitrepo             ;\
    cd /opt/gitrepo                                                           ;\
    ln -s /opt/gitrepo/container/install_beast_src.sh .                       ;\
    bash -x install_beast_src.sh 2>&1 | tee install_beast_src.log             ;\
    cd /    ;\
    echo ""

ENV DBG_DOCKERFILE 		Dockerfile
ENV DBG_CONTAINER_VER   "Dockerfile 2023.0105.htop"
ENV DBG_CONTAINER_INFO  "see /opt/gitrepo/install*log for build version eg OpenCL=No"

RUN  cd / \
  && touch _TOP_DIR_OF_CONTAINER_  \
  && echo  "--------" >> _TOP_DIR_OF_CONTAINER_   \
  && TZ=PST8PDT date  >> _TOP_DIR_OF_CONTAINER_   \
  && echo  "$DBG_CONTAINER_VER"   >> _TOP_DIR_OF_CONTAINER_   \
  && echo  "Grand Finale for Dockerfile"


ENV JAVA_HOME=/usr/bin

ENTRYPOINT [ "/opt/gitrepo/beast/bin/beast" ]


# ref: this version essentially same as https://github.com/tin6150/beast/actions/runs/3169484216
# vim: shiftwidth=4 tabstop=4 formatoptions-=cro nolist nu syntax=on
