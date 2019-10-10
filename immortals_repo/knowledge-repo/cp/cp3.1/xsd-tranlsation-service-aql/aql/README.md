# XSD Translation

This project is meant to translate between different versions of an XSD

## Requirements

- Python >= 3.7.*
- Pyenv (recommended, optional)
- Pipenv (to manage dependencies)

## How to install

With pipenv properly installed:

### Setup virtualenv with pipenv

```
$ pipenv --three
```

### Install dependencies

#### In development mode

```
$ make install-dev
```

#### In production mode (dev dependencies are not instaled, like test libraries)

```
$ pipenv install
```

## How to run

### Start up http server

```
$ make run
```

Now, our flask server is up and ready to receive requests.

### Unit and integration tests

```
$ make test
```

### Linter (style guide enforcement)


```
$ make test
```

## Testing MDL files translation in command line

### Mac version (maybe linux works well, but not tested)

Required `xmllint` and `xsltproc` installed.

To perform a translation based on XSLT result from server endpoint (works only with XSLT < `2.0`):

```
$ xsltproc translation.xslt mdl17.xml > result.xml
```

To work with things from XSLT 2.0 version (like `xsl:function`) you must use another tool, like `Saxon` (http://saxon.sourceforge.net/):

```
$ saxon -s:mdl17.xml -xsl:translation.xslt -o:result.xml
```

Now, result is with new version provided by `translation.xslt`, we can now perform a validation with new verison (considering here MDL 19**:

**Important: to run this we need to put all imported files by MDL 19 in the same dir, like Tmats, etc...**

```
$ xmllint --noout --schema MDL_v0_8_19.xsd result.xml > xmllint_validation_result.txt 2>&1
```

Now `xmllint_validation_result.txt` has validation results using MDL 19 spec:

```
result.xml:846: element Position: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}Position': Warning: No precomputed value available, the value was either invalid or something strange happend.
result.xml:846: element MACAddress: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}MACAddress': [facet 'pattern'] The value '' is not accepted by the pattern '([0-9a-fA-F]{2}(:|\-)){5}[0-9a-fA-F]{2}'.
result.xml:846: element SIUnits: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}SIUnits': This element is not expected.
result.xml:846: element RadioLinkRef: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}RadioLinkRef': This element is not expected.
result.xml:846: element PortType: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}PortType': [facet 'enumeration'] The value '' is not an element of the set {'Analog', 'Audio', 'TestPoint', 'Serial', 'Video', 'ARINC429', 'Ethernet', 'ARINC629', 'Fiberchannel', 'IEEE1394', 'CANbus', 'MIL-STD-1553', 'IntelliBus', 'Extension'}.
result.xml:846: element Position: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}Position': Warning: No precomputed value available, the value was either invalid or something strange happend.
result.xml:846: element PortType: Schemas validity error : Element '{http://inetprogram.org/projects/MDL}PortType': [facet 'enumeration'] The value '' is not an element of the set {'Analog', 'Audio', 'TestPoint', 'Serial', 'Video', 'ARINC429', 'Ethernet', 'ARINC629', 'Fiberchannel', 'IEEE1394', 'CANbus', 'MIL-STD-1553', 'IntelliBus', 'Extension'}.
```

## Implementation

### Tree hashing comparison

Documentation [here](./docs/tree-hashing-comparison.md).

## Transformation types found

The following provides a brief description of the transformations found in different versions of MDL and our capability to automatically handle them.

In the following lines, **bold** text represents the transformations that we can handle.

The verbs *add*, *remove*, *chage* are used to refer that the target version *added*/*removed*/*chaged* something to the source version. For example, say we meant to transform from 0.17 to 0.19, *Add field* can be read as "MDL 0.19 version added a new field NOT present in MDL 0.17 version."

### Entities

- **Add an entity.** This transformation adds a completely new entity to the schema. **Can be handled automatically**.

- **An entity changed name.**

- **Entity definition change place.** The definition of an entity have changed its location in the DOM.
It can occur that a definition is declared as a sub-node of a field and then extracted into a root child. **Can be handled automatically**

### Fields

- **Add a field.** This transformation adds a field to an entity. **Can be handled automatically**

- **Add an optional field.** This transformation adds a field to an entity who's appearance is optional. **Can be handled automatically**

- *Remove a field.* This transformation removes a field from an entity.

- **Change a field name.** Changes to the name can be handled automatically in the absence of major changes to the entity, such as only one field changing names or two fields changing name lightly (i.e. `ModelType` -> `ModelTypeId`). **Can be handled, but may require manual confirmation.**

- **Change a field type.** Changes in the field type can be handled on the assumption that equal field names under equal entity names are in fact, the same field. **Can be handled, but may require manual confirmation.**

- **Change a field type and name.**  This can be handled under the assumptions that fields with similar names are in fact the same field. **Can be handled, but may require manual confirmation.**

### Enumeration

- **Add an enumeration value.** Adds a enumeration value from a restriction list. **Can be handled automatically**

- *Remove an enumeration value.* Removes a enumeration value from a restriction list.

- **Change enumeration name.** This can be handled under the same assumptions of the Change Field Name

### XPaths

- *Change an XPath.* This can be handled, provided that we can find the path in the target version. **Can be handled automaticall in certain cases.**
