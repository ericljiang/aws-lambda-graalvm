FROM amazonlinux
RUN yum install -y which tar gzip gcc glibc-devel zlib-devel wget
RUN amazon-linux-extras install java-openjdk11
RUN export JAVA_HOME=$(readlink -f `which java`)
RUN wget -nv -O /tmp/graalvm.tar.gz https://github.com/oracle/graal/releases/download/vm-19.2.1/graalvm-ce-linux-amd64-19.2.1.tar.gz \
 && tar -xzf /tmp/graalvm.tar.gz -C /opt \
 && rm /tmp/graalvm.tar.gz
ENV GRAALVM_HOME="/opt/graalvm-ce-19.2.1"
RUN $GRAALVM_HOME/bin/gu install native-image
