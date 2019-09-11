# Schema Evolution Integration Plan

## Environment Details

Operating System: Ubuntu Linux Server 64-bit 18.04.5 (minimal install)  
CPU Cores: 8  
Memory: 32 GB  
Executing User: Non-Root

General Installed components:  
`
build-essential
curl
git
openssh-server
subversion
tmux
unzip
vim
wget
`

Specialized Installed Components:

| Component      | Version | Exports     | Path Binaries |
|:---------------|:-------:|:------------|:--------------|
| Fuseki         | 2.3.1   | FUSEKI_HOME | _None_        |
| Java (OpenJDK) | 1.8     | JAVA_HOME   | _Standard_    |
| Maven          | 3.3.9   | _None_      | "mvn"         |
| Python         | 3.5.2   | _None_      | "python3.5"   |

Exported Values

## System Configuration

The installation scripts used from Phase 2 will be reused for the initial component installation.

## Build

See [build.sh](../../../../phase3/build.sh) for the specific build script.

### Exported Values
 * JAVA_HOME
 * FUSEKI_HOME

## Execution

See [start.sh](../../../../phase3/start.sh) (and the utilized [start.py](../../../../phase3/start.py)) for the specific build script.

### Exported Values

 * JAVA_HOME
 * FUSEKI_HOME
 * ORIENTDB_TARGET
 * AQL_BRASS_SERVER_JAR

## Integration Tests

To facilitate end-to-end testing on SwRI's bamboo installation, I have documented the prodcedure for adding integration tests [here](../../../../phase3/integration-tests/README.md)