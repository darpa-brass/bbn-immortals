Automated Testing
=================

Prerequisites
---------------------------------------

1.  Install the development environment.

Test Configuration
----------------------

### Client
All of the client functionality can be manipulated with the ATAKLite-Config.json file.  It is well documented, so for more information on how to use it check client/ATAKLite/ATAKLite-Configuration.json.  Currently,  
Hitting the "Save History" button on the UI will output the transaction data (sent data, received data) to a "ATAKLite-TransactionData.json" file that can be used to analyze the results.

### Server
As long as you start it from a directory that contains it's matching CoreConfig.xml, it should work fine.  Image filter manipulation without an ontology is a work in progress.  

Test Execution
--------------

1.  Run the "scenariorunner.py" script to start the process. "-e" or "-fe" will start up emulators a part of the process, and "-r" will actually run the basic test.

Test Validation
---------------

With the default configuration, after the test has run, das/testing/TEST_ENVIRONMENT/results.json will be created that will contain the test results.
