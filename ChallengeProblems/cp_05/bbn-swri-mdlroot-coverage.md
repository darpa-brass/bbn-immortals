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

The following **BBNPortFunctionality** values have been seen but not yet examined or utilized:
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
 * Elements that are commented out are ignored
 * Multiplicities are not present in the document
 * Actual values are not present or are not applicable to the document.
 * Element tags are present but empty to indicate we are examining and utilizing them
 * Attribute placeholders are present to allow it to be a valid XML document

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
