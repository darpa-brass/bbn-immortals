# BBN SwRI MDLRoot Coverage

The purpose of this document is to indicate what we (BBN) are considering when making our decisions to ensure we have a 
mutual understanding on what is and is not in scope for evaluation. The intent is also to help drive example creation 
to cover scenarios we may not have been exposed to yet.

## Current Understandings

 * All ports with a **BBNPortFunctionality** tag will be applicable in a current or future example
 * Only ports with an **ID** Referenced by another **IDREF** outside of the DAU are applicable to adaptation

## Utilized BBNPortFunctionality Values 

The following **BBNPortFunctionality** values have been seen and are utilized:

 * _SignalConditioning_
 * _Thermocouple_
 * _Bus_

## Seen But Not Utilized BBNPortFunctionality Values

The following **BBNPortFunctionality** values have been seen but not yet examined or utilized. If they are utilized 
in a different way than the existing ports we are utilizing examples will be necessary:
 * _Serial_
 * _Ethernet_
 * _Time_
 * _Power_
 * _PCM_
 * _VGA_
 
## MDLRoot Psuedocode Coverage Document

I have put together an XML document that essentially combines the examples we have seen into a single simple coverage 
document that is only a few hundred lines of XML.

The following applies to it:
 * Elements that are commented out are currently ignored by us. If they need to be considered for adaptation or updated 
   as a result of adaptation we will need examples.
 * Multiplicities are not present in the document.
 * Actual values are not present or are not applicable to the document.
 * Element tags are present but empty to indicate we are examining and utilizing them.
 * Attribute placeholders are present to allow it to be a valid XML document.

To minimize the size of the document, the following fields have been excluded from all elements and are globally 
ignored:

```xml
</Name>
</Description>
</ProperName>
</Manufacturer>
</Model>
</ModelVersion>
</SerialID>
</InventoryID>
</Connectors>
</PinRefs>
</PinRef>
</ProperName>
</ReferenceDocumentation>
</Product>
</ProductVersion>
```

The document can be found [here](example_assumptions/ExampleCoverage.xml)

## Matching Guidelines

These indicate the rules governing when a **DAUInventory** value is a valid match for 
an **MDLRoot** value. 


#### Matching definitions
| Definition          | Description                                     |
|---------------------|-------------------------------------------------|
| AnyValue            | Any value, not including the absence of a value |
| AnyOrNoValue        | Any value or no value                           |
| Present             | The element or attribute is present             |
| NotPresent          | The element or attribute is not present         |
| ExactValue         | The exact value must match                      |
| CorrespondingValue | The corresponding value as documented here      |




#### Thermocouple Matching Rules

This is considered to be the Thermocouple extension value parsed from the PortType

| MDLRoot Value         | Matching Inventory Values |
|-----------------------|---------------------------|
| **ThermocoupleEnum**  | ExactValue                     |
| NotPresent            | AnyOrNoValue              |


#### PortDirection Matching Rules

| MDLRoot Value   | Matching Inventory Values   |
|-----------------|-----------------------------|
| "Input"         | ["Input", "Bidirectional"]  |
| "Output"        | ["Output", "Bidirectional"] |
| "Bidirectional" | "Bidirectional"             |
| "Unspecified"   | [NotPresent, "Unspecified"] |


#### Excitation Matching Rules

| Device ExcitationSource | Matching DAU Port ExcitationIsPresent presence  |
|-------------------------|-----------------------------------------------|
| "Internal"              | [Present, NotPresent]
| "External"              | Present
| NotPresent              | [Present, NotPresent]


#### Overall Matching Rules
| MDLRoot Attribute             | Matching Behavior When Present  | Matching Behavior When Not Present  |
|-------------------------------|---------------------------------|-------------------------------------|
| Measurement.SampleRate.Range  | ExactValue                      | AnyOrNoValue                        |
| Measurement.DataRate.Range    | ExactValue                      | AnyOrNoValue                        |
| Device.ExcitationSource       | CorrespondingValue              | AnyOrNoValue                        |
| PortDirection                 | CorrespondingValue              | N/A (Always Present)                |
| PortPolarity                  | ExactValue                      | AnyOrNoValue                        |
| PortType                      | ExactValue                      | NoValue                             |