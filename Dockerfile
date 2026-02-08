# Base image with Python 3.13
FROM python:3.13-slim

# Avoid interactive prompts during install
ENV DEBIAN_FRONTEND=noninteractive

# Install Java 21 and git
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-21-jdk \
        git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# Install Python modules
RUN pip install --no-cache-dir \
matplotlib \
numpy \
seaborn \
pandas

WORKDIR /home/user
COPY java/ ./java/
RUN chmod +x ./java/gradlew
RUN apt-get update && apt-get install -y dos2unix
RUN dos2unix ./java/gradlew
COPY python/ ./python/
COPY ground_truth/ ./ground_truth/

# result database
VOLUME /home/user/java/build/resources/main/database
# result illustrations
RUN mkdir /home/user/results
RUN mkdir /home/user/results/images
VOLUME /home/user/results/images

# clone ground truth git repositories
RUN git clone /home/user/ground_truth/REPOS_BARE/apache-httpd /home/user/ground_truth/REPOS/apache-httpd
RUN git clone /home/user/ground_truth/REPOS_BARE/argouml-spl /home/user/ground_truth/REPOS/argouml-spl
RUN git clone /home/user/ground_truth/REPOS_BARE/berkeley-db-libdb /home/user/ground_truth/REPOS/berkeley-db-libdb
RUN git clone /home/user/ground_truth/REPOS_BARE/busybox /home/user/ground_truth/REPOS/busybox
RUN git clone /home/user/ground_truth/REPOS_BARE/dia /home/user/ground_truth/REPOS/dia
RUN git clone /home/user/ground_truth/REPOS_BARE/irssi /home/user/ground_truth/REPOS/irssi
RUN git clone /home/user/ground_truth/REPOS_BARE/libssh /home/user/ground_truth/REPOS/libssh
RUN git clone /home/user/ground_truth/REPOS_BARE/openvpn /home/user/ground_truth/REPOS/openvpn
RUN git clone /home/user/ground_truth/REPOS_BARE/vim /home/user/ground_truth/REPOS/vim

# Build the project using Gradle wrapper
WORKDIR /home/user/java
RUN ./gradlew build -x test
CMD ./gradlew run && python ../python/src/plotting/result_plotting.py
