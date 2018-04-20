How to crate jar:
go to hddRASS folder.
$ gradle jar.


Go to hddRASS/build/libs
java -jar hddRASS-1.0-SNAPSHOT.jar -jsonFile /home/ubuntu/research/hddRASS/mutatescript.json

updated 03/26/2018:
added new parameters: stopOnSuccess and prioritizedClasses. I expect stopOnSuccess to be written by external entity.
I poll it at before beginning class level reduction and stop if it is set.




an example mutatescript.json file:


{
  "requiredValidators": [
    "com.bbn.marti.Tests.testSaTransmission",
    "com.bbn.marti.Tests.testImageTransmission"
  ],
  "buildTool": "gradle",
  "buildToolPath": "/root/arpit-marti/gradlew",
  "buildToolBuildParameter": "clean build",
  "buildToolValidationParameters": "clean validate",
  "applicationPath": "/root/arpit-marti/applications/server/Marti/",
  "sourceSubpath": "src",
  "buildFilePath": "build.gradle",
  "testResultPath": "/root/arpit-marti/applications/server/Marti/build/test-results/validate/",
  "testFileRegex": "",
  "BUILD_SUCCESS_STRING":"BUILD SUCCESS",
  "RUN_SUCCESS_STRING":"tests=\"2\" skipped=\"0\" failures=\"0\" errors=\"0\"",
  "PACKAGE_NAME" :"com.bbn.marti.Tests",
  "TEST_RESULT_PATH":"/root/arpit-marti/applications/server/Marti/build/test-results/validate/",
  "TEST_FILE_REGEX":".*Tests.*xml.*",
  "INTERMEDIATE_TEST_RESULT_OUTPUTPATH":"/home/ubuntu/results/temp/",
  "INTERMEDIATE_COMPILE_OUTPUT_PATH":"/home/ubuntu/results/temp/tempAntCompile.txt",
  "stopOnSuccess": false,
  "prioritizedClasses": [
      "mil.darpa.immortals.package.Class",
      "mil.darpa.immortals.otherpackage.OtherClass"
  ]

}


Good luck!!!!!!!!!!!!!!!!!!!!!!!