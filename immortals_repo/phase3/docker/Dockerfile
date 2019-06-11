from ubuntu:18.04

# Sample build command
# docker build --build-arg PUBLIC_KEY="`cat ~/.ssh/keys/id_rsa.pub`" --build-arg ISIS_GIT_PRIVATE_KEY="`cat ~/.ssh/keys/swri_git_id_rsa`" --build-arg ISIS_GIT_PUBLIC_KEY="`cat ~/.ssh/keys/swri_git_id_rsa.pub`"  --tag immortalsserver:latest .

# Set general environment variables
ENV SHELL /bin/bash
ENV USER root

# Install tools
RUN apt-get update
run apt-get -y install git htop vim tmux python3 sudo man-db rsync
RUN apt-get -y install openssh-server net-tools

# Add the default user
RUN useradd --create-home --shell /bin/bash user

# Add the default user to sudoers
RUN echo "user ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers

# Set the docker execution context from here on
USER user
ENV SHELL /bin/bash
ENV USER user

# Set up for SSH access
RUN mkdir -p /home/user/.ssh/keys
ARG PUBLIC_KEY
RUN echo ${PUBLIC_KEY} > /home/user/.ssh/authorized_keys
RUN sudo mkdir /var/run/sshd
RUN sudo ssh-keygen -A
EXPOSE 22
ENTRYPOINT ["sudo", "bash", "-c",  "/usr/sbin/sshd -D"]

ARG ISIS_GIT_PRIVATE_KEY
RUN echo "${ISIS_GIT_PRIVATE_KEY}" > /home/user/.ssh/keys/swri_id_rsa
ARG ISIS_GIT_PUBLIC_KEY
RUN echo "${ISIS_GIT_PUBLIC_KEY}" > /home/user/.ssh/keys/swri_id_rsa.pub
RUN chmod 700 /home/user/.ssh/keys/*
RUN printf 'Host git.isis.vanderbilt.edu\n  HostName git.isis.vanderbilt.edu\n  IdentityFile /home/user/.ssh/keys/swri_id_rsa\n  User git\nStrictHostKeyChecking no' > /home/user/.ssh/config

WORKDIR /home/user/
RUN git clone --branch master --depth=1 --single-branch git@git.isis.vanderbilt.edu:SwRI/bbn/bbn-immortals.git

WORKDIR /home/user/bbn-immortals/immortals_repo/phase3
RUN bash install.sh --build
WORKDIR /home/user
RUN rm -rf bbn-immortals

RUN printf 'git clone --branch master --depth=1 --single-branch git@git.isis.vanderbilt.edu:SwRI/bbn/bbn-immortals.git' > /home/user/clone.sh
RUN chmod +x /home/user/clone.sh
