# IMMoRTALS Workshop Execution Instructions


## Startup:

1.  Start up __IMMORTALS Terminal__ from the start menu or execute the following command:  
    `terminator -l workshop`
2.  In the **PROCESSES** tab, execute the following commands in the **WEBGME** pane to start WebGME:  
    `cd projects/brass/webgme-immortals`  
    `./init/runit.sh`  
3.  In the **PROCESSES** tab, execute the following commands in the **MARTI ROUTER** pane to start Marti Router:  
    `sudo su`  
    `cd /root/immortals_repo/applications/server/Marti`  
    `java -jar Marti-immortals.jar`
3.  In the **WORKSPACE** tab, execute the following in the **TEST HARNESS** pane:  
    `sudo su`  
    `cd /root/immortals_repo/harness`  
4.  In the **WORKSPACE** tab, execute the following in the **TEST ADAPTER** pane:  
    `sudo su`  
    `cd /root/immortals_repo/harness`  

##DAS Execution:

### Quick Usage:
1.  Execute ONE of the following in the **TEST_HARNESS** pane:
    `./testing.py llds all fail-all`  # Best general demo scenario. Replace 'all' with 'challenge' to skip baseline-B displaying failure
    `./testing.py llds all custom`  # Pops up Vi to modify the default template, then starts the dummy server with it
2.  Execute the following in the **TEST ADAPTER** pane:  
    `./start_das.py`  
3.  Observe the following:
    - The execution status dashboard will pop up
    - The DAS will start chugging along if utilized
    - The emulators will start up
    - The Dashboard indicates completion (a second dashboard will pop up for the "all" scenario)
4.  When the dashboard indicates completion, Ctrl-C both windows (the DAS will take several seconds to complete shutdown) 

### Detailed Usage
Execution takes place in the **WORKSPACE** tab.  The **TEST HARNESS** pane is for execution of the dummy server, and
 the **TEST ADAPTER** pane is for execution of our system. The **TEST HARNESS** must be started prior to the **TEST ADAPTER**.  
To see the valid flow and deployment model options along with their descriptions, execute the following **TEST HARNESS** command:  
`./testing.py llds --help`  

The _fail-all_ scenario is the ideal demo for the following reasons:
1.  It fails bandwidth and trustedgps scenarios.
2.  The GPS still functions which allows the bandwidth graph to show failure during the baseline scenario.
3.  It forces the change to a simulated GPS (the real android one in the emulator doesn't move and is stuck at 0,0).
4.  It resizes images to be small enough to be below the CoT message size limits of real ATAK.

The custom scenario will pop up a vi terminal to modify a copy of the all-options-shown and the dummy server will be started with that.

I recommend the following combinations as they have been verified:

1.  In the **TEST HARNESS** pane, execute the following command to start the test harness, where FLOW and DEPLOYMENT_MODEL are valid options:  
    `./testing.py llds FLOW DEPLOYMENT_MODEL`  
2.  In the **TEST ADAPTER** pane, execute the following command to start the DAS:  
    `./start_das.py`  
3.  A number of things will happen, including the following:
    - The execution status dashboard will pop up
    - The emulators will start up
    - The DAS will start chugging along
4.  When the dashboard indicates completion, Ctrl-C the processes running in the TEST_HARNESS and TEST_ADAPTER windows.

###Potential Usage Issues:
* Corrupt Emulators  
  - By default, the emulators will be left running between sessions.  If you experience issues that may be due to
   emulator problems, execute the following command between executions in the **TEST HARNESS** pane to remove them and
    they will be recreated during the next execution:  
  `./testing.py vacuum all`  
* Hanging Processes
  - WAIT FOR THE START_DAS.PY COMMAND TO SHUT DOWN ON ITS OWN!  OTHERWISE, THIS WILL HAPPEN!
  - Restarting or killing java and python processes will fix this, or restarting the machine.

##WebGME Usage
The WebGME server should default to the URL _http://127.0.0.1:8955_, which is bookmarked and will automatically start when you start Chromium.

The models are located in the _/home/zeus/models_ directory.
