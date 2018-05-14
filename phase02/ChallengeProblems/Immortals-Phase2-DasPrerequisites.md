# DAS Prerequisites

This document details the initial setup the evaluator must perform prior to usage of the DAS. It is  not necessary 
for the initial building of the DAS.

## Prerequisites Provided For Evaluators

We have supplied the [cp_dependencies.json](../../immortals_repo/harness/cp_dependencies.json) file to describe external challenge 
problem dependencies. It contains a mapping of each challenge problem endpoint to the resources that are required 
for proper evaluation.

### Data Dictionary

#### DASPrerequisites  
__Type__: JSON Object  
__Description__: Describes preconfiguration required prior to evaluating the DAS or SUT  

| Field | Type                         | Description                                          |  
| ----- | ---------------------------- | ---------------------------------------------------- |  
| cp1   | ChallengeProblemRequirements | The prerequisites for evaluating Challenge Problem 1 |  
| cp2   | ChallengeProblemRequirements | The prerequisites for evaluating Challenge Problem 2 |  
| cp3   | ChallengeProblemRequirements | The prerequisites for evaluating Challenge Problem 3 |  

#### ChallengeProblemRequirements  
__Type__: JSON Object  
__Description__: Indicates the requirements for proper evaluation of a challenge problem  

| Field               | Type                               | Description                                                                 |  
| ------------------- | ---------------------------------- | --------------------------------------------------------------------------- |  
| androidEmulators    | List\[AndroidEmulatorRequirement\] | A set of android emulators that must be available for use by the DAS or SUT |  
| challengeProblemUrl | str                                | The corresponding Test Adapter URL for convenience                          |  

#### AndroidEmulatorRequirement  
__Type__: JSON Object  
__Description__: Defines the requirement that the described Android emulator be available for use by the DAS or SUT  

| Field                                 | Type        | Description                                                                                                                 |  
| ------------------------------------- | ----------- | --------------------------------------------------------------------------------------------------------------------------- |  
| androidVersion                        | int         | The android version that the android emulator must be running                                                               |  
| externallyAccessibleUrls              | List\[str\] | Urls that must be accessible by the emulator. Assume all subdomains are valid and all values with contain a port at the end |  
| superuserAccess                       | bool        | Whether or not the applications on the emulator must be granted the ability to use 'su'                                     |  
| uploadBandwidthLimitKilobitsPerSecond | int         | The upload limit that must be applied to the emulator to ensure consistent results                                          |  


## Prerequisites Required by DAS

../../immortals_repo/harness/cp_dependencies.json
### In order to function properly the DAS requires an input configuration that reflects a number of external variables. 
A documented sample of the expected input can be found at [sample_override_file.json](../../immortals_repo/harness/sample_override_file_commented.json). 
An undocumented sample that is legal JSON can be found at [sample_override_file.json](../../immortals_repo/harness/sample_override_file.json). 

