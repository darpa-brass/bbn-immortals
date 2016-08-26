# Project Structure

## Initial Setup
The easiest way to set up your environment is to use the SetupHelper.py
script in the project root. Execute it to see instructions related to
usage.

## Gradle
There are currently two root gradle projects within the structure of this repository, each one having an associated
**build.gradle** and **settings.gradle** file.  They also contain subprojects that may contain their own **build.gradle**
file, but *not their own* **settings.gradle** They are the root folder and the shared/modules folder.  

## Maven Repositories
Except for the build environment repositories, all subprojects reference the following local checked-in maven repositories:  

* shared/REMOTE_CACHE_REPO  
* shared/IMMORTALS_REPO  

The *REMOTE_CACHE_REPO* contains all the external dependencies the projects depend on.  The *IMMORTALS_REPO* contains
all the modules used for code synthesis.  At the moment, this is generated locally as part of the build process for the root project.

## Global Build Configuration
Various configuration values used immortals-wide for building are imported from the shared/common.gradle file, including
android version details, publishing versioning, and default dependency versioning for items fetched from *IMMORTALS_REPO*.

## Root Project
The Root project is defined in the root of the repository It defines the following components that are defined in its
settings.gradle file:  

* server/Marti  
* client/ATAKLite  

### Root Project Gradle Usage  

Gradle consists of three phases:  

 1. __Initialization__ - The scripts are read in and projects are defined  
 2. __Configuration__ - All build scripts throughout the project are read and sanity tests (such as dependency validation) are run  
 3. __Execution__ - The command specified by the user is executed  

Due to the sanity checks executed during __Configuration__, it is impossible to successfully reach the execution step if
the *IMMORTALS_REPO* is unpopulated with dependencies on it. To work around this, an attempt to populate the
*IMMORTALS_REPO* is made during the __Initialization__ step if it does not exist or is empty.  This may fail in some
IDEs due to environment variable sourcing issues, so it is recommended to run "gradle" before importing it into an IDE.

The following commands are the most pertinent to the project:
"gradle" - Goes through the __Initialization__ and __Configuration__ steps
"gradle build" - Builds *Marti* and *ATAKLite*
"gradle clean" - Cleans the workspace, including wiping out *IMMORTALS_REPO*
"gradle generateImmortalsRepository" or "gradle gir" - Repopulates *IMMORTALS_REPO*, replacing anything currently in it
"gradle wipeImmortalsRepository" or "gradle wir" - Removes *IMMORTALS_REPO*

## Modules
The Modules project is defined in the *shared/modules* directory. It is used for shared resources to be defined in the
ontology such as the core libraries, DFUs, converters, and datatypes.  

It contains a build.gradle project that defines properties shared among all modules, a settings.gradle file that lists
all the modules, and the following subfolders:  

* *core* - Contains the core files used for ontology/synthesis functionality.  
* *datatypes* - Contains datatype modules  
* *dfus* - Contains dfu modules  
* *javatypeconverters* - Contains converters to be used during synthesis for java type conversion  

The datatypes, dfus, and javatypeconverters folders each contain modules with their own src directory and build.gradle
file. For the most basic modules the build.gradle file may be empty since the defaults defined in
shared/modules/build.gradle may be enough.  More advanced ones may contain additional dependencies or configuration
modifications.  Modules should never be referenced directly, but instead should be added as *IMMORTALS_REPO*
dependencies to projects that require them.  

### Modules Project Gradle Usage
Attempting to build in the modules directory will fail if *IMMORTALS_REPO* isn't populated since it has no knowledge
of the interdependencies between the different modules. The only command of real significance here is the
"gradle publish" command.  If it is run from the "modules" directory, all the modules will be published to
*IMMORTALS_REPO*. If it is run from within a specific module's directory (indicated by the presence of its own
"build.gradle" file, only that module will be published.
