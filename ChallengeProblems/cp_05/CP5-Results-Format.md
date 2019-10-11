# Flight Test - Scenario 5 Results Format

## General Description

The resultant MDL object is an adaptation of the original MDLRoot done in-place. A JSON document containing additional 
details is stored as follows: :

 - OrientDB - The **BBNEvaluationData**'s _outputJsonData_ attribute
 - Amazon S3 Bucket - The output set by the deployment script

## JSON Output Details

### Root jsonOutputData Description

| Label             | Description                                                                               |
|-------------------|-------------------------------------------------------------------------------------------|
| resultState       | The final state of the adaptation  (See the **AdaptationResult** below)                   |
| successPercentage | A positive decimal number from 0 to 1 that indicates the total success of the adaptation  |
| _detailedData      | Additional JSON data that is more detailed and is subject to change                      |

### AdaptationResult Description

| Label                         | Description                                           |
|-------------------------------|-------------------------------------------------------|
| AdaptationSuccessful          | Indicates a fully successful adaptation               |
| AdaptationNotRequired         | Indicates no adaptation was required                  |
| PerturbationInputInvalid      | Indicates the input was invalid                       |
| AdaptationUnexpectedError     | Indicates an unexpected error occurred                |
| AdaptationUnsuccessful        | Indicates adaptation was not successful               |
| AdaptationPartiallySuccessful | Indicates a partially successful adaptation           |

### DSL Metrics Description

The DAU swap driver outputs several metrics to describe the search space
explored by the execution of DSL programs which is stored in the 
_detailedData field. The metrics are written to a simple
JSON file containing a set of attribute-value pairs.

Although the search is constructed such that we will always find the globally
optimal solution, it's difficult and expensive to compute the size of the
entire search space in advance. Therefore, these metrics instead reflect the
space we actually explored, and the space we considered but were able to
condense and/or prune away without exploring.  

#### Fields

 * *required-port-groups*: We group identical ports in a DAU together to
   condense the search space. This is the total number of unique port groups in
   DAUs flagged for replacement in the request. The difference between
   *required-ports* and *required-port-groups* represents a condensing of the
   search space.

 * *daus-in-inventory*: This is just the number of DAUs in the inventory.

 * *candidate-sub-inventories*: To support generating DSL programs that
   describe different parts of the search space, we break the inventory down
   into various sub-inventories that conceptually represent a set of potential
   replacement DAUs. This is the total number of candidate sub-inventories we
   may have to explore. This is computed from the size of the inventory and the
   `--max-daus` option.

 * *ignored-sub-inventories*: We perform some *very* basic filtering to make
   sure that sub-inventories provide relevant functionalities to the request.
   This metric is the number of inventories that these sanity checks ruled out.
   (Note: improving this filtering step is an easy optimization that could be
   significant for large inventories.)

 * *explored-sub-inventories*: The number of sub-inventories we actually
   explored by a variational analysis; that is, by generating a corresponding
   DSL program and executing it.

 * *explored-ports*: The total number of ports in across all sub-inventories
   that were explored.

 * *explored-port-groups*: The total number of port groups (recall that a group
   is just a set of identical ports within a single DAU) across all
   sub-inventories that were explored.

 * *total-config-dimensions*: A variational analysis of a particular
   sub-inventory corresponds to an exhaustive search of all possible
   configurations and port assignments of the DAUs within that inventory. This
   is supported (and made efficient) by capturing differences among
   configurations locally and maintaining these differences while sharing
   commonalities throughout the execution of the DSL program. The variability
   within a variational analysis is described by a number of binary dimensions
   of variation. This counts the total number dimensions used for tracking
   different ways of configuring the attributes of a (subset of a) port group
   within a DAU.

 * *total-match-dimensions*: This counts the total number of dimensions used
   for tracking different ways of matching up required ports from the request
   to provided ports from the inventory DAUs. For a particular sub-inventory,
   the search space after condensing port groups is `2^(c+m)` where `c` is
   *total-config-dimensions* and `m` is *total-match-dimensions*. This is the
   exponentially sized space that the variational analysis helps us explore
   quickly.

## Output Files

Since Scenario 5 is iterative, exhausting the DAU inventory of all possible solutions, an _iteration_N_ tag is 
prepended to every output file to differentiate them, with N starting at index 0. The final value is expected to 
result in failure since no solution could be found.

The files are as follows:  

`dsl-swap-rules.json`
`dsl-swap-inventory.json`
`dsl-swap-request.json`
`dsl-swap-response.json`
`dsl-swap-metrics.json`

The output to **BBNEvaluationData** is stored 