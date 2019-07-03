# IMMoRTALS Phase 3 README

## Risk Reduction 2 Release Notes

### Scenario 5
* Full adaptation using the DSL implemented
* Input DAU Inventory: [shared/tools/odb/resources/dummy_data/s5_dauInventory.xml](shared/tools/odb/resources/dummy_data/s5_dauInventory.xml)
* Input MDLRoot: [shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml](shared/tools/odb/resources/dummy_data/s5_input_mdlRoot.xml)

#### Known Issues
 * None

### Scenario 6
 * Partial Adaptation using the existing Schemas supported.
   Valid schemas:  
   "v0_8_7"  
   "v0_8_8"  
   "v0_8_9"  
   "v0_8_10"  
   "v0_8_11"  
   "v0_8_12"  
   "v0_8_13"  
   "v0_8_14"  
   "v0_8_16"  
   "v0_8_17"  
   "v0_8_19 

#### Known Issues
 * None


## Risk Reduction 1 Release Notes

### Scenario 5

* Full integration achieved end-to-end (Starting with **BBNEvaluationInput** and ending with **BBNEvaluationOutput**
  - The **MDLRoot** graph is currently not modified.
  - Mock results are placed in the **BBNEvaluationOutput**
* The implementation of actual adaptation functionality is in progress.

#### Known Issues

 * None

### Scenario 6

* Full integration achieved end-to-end (Starting with **BBNEvaluationInput** and ending with **BBNEvaluationOutput**
  - Mock results are placed in the **BBNEvaluationOutput**
* The implementation of actual adaptation functionality is in progress.

#### Known Issues

* There is currently an issue we are investigating where Scenario 6 sometimes has a failure during execution. Results (In this case an error) will be stored in the **BBNEvaluationOutput** vertex.


## Usage Details

See [bbn-swri-interface](ChallengeProblems/bbn-swri-integration.md)
