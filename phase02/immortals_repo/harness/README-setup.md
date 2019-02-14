# IMMoRTALS Setup Instructions

## Quick Start 
1. Look at the chart below and add any binaries or set any environment variables to use existing components meeting the
 version requirements.
2. Navigate to `harness/` and execute `./prepare_setup.sh` to produce the _setup.sh_ script (nothing will be touched on your system at this step)
3. Examine the _setup.sh_ script for catastrophic operations and run it once you are satisfied.
4. Examine and copy the produced `immortalsrc` to your home directory or the immortals root directory as `.immortalsrc`.
5. Logout and log back in.
6. Perform "./gradlew buildAll" in the immortals root directory.
7. Go to `harness/` and execute the following command to execute an end-to-end smoke test:  
    `./testing.sh orchestrate smoketest`

## Overview  
The script _prepare_setup.py_ is provided to automate installation and updating of components on a variety of systems.
It examines your current system without touching it, and produces a _setup.sh_ file to be examined and executed to
install necessary components.  Attempts are made to utilize currently installed versions when possible, but due to 
different paths and versions, this is not guaranteed. To remove the need to run the harness as root and to minimize 
potential security issues, a python3.6 virtualenv is created and given the capability to capture network traffic.

## Requirements
 - Ubuntu Linux 16.04 or or OS X system  
 - Python 3.4+  
 - Recent [Homebrew](https://brew.sh/) if on OS X. If packages that utilize Homebrew as a source are installed manually 
   prior to setup you can probably get by without this.  

## Environment Configurations

To help facilitate development ease, different combinations of dependency sets can be provided to the `prepare_setup.sh` 
script with the 

BASE - Only installs the bare requirements for Java projects
ANDROID - Includes BASE and Android tools
DATABASE - Includes BASE and database tools
DSL - Includes BASE and DSL tools
FULL - Includes everything
 
##Installed Components

| Component             | Version   | OSX Source        | Ubuntu Source             | Autodetection             | Env Config |
|:----------------------|:---------:|:-----------------:|:-------------------------:|:-------------------------:|:----------:|
| Java                  | 1.8.x     | Homebrew (oracle) | Apt (openjdk)             | JAVA_HOME                 | BASE       |
| Maven                 | 3.3.9     | Download          | Download                  | Path Binary (mvn)         | BASE       |
| Android SDK           | 25.3.0+   | Download          | Download                  | ANDROID_HOME              | ANDROID    |
| PostGreSQL            | 9.5.x     | Homebrew          | Apt                       | Path Binary (psql)        | DATABASE   |
| haskell-stack         | current?  | Homebrew          | Download                  | Path Binary (stack)       | DSL        |
| z3                    | 4.4.1     | Download          | Download                  | Path Binary (z3)          | DSL        |
| Python3               | 3.6.x     | Homebrew          | Apt (third party repo)    | Path Binary (python3.6)   | FULL       |
| Apache Jena Fuseki    | 2.3.1     | Download          | Download                  | FUSEKI_HOME               | FULL       |
| Gradle                | 2.8       | Download          | Download                  | GRADLE_HOME               | FULL       |
| Docker                | 17.x      | Homebrew          | Apt (third party repo)    | Path Binary (docker)      | FULL       |


## Recommendations
 - Manual installation of the Android SDK and Java are recommended if it is being installed on a workstation to avoid 
path issues and redundant files
 - See above for how your existing installations will be autodetected. The environment variables can be set for running
  the script and then unset as they will be added to the immortalsrc exports.
 - For all other components, the presence of their executables in the environment PATH should ensure they are utilized 
 if they are the proper version. They must remain in your path for IMMoRTALS to function.

##Instructions
1.  If you have existing components you would like to use, make sure they are made available as indicated in the chart
 above and the proper versions
2.  Install python3 if it is not yet installed
    - On a Mac, execute `brew install python3`.
    - On Ubuntu, execute `sudo apt-get install -y python3 python3-pip`.
3.  Change to the harness directory and execute `./prepare_setup.py`.
4.  Carefully examine the produced setup.sh script (You are responsible if you do not understand the commands it is 
using and it does something you do not intend!).
5.  Within the same directory execute it with `./setup.sh`.
6.  Examine the generated _immortalsrc_ file and move it to one of the following locations for autodetection or set 
 the filepath to the **IMMORTALSRC** environment variable:
    * `${HOME}/.immortalsrc`
    * `<immortals source root>/.immortalsrc`
7.  Examine the environment.json file in the location the components were installed and modify if necessary. 
 Specifically, you may have to change the following values if this is a headless installation:
    * "visualization.enableImmortalsDashboard" - `true` to `false`
    * "validationEnvironment.displayAndroidEmulatorGui": `true` to `false`
    

##Environment Configuration File
The file immortals_root/harness/pymmortals/root_configuration.json attempts to define all constant variables utilized 
by the application.  An `environment.json` override file will be written to the component installation directory 
specified during the execution of _prepare_setup.py_ to override the default LL Cluster configuration with one meant 
for a self-contained desktop system.

### Syntax
Curly brackets are not valid characters within this file because they are used to denote two 
substitution types:

 - Environment Variable Substitution - If a string contains a value surrounded by curly brackets that **is** prefixed by a dollar 
 sign, an attempt to replace it with a set environment variable will be made. If the specified environment variable is 
 undefined, an error will occur.  For example, near the top, "installationRoot" is defined as "${HOME}/.immortals/". 
 This replaces ${HOME} with your  defined "HOME" environment variable.
 
 - Parent/Neighbor Substitution - If a string contains a value surrounded by curly brackets that is **not** prefixed 
 by a dollar sign, an attempt will be made to substitute neighbor values or parent values with the same identifier. If 
 none can be found, an error will occur. For example, near the top of the file, "runtimeRoot" is defined as 
 "{immortalsRoot}/PRODUCTS". This inserts the value of the neighbor key "immortalsRoot" in place of  "{immortalsRoot}". 
 Similarly, examine "repositoryService.root". This defines its "root" key with the value 
 "{immortalsRoot}/knowledge-repo/knowledge-repo/repository-service/". This will search neighbor keys and then crawl up 
 the file until it finds a parent with a variable that matches "immortalsRoot", and ultimately settles on the top-level 
 value.
 
### Overrides
An override file provides a way to override most values in the root_configuration file without touching it. The
 override file contains a JSON map that contains defines period-separated key paths as keys and corresponding override
 values. The override file is optional, and the default configuration is set up for the LL Cluster Environment.
 THe override file is loaded if it exists in one of the following locations by priority:
1.  The filepath defined by the *IMMORTALS_OVERRIDES* environment variable
2.  '/test/data/environment.json' (via the directory defined in the root_configuration as "dataRoot")

By default, the emulators run with 512M of RAM and 2 CPU cores. this can be adjusted by modifying the values in the 
override file.