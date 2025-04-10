FROM openjdk:8-jdk

# Install Ant, sudo, X11 and other dependencies
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends ant sudo wget git \
        libx11-6 libxext6 libxrender1 libxtst6 libxi6 x11-apps \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user and add to sudo group
RUN useradd -m -s /bin/bash vscode \
    && mkdir -p /workspace \
    && chown vscode:vscode /workspace \
    && adduser vscode sudo \
    && echo "vscode ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/vscode

# Set up X11 permissions
RUN mkdir -p /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix

# Set the working directory
WORKDIR /workspace

# Switch to non-root user
USER vscode 