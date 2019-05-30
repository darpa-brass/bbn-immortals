#!/usr/bin/env python
"""
Automatic generation of XSLT files
"""

import os

from xml.dom import minidom
from lxml import etree as et

from mapping.xsdmapper import Mapper

# -----------------------------------------------------------------------

EXTRA_COMMENT = """
  Description:
     Stylesheet that generates XHTML documentation, given an XML 
     Schema document
  Assumptions:
     -Resulting documentation will only be displayed properly with 
      the latest browsers that support XHTML and CSS. Older 
      browsers are not supported.
     -Assumed that XSD document conforms to the XSD recommendation.
      No validity checking is done.
  Constraints:
     -Local schema components cannot contain two dashes in 
      'documentation' elements within their 'annotation' element.
      This is because the contents of those 'documentation' 
      elements are displayed in a separate window using Javascript. 
      This Javascript code is enclosed in comments, which do not
      allow two dashes inside themselves.
  Notes:
     -Javascript code is placed within comments, even though in 
      strict XHTML, JavaScript code should be placed within CDATA 
      sections. This is because current browsers generate a syntax 
      error if the page contains CDATA sections. Placing Javascript 
      code within comments means that the code cannot contain two 
      dashes.
      (See 'PrintJSCode' named template.)
"""
HTML_NS = "http://www.w3.org/1999/xhtml"
XSL_NS = "http://www.w3.org/1999/XSL/Transform"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
MDL_NS = "http://inetprogram.org/projects/MDL"


# -----------------------------------------------------------------------


def build_ast(include_comments=False):
    root_node = et.Element(
        et.QName(XSL_NS, 'stylesheet'),
        version='1.0',
        nsmap={
            None: HTML_NS,
            'mdl': MDL_NS,
            'xsl': XSL_NS,
            'xsi': XSI_NS
        },
    )

    # include initial comments if requested by user
    if include_comments:
        # add copyright notice and some extra comments
        root_node.addprevious(et.Comment(EXTRA_COMMENT))

    # To add comments somewhere in the code read:
    # . https://stackoverflow.com/questions/36320484/adding-comments-to-xml-documents

    # add root node to AST
    ast = et.ElementTree(root_node)

    return ast


def handle_default_content(parent_node):
    template_node = et.SubElement(
        parent_node,
        et.QName(XSL_NS, 'template'),
        match='@*|node()',
    )

    copy_node = et.SubElement(
        template_node,
        et.QName(XSL_NS, 'copy')
    )

    apply_templates = et.SubElement(
        copy_node,
        et.QName(XSL_NS, 'apply-templates'),
        select='@*|node()'
    )

    # add comments and extra info
    template_node.addprevious(et.Comment(' identity template, copies everything as is '))


def handle_mapper_additions(parent_node, mapper):
    # # <xsl:template match="/xs:schema/xs:complexType[last()]">
    # template_node = et.SubElement(
    #     parent_node,
    #     et.QName(XSL_NS, 'template'),
    #     match='/xs:schema/xs:complexType[last()]',
    # )
    #
    # copy_node = et.SubElement(
    #     template_node,
    #     et.QName(XSL_NS, 'copy')
    # )
    #
    # apply_templates = et.SubElement(
    #     copy_node,
    #     et.QName(XSL_NS, 'apply-templates'),
    #     select='@*|node()'
    # )
    #
    # for i, field in enumerate(mapper.added):
    #     # get string representation of field dom_element
    #     field_xml_str_repr = field.dom_element.toxml()
    #
    #     new_node = et.fromstring(field_xml_str_repr)
    #
    #     template_node.insert(i + 1, new_node)
    #
    # # add comment to signal the start of additions
    # template_node.addprevious(et.Comment(' include additions after last complexType '))

    # # manage add fields rules/templates
    # for i, field in enumerate(mapper.added):
    #     # node_bytes = et.tostring(field.dom_element, pretty_print=True, encoding='utf-8')
    #
    #     if i == 71:
    #
    #         with open('./tmp.xml', 'w') as f:
    #             doc = minidom.Document()
    #             doc.appendChild(field.dom_element)
    #             doc.writexml(f)
    #
    #             # f.write(node_bytes.decode(encoding='utf-8'))
    #
    #         k = 0
    pass

def get_element_path(field):
    if field.entity is None:
        return ''
    else:
        parent = get_element_path(field.entity)
        if parent != '':
            parent = parent + "/"
        return parent + field.name


