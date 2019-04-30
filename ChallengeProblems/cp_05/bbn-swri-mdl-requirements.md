# BBN-SWRI Interface Specification

A majority of the information sent between BBN and SwRI will be in the MDL-compliant graph that exists in OrientDB. 
However, additional information is needed to execute Flight Test Scenario 5. This serves as a means to document those 
requirements.

## Notes

 * Although we will do our best to provide a suitable replacement and consider all data available, unless properly documented 
 by SwRI in a central location we are aware of it is possible some properties may not be considered for DAU selection!

 * Measurements have been excluded from this draft until we receive a response to the [Measurements Clarification Issue](https://git.isis.vanderbilt.edu/SwRI/BBN/bbn-immortals/issues/50)
 
 * The general idea for how we are handling additional data that does not follow the MDL spec is as follows:
   1.  It is defined as an element of the GenericParameter
   2.  During our internal processing, it is brought into the parent object (such as a port or DAU), replacing any 
       existing data that shares the same element label
 
## DAU Inventory

The DAU Inventory consists of a number of **NetworkNode** child elements within a top level element with the identifier **DAUInventory**. For example, the following is a list of three DAUs (contents omitted):

```xml
<DAUInventory>
  <NetworkNode>
  </NetworkNode>
  <NetworkNode>
  </NetworkNode>
  <NetworkNode>
  </NetworkNode>
</DAUInventory>
```

It will be provided in an XML format prior to test execution. It will also be hosted in the same OrientDB as the faulty Flight Test Configuration to simplify integrating DAUs into the configuration.

A sample we are using for internal testing based on the initial example gone over during the last PI Meeting can be found [here](../../../../shared/tools/odbhelper/resources/dummy_data/s5_dauInventory.xml)

## DAU Inventory Metadata

This section covers all the data that is required prior to evaluation that should be provided within DAUs inside the DAU Inventory.

### Required Information

 * Module Functionality - This defines the top level purpose of a module. It is used to indicate modules tagged with the 
  same value provide the same general functionality (although other properties may still cause incompatibilities) 
  Identifier: **BBNModuleFunctionality**

 * Port Functionality - This defines the purpose of a port. It is used to indicate ports tagged with the same value 
 provide the same general functionality (although other properties may still cause incompatibilities) 
 Identifier: **BBNPortFunctionality**
 
 * Port Sample Rates - In the DAU Inventory, there will be one or more individual values that are valid sample rates 
   for each applicable port. These are defined by a number of **SampleRate** elements, each consisting of no more than 
   a single integer as the body that represents a valid sample rate.
 
 * Port Data Length - The length of that data being sent by the port. Identifier: **DataLength**

 * DAU Monetary Cost - The cost to utilize a replacement DAU. This is used to provide an optimal solution. Identifier: **BBNDauMonetaryCost**

 * Excitation Port Presence - As indicated in [this](https://git.isis.vanderbilt.edu/SwRI/bbn-immortals/issues/55) 
   issue it has been decided to simply use a flag to associate excitation ports. This is optional, and the absence 
   indicates no excitation port is present. Identifier: **ExcitationPortIsPresent**
 
### Pre-Evaluation Metadata Format

This is a significantly trimmed down version of the DAU utilized in [BRASS_Scenario5_BeforeAdaptation.xml](https://git.isis.vanderbilt.edu/SwRI/challenge-problems/challenge-problems/Scenarios/FlightTesting/Scenario_5/BRASS_Scenario5_BeforeAdaptation.xml) 
to provide an example of the metadata usage. Only a single DAU, Module, and Port is being used as an example. All DAUs, 
Modules, and Ports within the DAU inventory are expected to contain this additional information.

```xml
<NetworkNode ID="TTCDAU">
  <Manufacturer>TTC</Manufacturer>
  <Model>DAU Model TBD</Model>
  <GenericParameter>
    <!--  DAU configuration cost in Dollars -->
    <BBNDauMonetaryCost>50000</BBNDauMonetaryCost>
    <!-- DAU Opportunity Cost in a TBD unit -->
    <BBNDauOpportunityCost>1234</BBNDauOpportunityCost>
    <!-- Is this DAU in need of replacement? -->
    <BBNDauFlaggedForReplacement/>
  </GenericParameter>
  <InternalStructure>
    <Modules>
      <Module ID="module-2">
        <GenericParameter>
          <BBNModuleFunctionality>ThermocoupleConditionerModule</BBNModuleFunctionality>
        </GenericParameter>
        <Manufacturer>TTC</Manufacturer>
        <Model>MTCD</Model>
        <Ports>
          <Port Enabled="true" ID="Module2-Ch1" Index="1">
            <GenericParameter>
              <BBNPortFunctionality>ThermocoupleConditionerPort</BBNPortFunctionality>
              <ExcitationPortIsPresent/>
              <DataLength>16</DataLength>
              <SampleRate>128</SampleRate>
              <SampleRate>192</SampleRate>
              <SampleRate>256</SampleRate>
            </GenericParameter>
            <Name>Ch1</Name>
            <PortTypes>
              <PortType Thermocouple="J">Thermocouple</PortType>
              <PortType Thermocouple="K">Thermocouple</PortType>
              <PortType Thermocouple="T">Thermocouple</PortType>
            </PortTypes>
          </Port>
        </Ports>
      </Module> 
    </Modules>
  </InternalStructure>
</NetworkNode>
```

## Evaluation Metadata

The purpose of evaluation is to replace faulty DAUs with suitable replacements.  Since we are still focusing on the 
initial example it will initially be a 1-1 swap. This section covers the additional metadata that is expected to be 
part of the input Test Configuration.

A sample we are using for internal testing based on the initial example gone over during the last PI Meeting can be found [here](../../../../shared/tools/odbhelper/resources/dummy_data/scenario5_input_mdlRoot.xml)

### Required Information

 * Acceptable Measurement Ranges - These will be used to determine if a DAU meets the required reporting qualities 
 required for a given measurement. Within the GenericParameter for a measurement, it is expected there will be a range 
 associated with the **SampleRate**, **DataLength**, and **DataLength**. This will be represented by a **Min** and 
 **Max** containing a single integer as the body value for each measurement requirement. 
 
 * Faulty DAU - The DAU that must be replaced. This will be identifier by adding the appropriate element to the 
 DAU's GeneralParameter section. It's presence indicates it is required and its absence indicates it is not required. Identifier: **BBNFlaggedForReplacement**
 
 * Excitation Port Required - This indicates an excitation port is required for usage. It's presence indicates it is 
   required and its absence indicates it is not required. Identifier: **ExcitationPortRequired**
 
### Evaluation Metadata Example

This is a significantly trimmed down version of the scenario located at [BRASS_Scenario5_BeforeAdaptation.xml](https://git.isis.vanderbilt.edu/SwRI/challenge-problems/challenge-problems/Scenarios/FlightTesting/Scenario_5/BRASS_Scenario5_BeforeAdaptation.xml)  
to provide an example of the metadata usage. Only a single measurement, dslDau, and dslModule are used to provide a basic 
concept for the API. All Measurements, DAUS, Modules, and Ports are expected to contain this additional information. 
All new data is contained in **GenericParameter** elements as agreed on.

```xml
<MDLRoot>
  <MeasurementDomains>
    <MeasurementDomain>
      <Measurements>
        <Measurement ID="meas-1-1">
          <GenericParameter>
            <NameValues>
              <NameValue Name="User" Index="1">BBN</NameValue>
            </NameValues>
            <SampleRate>
              <Min>80</Min>
              <Max>120</Max>
            </SampleRate>
            <DataRate>
              <Min>0</Min>
              <Max>32768</Max>
            </DataRate>
            <DataLength>
              <Min>8</Min>
              <Max>19</Max>
            </DataLength>
          </GenericParameter>
        </Measurement>
      </Measurements>
    </MeasurementDomain>
  </MeasurementDomains>
  <NetworkDomain>
    <Networks>
      <Network ID="TA1-Example5">
        <NetworkNodes>
          <NetworkNode ID="TTCDAU">
            <GenericParameter>
              <NameValues>
                <NameValue Name="BBN" Index="0"></NameValue>
              </NameValues>
              <BBNDauMonetaryCost>50000</BBNDauMonetaryCost>
              <BBNDauOpportunityCost>1234</BBNDauOpportunityCost>
              <BBNFlaggedForReplacement/>
            </GenericParameter>
            <InternalStructure>
                <Module ID="dslModule-1">
                  <GenericParameter>
                    <NameValues>
                      <NameValue Name="BBN" Index="0"></NameValue>
                    </NameValues>
                    <BBNModuleFunctionality>SignalConditionerModule</BBNModuleFunctionality>
                  </GenericParameter>
                  <Ports>
                    <Port Enabled="true" ID="Module1-Ch1" Index="1">
                      <GenericParameter>
                        <NameValues>
                          <NameValue Name="BBN" Index="0"></NameValue>
                        </NameValues>
                        <BBNPortFunctionality>SignalConditionerPort</BBNPortFunctionality>
                        <ExcitationPortRequired/>
                      </GenericParameter>
                    </Port>
                  </Ports>
                </Module>
              </Modules>
            </InternalStructure>
          </NetworkNode>
        </NetworkNodes>
      </Network>
    </Networks>
  </NetworkDomain>
</MDLRoot>
```

