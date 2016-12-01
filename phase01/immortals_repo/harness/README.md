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
1. With a running server, modify the file located at ~/immortals_repo/harness/sample_submission.json
2. Submit it to the server, such as through the following curl command:
    `curl -X POST -d @sample_submission.json http://localhost:55555/submit --header "Content-Type:application/json"`
3. You will eventually get a reply back with the status of the code augmentation like the following:  
{  
    "adaptationResult": {  
        "adaptationStatusValue": "SUCCESSFUL",  
        "details": "Scaling factor used during synthesis: 0.015625",  
        "selectedDfu": "mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated"  
    },  
    "identifier": "S148062879802",  
    "synthesisFinished": true,  
    "validationFinished": false,  
    "validationResult": null  
} 
4.  You may then periodically submit a query for the identifier to determine the validation status. In this instance, that would be something like the following:
    `curl http://localhost:55156/query/S148062879802`
5.  It will either return what you got previously if it has not finished yet, or validationFinished will read true, with something similar to the following for the validationResult:
{  
    "results": [  
        {  
            "currentState": "PASSED",  
            "detailMessages": [  
                "ATAKLite-S148062879802_0-001-MyImageSent->",  
                "ATAKLite-S148062879802_0-000-MyImageSent->"  
            ],  
            "errorMessages": [],  
            "validatorIdentifier": "client-image-produce"  
        },  
        {  
            "currentState": "PASSED",  
            "detailMessages": [  
                "ATAKLite-S148062879802_0-001-[m-r-p]->",  
                "ATAKLite-S148062879802_0-000-[m-r-p]->",  
                "ATAKLite-S148062879802_0-000-[m-r-p]->",  
                "ATAKLite-S148062879802_0-000-[m-r-p]->"  
            ],  
            "errorMessages": [],  
            "validatorIdentifier": "client-location-trusted"  
        },  
        {  
            "currentState": "PASSED",  
            "detailMessages": [  
                "ATAKLite-S148062879802_0-001-MyLocationProduced->",  
                "ATAKLite-S148062879802_0-000-MyLocationProduced->"  
            ],  
            "errorMessages": [],  
            "validatorIdentifier": "client-location-produce"  
        },  
        {  
            "currentState": "PASSED",  
            "detailMessages": [  
                "ATAKLite-S148062879802_0-001<-FieldLocationUpdated-ATAKLite-S148062879802_0-000",  
                "ATAKLite-S148062879802_0-000<-FieldLocationUpdated-ATAKLite-S148062879802_0-001"  
            ],  
            "errorMessages": [],  
            "validatorIdentifier": "client-location-share"  
        },  
        {  
            "currentState": "PASSED",  
            "detailMessages": [  
                "ATAKLite-S148062879802_0-001<-FieldImageReceived-ATAKLite-S148062879802_0-000",  
                "ATAKLite-S148062879802_0-000<-FieldImageReceived-ATAKLite-S148062879802_0-001"  
            ],  
            "errorMessages": [],  
            "validatorIdentifier": "client-image-share"  
        }  
    ],  
    "testDurationMS": 72451  
}  

## Other Notes
The run artifacts (which take up about 100M each) are not automatically cleared from the DAS and there is not yet a
 rest command to clear them. They must be cleared manually by removing the directory corresponding to each run 
 identifier in ~/immortals_repo/PRODUCTS  
 
If any unexpected results are found, a log of the output on the DAS and the contents of ~/immortals_repo/PRODUCTS should be provided for analysis.

