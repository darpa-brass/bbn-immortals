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
 * Baseline A, Baseline B, and Challenge fully implemented.
 * Currently returning success for repair even though one DFU is failing to repair. Detailed degraded state will be 
   added in an upcoming version.

### CP2
 * Implementation in progress 

### CP3
 * Implementation in progress 
 
## Initial Online Setup

The initial setup will go through dependency installation, building, and installation of the DAS. It will also copy the 
immortals_root to /tmp/immortals_deployment_test to perform some tests while keeping the execution directory pristine.

1. Install Python 3.5 if necessary  
    `$ sudo apt-get install python3.5`
2. As a normal user, copy immortals_repo into their home directory so that the following structure matches up with the 
repository:  
    `~/immortals_repo/das`
3.  Navigate to the utilities within the extracted directory  
    `$ cd ~/immortals_repo/shared/utils`
4.  Execute the installation script. You may be asked one or more times for your root credentials to install software.  
    `$ ./install.sh --ll-mode`  

A successful result should output something like the following with a zero return code for automated validation checking (The 
Basic tests are for basic development only and ignored for deployment):  

```
| Basic Installation         | NOT RUN |  
| Basic Build                | NOT RUN |  
| Full Installation          |  PASS   |  
| Full Deployment            |  PASS   |  
| Full Deployment Validation |  PASS   |  
| Baseline Marti             |  PASS   |  
| API Smoke Test             |  PASS   |
```

### Additional Testing

Once an image has been created and saved, you should also be able to perform a full execution of CP1's Baseline A, 
Baseline B, and Challenge scenario as follows.  It is recommended to not do this on an image you intend to continue 
 using for deployment as behavior in that scenario is untested. This script location and naming convention is subject 
 to change.

1.  Navigate to the harness directory
    `$ cd ~/immortals_repo/harness`
2.  Execute the test. This may take some time depending on the system (30 - 60 minutes is the estimate, but it is 
heavily bound to single-core CPU power):
    `$ ./test_cp1.sh`

## Configuration

Many configuration parameters can be overridden using a configuration file. A subset of these are documented below for 
deployment use.

### Specifying Configuration Properties

Configuration properties can be set one of the following ways:

#### Set an environment variable

A file can be specified for the **IMMORTALS_OVERRIDE_FILE** environment variable as follows:  

`export IMMORTALS_OVERRIDE_FILE=/path/to/my/json/file.json`

#### Pass to Script

Some scripts are set up to allow a value to be passed to them and export that environment variable automatically. For 
Example, when you start the das, you may do the following:

``$ ./start.sh --set-override-file-data "`cat /path/to/my/json/file.json`"``

### Configuration Sample
```json
{
    "testHarness": {
        // The port the Test Harness is running on
        "port": 44444,
        // The host address the Test Harness is running on
        "url": "127.0.0.1"
    },
    "testAdapter": {
        // The port the Test Adapter should run on
        "port": 55555,
        // The port the Test Adapter should run on
        "url": "127.0.0.1"
    },
    // Values that are specific to the deployment environment that will need to be modified
    "deploymentEnvironment": {
        // The address the Marti server instance will run on. This will be the same as the DAS
        "martiAddress": "10.0.2.2",
        // A list of android emulators available for the DAS and SUT to utilize via ADB. You may configure the number 
        // and version of the emulators per-challenge problem. However, if you would like to simplify things, providing 
        // two emulators running androidVersion 21 and two emulators running androidVersion 23 for all challenge 
        // problems would also worked.
        "androidEnvironments" : [
            {
                // The port the given android emulator uses for ADB access
                "adbPort" : 5580,
                // The URL of the android emulator
                "adbUrl" : "127.0.0.1",
                // The Identifier used to identify it within an ADB instance
                "adbIdentifier": "emulator-5580",
                // The Android version in use.
                "androidVersion": 21
            },
            {
                "adbPort" : 5578,
                "adbUrl" : "127.0.0.1",
                "adbIdentifier": "emulator-5578",
                "androidVersion": 21
            }
        ]
    }
}
```

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

