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

## The BRASS AQL Server

The server is retrievable as an mvn-repo from here...

https://github.com/babeloff/mvn-repo

Documentation is in the README.md in that repository.

The principle input data is json and like this...

https://github.com/babeloff/aql-server-brass/blob/538b8ac87f1bc261305e4154f1224cfa727d5fa4/aql/src/aql/brass/data.clj#L15

{"martiServerModel"
   {"requirements"
    {"postgresqlPerturbation"
     {"tables"
      [{"table"  "cot_action"
        "columns"
         ["CotEvent_How"
          "CotEvent_ServerTime"
          "Position_PointCE"
          "Position_PointLE"
          "Position_TileX"
          "Position_Longitude"
          "Position_Latitude"]}
       {"table" "cot_detail"
        "columns"
        ["Position_PointHae"
         "CotEvent_Detail"
         "Position_TileY"
         "CotEvent_CotType"]}]}}}})

The source code for the server lives here:

https://github.com/babeloff/aql-server-brass

To call the server see the sample client.

https://github.com/babeloff/aql-server-brass/blob/master/aql/src/aql/brass/client.clj


# Context

svn/buildSrc/ImmortalsConfig/src/main/java/mil/darpa/immortals/config/extensions/AqlBrassConfiguration.java

Has the variable names
* mavenRepositoryUrl = "https://github.com/babeloff/mvn-repo/raw/master/releases";
* mavenGroupId = "babeloff";
* mavenArtifactId = "aql-server-brass";
* mavenVersion = "2018.03.20";
