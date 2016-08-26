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
4.  Execute the configuration script
    `$ ./system_setup.sh`

## Offline DAS Initialization
1.  As root, navigate to ~/immortals_repo/das/
2.  Execute the following command to start the DAS and related components in the current terminal that will be used for monitoring DAS activity:
    `$ ./das.py`
4.  Submissions can be made via curl commands

## Offline DAS Submission
1.  The DAS server can receive submissions via a rest interface. A sample submission file is located in das/das-service/sample_das_input.txt
2.  For example, using curl, the following command would submit a configuration to the server for synthesis from within the immortals_root:
    `$ curl -H "Content-Type:application/json" -X POST --data-binary @das/das-service/sample_das_input.txt http://localhost:8080/bbn/das/deployment-model`
3.  Activity can be observed on the screen used to start das.py
