CLIENT DOCUMENTATION
================

Configuration
----------------
The client can be configured in one of two ways:  

* The "Settings" menu within the client.  Note this does not contain all the possible settings.
* Copying an ATAKClient-Config.json file to the device's home directory.  See The "ATAKClient-Config.json" file in the client source code directory for more details.  

DFU Hardware Presence
----------------
Since we do not have much of the hardware the DFUs represent, we are using the existence of files that correlate to the module names to represent their presence.  See the "LocationProviderSimulated.json" file in the client source code directory for more details.  


Monitoring
----------------
There are three ways of monitoring the client:  

* Using the Google Maps UI. This will be automatically selected if available and it has not been overridden by the config file. It only tracks locations.  
* Using the non-Google Maps UI. This displays the number of location updates and number of images received from each client.  
* Pressing the "Save History" button to save a dump of the location/image history to the home folder.  
* Using adb logcat to monitor activity
