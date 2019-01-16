# BBN SwRI Integration Plan

The purpose of this document is to define the shared integration points for the challenge problems for Scenario 5 and Scenario 6. 

## Evaluation System Creation

The system will be created by BBN as an AWS instance provided to the evaluator for evaluation, which is the _Evaluation Target_

## Evaluation Environment

* It will be offline with no internet connection

* The system the _Evaluation Target_ is must have at least 4 CPU cores and 8 GB of memory. This is subject to change if 
bottlenecks are encountered during internal testing.

* Persistent storage is vital for us to properly do our own detailed evaluation of how our system performed. We propose 
two possible options:
    1. BBN provides the evaluator with another AWS instance that must be running and connectable from the  _Evaluation Target_ during all evaluations.
    2. The Evaluator provides a persistent storage endpoint (Which may simply be another OrientDB instance with a blank graph)
 
 
## Evaluation Execution 
The steps performed by the evaluator to execute evaluation will be as follows:

1.  The persistant storage solution will be started if not already running.
2.  An instance of our provided AWS instance is brought up (How does the evaluator know it is finished starting?)
3.  The evaluator opens a shell on the AWS instance and starts our evaluation as follows:
`bash ~/immortals_repo/phase3/start.sh --scenario <scenarioIdentifier> --odb-url <odbUrl> --odb-user <<odbUser> --odb-password <odbPassword> --odb-persistence-url <odbPersistanceUrl>`

Where the parameters are the following:

| Flag              | Value Placeholder    | Description                                                                                                    |
|:------------------|----------------------|----------------------------------------------------------------------------------------------------------------|
| --scenario        | <scenarioIdentifier> | The identifier for the scenario that is being executed. Valid values: '5' or '6'                               |
| --odb-url         | <odbUrl>             | The Url of the OrientDB instance and graph. example: 'remote:OrientDB.example.com:2424/GratefulDeadConcerts'   |
| --persistence-url | <persistanceUrl>     | The Url of the persistent storage                                                                              |
| --odb-user        | <odbUser>            | The OrientDB user name                                                                                         |
| --odb-password    | <odbPassword>        | The OrientDB user password                                                                                     |

At this point, the following occurs within the _Evaluation Target_:  

1.  The system validates a connection to persistent storage
2.  The system queries the OrientDB graph for data relating to the specified scenario
3.  The system attempts to resolve the perturbation
4.  The results are stored to a predefined location.
5.  Additional data is stored to the persistent storage location
6.  The script shuts down, indicating the completion of the evaluation session.

In the event of an error, it will be output to the console and an attempt to upload it to OrientDB will be performed if possible.

## Scenario 5 Details

The primary input and output artifact will be the MDLRoot object contained in the OrientDB repository. Specifics 
relating to this are documented in [bbn-swri-mdl-requirements](cp_05/bbn-swri-mdl-requirements.md)

## Scenario 6 Details

We propose that a specific node contains a property containing a JSON String.

The initial draft of this JSON object has the following three fields:

| Identifier        | Type      | Description                                                                                                                   |
|:------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| initialMdlVersion | String    | The initial MDL version. This will be taken from a list of predefined values agreed upon prior to evaluation                  |
| updatedMdlVersion | String    | The new MDL version. This must be later than the initial version and will be taken from the same list of predefined values    |
| updatedMdlSchema  | String    | The new MDL schema document to upgrade to. It must be less than 1 MB                                                          |

The **initialMdlVersion** is mandatory. The **updatedMdlVersion** and **updatedMdlSchema** are mutually exclusive.

TBD: Where will this data be stored?

### Examples

The values utilized in these do not reflect the final allowable values.

#### Input Example A - Existing XSLT Transformations

```json
{
    "initialMdlVersion": "V0_8_1",
    "updatedMdlVersion": "V0_8_4"
}
```

#### Input Example B - Creating new Transformations

Ellipses are used in place of a new MDL schema for readability.

```json
{
    "initialMdlVersion": "V0_8_1",
    "updatedMdlSchema": "..."
}
```

### Results Output

TBD
