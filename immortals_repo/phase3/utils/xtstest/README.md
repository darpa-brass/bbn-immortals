# XTS Tester

This tool is to assist in validating and debugging XTS translations.

## Requirements

It requires the following:  
 * python3  
 	- lxml  
 * The knowledge-repo tester tool built to the default location  
 * The environment variable `IMMORTALS_CHALLENGE_PROBLEMS_ROOT` to be set to a 
   local copy of SwRI's [challenge-problems repo](https://git.isis.vanderbilt.edu/SwRI/challenge-problems).  
 * The knowledge-repo xts-tester to be built  
 * Some external schemas copied to the challenge-problems-repo updated xsd directory


General setup of the requirements is as follows:
```

# Activate anaconda and install lxml and xmldiff
source <CONDA_DIR>/bin/activate
conda activate aql
pip3 install lxml xmldiff

# clone using SSH key
git clone --branch master --depth=1 --single-branch git@git.isis.vanderbilt.edu:SwRI/challenge-problems.git

# Export challenge problems root
export IMMORTALS_CHALLENGE_PROBLEMS_ROOT="`pwd`/challenge-problems"

# CD to the immortals root
cd <immortals_root>

# Copy some external schemas to the challenge-problems directory
cp knowledge-repo/cp/cp3.1/cp-ess-min/etc/schemas/v19/Tmats* ${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}/Scenarios/FlightTesting/Scenario_6/test_schemas/

## Build knowledge-repo translation service
cd knowledge-repo/cp/cp3.1/xsd-translation-service-test
mvn

# Go to the tester directory
cd ../../../../phase3/utils/xtstest/

# Get the saxon parser.
mkdir saxon
wget https://downloads.sourceforge.net/project/saxon/Saxon-HE/9.9/SaxonHE9-9-1-5J.zip -O saxon/saxon.zip
unzip -d saxon saxon/saxon.zip
rm saxon/saxon.zip
```

the script I use on my test image is as follows:
```
#!/usr/bin/env bash

set -e

source ~/.immortals/anaconda/bin/activate
conda activate aql
pip3 install lxml xmldiff
git clone --branch master --depth=1 --single-branch git@git.isis.vanderbilt.edu:SwRI/challenge-problems.git
export IMMORTALS_CHALLENGE_PROBLEMS_ROOT="`pwd`/challenge-problems"
cd immortals_rsync
cp knowledge-repo/cp/cp3.1/cp-ess-min/etc/schemas/v19/Tmats* ${IMMORTALS_CHALLENGE_PROBLEMS_ROOT}/Scenarios/FlightTesting/Scenario_6/test_schemas/
cd knowledge-repo/cp/cp3.1/xsd-translation-service-test
mvn
cd ../../../../phase3/utils/xtstest/
mkdir saxon
wget https://downloads.sourceforge.net/project/saxon/Saxon-HE/9.9/SaxonHE9-9-1-5J.zip -O saxon/saxon.zip
unzip -d saxon saxon/saxon.zip
rm saxon/saxon.zip
```

At the end of testing either errors will be displayed or a diff command to 
compare the expected with desired resuls if desired results have been created.
The default diff command is `meld`, but it can be overridden by setting the 
environment variable `IMMORTALS_DIFF_TOOL`.

Check the options with `./tester --help`. `./tester -s` will execute all the custom swri scenarios.
