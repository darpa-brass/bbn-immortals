# IMMoRTALS Runtime Harness Overview

##Overview
The harness consists of a copy of the IMMoRTALS source code repository.

The environment we are running it in consists of a qemu virtual machine running the image located at https://cloud-images.ubuntu.com/releases/14.04/release/ubuntu-14.04-server-cloudimg-amd64-disk1.img.

We start it with the following arguments, which may or may not be optimal: qemu-system-x86_64 -m 12288 -hda immortals.img -hdb immortals-seed.img -enable-kvm -cpu host -curses

Root is (ab)used and used for everything at the moment since it runs in a self-contained VM and the docker usage requires root access.

## Initial Online Setup
1.  Within the host, switch to the root user and their home directory.
2.  Download the repository tarball or files. so that the following structure matches up with the repository:
    `~/immortals_repo/harness`
3.  Ensure you are the owner of the directory
    `$ chown -R root:root immortals_repo`
3.  Navigate to harness within the extracted directory
    `$ cd ~/immortals_repo/harness`
4.  Execute the configuration script (The ". ./" syntax is very important to ensure the proper environment variables are applied within the script by the modified ~/.bashrc)
    `$ . ./system_setup.sh`

## Offline DAS Initialization
1.  As root, navigate to ~/immortals_repo/harness
2.  Execute the following command to start the DAS and related components in the current terminal that will be used for monitoring DAS activity:
    `$ ./start_das.py
4.  Submissions can be made via curl commands

## Scenario validation  
Please see immortals_repo/models/scenario/README.md

## Scenario artifacts  
immortals_repo/PRODUCTS contains the artifacts in the following directory structure:  
IMMORTALS_REPO - Shared maven repository that all the synthesized DFUs are published to  
I############# - Identifier generated for each scenario call based on the epoch time  
I#############/applications - Directory for modified applications
I#############/modules - Directory for synthesized DFUs
I#############/deployment_environment - All application runtime information is placed here. They also contain the stderr/stdout streams of the applications and their environments being brought up and can be used to monitor the current status of the scenario validation construction and execution.
