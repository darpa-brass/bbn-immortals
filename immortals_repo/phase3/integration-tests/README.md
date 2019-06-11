# IMMoRTALS Phase 3 Integration Test Suite

The purpose of this project is to execute end to end tests in a manner consistent with the actual execution environment.

For portability it utilizes an embedded OrientDB server in place of an external one, but otherwise utilizes the knowledge repository and DSL in an identical manner to the actual execution.

## Test Definitions

Scenario 5 tests are defined in [s5_scenarios.json](src/integrationtest/resources/s5_scenarios.json) and Scenario 6 tests are defined in [s6_scenarios.json](src/integrationtest/resources/s6_scenarios.json).

## Test Definition Description

| Identifier                    | type                  | description                                                               |  
|-------------------------------|-----------------------|---------------------------------------------------------------------------|  
| shortName                     | Alphanumeric String   | A short unique name used to identify the scenario at a glance for tagging |  
| prettyName                    | String                | A long name used to provided clearer identification for reports           |  
| jsonInputPath                 | String                | An absolute path from the immortals root to the content that should be provided to SwRI for the **inputJsonData** field of the **Evaluation Input/Output** section of the [bbn-swri integration document](../../docs/CP/phase3/bbn-swri-integration.md)   |  
| expectedStatusSequence        | List\<String\>        | A single value (in a list for extended compatibility with other scenarios) indicating the expected final status of the evaluation as defined in the **currentState** section of the [bbn-swri integration document](../../docs/CP/phase3/bbn-swri-integration.md) |  
| expectedJsonOutputStructure   | JSON Object           | Any elements contained in this structure and their properties must contain a corresponding match in the **outputJsonData** field of the **Evaluation Input/Output** section of the [bbn-swri integration document](../../docs/CP/phase3/bbn-swri-integration.md)  |  

### expectedJsonOutputStructure Details

Basically, if the structure of this is followed parallel to the actual output, all objects in it must exist within the actual output. In addition to this, all attributes and their corresponding values must exist within the actual output. All lists should be considered ordered (TreeSets as an internal representation may be useful for unordered data).

## Integration Procedure

The plan is for contributors to scenario 5 or scenario 6 to add additional test definitions to the "scenarios" list of the test definition documents as necessary which will then be manually integrated into the integration tests, executed against the staging branch on SwRI's server, and then merged into master if they pass.