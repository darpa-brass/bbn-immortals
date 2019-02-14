# BBN SwRI Integration Plan

The purpose of this document is to define the shared integration points for the challenge problems for Scenario 5 and Scenario 6. 

Discussions have led us to create an image that may serve one of two roles:

 * **Evaluation Target** - The virtual machine that the evaluation will take place on.
 * **Persistent Storage** - The virtual machine that must available to all executions that will be used to collect execution data for 
   post-evaluation analysis.

## Evaluation Target

* After image creation it will be offline with no internet connection
* It must have at least 4 CPU cores and 16 GB of memory. This is subject to change if bottlenecks are encountered during internal testing.
* Installation requires sudo access, so if the installation wil be unattended make sure sudo will be granted to the logged in user automatically.

### General Tips

* Make sure you are using bash as your login shell. Sometimes ssh and AWS-specific instances use lighter weight alternatives.
* Make sure your ~/.bashrc is being sourced properly. Depending on the parameters, SSH and Bash may not honor it.

### Creation

#### Steps
The following steps will prepare a system for use:

1.  Download the immortals source code.
2.  Navigate to the 'phase3' directory in the immortals root.
3.  Execute the "install.sh" script with the build flag to install dependencies and build the system (If the user requires a password for sudo):  
    `./install.sh --build`
4.  Execute the "build.sh" script to build the system:  
    `./build.sh`
5.  Shut down the system and save it to an image.

## Persistent Storage

The **Persistent Storage** system will utilize the same image as the **Evaluation Target** but will be started using different Parameters.

## OrientDB Structure

In order to facilitate challenge problem execution and validation, a formal structure must be defined. Given our input 
and output records will be well below the 10MB size where slowdown can become a problem, I propose the following structure:

### Evaluation Input

A root node class by the name **BBNEvaluationInput** is created to contain input data.

This node must contain one of two things depending on the challenge problem:

 * For Scenario 5, it must contain an Containment edge that points to an **MDLRoot** object that should be considered the input configuration to the problem.
 * For Scenario 6, it must contain a **jsonData**  property that contains a UTF-8 encoded JSON configuration.

An example creation of this on an OrientDB instance is as follows:

```sql
CREATE CLASS BBNEvaluationInput;
CREATE PROPERTY BBNEvaluationInput.jsonData STRING
```

### Evaluation Output

A root node class by the name **BBNEvaluationInput** must be created to contain output data.

The node must contain four properties as of this moment:

**jsonData** : JSON Data in a yet-to-be finalized format to be used to analyze the results of our adaptation.
**finalState**: A value that indicates the terminal state of the evaluation including errors.
**finalStateInfo**: A summary of the final state. This may be empty, and is mainly intended to provide simple error investigation when possible.

The intent is to contain complex data within the **jsonData** property, but to bubble up some basic summary data such as errors, general resultant state, and perhaps degree of failure for easy analysis.

### finalState

The finalState will not be updated until the perturbation has reached a terminal state. Although not set in stone, we currently expect the following possible values, and are treating it like an Enum:
 * AdaptationSuccessful
 * AdaptationNotRequired
 * PerturbationInputInvalid
 * AdaptationUnexpectedError
 * AdaptationUnsuccessful
 * AdaptationPartiallySuccessful

An example creation of this on an OrientDB Instance is as follows:

```sql
CREATE CLASS BBNEvaluationOutput
CREATE PROPERTY BBNEvaluationOutput.jsonData STRING
CREATE PROPERTY BBNEvaluationOutput.finalState STRING
CREATE PROPERTY BBNEvaluationOutput.finalStateInfo STRING
```

## Evaluation Execution 
The steps performed by the evaluator to execute evaluation will be as follows:

1.  The **Persistent Storage** must be started if not already running (executed in the root immortals folder):
    `./shared/tools.sh odbhelper start --persistence-only --use-default-root-password`
3.  The evaluator opens a shell on the AWS instance for the **Evaluation Target** and starts our evaluation as follows:
`bash ~/immortals_repo/phase3/start.sh --scenario <scenarioIdentifier> --odb-url <odbUrl> --odb-user <<odbUser> --odb-password <odbPassword> --odb-persistence-url <odbPersistanceUrl>`

