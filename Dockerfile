# necessary dependencies: java 21, python 3.13
FROM openjdk:21-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl build-essential zlib1g-dev libncurses5-dev libgdbm-dev libnss3-dev \
    libssl-dev libreadline-dev libffi-dev libsqlite3-dev wget xz-utils tk-dev \
    && rm -rf /var/lib/apt/lists/*

# Install Python 3.13 from source
ENV PYTHON_VERSION=3.13.0

RUN curl -O https://www.python.org/ftp/python/${PYTHON_VERSION}/Python-${PYTHON_VERSION}.tgz && \
    tar -xf Python-${PYTHON_VERSION}.tgz && \
    cd Python-${PYTHON_VERSION} && \
    ./configure --enable-optimizations && \
    make -j$(nproc) && \
    make altinstall && \
    cd .. && rm -rf Python-${PYTHON_VERSION}*

# Set python & pip symlinks
RUN ln -s /usr/local/bin/python3.13 /usr/bin/python && \
    ln -s /usr/local/bin/pip3.13 /usr/bin/pip

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

# Build the project using Gradle wrapper
WORKDIR /home/user/java
RUN ./gradlew build -x test
CMD ./gradlew run && python ../python/src/plotting/result_plotting.py
