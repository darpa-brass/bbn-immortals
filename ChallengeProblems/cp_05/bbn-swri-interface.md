# BBN-SWRI Interface Specification

A majority of the information sent between BBN and SWRI will be in the MDL-compliant graph that exists in orientdb. 
However, additional information is needed to execute Flight Test Scenario 5. This serves as a means to document those 
requirements.

## Pre-Evaluation Metadata

This section covers all the data that is required prior to evaluation that should be provided along side the DAU Inventory

### Additional DAU Information

 * Module Functionality - This defines the top level purpose of the module. It is used to know when one 
module can be substituted for another in terms of functionality.

 * Port Functionality - This defines the purpose of a port. It is used to know which ports of a given module can be utilized 
in place of ports on the faulty module.

**NOTE: Unless otherwise told we will strictly be using the functionality tags to validate full compatibility of a 
replacement DAU. If other things such as PortType, Port Pins, Direction, etc need to be considered this must be 
documented in a central location!
 
### Optimization

In order to provide an optimized solution, we need to know what information will be provided and how it should be 
considered in terms of optimality. Thus far we are tracking the following:

Monetary Cost - The cost to utilize the replacement DAU
Configuration Time - How long it takes to reconfigure for the replacement DAU

### Data Format

We (BBN) are flexible in terms of the format of the data, but I believe the most useful way to do this for us would be 
the following:

1. Define an overlay of sorts on top of specific MDL objects that adds new information to the defined DAU Inventory XML.
2. Do one of the following:  
    1. Insert those elements into the existing DAU Inventory XML.  
    2.  Define those elements in a separate XML file and provide a tool to insert the data automatically.  
 
 I have created a rough example of what 2ii might look like based on  a couple of the modules listed in 
[BRASS_Scenario5_BeforeAdaptation.xml](challenge-problems/Scenarios/FlightTesting/Scenario_5/BRASS_Scenario5_BeforeAdaptation.xml)

It should be easy to see how such data would be inserted into the existing DAU inventory manually or automatically.

Assume the DAU Test Inventory contains the following structure based on a proper MDL specification (Unrelated portions omitted):
```xml
<MDLRoot>
    <NetworkDomain>
        <Networks>
            <Network>
                <NetworkNodes>
                    <NetworkNode>
                        <SerialID>3258089</SerialID>
                    </NetworkNode>
                </NetworkNodes>
            </Network>
        </Networks>
    </NetworkDomain>
</MDLRoot>
```

All our elements are prepended with BBN to differentiate them from normal MDL.

Consider the following xsd that defines the world of all possible module functionality and port functionality for evaluation:
```xsd
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <xsd:simpleType name="BBNModuleFunctionality">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="SignalConditioner"/>
            <xsd:enumeration value="ThermocoupleConditioner"/>
        </xsd:restriction>
    </xsd:simpleType>
    <xsd:simpleType name="BBNPortFunctionality">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="SignalConditioner"/>
            <xsd:enumeration value="SignalConditionerExcitation"/>
            <xsd:enumeration value="ThermocoupleConditioner"/>
        </xsd:restriction>
    </xsd:simpleType>
</xsd:schema>
```

For this DAU portion I would expect something like the following:

```xml
<!-- Container for all BBN-specific metadata -->
<BBNMDLMetadata>
    <!-- This would correspond to all DAUs within the test inventory -->
    <BBNDaus>
        <!-- For the currently provided DAUs this BBNDau would correspond to a NetworkNode -->
        <BBNDau>
            <!-- That NetworkNode has the following SerialID wich corresponds to the NetworkNode with the description "Test article network for Scenario TA1" -->
            <!-- It's usage assumes the details in this BBNDau section are applicable to all DAUs with this SerialID -->
            <SerialID>3258089</SerialID>
            <!--  DAU cost in Dollars? -->
            <Cost>50000</Cost>
            <!-- Hours necessary to reconfigure it? -->
            <ConfigurationTime>12</ConfigurationTime>
        </BBNDau>
    </BBNDaus>
    <!-- This list of Modules would correspond to all Modules utilized by all DAUs that impact DAU substitution
    <!-- This assumes these traits are independent of the DAU they are utilized in -->
    <Modules>
        <!-- 6-channel Simultaneous Sampling Signal Conditioner -->
        <Module>
            <!-- The usage of the SerialID as the key assumes these details will be valid for all Modules with this SerialID -->
            <SerialID>3250020</SerialID>
            <BBNModuleFunctionality>SignalConditioner</BBNModuleFunctionality>
            <Ports>
                <Port>
                    <!-- The usage of the "Name" field assumes this is a durable identifier that will be the same for all instances of this port on all instances of this module -->
                    <Name>Ch1</Name>
                    <!-- This indicates this port is a signal conditioning port -->
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch2</Name>
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch3</Name>
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch4</Name>
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch5</Name>
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch6</Name>
                    <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
                </Port>
                <Port>
                    <Name>Ch1-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch2-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch3-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch4-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch5-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch6-EC</Name>
                    <BBNPortFunctionality>SignalConditionerExcitation</BBNPortFunctionality>
                </Port>
            </Ports>
        </Module>
        <Module>
            <SerialID>3251874</SerialID>
            <BBNModuleFunctionality>ThermocoupleConditioner</BBNModuleFunctionality>
            <Ports>
                <Port>
                    <Name>Ch1</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch2</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch3</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch4</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch5</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch6</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch7</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch8</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
                 <Port>
                    <Name>Ch9</Name>
                    <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
                </Port>
            </Ports>
        </Module>
    </Modules>
</BBNMDLMetadata>
```

The SerialID values (assuming they are unique as indicated) could then be utilized to insert the additional data into 
the XML.

## Evaluation Input

This section will define what is provided to BBN to kick of the evaluation.
