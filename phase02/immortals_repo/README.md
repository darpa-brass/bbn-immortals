# IMMoRTALS Repository README

## Quick Start

For most users who do not need to run the entire DAS, the following should suffice:

1. Install JDK 8 (Oracle or OpenJDK) and set your `JAVA_HOME` environment variable to the installation directory.
2. Install maven 3.x and make sure the `mvn` executable it is in your PATH
3. If you need to build android components, you must set up the Android SDK (everything else will build fine without it)
    1. Extract the [Android SDK Tools](https://developer.android.com/studio/index.html#downloads) to a location. 
    **The location containing the extracted "tools" folder will be the sdk root and must be writeable 
    by the user!**
    2.  Set your `ANDROID_HOME` to the directory containing the "tools" folder.
    3.  Within `ANDROID_HOME`, execute the following to read and (assuming you choose to) accept the licenses:
        `./tools/bin/sdkmanager --licenses`
3. From the immortals_root with the proper exports and executables in the path, perform the following:  
    `./gradlew build`  

## Immortals Build System Overview

### General Gradle Sequence

When you execute `./gradlew build` on any Gradle project, the following occurs:
1.  The buildSrc project is built (if it exists).  
2.  The build configuration is evaluated and validated.  
3.  If validation is successful, the project and subprojects are built.  

### IMMoRTALS buildSrc directory

The buildSrc projects is used in immortals to define global configurations used by the build itself, different DAS 
components, built artifacts, and applications or libraries to be used by the DAS. It produces two artifacts:

* ImmortalsConfig
    - This jar is published to the IMMORTALS_REPO and contains a POJO of the DAS configuration.  The defaults are 
    defined within the source code.
     - A JSON copy of this is written to the root of the immortals directory as __immortals_config.json__ for 
     language-agnostic use.
     - __immortals_config.json__ is also written to the application directories to define the IMMORTALS_REPO location 
     in a path and environment independent manner. In a real world scenario, this would point to a static DAS. All other 
     information from the file is ignored.
     - The DAS configuration can be overridden during execution by setting the environment variable 
     **IMMORTALS_OVERRIDE_FILE** equal to the path of a modified configuration file prior to prior to execution of 
     anything that uses the ImmortalsConfig jar.
* ImmortalsGradle
     - This jar contains gradle plugins to simplify the construction of DFUs and facilitate analysis

### Build Workflow

Below is a sequence diagram of the build sequence. Each participant is an isolated component with a manually 
constructed build dependency sequence, and cross-communication between the components at runtime is handled through 
artifacts published to the local repository such as the **ImmortalsConfig** artifact or __immortals_config.json__ file.
 
![Submission Wokflow](docs/architecture/build_sequence.png)


## DAS Execution (WIP)

The Overall system including the evaluation infrastructure consists of the following:

![Deployment Model](docs/architecture/deployment_diagram.png)

### Start Sequence
The start sequence is as follows:
1.  Start Test Harness
2.  Start Das
    1.  Start Fuseki
    2.  Start Repository Service
    3.  Start Das Service
3.  Start Test Adapter

Once the Test Adapter is successfully started, it notifies the Test Harness, which begins the evaluation process.

The best way to set up a local environment is to copy the immortals_config.json file, modify values necessary for your 
environment, remove the unmodified values, and add this to your normal terminal or IDE exports. This will override 
your local values while allowing the defaults (which may change) to be loaded from the backing classes.

## Root Project Build Instructions
Build Command: `./gradlew build`  
Clean Command: `./gradlew clean`  

## Application Instructions:
Dependent on the root project being built.  
Located in their corresponding directories relative to the project root. Commands assumed to be run from their own directories.

### Marti Router
#### Building
Directory: _applications/server/Marti_  

Build Command: `../../../gradlew build`  
Clean Command: `../../../gradlew clean`  
Produced Appliation: "Marti-immortals.jar"  

Application Dependencies:  
 * **Marti-Config.json** in the execution directory  

#### Testing and Validation (Not applicable to all projects!)
Command: `../../../gradlew clean validate`  

Output Directory: build/test-results/validate/  

Details:  
The build process will produce a list of all tests in _build/test-results/test/TEST-com.bbn.marti.Tests.xml_. To run a subset of these tests perform something similar to the following command:  
`../../../gradlew clean validate --tests com.bbn.marti.Tests.testImageSave --tests com.bbn.marti.Tests.testImageTransmission`  

The _--tests_ argument can be used multiple times to specify a specific test or specify a filter to dictate which tests to run. Please see the [Java Test](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_test) section of the [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html) for more information.


### ATAKLite
Directory: _applications/client/ATAKLite_  

Build Command: `../../../gradlew build`  
Clean Command: `../../../gradlew clean`  
Produced Appliation: **ATAKLite-debug.apk**  
Application Dependencies:  
 * **env.json** in _/sdcard/ataklite/_ on the android filesystem  
 * **ATAKLite-Config.json** in _/sdcard/ataklite/_ on the android filesystem  

## Tips and Tricks
 * **shared/IMMORTALS_REPO** contains the dependencies (DFUs, utilities, etc) produced by the project and should be regenerated by performing a `./gradle clean build` from the root whenever the repository is updated.
 * Since artifacts produced by the project are published to the local IMMORTALS_REPO every build, the local maven cache should __NOT__ be enabled in any gradle scripts. Gradle will manage its own cache that knows not to cache the local repository.
