## Samples for IMMoRTALS

The primary tool for looking at this data is the AQL/IDE.
It can be obtained from https://github.com/babeloff/fql

  git clone https://github.com/babeloff/fql.git
  cd fql
  export AQL_HOME=$(pwd)
  mvn package

The import_csv instructions are set up to work with relative paths.
Start the AQL/IDE from the root of this project (where this README.md is located).

  java -jar ${AQL_HOME}/target/fql-0.9-SNAPSHOT-maven-jar-with-dependencies.jar

The aql sample files are located in src/aql.
