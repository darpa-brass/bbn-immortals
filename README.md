# IMMoRTALS Test Adapter Instructions

## Overview
The IMMoRTALS Test Adapter consists of a copy of the IMMoRTALS source code repository.

The environment we are running it in consists of a virtual machine installed using the image located at 
http://mirror.cs.pitt.edu/ubuntu/releases/16.04.4/ubuntu-16.04.4-server-amd64.iso with the default settings other than
an openssh server enabled.  

I would recommend a base system with 8GB of memory and 8 threads (via 8 cores or 4 cores with hyperthreading).  As we 
continue to add functionality it may be worth varying some of those values to see what provides decent execution time.

## Initial Online Setup

1. As a normal user, copy immortals_repo into their home directory so that the following structure matches up with the repository:  
    `~/immortals_repo/das`
2.  Navigate to the utilities within the extracted directory  
    `$ cd ~/immortals_repo/shared/utils`
3.  Execute the LL setup script. You may be asked one or more times for your root credentials to install software.
    `$ ./ll_setup.sh`
9.  Change to the immortals root directory
    `$ cd ~/immortals_repo`
9.  deploy the project
    `$ ./gradlew deploy`

## Deployment Test
A deployment test has been constructed that can be used to validate the deployment. It includes the previous 'smoke.sh' 
script execution and validation. It copies its parent repository to a location in /tmp/ to leave the current directory 
untouched.  

Execution Steps:  
1.  Navigate to the utilities within the extracted directory  
    `$ cd ~/immortals_repo/shared/utils`
2.  Execute the deployment test as follows:
`./deploymenttest.py --skip-basic --skip-full-deployment`

A successful result should output something like this:  
| Basic Installation         | NOT RUN |  
| Basic Build                | NOT RUN |  
| Full Installation          | NOT RUN |  
| Full Deployment            | NOT RUN |  
| Full Deployment Validation |  PASS   |  
| Baseline Marti             |  PASS   |  
| API Smoke Test             |  PASS   |
