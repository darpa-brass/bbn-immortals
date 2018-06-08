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

### CP2
See details at the [CP2 Document](phase02/ChallengeProblems/Immortals-Phase2-cp2-CrossAppDepend.md)

Current Status:
 * Implementation in progress 

### CP3
See details at the [CP3 Document](phase02/ChallengeProblems/Immortals-Phase2-cp3-LibraryEvol.md)

Current Status:
 * Baseline A, Baseline B, and Challenge have been fully implemented for library mutation with one server mutation 
   library (ElevationApi_2)
 * Baseline A, Baseline B, and Challenge have been fully implemented for partial library upgrades with one client 
   mutation library(Dropbox_3_0_6)
 * One additional library upgrade adaptation is in progress
 
 
## Initial Online Setup

The initial setup requires an internet connection and will go through dependency installation, building, and 
installation of the DAS. It is recommended to use the **Vagrant Installation** or **Assisted Installation** as they 
will perform additional checks to ensure the DAS is ready for use.

#### Manual Installation

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

#### Assisted Installation

Assisted installation performs all steps included in **Manual Installation**.  

It also copies the repository to `/tmp/immortals_deployment_test/` and executes the smoke test along with some 
additional verifications to ensure the environment has been set up properly. In a successful scenario the end result 
is a chart similar to the following:  

| Results                    |         |
| ---------------------------|---------|
| Basic Installation         |  PASS   |
| Basic Build                |  PASS   |
| Full Installation          |  PASS   |
| Full Deployment            |  PASS   |
| Full Deployment Validation |  PASS   |
| Baseline Marti             |  PASS   |
| API Smoke Test             |  PASS   |

Steps:

1.  Navigate to the utils directory  
    `$ cd ~/immortals_repo/shared/utils`  
2.  Execute the install script  
    `$ ./install.sh`  

In the event of any test failures the _install.sh_ script returns a non-zero exit code.

#### Vagrant Installation

The Vagrant installation wraps the **Assisted Installation**. Simply execute `vagrant up` within the immortals_repo 
folder containing the `Vagrantfile`. This will execute an installation within a vagrant container.


## Testing

Testing should not be done on an image intended to be saved for evaluation since the DAS is not guaranteed to be 
stateless between executions!

### Smoke Test

The smoke test is included within the **Assisted Installation** and **Vagrant Installation**. However, if you wish to 
run it manually (which can be useful for validating the actual Test Harness works with the Test Adapter) you may do so 
as follows:

1.  Navigate to the immortals harness directory  
    `$ cd ~/immortals_repo/harness`  
2.  Change `smoke_override_file.json` to reflect the proper Test Harness and Test Adapter configurations.  
3.  Execute the following command:  
    `$ ./smoke.sh`  

It will return a non-zero status along with a description of the issue if the smoke test fails.

### Validation Test

The validation test will run a single Challenge instance of each perturbation type that has been implemented to ensure 
that the DAS executes as expected given valid perturbation info from our mock Test Harness. 

Steps:

1.  Navigate to the utils directory  
    `$ cd ~/immortals_repo/shared/utils`  
2.  Execute the test script  
    `$ ./test.sh`  

It will return a non-zero status along with a description of the issue if any of the validation tests fails.


## DAS Execution

### Configuration

Certain environment components such as Android emulators, machine hosts, and ports must be provided by the evaluator. 
Please see the [Prerequisites](phase02/ChallengeProblems/Immortals-Phase2-DasPrerequisites.md) document 
for more details.

### Starting The DAS

To start the DAS, perform the following steps. If the Test Harness is not ready to receive the _/ready_ signal, startup 
will fail!

Steps:

1.  Navigate to the DAS Directory  
    `cd ~/immortals_repo/das`  
2.  Start the das, providing the configuration as a variable  
    ``./start.sh --set-override-file-data "`cat /path/to/my/json/file.json``  