Where the parameters are the following:

| Flag              | Value Placeholder    | Description                                                                                                    |
|:------------------|----------------------|----------------------------------------------------------------------------------------------------------------|
| --scenario        | <scenarioIdentifier> | The identifier for the scenario that is being executed. Valid values: '5' or '6'                               |
| --odb-url         | <odbUrl>             | The Url of the OrientDB instance and graph. example: 'remote:OrientDB.example.com:2424/GratefulDeadConcerts'   |
| --persistence-url | <persistenceUrl>     | The Url of the persistent storage                                                                              |
| --odb-user        | <odbUser>            | The OrientDB user name                                                                                         |
| --odb-password    | <odbPassword>        | The OrientDB user password                                                                                     |

As an example, if the **Persistent Storage** vm has an ip of '10.26.55.41', the _persistanceUrl would be the following:  
`remote:10.26.55.41:2424/BBNPersistent`

At this point, the following occurs within the _Evaluation Target_:  

1.  The system queries the OrientDB graph for data relating to the specified scenario and saves it to **Persistent Storage**
2.  The system attempts to resolve the perturbation
3.  The results are stored in the defined location and in **Persistent Storage**
4.  The script shuts down, indicating the completion of the evaluation session.

In the event of an error, it will be output to the console and an attempt to upload it to OrientDB will be performed if possible.

## Scenario 5 Details

The primary input and output artifact will be the MDLRoot object contained in the OrientDB repository. Specifics 
relating to this are documented in [bbn-swri-mdl-requirements](cp_05/bbn-swri-mdl-requirements.md)

## Scenario 6 Details

We propose that a specific node contains a property containing a JSON String.

The initial draft of this JSON object has the following three fields:

| Identifier        | Type      | Valid Values           | Description                                                                                                                   |
|:------------------|-----------|------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| initialMdlVersion | String    | ["V0_8_17", "V0_8_19"] | The initial MDL version. This will be taken from a list of predefined values agreed upon prior to evaluation                  |
| updatedMdlVersion | String    | ["V0_8_17", "V0_8_19"] | The new MDL version. This must be later than the initial version and will be taken from the same list of predefined values    |
| updatedMdlSchema  | String    | A valid MDL XSD schema | The new MDL schema document to upgrade to. It must be less than 1 MB It should follow appropriate JSON escaping as necessary  |

The **initialMdlVersion** is mandatory. The **updatedMdlVersion** and **updatedMdlSchema** are mutually exclusive.

### Examples

#### Input Example A - Existing XSLT Transformations

```json
{
    "initialMdlVersion": "V0_8_17",
    "updatedMdlVersion": "V0_8_19"
}
```

#### Input Example B - Creating New Transformations

Ellipses are used in place of a new MDL schema for readability.

```json
{
    "initialMdlVersion": "V0_8_19",
    "updatedMdlSchema": "<...>"
}
```

## Local Smoke Test

A local smoke test can be executed on the server from the 'phase3' directory after a prepared image has been saved. 

For Scenario 5 we are using [this](../../../../shared/tools/odbhelper/resources/dummy_data/scenario5_input_mdlRoot.xml) 
input configuration and [this](../../../../shared/tools/odbhelper/resources/dummy_data/scenario5_input_mdlRoot.xml) 
dau inventory.

For Scenario 6 we are using the above examples, with the schema for V0_8_19 as the updatedMdlSchema.

The steps are as follows (it requires two terminals, one for the OrientDB instance and another for the evaluation target):

1. From the immortals root directory start the OrientDB server and wait for it to finish loading:
   `./shared/tools.sh odbhelper start --use-default-root-password`
2. From the phase3 directory execute one of the following commands (There is an "all" option but there are some issues related to running the tests consecutively that we are still debugging)): 
   `./start.sh --local-test s5`
   `./start.sh --local-test s6a`
   `./start.sh --local-test s6b`

After it has finished you will see the results of the tests.



