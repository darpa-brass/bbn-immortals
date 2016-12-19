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
4.  Submissions can be made via http requests to the server

## Scenario Execution
1. Within the immortals_repo, modify the file located at ~/immortals_repo/harness/sample_submission.json
2. Once Ready has been sent from the TA to the TH, that file can be submitted to the TA via the "/action/submitDeploymentModel" url
3. Submission of the file ~/immortals_repo/harness/base_submission.json will deploy the base client, which will not trigger augmentation.

## Other Notes
The run artifacts (which take up about 100M each) are not automatically cleared from the DAS and there is not yet a
 rest command to clear them. They must be cleared manually by removing the directory corresponding to each run 
 identifier in ~/immortals_repo/PRODUCTS  