def handle_mapper_removals(parent_node, mapper):
    # add rule for each removed node
    already_matched = {}
    for i, field in enumerate(mapper.removed):
        # <xsl:template match="/schema/{field.entity.name}/{field.name}" />
        # find the fields of this particular type
        for j, field_matching_type in enumerate(filter(lambda f : f.field_type == field.entity.name, mapper.old_fields)):
            match_str = "//mdl:{}/mdl:{}".format(field_matching_type.name, field.name)
            if match_str not in already_matched:
                template_node = et.SubElement(
                    parent_node,
                    et.QName(XSL_NS, 'template'),
                    match=match_str,
                )
                # comment for rule added for each removed node
                template_node.addprevious(
                    et.Comment(" REMOVE node {}/{} ".format(field.entity.name, field.name))
                )
                already_matched[match_str] = match_str


def handle_relocate_document(parent_node, mapper):

    et.SubElement(
        parent_node,
        et.QName(XSL_NS, 'param'),
        name="targetMDLVersion",
        select="'http://inetprogram.org/projects/MDL/ " + mapper.new_schema_location + "'"
    )

    template_node = et.SubElement(
        parent_node,
        et.QName(XSL_NS, 'template'),
        match='/mdl:MDLRoot/@xsi:schemaLocation'
    )

    attribute_node = et.SubElement(
        template_node,
        et.QName(XSL_NS, 'attribute'),
        name='xsi:schemaLocation'
    )

    et.SubElement(
        attribute_node,
        et.QName(XSL_NS, 'value-of'),
        select="$targetMDLVersion"
    )


def handle_mapper_renames(parent_node, mapper):
    # add rule for each field rename
    already_matched = {}
    for i, (original, renamed) in enumerate(mapper.renamed):
        #     <xsl:template match="CATALOG/NAME">
        #         <CATALOG-NAME><xsl:apply-templates select="@*|node()" /></CATALOG-NAME>
        #     </xsl:template>
        match_str = "//mdl:{}".format(original.name)
        if match_str not in already_matched:
            template_node = et.SubElement(
                parent_node,
                et.QName(XSL_NS, 'template'),
                match=match_str,
            )

            renamed_node = et.SubElement(
                template_node,
                et.QName(MDL_NS, renamed.name)
            )

            et.SubElement(
                renamed_node,
                et.QName(XSL_NS, 'apply-templates'),
                select='@*|node()'
            )

            # comment for rule added for each 'renamed' node
            template_node.addprevious(
                et.Comment(" RENAME node <{}> into <{}> ".format(original.name, renamed.name))
            )
            already_matched[match_str] = match_str


def handle_mapper_type_renames(root_node, mapper):
    pass


def generate_xslt_file(mapper, output_file=None):
    # build XSLT file AST
    ast = build_ast()

    # get root node of tree
    root_node = ast.getroot()

    handle_relocate_document(root_node, mapper)

    # copy/maintain all content by default
    handle_default_content(root_node)
    handle_mapper_renames(root_node, mapper)
    handle_mapper_additions(root_node, mapper)
    handle_mapper_removals(root_node, mapper)
    handle_mapper_type_renames(root_node, mapper)

    sheet_bytes = et.tostring(ast, xml_declaration=True, pretty_print=True, encoding='utf-8')
    # write XSLT content to file
    if output_file is not None:
        with open(output_file, 'w') as f:
            f.write(sheet_bytes.decode(encoding='utf-8'))
    else:
        return sheet_bytes.decode(encoding='utf-8')


def apply_xslt_transform(xslt_path, xml_path, output):
    # load/parse XSLT and XML files from HDD
    xslt_file = et.parse(xslt_path)
    xml_file = et.parse(xml_path)

    # apply transformation
    transform = et.XSLT(xslt_file)
    transformed_xml = transform(xml_file)

    # save xml file to HDD
    new_xml_file_path = output

    with open(new_xml_file_path, 'w') as f:
        sheet_bytes = et.tostring(transformed_xml, xml_declaration=True, pretty_print=True, encoding='utf-8')
        f.write(sheet_bytes.decode(encoding='utf-8'))


if __name__ == '__main__':
    OUTPUT_FILE_PATH = './out/output.xsl'

    # perform mapping
    mapper = Mapper()
    mapper.new_schema_location = "MDL_v0_8_19.xsd"
    with open('./data/MDL_v0_8_17.xsd', 'r') as old_version, open('./data/MDL_v0_8_19.xsd', 'r') as new_version:
        old_xsd = minidom.parse(old_version)
        new_xsd = minidom.parse(new_version)

    mapper.map(old_xsd, new_xsd)

    generate_xslt_file(mapper, OUTPUT_FILE_PATH)

    apply_xslt_transform(OUTPUT_FILE_PATH, './data/AssetAssociations.xml', './out/AssetAssociationsOut.xml')

