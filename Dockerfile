# Yves Hwang
# 02.11.2014
#
# USAGE
#
# docker build -t macyves:dev .
# docker run -t -i --name macyvesdev --volume <localdir>:/workspace:rw \
#     macyves:dev ..
# note that the user "macyves" is created to illustrate that runtime credential

FROM ubuntu:trusty
MAINTAINER Yves Hwang, yveshwang@gmail.com
RUN useradd -m macyves
RUN adduser macyves nogroup

# Install and update existing tools, apt-utils for configuring mongodb
RUN apt-get update
RUN apt-get dist-upgrade -y
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections

# yh 05.06.2015. hot fix for broken python software properties. see http://askubuntu.com/questions/598465/apt-get-install-python-software-properties-not-working
# bug reported here https://bugs.launchpad.net/ubuntu/+source/apt-setup/+bug/1434699
RUN rm -fr /var/lib/apt/lists
RUN apt-get update
RUN apt-get install software-properties-common -y
RUN apt-get install python-software-properties -y
RUN apt-get install -y apt-utils
RUN apt-get install -y wget

# Install Jetty
ENV JETTY_VERSION 8.1.17.v20150415

RUN wget http://download.eclipse.org/jetty/${JETTY_VERSION}/dist/jetty-distribution-${JETTY_VERSION}.zip -O /tmp/jetty.zip
RUN apt-get install -y unzip
RUN cd /opt && unzip /tmp/jetty.zip
RUN ln -s /opt/jetty-distribution-${JETTY_VERSION} /opt/jetty
RUN rm /tmp/jetty.zip

ENV JETTY_HOME /opt/jetty
ENV PATH $PATH:$JETTY_HOME/bin

# Install OpenJDK Java jdk
RUN apt-get install -y openjdk-7-jdk

# Install MongoDB
ENV MONGODB_VERSION 2.6.8
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' > /etc/apt/sources.list.d/mongodb.list
RUN apt-get update
RUN apt-get install -y mongodb-org=${MONGODB_VERSION} mongodb-org-server=${MONGODB_VERSION} mongodb-org-shell=${MONGODB_VERSION} mongodb-org-mongos=${MONGODB_VERSION} mongodb-org-tools=${MONGODB_VERSION}
RUN echo "mongodb-org hold" | dpkg --set-selections
RUN echo "mongodb-org-server hold" | dpkg --set-selections
RUN echo "mongodb-org-shell hold" | dpkg --set-selections
RUN echo "mongodb-org-mongos hold" | dpkg --set-selections
RUN echo "mongodb-org-tools hold" | dpkg --set-selections
RUN rm -rf /var/lib/apt/lists/*

RUN mkdir /data
RUN mkdir /data/db

RUN mkdir -p /workspace
VOLUME ["/workspace"]
WORKDIR  /workspace
CMD ["/bin/bash"]
