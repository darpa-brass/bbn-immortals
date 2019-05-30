from xml.dom import minidom

MAIN_XSLT = """
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tmats="https://wsmrc2vger.wsmr.army.mil/rcc/manuals/106-11" xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:tmatsP="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsP" xmlns:tmatsD="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsD" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tmatsCommon="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsCommon" exclude-result-prefixes="tmats tmatsCommon tmatsP tmatsD" version="1.0"><!--Identity transform and MDL target version-->
{}
</xsl:stylesheet>
"""

TEMPLATE_ELEMENT = """
    <xsl:template match="mdl:{}/mdl:{}">
        <xsl:element name="{}" namespace="http://inetprogram.org/projects/MDL">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
"""

ENTITY_TEMPLATE = """
schema {} = literal : Ty {{
  entities
    {}
  foreign_keys
    {}
  attributes
{}
}}
"""

AQL_MAPPINGS = """
mapping F = literal : {} -> {} {{
  entities
    {}
  attributes
{}
}}"""

XSD_TYPES = {
    'xsd:boolean': 'bool',
    'xsd:string': 'string',
    'xsd:token': 'string',
    'xsd:double': 'double',
    'xsd:unsignedShort': 'unsignedShort',
    'xsd:byte': 'byte',
    'xsd:unsignedInt': 'unsignedInt',
    'xsd:integer': 'integer',
    'xsd:unsignedLong': 'unsignedLong',
    'xsd:unsignedByte': 'unsignedByte',
    'xsd:positiveInteger': 'positiveInteger',
    'xsd:nonNegativeInteger': 'nonNegativeInteger',
    'xsd:anyURI': 'anyURI',
    'xsd:int': 'int',
    'xsd:date': 'date'
}


def _rarrow_dict(str_list):
    fks = {}
    for fk in str_list:
        fk = fk.strip()
        if len(fk) > 0 and '->' in fk:
            sp = fk.split('->')
            fks[sp[0].strip()] = sp[1].strip()
    return fks


def aql_to_xsl(src_code):

    entities = src_code.split('entities')[1]
    sp = entities.split('attributes')
    entities, attributes = sp[0], sp[1]

    entities = _rarrow_dict([entities])
    attributes = _rarrow_dict(attributes.split('\n'))

    visited_entities = []
    for e in entities:
        ename = e.split('__')[1]
        if ename not in visited_entities:
            visited_entities.append(ename)
        template_elems = [TEMPLATE_ELEMENT.format(ename, k, v.split('.')[-1]) for k, v in attributes.items()]

    return MAIN_XSLT.format(''.join(template_elems))


def xslt_to_aql(xls_code, vfrom, vto):
    """
    Transforms xslt code to AQL mappings
    :param xls_code: Code to transform
    :return: AQL code with the mapping
    """
    entities = []
    fields = []
    result = []
    xmldoc = minidom.parseString(xls_code)
    templates = xmldoc.getElementsByTagName("xsl:template")
    for t in templates:
        src = t.getAttribute('match').split('/')
        entity, field = src[0][4:], src[1][4:]

        if entity not in entities:
            entities.append(entity)
        dst = t.getElementsByTagName('xsl:element')[0].getAttribute('name')
        fields.append((field, dst))

    for e in entities:
        from_entity = vfrom + e
        to_entity = vto + e
        result_fields = ['    {} -> lambda x: x.{} \n'.format(f[0], f[1]) for f in fields]
        r = AQL_MAPPINGS.format(from_entity, to_entity, from_entity + ' ' + to_entity, ''.join(result_fields))
        print(r)
        result.append(r)
    return result


def xsd_to_aql(xsd, schema_namespace, schema_name='Schema'):
    """
    Translate one XSD to an AQL file
    :param xsd_schema: List with set of XSD to translate
    :param namespace: namespace of the resulting entity
    :return: a text with the AQL Entity
    """

    # Name of the complex types found
    complex_names = []
    # Name of all attributes found for all the complex types
    attr_names = []
    # Names of the resulting entities
    entity_names = []
    # Foreing keys
    foreing_keys = []
    # Types that could not be resolved
    unknown_types = []

    # Go through the XSD coljecting the elements
    xmldoc = minidom.parseString(xsd)
    # Get all the complex types

    simpleTypes = {}

    simple = xmldoc.getElementsByTagName("xsd:simpleType")
    for s in simple:
        simple_name = s.getAttribute('name')
        restrictions = s.getElementsByTagName('xsd:restriction')
        for r in restrictions:
            if r.hasAttribute('base'):
                simpleTypes[simple_name] = r.getAttribute('base')

    complex = xmldoc.getElementsByTagName("xsd:complexType")
    for c in complex:
        try:
            complex_name = c.getAttribute('name')
            complex_names.append(complex_name)
            entity_names.append(schema_namespace + complex_name)
            # Assign a name to the parsed entity. Add a namespace from the schema name list
        except:
            print(f"Unable to translate {complex_name}")

    j = 0
    for c in complex:
        try:
            complex_name = complex_names[j]
            xsdelements = c.getElementsByTagName("xsd:element")
            for x in xsdelements:
                name = x.getAttribute('name')
                x_type = x.getAttribute('type')
                if x_type in complex_names:
                    fk = f"{schema_namespace}{complex_name} -> {schema_namespace}{x_type}"
                    if fk not in foreing_keys:
                        foreing_keys.append(fk)
                elif x_type in XSD_TYPES:
                    x_type = XSD_TYPES[x_type]
                    attr_names.append('{} : {} -> {}'.format(name, complex_name, x_type))
                elif x_type in simpleTypes:
                    x_type = XSD_TYPES[simpleTypes[x_type]]
                    attr_names.append('{} : {} -> {}'.format(name, complex_name, x_type))
                elif x_type not in unknown_types:
                    unknown_types.append(x_type)

        except:
            print(f"Unable to translate {complex_name}")
        j += 1

    result = ENTITY_TEMPLATE.format(schema_name, '\n    '.join(entity_names), '\n    '.join(foreing_keys), '\n    '.join(attr_names))

    if len(unknown_types) > 0:
        print('\n'.join(unknown_types))

    return result
