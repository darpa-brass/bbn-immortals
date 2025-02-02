
## MDL Configuration Message

A message with an XML payload conforming to a specific XMLSchema.
These messages are generated by performers as candidates to replace/deprecate a prior similar message.

The metadata should include the following:

### Provenance

Provenance indicates the genealogy of a data item.

#### Creator

The principal may be a person or an application.

#### Inputs

The messages and data from which the message was derived.
The notion of version is subsumed by the retention of the identities of the inputs.

#### Schemas

The inputs are presumed to conform to some schema and that schema's constraints.
The message itself also has a schema to which it conforms.

#### Mappings

A declarative description describing the relationships between the schemas.
It may be the case that there are imperative elements are present
in the descriptions but these should be avoided and flagged when they occur.


