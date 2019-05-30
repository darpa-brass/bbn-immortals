from difflib import SequenceMatcher

from mapping.xsdfield import field_diff
from translation.aqltranslation import XSD_TYPES

import progressbar


def _get_names(element, tagname):
    """
    Get all the names of elements in an XSD given an specific tag
    :param element: Root element containing the sub-elements that we want to obtain the attr name for
    :param tagname: Name for the tag we are looking for
    :return: The list of names of the element (i.e. the content of the "name" attr.
    """
    return [n.getAttribute('name') for n in element.getElementsByTagName(tagname)]


def find_entity_diff(srcdoc, destdoc):
    """
    Returns the difference in complex types from one version of the schema to another
    :param srcdoc: Parsed source XSD
    :param destdoc: Parsed destiniy XSD
    :return: The added and removed entities between two versions
    """
    srctypes = _get_names(srcdoc, "xsd:complexType") + _get_names(srcdoc, "xsd:simpleType")
    dsttypes = _get_names(destdoc, "xsd:complexType") + _get_names(destdoc, "xsd:simpleType")
    setsrc = set(srctypes)
    setdst = set(dsttypes)
    return setdst - setsrc, setsrc - setdst


def get_all_fields(doc):
    """
    Return all fields from an xsd document
    :param doc: Parsed XSD document
    :return: All the fields in the XSD document
    """

    fields = []

    for n in doc.getElementsByTagName("xsd:complexType"):
        fields.extend(get_fields(n, "xsd:element", n.getAttribute('name')))

    return fields


def find_fields_diff(srcfields, dstfields, removed=None, added=None):
    """
    Finds the difference in fields from to xsd document
    :param srcdoc: Source fields
    :param destdoc: Dest fields
    :return: The fields that were added, removed, renamed and type renamed
             (i.e. same name but type changed) from version to version
    """

    renamed, typerenamed, added_fields, removed_fields = [], [], [], []

    # Removed fields are those fields in src that are noit in dst
    if removed is None:
        removed = field_diff(srcfields, dstfields)  # Freaking set difference didn't wanted to work   >:(
    # Added fields are those fields in dst that are not in src
    if added is None:
        added = field_diff(dstfields, srcfields)  # Freaking set difference didn't wanted to work   >:(

    # Compute renamed
    bar = progressbar.ProgressBar(maxval=len(removed) * len(added),
                                  widgets=[progressbar.Bar('=', '[', ']'), ' ', progressbar.Percentage()])
    bar.start()
    i = 0
    for s in removed:
        for d in added:
            ns, ts = s.similar(d)
            if 0.5 < ns < 1 and ts == 1:
                renamed.append((s, d))
            elif 0.5 < ts < 1 and ns == 1:
                typerenamed.append((s, d))
            i += 1
            bar.update(i)

    # At this point all fields in renamed are both in added and
    # deleted, remove this.
    for r in renamed:
        if r[1] in added:
            added.remove(r[1])
        if r[0] in removed:
            removed.remove(r[0])
    for r in typerenamed:
        if r[1] in added:
            added.remove(r[1])
        if r[0] in removed:
            removed.remove(r[0])

    added_fields.extend(added)
    removed_fields.extend(removed)

    return added_fields, removed_fields, renamed, typerenamed


def _intersection_renamed(renamed, b):
    """
    Returns the intersection between a renamed field (composed by a tuple (old, new)) and another set of fields
    """
    result = []
    for s in renamed:
        r = False
        for d in b:
            if s[0].entity == d.entity and s[0].name == d.name and s[0].field_type == d.field_type:
                r = True
                break
        if r:
            result.append(s)
    return result


def diff_to_mapping(srcfields, destfields, query_fields):
    """
    Creates a mapping between srcxsd to destxsd given a set of required fields in a query.

    :param srcfields: Fields from the source
    :param destfields: Fields from destination
    :param query_fields: fields in the query
    :return:
    """

    added, removed, renamed, typerenamed = find_fields_diff(srcfields, destfields)

    REMOVED_REQUIRED = \
        "There are required removed fields from the source that are not found on the destiny: {}"

    RENAME_XSLT = """
        <xsl:template match="mdl:{}/mdl:{}">
            <xsl:element name="{}" namespace="http://inetprogram.org/projects/MDL">
                <xsl:apply-templates select="@*|node()"/>
            </xsl:element>
        </xsl:template>
    """

    XLST_RESULT = """
    <?xml version="1.0" encoding="UTF-8"?>
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tmats="https://wsmrc2vger.wsmr.army.mil/rcc/manuals/106-11" xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:tmatsP="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsP" xmlns:tmatsD="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsD" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tmatsCommon="http://www.wsmr.army.mil/RCCsite/Documents/106-13_Telemetry%20Standards/TmatsCommon" exclude-result-prefixes="tmats tmatsCommon tmatsP tmatsD" version="1.0">
        <!--Identity transform and MDL target version-->
        <xsl:template match="@*|node()">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:template>
        {}
    </xsl:stylesheet>
    """

    # Check that there is not at least one removed field
    removed_result = ""
    if removed:
        removed_in_required = []
        for r in query_fields:
            if r in removed:
                removed_in_required.append(r)
        if removed_in_required:
            removed_result = REMOVED_REQUIRED.format(removed_in_required)

    # Return a list of mappings with renamed fields
    xslt_result = ""

    required_renamed = _intersection_renamed(renamed, query_fields)
    for r in required_renamed:
        xslt_result += RENAME_XSLT.format(r[0].entity, r[0].name, r[1].name)

    required_type_renamed = _intersection_renamed(typerenamed, query_fields)
    for r in required_type_renamed:
        xslt_result += RENAME_XSLT.format(r[0].entity, r[0].name, r[1].name)

    return removed_result, XLST_RESULT.format(xslt_result)
