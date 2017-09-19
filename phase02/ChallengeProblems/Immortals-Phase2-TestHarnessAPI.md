# Unified Test Harness API

## Description
To orchestrate a cohesive adaptation scenario throughout IMMoRTALS, we have decided to continue forward in phase 2 with 
a unified API similar to what we did for phase 1. Many of the concepts are the same, but with additional flexibility 
in terms of how requirements and resources are described.  

In terms of the perturbation interface, the challenge problems will share the same perturbation and response JSON 
structures similarly to how they did for phase 1.  The documentation for each challenge problem with only specify the 
subset of options used for that challenge problem, and other fields can be ignored.

## Workflow 

The overall workflow is as follows:
![Submission Wokflow](Immortals-Phase2-TestHarnessAPI-Workflow.png)  

Th general idea is that the TH POSTs a submission to the TA in the form of a **SubmissionModel** wrapped in a **TEST_ACTION**
 object and receives back a **TestAdapterState** wrapped in a **ACTION_RESULT**. Whenever a meaningful state change occurs 
 with the DAS or SUT, an updated **TestAdapterState** is submitted to the status endpoint on the TH. When the TA reaches 
 a terminal done state, the result will be submitted to the done endpoint on the TH.

## Perturbation Submission
The root **SubmissionModel** contains the following fields: _atakliteClientModel_, _martiServerModel_, and 
_globalPerturbations_. These each contain _requirements_ and _properties_ that apply to the client, server, and both. 
These _requirements_ and _properties_ each contain a _general_ field that lists simple identifier-based attributes along with 
specific JSON-Objects for more advanced attributes.  

## Test Adapter State
The **TestAdapterState* return object is largely unchanged since phase 1. The most significant changes are as follows:
1. The test state has replaced the non-intermediary states with the **TestOutcome** values defined in the evaluation 
methodology document.
2. The primary indicator of success or failure is now a _verdictOutcome_ field in the **ValidationState** that consists of 
intermediary states along with the **Verdict** values defined in the evaluation methodology.
3. Each **TestDetail** object has an _intent_ element in it to pair the test with the intent it validates.  

The **AdaptationState** and **ValidationState** also each contain a _details_ field.  The intent of this is to provide 
a summary of what is happening in the DAS and SUT. As that is an ongoing task, those values are stubbed out as General 
JSON Objects for now.

### Sample TestAdapterState response value

```  
{
    "adaptation": {
        "adaptationStatus": "SUCCESS",
        "details": {}
    },
    "identifier": "PerturbationValidationInstanceIdentifier",
    "validation": {
        "executedTests": [
            {
                "actualStatus": "COMPLETE",
                "desiredStatus": "COMPLETE",
                "details": {},
                "intent": "SendLocation",
                "testIdentifier": "LocationSendTest"
            },
            {
                "actualStatus": "COMPLETE",
                "desiredStatus": "COMPLETE",
                "details": {},
                "intent": "SendImage",
                "testIdentifier": "ImageSendTest"
            }
        ],
        "verdictOutcome": "PASS"
    }
}  
```  

### TestAdapterState Data Dictionary

#### TestAdapterState  
__Type__: JSON Object  
__Description__: The overall status of the Test Adapter used for all done, status, and perturbation responses  

| Field      | Type            | Description                                                                      |  
| ---------- | --------------- | -------------------------------------------------------------------------------- |  
| identifier | str             | The internal identifier used to bind this perturbation to any artifacts produced |  
| adaptation | AdaptationState | The state of the DAS adaptation                                                  |  
| validation | ValidationState | The state of the validation                                                      |  

#### AdaptationState  
__Type__: JSON Object  
__Description__: The state of the DAS adaptation  

| Field            | Type                | Description                                     |  
| ---------------- | ------------------- | ----------------------------------------------- |  
| adaptationStatus | DasOutcome          | Indicates the current state of the DAS          |  
| details          | Generic JSON Object | A POJO object detailing the behavior of the DAS |  

#### DasOutcome  
__Type__: String Constant  
__Description__: The current state of the DAS  

| Values         | Description                                               |  
| -------------- | --------------------------------------------------------- |  
| PENDING        | DAS execution is pending (non-terminal)                   |  
| RUNNING        | DAS is executing analysis and augmentation (non-terminal) |  
| NOT_APPLICABLE | Baseline Submission - No DAS needed                       |  
| NOT_POSSIBLE   | An invalid perturbation has been submitted                |  
| SUCCESS        | Augmentation Successful                                   |  
| ERROR          | An error has occured                                      |  

#### TestDetails  
__Type__: JSON Object  
__Description__: The current state of a test execution  

| Field          | Type        | Description                                 |  
| -------------- | ----------- | ------------------------------------------- |  
| testIdentifier | str         | An identifier for the test                  |  
| currentState   | TestOutcome | The current state for the test              |  
| errorMessages  | List[str]   | Messages indicating the reasons for failure |  
| detailMessages | List[str]   | Messages indicating the reasons for success |  

#### ValidationState  
__Type__: JSON Object  
__Description__: The current state of intent satisfaction validation  

| Field          | Type             | Description                                       |  
| -------------- | ---------------- | ------------------------------------------------- |  
| verdictOutcome | VerdictOutcome   | The outcome of the intent preservation            |  
| executedTests  | List[TestResult] | The tests executed to support the verdict outcome |  

#### VerdictOutcome  
__Type__: String Constant  
__Description__: See LL Evaluation Methodology  

| Values       | Description                    |  
| ------------ | ------------------------------ |  
| PENDING      | The verdict outcome is pending |  
| PASS         | See LL Evaluation Methodology  |  
| DEGRADED     | See LL Evaluation Methodology  |  
| FAIL         | See LL Evaluation Methodology  |  
| INCONCLUSIVE | See LL Evaluation Methodology  |  
| INAPPLICABLE | See LL Evaluation Methodology  |  
| ERROR        | See LL Evaluation Methodology  |  

#### TestResult  
__Type__: JSON Object  
__Description__: The current state of an intent test validation  

| Field          | Type                | Description                                                             |  
| -------------- | ------------------- | ----------------------------------------------------------------------- |  
| testIdentifier | str                 | An identifier for the test                                              |  
| intent         | str                 | The intent the test validates                                           |  
| desiredStatus  | TestOutcome         | The desired outcome for the test. Will always be 'COMPLETE' for Phase 2 |  
| actualStatus   | TestOutcome         | The current state for the test                                          |  
| details        | Generic JSON Object | Details relating to the resultant test state.                           |  

#### TestOutcome  
__Type__: String Constant  
__Description__: The outcome of a test  

| Values         | Description                                              |  
| -------------- | -------------------------------------------------------- |  
| PENDING        | Indicates the specified action is pending (non-terminal) |  
| RUNNING        | Indicates the specified action is running (non-terminal  |  
| NOT_APPLICABLE | Indicates the specified action is not applicable         |  
| COMPLETE       | See LL Evaluation Methodology                            |  
| INVALID        | See LL Evaluation Methodology                            |  
| INCOMPLETE     | See LL Evaluation Methodology                            |  
| ERROR          | See LL Evaluation Methodology                            |  
