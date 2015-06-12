# Yves Hwang
# 02.11.2014
#
# USAGE
#
# docker build -t macyves:dev .
# docker run -t -i --name macyvesdev --volume <localdir>:/workspace:rw \
#     macyves:dev ..
# note that the user "macyves" is created to illustrate that runtime credential

FROM dockerfile/ubuntu
MAINTAINER Yves Hwang, yveshwang@gmail.com
RUN useradd -m macyves
RUN adduser macyves nogroup

# install gradle
RUN \
    add-apt-repository ppa:cwchien/gradle -y && \
    apt-get update && \
    apt-get install -y gradle
RUN apt-get install -y openjdk-7-jdk

# Install MongoDB.
RUN \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
    echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' > /etc/apt/sources.list.d/mongodb.list && \
    apt-get update && \
    apt-get install -y mongodb-org && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /workspace
EXPOSE 8080
VOLUME ["/workspace"]
WORKDIR  /workspace
CMD ["/bin/bash"]
