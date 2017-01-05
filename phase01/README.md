# IMMoRTALS Test Adapter Instructions

#### ADAPTATION_TIMEOUT:  15 minutes (TODO: Shorten this if the startup issues reported by Lincoln have been resolved)
#### TRIAL_TIMEOUT:  10 minutes

## Overview
The IMMoRTALS Test Adapter consists of a copy of the IMMoRTALS source code repository.

The environment we are running it in consists of a qemu virtual machine running the image located at https://cloud-images.ubuntu.com/releases/14.04/release/ubuntu-14.04-server-cloudimg-amd64-disk1.img.

We start it with the following arguments, which may or may not be optimal: `qemu-system-x86_64 -m 12288 -smp cpus=8,cores=4 -hda immortals.img -hdb immortals-seed.img -enable-kvm -cpu host -curses`

Root is (ab)used and used for everything at the moment since it runs in a self-contained VM and some functionality requires root access.

## Initial Online Setup
1.  Within the host, switch to the root user and their home directory
2.  Download the repository tarball or files. so that the following structure matches up with the repository: 
    `~/immortals_repo/harness`
3.  Ensure you are the owner of the directory  
    `$ chown -R root:root immortals_repo`
3.  Navigate to harness within the extracted directory  
    `$ cd ~/immortals_repo/harness`
4.  Execute the configuration script (The ". ./" syntax is very important to ensure the proper environment variables are applied within the script by the modified ~/.bashrc)  
    `$ . ./system_setup.sh`
5.  Restart or shutdown the image for offline usage  

## Offline DAS Initialization
The DAS does not utilize any data placed in the /test/data directory, so it can remain empty.

1.  As root, navigate to ~/immortals_repo/harness  
    `$ cd ~/immortals_repo/harness`
2.  Execute the following command to start the DAS and related components in the current terminal that will be used for monitoring DAS activity:  
    `$ ./start_das.py`
4.  Submissions can be made via http requests to the server

## Data Dictionary

### STATUS
* **Description** :  A Field Value indicating the current state of some functionality provided by the Test Adapter.  
* **Possible Values**:
  - **PENDING** - Execution or determination of need is pending
  - **NOT_APPLICABLE** - Not applicable to the specified action
  - **RUNNING** - The function is currently being performed
  - **SUCCESS** - The execution of the functionality completed as expected without issues or a negative resultant state
  - **FAILURE** - The execution of the functionality failed due to an error or a negative resultant state


### TA_STATE
* **Description** : A JSON-formatted high level overview indicating the state of an action submitted to the Test Adapter
* **Contents**:
    ```
    {
        "identifier": A String indicating the identifier related to the action performed,
        "adaptation": {
            "status": STATUS field value,
            "details": JSON-Formatted details relating to the adaptation state with the associated identifier
        },
        "validation": {
            "status": STATUS field value,
            "details": JSON-Formatted details relating to the current validation state
        }
    }
    ```

## DAS Endpoints

### validateBaselineApplication
* **URI**: http://brass-ta/action/validateBaselineApplication
* **Description** :  Submit a validation request to the  for the baseline application.  
* **Body Description**:  (optional)  The deployment model to base the environment on.  If omitted, the baseline environment will be used.
* **Body Example** (From _immortals_repo/harness/sample_submission.json_):
    ```
    {
        "server": {
            "bandwidth": 1000
        },
        "clients": [
            {
                "imageBroadcastIntervalMS": "2000",
                "latestSABroadcastIntervalMS": "1000",
                "count": 2,
                "presentResources" : [
                    "bluetooth",
                    "usb",
                    "internalGps",
                    "userInterface",
                    "gpsSatellites"
                ],
                "requiredProperties" : [
                    "trustedLocations"
                ]
            }
        ],
        "minimumRunTimeMS": 30000
    }
    ```
* **Return Value**: TA_STATE object containing the identifier.  Additional POSTs will be made to the TH with an updated TA_STATE object updated as events occur.
  
### adaptAndValidateApplication
* **URI**: http://brass-ta/action/adaptAndValidateApplication
* **Description** :  Submit an adaption request to the DAS and validate it in the corresponding environment configuration.
* **Body Description**:  (mandatory) The deployment model to submit to the DAS and base the environment on.
* **Body Example** (From _immortals_repo/harness/sample_submission.json_):
    ```
    {
        "server": {
            "bandwidth": 1000
        },
        "clients": [
            {
                "imageBroadcastIntervalMS": "2000",
                "latestSABroadcastIntervalMS": "1000",
                "count": 2,
                "presentResources" : [
                    "bluetooth",
                    "usb",
                    "internalGps",
                    "userInterface",
                    "gpsSatellites"
                ],
                "requiredProperties" : [
                    "trustedLocations"
                ]
            }
        ],
        "minimumRunTimeMS": 30000
    }
    ```
* **Return Value**: TA_STATE object containing the identifier.  Additional POSTs will be made to the TH with an updated TA_STATE object updated as events occur.
