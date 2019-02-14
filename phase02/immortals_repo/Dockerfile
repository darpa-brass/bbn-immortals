from ubuntu:16.04

# Set general environment variables
ENV SHELL /bin/bash
ENV USER root

# Copy the immortals repository directory to the container
ADD ./ /immortals/

# Update and install python3 (for the setup preparation script) and other deps
RUN apt-get update
RUN apt-get install -y python3 sudo man-db

# Run the setup preparation scrypt with the unattended flag to avoid prompts
WORKDIR /immortals/harness/
RUN ./prepare_setup.sh --unattended-setup

# Remove python3 since the project requires 3.6 and it will only cause confusion
RUN apt-get -y remove python3

# Run the setup script
RUN ./setup.sh

# Copy and source the immortalsrc in the bashrc
RUN cp immortalsrc /root/.immortalsrc
RUN echo "source /root/.immortalsrc" >> /root/.bashrc

# Install additional tools
RUN apt-get -y install openssh-server net-tools

# Set up for SSH access
RUN mkdir /root/.ssh
ARG PUBLIC_KEY
RUN echo ${PUBLIC_KEY} > /root/.ssh/authorized_keys
RUN mkdir /var/run/sshd
EXPOSE 22

# Initialize and buildAll
WORKDIR /immortals/
RUN bash gradlew
RUN bash gradlew buildAll

# Set the entrypoint for SSH
ENTRYPOINT ["/usr/sbin/sshd", "-D"]
