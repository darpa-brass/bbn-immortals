# IMMoRTALS Tools

The tool commands are fully documented within their '--help' option which should be available at every level.

## installer

*Note: Without Android or VoltDB, this process only takes a couple minutes unlike during Phase 2!

The installer is used to install the dependencies on an Ubuntu 16.04 system. To get started, From the immortals root, 
execute the following and follow the instructions:  

`./shared/tools.sh installer`


## odbhelper

The odbhelper tool is intended to facilitate the configuration of mock OrientDB databases for testing. To use it:  

1.  On a system with Java and unzip (such as one configured with the **installer**), get the OrientDB executable:  
    `wget -O orientdb-community-2.2.24.zip https://orientdb.com/download.php?file=orientdb-community-2.2.24.zip&os=multi`
2.  Extract it to a location.  
    `unzip orientdb-community-2.2.24.zip`
3.  Export the directory location to an environment variable:
    `export ORIENTDB_HOME="\`pwd\`/orientdb-community-2.2.24"`
4.  Export the root password to be used for the OrientDB server:
    `export ORIENTDB_ROOT_PASSWORD=mySuperSecurePassword`
4.  Install python dependencies:  
    `sudo apt install python3-pip`
    `pip3 nstall pyorient lxml`
4.  From the immortals root directory, execute the odbhelper
    `./shared/tools.sh odbhelper start`
