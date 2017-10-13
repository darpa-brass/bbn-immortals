# IMMoRTALS Test Adapter Instructions

## Overview
The IMMoRTALS Test Adapter consists of a copy of the IMMoRTALS source code repository.

The environment we are running it in consists of a qemu virtual machine running the image located at https://cloud-images.ubuntu.com/releases/16.04/release/ubuntu-16.04-server-cloudimg-amd64-disk1.img.

We start it with the following arguments, which may or may not be optimal, where immortals_seed.img is an image prepared with cloud-config to jumpstart the system:  
`qemu-system-x86_64 -m 8192 -smp 14 -hda ubuntu-16.04-server-cloudimg-amd64-disk1.img -hdb immortals-seed.img -enable-kvm -curses -device e1000,netdev=user.0 -netdev user,id=user.0,hostfwd=tcp::51022-:22,hostfwd=tcp::55555-:55555  -cpu host

Root is (ab)used and used for everything at the moment since it runs in a self-contained VM and some functionality requires root access.

## Initial Online Setup

There are more steps involved in this (as well as an inability to do it unattended) than I would like. I plan to simplify this a bit in the future.

1.  Within the host, switch to the root user and their home directory
2.  Download the repository tarball or files. so that the following structure matches up with the repository:  
    `~/immortals_repo/harness`
3.  Ensure you are the owner of the directory  
    `$ chown -R root:root immortals_repo`
4.  Navigate to harness within the extracted directory  
    `$ cd ~/immortals_repo/harness`
5.  Execute the environment examination script with the unattended flag which will also generate an installer file matching your system:  
    `$ ./prepare_setup.sh --unattended-setup`
6.  Execute the created setup script in such a way that it can pick up path changes:  
    `$ . ./setup.sh`
7.  When the request to accept the android license comes up, accept it.
8.  copy the generated immortalsrc file from the harness directory to ~/immortals_repo/
    `$ cp immortalsrc ../immortalsrc`
9.  Change to the immortals root directory
    `$ cd ~/immortals_repo`
9.  Build the project
    `$ ./gradlew buildAll`
  
## Smoke Test

### Description

The `smoke.sh` file is a convenience wrapper for running the smoke tests. It references the `harness/sample_override_file.json`  
which should be modified directly for this release to change the url or port of the Test Harness or Test Adapter. 

It simply runs through all the interactions between the TA and TH and verifies that the expected endpoints and data are being transferred.  Upon the failure of one, an exception will be thrown and the smoke test will exit.

The formated of the expected results is a '.'-joined sequence of dictionary keys. So for example, given an expectedy BODY like this:  
```json
{
    "adaptation.adaptationStatus": "NOT_APPLICABLE",
    "validation.verdictOutcome": "PASS"
}
```

You would expect the body to minimally contain the following:  
```json
{
    "adaptation": {
        "adaptationStatus": "NOT_APPLICABLE"
    },
    "validation": {
        "verdictOutcome": "PASS"
    }
}
```

### Usage

1.  Navigate to the harness directory as the root user:  
    `$ cd ~/immortals_repo/harness`
2.  Execute smoke.sh:
    `$ ./smoke.sh`
