# Validators

** NOTE: Italicized words in this document are fields from an IM_ANALYTICS_EVENT instance.  

Validators consume the stream of IM_ANALYTICS_EVENT's produced by the system. 
Each validator is provided a set of clients (THe _eventSource_'s) the validator should be run against. 
Provided clients not meeting the validator's criteria result in a failure.  More clients than expected meeting the validator's criteria also result in a failure.

## Event Types

These values are used in the _eventType_ field for validation.  

| eventType                     | Description                                                                                               |
|:------------------------------|:----------------------------------------------------------------------------------------------------------|
| FieldLocationUpdated          | Indicates _eventSource_ has received a location update from _eventRemoteSource_                           |
| MyImageSent                   | Indicates _eventSource_ has sent an image                                                                 |
| MyLocationProduced            | Indicates _evnetSource_ has produced a location                                                           |
| FieldImageUpdated             | Indicates _eventSource_ has received an image from _eventRemoteSource_                                    |
| CombinedServerTrafficBytes    | A periodic check (approximately once a second) of the total data usage between all clients and the server |

## Validator Types

### ClientEventValidator

Takes a set of _eventSource_'s and validates them and only them emit a specific _eventType_.  
Validation is terminated once all clients have emitted their expected events.

### ClientShareValidator

Takes a set of _eventSource_'s and validates that they all receive a specific _eventType_ from each other as _remoteEventSource_'s.  
Validation is terminated manually, and will continue validating the state until that point.

### ClientLocationSourceValidator

Takes a set of _eventSource_'s and validates the data of all _eventType_'s of **FieldLocationUpdated** are a coordinate with one of a set of predetermined how (source) values.
Validation is terminated manually, and will continue validating the state until that point.

# BandwidthValidator

This is used to determine whether or not a bandwidth threshold has been reached.
It performs the following steps:
1.  Waits for a message of any sort from all clients
2.  Waits half the maximum transmission interval of the clients (imageBroadcastIntervalMS or latestSABroadcastIntervalMS, whichever is greatest).
3.  Marks the bandwidth in KBytes/sec each second based on a sliding window equal to twice the maximum transmission interval (Treating measurements prior to measurement started as zero)
4.  Does this for two bandwidth intervals
5.  If any of these calculations were over the maximum bandwidth, the test fails. 


## Validators

| Validator Identifier              | Validator Type                | Details                                   |
|:----------------------------------|-------------------------------|-------------------------------------------|
| client-location-produce           | ClientEventValidator          | eventType: **MyLocationProduced**         |
| client-image-produce              | ClientEventValidator          | eventType: **MyImageSent**                |
| client-location-share             | ClientShareValidator          | eventType: **FieldLocationUpdated**       |
| client-image-share                | ClientShareValidator          | eventType: **FieldImageUpdated**          |
| client-location-source-trusted    | ClientLocationSourceValidator | Valid sources: 'm-r-p','m-r-e','m-r-t'    |
| client-location-trusted           | ClientLocationSourceValidator | Valid sources: 'm-r-p','m-r-e','m-r-t'    |
| client-location-source-usb        | ClientLocationSourceValidator | Valid sources: 'm-g-s-u'                  |
| client-location-source-bluetooth  | ClientLocationSourceValidator | Valid sources: 'm-g-s-b'                  |
| client-location-source-androidgps | ClientLocationSourceValidator | Valid sources: 'm-g'                      |
| client-location-source-manual     | ClientLocationSourceValidator | Valid sources: 'h-e-s'                    |
| bandwidth-maximum-validator       | BandwidthValidator            | eventType: **CombinedServerTrafficBytes** |


