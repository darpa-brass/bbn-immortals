from mapping.xsdfield import Field


class Entity(object):

    def __init__(self, element):
        self.dom_element = element
        self.name = element.getAttribute('name')
        self.fields = self._get_fields()

    def _get_fields(self):
        """
        Gets all the fields of a complex entity in an XSD
        :param element: Complex element to obtain fields from
        :param tagname: Tag to filter the elements for
        :param entity: Specifies a name for the entity we are looking for in the case is not the same as the one found in
                       the name attribute of element
        :return:
        """
        element, tagname, entity = self.dom_element, "xsd:element", self.name

        if not entity:
            entity = element.getAttribute('name'),

        result = []
        for n in element.getElementsByTagName(tagname):
            f = Field(self, n.getAttribute('name'), n.getAttribute("type"))
            f.dom_element = element
            result.append(f)
        return result

    #def similar(self, other):
    #    result = 0
    #    for f in self.fields:
    #        max_sim = 0
    #        other.

    def __str__(self):
        return self.name

    def __repr__(self):
        return self.__str__()


def find_entities(dom):
    """
    Finds all the entities of a given dom
    """
    result = {}
    for n in dom.getElementsByTagName("xsd:complexType"):
        e = Entity(n)
        if e.name != "":
            result[e.name] = e
    return result
