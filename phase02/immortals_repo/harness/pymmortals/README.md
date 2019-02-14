# IMMoRTALS infrastructure Overview

## Folders

docker - This contains some docker build scripts for images used by the project (android_staticanalysis cannot be set up without the bbnAnalysis tarball)
docs - Miscellaneous documentation
immortals - Python module containing the nuts and bolts for automated testing and analysis of the platform_setup

### Configuration - infrastucture_configuration.json
Although daunting, this file provides a good check of the flexibility within the application.  Most of it is pretty straightforward and documented, but a few sections need some additional details to fully understand:

#### deploymentEnvironments
This section defines possible deployment configurations possible for applications.  The identifier for each one is tied to an associated python file that contains the implementation details.  They are bound together by their 'deploymentPlatform' value, which indicates what kind of applications can run on a given platform. In theory, as long as a set of deployment environments share the same deployment platform, they should be able to be used interchangeably by applications. These are bound in the **platformhelper.py** file.

#### deploymentApplications
This section lists application configuration details to be deployed to their matching 'deploymentPlatformEnvironment'.

### Configuration - configs/scenarios.json
This file provides definitions of scenarios to be run against the build results. Each 'deploymentApplication' is essentially a copy of the item with the matching "application" tag from deploymentApplications in *infrastucture_configuration.json* with various properties overridden.

### Configuration - configurationmanager.py
This is the only file that should interact with the configuration json files.  Although it seems like an unnecessary layer, it provides a few useful features when reading in the configurations:

* Ensures that files that should exist do exist
* Converts relative paths to absolute paths that can be used anywhere within the framework
* Generates the proper scenario application definitions using the base application configurations and scenario-specific application configurations

### Application-Platform Structure

The general structure of the scenario environment is as follows:

Application Instance
Platform Application
Deployment Platform
Deployment Environment

#### Deployment Platform
The Platform consists of a given runtime platform and encapsulates interface needed to interact with a given platform.  They implement the interfaces defined in *deploymentplatform.py*, and those are the only functions that should be used by the rest of the framework.

*android* captures this in *androidplaform.py*, which encapsulates some of the basic things necessary to interact with an android runtime environment.  

*java* captures this in *javaplatform.py*, which encapsulates some general java functionality

#### Deployment Environment
The Deployment Environment utilizes or overrides the behavior of the methods defined in the *Deployment Platform* to allow for different runtime environments.  Depending on features that may or may not be necessary, some functions in *androidplatform.py* may be placed in a separate function so they can be omitted from runtime behavior (such as deployment-specific things for android applications that do an analysis that does not require a full deployment)

*Android* has a few variants of this. Composition in combination with factoring out methods as necessary within utilized platform instances are used to combine functionality.  For example, in *androidplatform_dynamicanalysis.py*, the compatible POpen, call, and check_output calls in the *androidplatform_emulator.py* instance replaced with the docker POpen, call, and check_output calls to route system calls to the docker instance.  In addition to this, because the emulator used for dynamic analysis utilizes a pre-existing emulator and has some additional actions that need to be performed, some calls in *androidplatform_emulator.py* have been abstracted out within the file and are overridden in the instance defined within *androidplatform_dynamicanalysis.py*.

 One is the *androidplatform_docker.py* module, which modifies the platform calls as necessary to set up the docker environment and route the adb and emulator commands to the docker container.  The other is the *androidplatform_staticanalysis.py* module.  This utilizes  module and modifies the methods called as necessary to provide the desired functionality.

#### Platform Application
This is an interface that provides general application lifecycle methods that interact with the related platform.  It expects an application configuration as structured in *configurationmanager.py*, which is based on the application definitions in *infrastucture_configuration.json*  

#### Application Instance
This is simply a Platform Application with an associated configuration and unique identifier to be used in a scenario.
