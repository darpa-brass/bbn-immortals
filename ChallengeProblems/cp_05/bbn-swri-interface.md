# BBN-SWRI Interface Specification

A majority of the information sent between BBN and SWRI will be in the MDL-compliant graph that exists in orientdb. 
However, additional information is needed to execute Flight Test Scenario 5. This serves as a means to document those 
requirements.

## Notes

 * Although we will do our best to provide a suitable replacement and consider all data available, unless properly documented 
 by SwRI in a central location we are aware of it is possible some properties may not be considered for DAU selection!

 * Measurements have been excluded from this draft until we receive a response to the [Measurements Clarification Issue](https://git.isis.vanderbilt.edu/SwRI/BBN/bbn-immortals/issues/50)
 
 * PortType Extensions have been exluded from this draft until we receive a response to the [PortTypes in DAU Inventory Issue](https://git.isis.vanderbilt.edu/SwRI/BBN/bbn-immortals/issues/51)

## Additional Metadata Coordination

We would prefer all information relating to the DAUs exist in the DAU inventory. However, since adding tags not 
 defined within the MDL specification (e.g. cost information) would result in  non-compliant MDL I propose a separate 
 XML structure that can be easily linked to or injected into a local DAU inventory.

## Unique Identifiers

It has been indicated by [Austin Whittington](https://git.isis.vanderbilt.edu/SwRI/BBN/bbn-immortals/issues/47) that 
using the Manufacturer and Model tags of a DAU or Module is the best way to indicate a specific piece of hardware, so we 
will rely on these to match metadata with their corresponding Module or DAU.
 
## Pre-Evaluation Metadata

This section covers all the data that is required prior to evaluation that should be provided along side the DAU Inventory

### Required Information

 * Module Functionality - This defines the top level purpose of a module. It is used to indicate modules tagged with the 
  same value provide the same general functionality (although other properties may still cause incompatibilities) 
  Identifier: **BBNModuleFunctionality**

 * Port Functionality - This defines the purpose of a port. It is used to indicate ports tagged with the same value 
 provide the same general functionality (although other properties may still cause incompatibilities) 
 Identifier: **BBNPortFunctionality**

 * DAU Monetary Cost - The cost to utilize a replacement DAU. This is used to provide an optimal solution. Identifier: **BBNMonetaryCost**

 * DAUConfiguration Time - How long it takes to reconfigure for the replacement DAU in hours. This is used to provide an 
 optimal solution. Identifier: **BBNDauConfigurationTime**
 TODO: Update label
 
### Pre-Evaluation Metadata Format

If the data cannot be directly inserted into the DAU inventory, I propose the following metadata format.  All non-MDL 
elements have been prepended with "BBN" for clarity indicating they have been created for BBN and there is no 
MDL-equivalent type. It is based on the DAU utilized in [BRASS_Scenario5_BeforeAdaptation.xml](https://git.isis.vanderbilt.edu/SwRI/challenge-problems/challenge-problems/Scenarios/FlightTesting/Scenario_5/BRASS_Scenario5_BeforeAdaptation.xml)

```xml
<!-- Container for all BBN-specific metadata -->
<BBNMDLMetadata>
    <!-- Lists all possible module types we may encounter -->
    <BBNModuleFunctionalitys>
        <BBNModuleFunctionality>SignalConditioner</BBNModuleFunctionality>
        <BBNModuleFunctionality>ThermocoupleConditioner</BBNModuleFunctionality>
    </BBNModuleFunctionalitys>
    <!-- Lists all possible PortFunctionality we may encounter -->
    <BBNPortFunctionalitys>
        <BBNPortFunctionality>SignalConditioner</BBNPortFunctionality>
        <BBNPortFunctionality>ThermocoupleConditionerExcitation</BBNPortFunctionality>
        <BBNPortFunctionality>ThermocoupleConditioner</BBNPortFunctionality>
    </BBNPortFunctionalitys>
    <!-- This would correspond to all DAUs within the test inventory -->
    <BBNDaus>
        <!-- This reflects the single DAU utilized in the above BeforeAdaptation example -->
        <BBNDau>
            <!-- The corresponding DAU manufacturer tag -->
            <Manufacturer>TTC</Manufacturer>
            <!-- The corresponding DAU Model tag -->
            <Model>DAU Model DBD</Model>
            <!--  DAU configuration cost in Dollars -->
            <BBNMonetaryCost>50000</BBNMonetaryCost>
            <!-- Hours necessary to reconfigure the DAU configuration -->
            <BBNDauConfigurationTime>12</BBNDauConfigurationTime>
        </BBNDau>
    </BBNDaus>
    <!-- This list of Modules would correspond to all Modules utilized by all DAUs that impact DAU substitution -->
    <!-- This assumes these traits and port availability are independent of the DAU they are utilized in -->
    <!-- If they are not, these may have to be nested individually within each DAU -->
    <Modules>
        <!-- 6-channel Simultaneous Sampling Signal Conditioner -->
        <Module>
            <Manufacturer>TTC</Manufacturer>
            <Model>MSCD</Model>
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
            <Manufacturer>TTC</Manufacturer>
            <Model>MTCD</Model>
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