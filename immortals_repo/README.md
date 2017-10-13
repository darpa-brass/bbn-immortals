# IMMoRTALS Repository README

## Environment Setup

I recommend at least 30GB of space be set aside for the environment (The built docker image is 17GB currently), and usually set it to 40GB myself.

### Docker and SSH
__+__ Contained  
__+__ Little speed penalty to emulators  
__-__ Harder to actively work in with  no GUI  

This is by far the easiest and most self-contained way to set up the environment.

1. Install [Docker](http://www.docker.com) if it is not already installed.  
2. Check out the repository using svn or svn-git if you have not already:  
   `svn co https://dsl-external.bbn.com/svn/immortals/trunk immortals_root`  
3. Build the image with the following command where _immortals_root_ is your repository root (which contains the Dockerfile) and _/home/user/.ssh/id_rsa.pub_ matches the absolute path of your public ssh key readable by the root user.  Image construction including the automatic initial gradle build took 12 minutes on my machine (and may take longer depending on internet speed) and may seem stuck after the base immortals dependencies have been installed:  
  ``docker build --tag immortals_environment --build-arg PUBLIC_KEY="`cat /home/user/.ssh/id_rsa.pub`" immortals_root``  
4. Execute the following command to start it where _immortals_instance_ is the desired container name:  
   `docker run --name immortals_instance -d immortals_environment`
5. Determine the container IP address by executing the following command:  
   `docker inspect immortals_instance | grep IPAddress`
   ```
            "SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.3",
                    "IPAddress": "172.17.0.3",
   ```
6. SSH into the system using the address determined above:
  `ssh root@172.17.0.3`

See the [Docker Run Reference](https://docs.docker.com/engine/reference/run/) for details about forwarding from the host to the docker container's SSH port (hint: "--publish 22:<hostMachinePort>") or forwarding kvm for x86 emulation on Linux (hint: "--device /dev/kvm:/dev/kvm"). Scripting rsync to copy source files to the container can also help make integration testing run smoother.

### Local Automated Installation
__+__ No speed penalty to emulators  
__+__ Easy to actively work in  
__+__ Installation script is easy to understand
__-__ Less Contained  
__-__ Slightly more complicated to set up

Provides local environment installation by examining what is currently available on your system and generating a bash script that can be used to set up your environment.  Less self contained, but attempts to not trample over existing installations of software when possible. See _harness/README-setup.md_


### Virtual Machine Installation
__+__ Contained  
__-__ Slightly more complicated to set up  
__-__ Significant speed penality to emulators  

Same installation method as **Local Automated Installation** after setting up the virtual machine.



## Root Project Build Instructions
Docker container directory: `/immortals/`  
Build Command: `./gradlew buildAll`  
Clean Command: `./gradlew cleanAll`  

## Application Instructions:
Dependent on the root project being built.  
Located in their corresponding directories relative to the project root. Commands assumed to be run from their own directories.

### Marti Router
#### Building
Directory: _applications/server/Marti_  

Build Command: `../../../gradlew build`  
Clean Command: `../../../gradlew clean`  
Produced Appliation: "Marti-immortals.jar"  

Application Dependencies:  
 * **Marti-Config.json** in the execution directory  

#### Testing and Validation (Not applicable to all projects!)
Command: `../../../gradlew clean validate`  

Output Directory: build/test-results/validate/  

Details:  
The build process will produce a list of all tests in _build/test-results/test/TEST-com.bbn.marti.Tests.xml_. To run a subset of these tests perform something similar to the following command:  
`../../../gradlew clean validate --tests com.bbn.marti.Tests.testImageSave --tests com.bbn.marti.Tests.testImageTransmission`  

The _--tests_ argument can be used multiple times to specify a specific test or specify a filter to dictate which tests to run. Please see the [Java Test](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_test) section of the [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html) for more information.


### ATAKLite
Directory: _applications/client/ATAKLite_  

Build Command: `../../../gradlew build`  
Clean Command: `../../../gradlew clean`  
Produced Appliation: **ATAKLite-debug.apk**  
Application Dependencies:  
 * **env.json** in _/sdcard/ataklite/_ on the android filesystem  
 * **ATAKLite-Config.json** in _/sdcard/ataklite/_ on the android filesystem  

## Global Build Configuration
Various configuration values used immortals-wide for building are imported from the shared/common.gradle file, including
android version details, publishing versioning, and default dependency versioning for items fetched from *IMMORTALS_REPO*.

## Tips and Tricks
 * **shared/IMMORTALS_REPO** contains the dependencies (DFUs, utilities, etc) produced by the project and should be regenerated by performing a `./gradle cleanAll buildAll` from the root whenever the repository is updated.
 * Since artifacts produced by the project are published to the local IMMORTALS_REPO every build, the local maven cache should __NOT__ be enabled in any gradle scripts. Gradle will manage its own cache that knows not to cache the local repository.
