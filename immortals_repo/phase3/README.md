# IMMoRTALS Phase 3 Usage Instructions

## Environment Setup

1.  Navigate to the repository root:  
    `cd ../`
2.  Execute the installation script generator:  
    `./shared/tools.sh installer`
3.  Examine the generated _setup.sh_ and execute it, granting superuser access as necessary:
    `./setup.sh`
4.  Copy the generated immortalsrc into the user's home directory as '.immortalsrc':  
    `cp immortalsrc ~/.immortalsrc`

## Build

1.  Navigate to the phase3 directory if not already in it:
    `cd phase3`
2.  Execute the build script:
    `./build.sh`

## Start

See [bbn-swri-integration](../docs/CP/phase3/bbn-swri-integration.md) for usage information or execute _start.sh_ with 
no parameters to see usage instructions.
