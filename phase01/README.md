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

## Optional Test Adapter Configuration
The Test Adapter can optionally be configured with an "env.json" file placed with the TEST_DATA_URI directory formatted as a IM_TA_CONFIGURATION object, and is currently not necessary for anything other than debugging.

## Offline DAS Initialization  
The DAS does not utilize any data placed in the /test/data directory, so it can remain empty.

1.  As root, navigate to ~/immortals_repo/harness  
    `$ cd ~/immortals_repo/harness`
2.  Execute the following command to start the DAS and related components in the current terminal that will be used for monitoring DAS activity:  
    `$ ./start_das.py`
4.  Submissions can be made via http requests to the server

## Data Dictionary  

### IM_TA_CONFIGURATION
__TYPE__: JSON

__Description__: Miscellaneous configuration options exposed for troubleshooting and calibration purposes

| Identifier                       | Type  | Description                                                                                                        |
|:---------------------------------|:-----:|:-------------------------------------------------------------------------------------------------------------------|
| validation.minimumTestDurationMS | int   | The minimum amount of time for a test to run. May vary depending on client count, send rate, system specs, etc.    |


### IM_STATUS  
__Type__: String Constant

__Description__ : Indicates the current state of some functionality

| Value             | Description                                                                                              |
|:------------------|:---------------------------------------------------------------------------------------------------------|
| PENDING           | Execution or determination of need is pending                                                            |
| NOT_APPLICABLE    | Not applicable to the specified action                                                                   |
| RUNNING           | The function is currently being performed                                                                |
| SUCCESS           | The execution of the functionality completed as expected without issues or a negative resultant state    |
| FAILURE           | The execution of the functionality failed due to an error or a negative resultant state                  |


### IM_ADAPTATION_STATE
__Type__: JSON

__Description__: The current state of an adaptation

| Field             | Type          | Description                               |
|:------------------|:-------------:|:------------------------------------------|
| adaptationStatus  | IM_STATUS     | The current status of the adaptation      |
| details           | JSON          | Internal data related to the adaptation   |


#### IM_TEST_DETAILS
__Type__: JSON

__Description__: The details for an executed test

| Field             | Type      | Description                                   |
|:------------------|:---------:|:----------------------------------------------|
| testIdentifier    | String    | The identifier for the test                   |
| expectedStatus    | IM_STATUS | The expected resultant status of the test     |
| actualStatus      | IM_STATUS | The current status of the test                |
| details           | JSON      | Internal data related to the test execution   |


### IM_VALIDATION_STATE
__Type__: JSON

__Description__ : A JSON-formatted resultant state for an individual test

| Field                     | Type              | Description                                                                                               |
|:--------------------------|:-----------------:|:----------------------------------------------------------------------------------------------------------|
| executedTests             | IM_TEST_DETAILS   | The list of tests executed                                                                                |
| overallIntentStatus       | IM_STATUS         | Indicates whether or not all tests with a SUCCESS or FAILURE expectedStatus had a matching actualStatus   |


### IM_TA_STATE  
__Type__: JSON

__Description__ : A high level overview indicating the state of an action submitted to the Test Adapter.

| Identifier    | Type                      | Description                                                               |
|:--------------|:-------------------------:|:--------------------------------------------------------------------------|
| identifier    | String                    | An identifier used identify the action the state is associated with       |
| adaptation    | IM_ADAPTATION_STATE       | Adaptation details                                                        |
| validation    | IM_VALIDATION_STATE       | Validation details                                                        |
| rawLogData    | IM_ANALYTICS_EVENT list   | Raw log data that will only include the "combinedServerTrafficBytes" type |


### IM_ANALYTICS_EVENT
__Type__: JSON

__Description__ : A message format that all data used for validation and analysis is transferred in

| Identifier        | Type              | Description                                                                                                                   |
|:------------------|:-----------------:|:------------------------------------------------------------------------------------------------------------------------------|
| type              | String Constant   | Indicates what kind of event has occurred                                                                                     |
| eventSource       | String            | Identifier for immediate source of the event                                                                                  |
| eventTime         | long              | The epoch time at which the event occurred                                                                                    |
| eventRemoteSource | String            | An optional identifier to indicate the original source of the data (for example, if one client sends a message to another)    |
| dataType          | String            | Java identifier for the serialized data type                                                                                  |
| eventId           | long              | A numberical identifier to uniquely identify the event among all the events sent by the eventSource                           |
| data              | String            | Event data. May be JSON encoded if the dataType is not a primitive                                                            |


## Test Adapter Endpoints  

### validateBaselineApplication  
* __URI__: http://brass-ta/action/validateBaselineApplication
* __Description__ : Submit a validation request to the  for the baseline application.  
* __Body Description__: (optional)  The deployment model to base the environment on.  If omitted, the baseline environment will be used.
* __Body Example__ (From _immortals_repo/harness/sample_submission.json_):
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
        ]
    }
    ```
* __Return Value__: IM_TA_STATE object containing the identifier.  Additional POSTs will be made to the TH with an updated IM_TA_STATE object updated as events occur.
  
### adaptAndValidateApplication  
* __URI__: http://brass-ta/action/adaptAndValidateApplication
* __Description__ : Submit an adaption request to the DAS and validate it in the corresponding environment configuration.
* __Body Description__: (mandatory) The deployment model to submit to the DAS and base the environment on.
* __Body Example__ (From _immortals_repo/harness/sample_submission.json_):
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
        ]
    }
    ```
* __Return Value__: IM_TA_STATE object containing the identifier.  Additional POSTs will be made to the TH with an updated IM_TA_STATE object updated as events occur.

## Test Harness Endpoints

### done
* __URI__: http://brass-th/action/done
* __Description__ : Returns the results of the completed operations of the last TEST_ACTION message and indicates a terminal state for the associated TEST_ACTION
* __Body__: IM_TA_STATE object


## DAS_INFO Logged Data
 - Changes to the current IM_TA_STATE object
 - IM_RAW_VALIDATION_DATA object upon completion of validation
 

## General 

### Bandwidth data 
The bandwidth data will be logged in the rawLogData parameter of the IM_TA_STATE after validation completes. Events associated with can be identified with the following details and the mandatory timestamp (an initial traffic point of zero KBytes will be sent initially):

* type: "combinedServerTrafficBytes"
* eventSource: TBD
* dataType: TBD (but a whole number of some sort)

## Sample Execution (How we are doing it)

1.  Extract a copy of the repository to the dummy Test Harness machine.
2.  With a prepared Test Adapter image, (See Initial Online Setup above), start the machine hosting it, but do not execute the startup script.
3.  Set up the host files on the TA and TH to point to one another
4.  Create the "/test" and "/test/data" directory on the TA.
5.  Execute the following command from the _immortals_root/harness_ directory on the Test Harness to start the dummy server:  
    `./testing.py llds -tha 0.0.0.0 -thp 44444 -tap 55555 all`
6.  This will start a "Lincoln Labs Dummy Server" on 'Test Harness Address' 0.0.0.0 (indicates all network interfaces) using the 'Test Harness Port' 44444, 'Test Adapter Port' 55555, and run 'all' tests (Baseline A, Baseline B, and Challenge) upon a ready signal.
7.  Switch to the Test Adapter machine, and from the "immortals_root/harness" directory, execute the following command:
    `./start_das.py -taa 0.0.0.0 -thp 44444 -tap 55555`
8.  If all goes well, events should be occurring on the Test Adapter console and _immortals_root/harness/ll_dummy_server.log_ should display the network events as they occur.
