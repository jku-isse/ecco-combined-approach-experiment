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

RUN unzip /home/user/ground_truth/REPOS/apache-httpd.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/argouml-spl.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/berkeley-db-libdb.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/busybox.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/dia.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/irssi.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/libssh.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/openvpn.zip -d /home/user/ground_truth/REPOS
RUN unzip /home/user/ground_truth/REPOS/vim.zip -d /home/user/ground_truth/REPOS

# Build the project using Gradle wrapper
WORKDIR /home/user/java
RUN ./gradlew build -x test
CMD ./gradlew run && python ../python/src/plotting/result_plotting.py
