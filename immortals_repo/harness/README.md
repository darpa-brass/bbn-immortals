# IMMoRTALS Test Adapter Instructions

## Overview
The IMMoRTALS Test Adapter consists of a copy of the IMMoRTALS source code repository.

The environment we are running it in consists of a virtual machine installed using the image located at 
http://mirror.cs.pitt.edu/ubuntu/releases/16.04.4/ubuntu-16.04.4-server-amd64.iso with the default settings other than
an openssh server enabled.  

I would recommend a base system with 8GB of memory and 8 threads (via 8 cores or 4 cores with hyperthreading).  As we 
continue to add functionality it may be worth varying some of those values to see what provides decent execution time.

## Status

#### CP1
See details at the [CP1 Document](phase02/ChallengeProblems/Immortals-Phase2-cp1-SchemaMigration.md)

Current Status:
 * Baseline A, Baseline B, and Challenge fully implemented.
 * The failure of one of the CP1 tests is now passed through and results in a "DEGRADED" state. The intent is to 
   improve what we are doing to resolve this issue prior to final evaluation.

### CP2
See details at the [CP2 Document](phase02/ChallengeProblems/Immortals-Phase2-cp2-CrossAppDepend.md)

Current Status:
 * Implementation in progress 

### CP3
See details at the [CP3 Document](phase02/ChallengeProblems/Immortals-Phase2-cp3-LibraryEvol.md)

Current Status:
 * Baseline A, Baseline B, and Challenge have been fully implemented for library mutation with one server mutation library (ElevationApi_2)
 * Implementation of additional libraries and partial library upgrades are in progress 
 
## Initial Online Setup

The initial setup will go through dependency installation, building, and installation of the DAS.  

##### Vagrant Installation

Simply execute `vagrant up` within the immortals_repo folder containing the `Vagrantfile`

##### Manual Installation

1. Install Python 3.5 if necessary  
    `$ sudo apt-get install python3.5`
2. As a normal user, copy immortals_repo into their home directory so that the following structure matches up with the 
repository:  
    `~/immortals_repo/das`
3.  Navigate to the harness directory  
    `$ cd ~/immortals_repo/harness`
4.  Execute the setup preparation command  
    `$ ./prepare_setup.sh`
5.  Execute setup. You may be asked one or more times for your root credentials to install software.  
    `$ ./setup.sh`
6.  Copy the generated immortalsrc to your home directory  
    `$ cp immortalsrc ~/.immortalsrc`
7.  Navigate to the root immortals repository  
    `$ cd ~/immortals_repo`
8.  Execute DAS deployment  
    `$ ./gradlew deploy`
9.  Execute the installation script. You may be asked one or more times for your root credentials to install software.  
    `$ ./install.sh --ll-mode`  

##### Assisted Installation

Assisted installation (which performs all the manual steps and executes some tests) is currently being updated.

## Smoke Test

The smoke test can be run as follows:

1.  Navigate to `~/immortals_repo/harness`  
2.  Change `smoke_override_file.json` to reflect the proper Test Harness and Test Adapter configurations.  
3.  Execute the following command:  
    `./smoke.sh`  

It will return a non-zero status along with a description of the issue if the smoke test fails.

## DAS Execution Configuration

Many configuration parameters can be overridden using a configuration file. 
Please see the [Prerequisites](phase02/ChallengeProblems/Immortals-Phase2-DasPrerequisites.md) document 
for more details.


## Starting The DAS

To start the DAS, perform the following steps. If the Test Harness is not ready to receive the _/ready_ signal, startup 
will fail!

Start by changing to the DAS directory as follows:  
`cd ~/immortals_repo/das`

You can then start it one of two ways:

1.  Provide the configuration as a variable  
``./start.sh --set-override-file-data "`cat /path/to/my/json/file.json``  

2.  Export the configuration and then start it  
``export IMMORTALS_OVERRIDE_FILE=/path/to/my/override/file.json``  
``./start.sh``

