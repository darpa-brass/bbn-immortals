ENVIRONMENT INSTALLATION
================

These instructions will guide you on how to set up the dev environment for immortals.  

Tips
----------------

### OS X
I recommend using Brew for package management.  It makes dealing with the dependencies far more easier and organized.  


### General
After installing the Android SDK, the “android” tool can be used to install SDK components. if it isn’t added to your PATH, it is located in the “tools” folder of the Android SDK.  

Build and Test Environment Setup
----------------

Steps:

1.  Install the Android SDK (this can be done via “brew install android-sdk” on a Mac with Brew)  
2.  Set the “ANDROID_HOME” environment variable to your Android SDK root folder  
3.  Install a Gradle version between 2.2 and 2.9 (Android doesn’t support higher than that) (This can be done on a Mac with Brew via “brew install homebrew/versions/gradle28”)  
4.  run the “setup_sdk.sh” script in the client/ATAKLite folder to install android sdk dependencies (It is advised you look at this and any script you run on your computer so you know what it is doing)  
5.  Install python 2.7ish.  