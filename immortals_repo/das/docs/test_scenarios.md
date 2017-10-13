TEST SCENARIOS
================

Client LatestSA Send/Receive
----------------

1.  Configure the client to send LatestSA at a specified interval with a different location.  
2.  Start the server.  
3.  Start the client(s).  

Expected result: The LatestSA data is sent and received by all clients and the server.  

Client Validation: The sending client's coordinates change to reflect their new location, and this change is indicated on any receiving clients.  
Server Validation: The server receives each LatestSA message send by the sending clients printed to stdout.  

Client Image Send/Receive
----------------

NOTE: Image sneding/receiving currently causes significant delays in LatestSA routing, especially if the image is large!  

1.  Configure the client to send images at a specified interval  
2.  Start the server  
3.  Start the client(s)  

Expected Result: The image data is send and received by all clients and the server.  

Client Validation: The receiving client will receive new images as indicated by their received image count going up.  
Server Validation: The server will receive new image data as Base64 encoded Strings printed to stdout.  
